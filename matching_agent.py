"""
Milestone 3 — LangGraph Matching Agent
=======================================
A fully agentic resume-to-job-description matching system built with LangGraph.

Architecture at a glance
─────────────────────────
  User query (natural language)
        │
        ▼
  ┌─────────────────────────────────────────────┐
  │            LangGraph State Machine           │
  │                                             │
  │  AgentState (shared memory across all nodes)│
  │                                             │
  │  Nodes (in order):                          │
  │  1. parse_jd          ─ understand the JD   │
  │  2. extract_reqs      ─ must-have / nice-to │
  │  3. search_resumes    ─ RAG semantic search  │
  │  4. rank_candidates   ─ score & sort         │
  │  5. generate_report   ─ LLM writes report    │
  │  6. human_feedback    ─ wait for user input  │
  │     ├─ "refine"  → back to extract_reqs      │
  │     ├─ "compare" → compare_candidates node   │
  │     ├─ "questions"→ interview_questions node  │
  │     └─ "end"     → END                       │
  └─────────────────────────────────────────────┘
        │
        ▼
  Streamlit chat UI  (chat_interface.py)

LLM backend: Ollama (100% local, no API key needed)
  Default model: qwen3-nothink:latest
  Embedding model: all-MiniLM-L6-v2 (also local)
  Vector DB: ChromaDB (persisted to ./chroma_db)
"""

from __future__ import annotations

import json
import os
import re
from typing import Annotated, Any

from typing_extensions import TypedDict

# ── LangChain / LangGraph imports ────────────────────────────────────────────
from langchain_core.messages import (
    HumanMessage, AIMessage, SystemMessage, ToolMessage,
)
from langchain_core.tools import tool
from langchain_ollama import ChatOllama
from langgraph.graph import StateGraph, START, END
from langgraph.graph.message import add_messages
from langgraph.checkpoint.memory import MemorySaver

# ── Milestone 1: file-system helpers ─────────────────────────────────────────
from fs_tools import (
    read_file as _read_file,
    list_files as _list_files,
    write_file as _write_file,
    search_in_file as _search_in_file,
)

# ── Milestone 2: RAG + matching helpers ──────────────────────────────────────
from resume_rag import ResumeRAGSystem
from job_matcher import JobMatcher

# =============================================================================
# SECTION 1 — AGENT STATE
# =============================================================================
# TypedDict is a typed dictionary — Python's way of declaring exactly what
# keys a dictionary must have and what types they hold.
#
# LangGraph passes this dict through every node.  Each node reads what it
# needs and writes back the keys it updates.  This is the agent's "memory".
#
# The special Annotated[list, add_messages] on `messages` means LangGraph
# will *append* new messages to the list rather than replacing it — giving
# us persistent conversation history across the entire workflow.
# =============================================================================

class AgentState(TypedDict):
    # ── Conversation ──────────────────────────────────────────────────────────
    messages: Annotated[list, add_messages]
    # Every user message, AI reply, and tool result goes here.
    # add_messages ensures we never overwrite — we only append.

    # ── Job description ───────────────────────────────────────────────────────
    job_description: str          # Raw JD text the user provided
    parsed_jd_summary: str        # LLM-generated plain-English summary of JD

    # ── Requirements ─────────────────────────────────────────────────────────
    requirements: dict
    # {
    #   "must_have": ["Python 3+", "5 years experience", "AWS"],
    #   "nice_to_have": ["Kubernetes", "Go", "GraphQL"],
    #   "min_years": 5,
    #   "education": "Bachelor"
    # }

    # ── Candidates ────────────────────────────────────────────────────────────
    all_candidates: list          # Round 1: all candidates from RAG search
    shortlist: list               # Round 2: top 10 after initial filter
    final_candidates: list        # Round 3: top 3 after deep analysis

    # ── Reports & explanations ────────────────────────────────────────────────
    match_report: str             # Full LLM-generated match report
    ranking_reasoning: dict       # {candidate_name: "why they ranked here"}
    strengths_gaps: dict          # {candidate_name: {strengths:[], gaps:[]}}
    comparison_data: str          # Head-to-head comparison table (markdown)
    interview_questions: dict     # {candidate_name: [q1, q2, ...]}

    # ── Workflow control ──────────────────────────────────────────────────────
    current_stage: str            # Which node we're currently in
    screening_round: int          # 1, 2, or 3
    feedback_history: list        # All user feedback entries
    resumes_dir: str              # Path to the folder of resumes
    next_action: str              # "refine"|"compare"|"questions"|"end"


# =============================================================================
# SECTION 2 — LLM SETUP
# =============================================================================
# ChatOllama connects to the locally running Ollama server.
# We use qwen3-nothink by default because:
#   • "nothink" = thinking tokens disabled → faster, more predictable JSON
#   • 8B parameters = good quality for structured tasks like requirement parsing
#   • 100% local = no API key, no data leaving your machine
#
# temperature=0 means the model always picks the highest-probability token.
# For tool calling and structured output, determinism is better than creativity.
# =============================================================================

OLLAMA_MODEL = os.environ.get("OLLAMA_MODEL", "qwen3-nothink:latest")
OLLAMA_URL   = os.environ.get("OLLAMA_URL",   "http://localhost:11434")
RESUMES_DIR  = os.environ.get("RESUMES_DIR",  "./resumes")
CHROMA_DIR   = os.environ.get("CHROMA_DIR",   "./chroma_db")


def get_llm() -> ChatOllama:
    """Return a fresh ChatOllama instance.  Called inside each node so
    nodes can be tested independently without a global LLM object."""
    return ChatOllama(model=OLLAMA_MODEL, base_url=OLLAMA_URL, temperature=0)


# =============================================================================
# SECTION 3 — TOOLS
# =============================================================================
# There are 7 tools total:
#   A. 4 file-system tools  (re-exported from Milestone 1's fs_tools.py)
#   B. 1 RAG search tool    (wraps Milestone 2's semantic search)
#   C. 3 new agent tools    (extract_requirements, compare_candidates,
#                            generate_interview_questions)
#
# Every tool is a plain Python function decorated with @tool.
# The decorator reads the function's type annotations and docstring to build
# a JSON schema the LLM uses to decide WHEN and HOW to call the tool.
#
# Critical rule: the docstring IS the tool description the model reads.
# Write it clearly — the model's ability to pick the right tool depends on it.
# =============================================================================

# ── A. File-system tools (Milestone 1) ───────────────────────────────────────

@tool
def read_file(filepath: str) -> str:
    """Read and extract text content from a resume file (PDF, TXT, or DOCX).
    Returns the file content as text plus metadata (size, filename, date)."""
    return json.dumps(_read_file(filepath))


@tool
def list_files(directory: str, extension: str = None) -> str:
    """List all files in a directory. Optionally filter by extension
    (e.g. '.pdf', '.txt'). Returns name, path, size, and date for each file."""
    return json.dumps(_list_files(directory, extension))


@tool
def write_file(filepath: str, content: str) -> str:
    """Write text content to a file, creating any missing parent directories.
    Use this to save match reports or comparison results to disk."""
    return json.dumps(_write_file(filepath, content))


@tool
def search_in_file(filepath: str, keyword: str) -> str:
    """Search for a keyword inside a file (case-insensitive).
    Returns every matching line with 2 lines of surrounding context."""
    return json.dumps(_search_in_file(filepath, keyword))


# ── B. RAG search tool (Milestone 2) ─────────────────────────────────────────

@tool
def rag_search(query: str, top_k: int = 10, resumes_dir: str = RESUMES_DIR) -> str:
    """Semantic search over all indexed resumes using the RAG vector database.
    Finds candidates whose experience best matches the query text, even if
    they use different words.  Returns ranked candidates with match scores.

    Args:
        query:       The job description or skill requirements to search for.
        top_k:       How many top candidates to return (default 10).
        resumes_dir: Path to the directory containing resume files.
    """
    try:
        # First try to connect to an existing ChromaDB collection
        try:
            matcher = JobMatcher(persist_directory=CHROMA_DIR)
        except Exception:
            # Collection does not exist yet — index the resumes first
            rag = ResumeRAGSystem(persist_directory=CHROMA_DIR)
            rag.process_resume_directory(resumes_dir)
            matcher = JobMatcher(persist_directory=CHROMA_DIR)

        result = matcher.match_job(query, top_k=top_k, apply_filters=False)
        # Return only the essential fields to keep the context window small
        slim = []
        for c in result.get("top_matches", []):
            slim.append({
                "name":             c.get("candidate_name", "Unknown"),
                "resume_path":      c.get("resume_path", ""),
                "match_score":      c.get("match_score", 0),
                "skills":           c.get("matched_skills", []),
                "experience_years": c.get("experience_years"),
                "education":        c.get("education", []),
                "reasoning":        c.get("reasoning", ""),
            })
        return json.dumps({"candidates": slim, "total_found": result.get("total_candidates_found", 0)})
    except Exception as exc:
        return json.dumps({"error": str(exc), "candidates": []})


# ── C. New agent-specific tools (Milestone 3) ────────────────────────────────

@tool
def extract_requirements(job_description: str) -> str:
    """Parse a job description with the LLM to separate must-have requirements
    from nice-to-have ones.  Returns structured JSON with:
      - must_have:    skills / experience the candidate MUST have
      - nice_to_have: bonus qualifications
      - min_years:    minimum years of experience (number or null)
      - education:    required degree level (string or null)

    Use this at the start of any matching workflow.
    """
    llm = get_llm()
    prompt = f"""You are a recruiting expert.  Analyse this job description and
extract requirements into a structured JSON object.

Job Description:
{job_description}

Return ONLY valid JSON in this exact format (no extra text, no markdown):
{{
  "must_have": ["requirement 1", "requirement 2"],
  "nice_to_have": ["bonus 1", "bonus 2"],
  "min_years": <number or null>,
  "education": "<degree level or null>",
  "role_title": "<inferred job title>"
}}"""
    response = llm.invoke([HumanMessage(content=prompt)])
    raw = response.content.strip()
    # Strip markdown code fences if the model added them
    raw = re.sub(r"^```(?:json)?\s*", "", raw)
    raw = re.sub(r"\s*```$", "", raw)
    try:
        parsed = json.loads(raw)
        return json.dumps(parsed)
    except json.JSONDecodeError:
        # Return the raw text so the caller can still display it
        return json.dumps({"raw": raw, "parse_error": "Could not parse JSON"})


@tool
def compare_candidates(candidate_data: str) -> str:
    """Generate a head-to-head comparison of 2–5 candidates as a markdown table.

    Args:
        candidate_data: JSON string — a list of candidate dicts, each with
                        keys: name, match_score, skills, experience_years,
                        education, strengths, gaps.

    Returns a markdown table comparing them side by side so the recruiter
    can quickly see who is stronger in each dimension.
    """
    llm = get_llm()
    prompt = f"""You are a recruiting expert comparing candidates for a role.

Here is the candidate data:
{candidate_data}

Create a clear markdown comparison table with these rows:
  • Match Score
  • Years of Experience
  • Key Skills (top 5)
  • Education
  • Top Strength
  • Main Gap

One column per candidate.  After the table, write 2–3 sentences recommending
who to advance and why.  Be concise and specific."""
    response = llm.invoke([HumanMessage(content=prompt)])
    return response.content.strip()


@tool
def generate_interview_questions(candidate_info: str, job_description: str) -> str:
    """Generate 5–8 targeted screening interview questions for a specific candidate.

    The questions are tailored to:
      • Verify claimed skills and experience depth
      • Probe gaps identified against the job description
      • Assess culture / role fit

    Args:
        candidate_info:  JSON string with candidate name, skills, experience,
                         education, strengths, and gaps.
        job_description: The full job description text.

    Returns a numbered list of questions the interviewer should ask.
    """
    llm = get_llm()
    prompt = f"""You are a senior technical recruiter preparing a screening interview.

Candidate profile:
{candidate_info}

Job Description:
{job_description}

Write 5–8 targeted interview questions.  For each question:
  • Reference specific claims in the candidate's resume
  • Probe for depth, not just surface knowledge
  • Include at least 2 questions about gaps or missing requirements

Format: numbered list.  Each question on its own line."""
    response = llm.invoke([HumanMessage(content=prompt)])
    return response.content.strip()


# Collect all tools into one list — used when binding tools to the LLM
ALL_TOOLS = [
    read_file,
    list_files,
    write_file,
    search_in_file,
    rag_search,
    extract_requirements,
    compare_candidates,
    generate_interview_questions,
]

TOOL_MAP = {t.name: t for t in ALL_TOOLS}


# =============================================================================
# SECTION 4 — GRAPH NODES
# =============================================================================
# Each function is a LangGraph node.
#
# Signature rule: every node takes (state: AgentState) and returns a dict.
# The dict contains ONLY the state keys the node changed.
# LangGraph merges this partial dict back into the full state automatically.
#
# Execution order (defined in Section 5):
#   START
#     → parse_jd_node
#     → extract_requirements_node
#     → search_resumes_node        ← Round 1: all resumes → top 10 via RAG
#     → rank_candidates_node       ← Round 2: deep LLM analysis of top 10
#     → generate_report_node       ← Round 3: formal match report
#     → human_feedback_node        ← pause and wait for user
#         ├─ "refine"   → extract_requirements_node (loop back)
#         ├─ "compare"  → compare_candidates_node
#         ├─ "questions"→ interview_questions_node
#         └─ "end"      → END
# =============================================================================

# ── Helper: ask the LLM a question and return its text response ───────────────

def _llm_ask(prompt: str) -> str:
    """Send a single prompt to the LLM and return the text content."""
    llm = get_llm()
    response = llm.invoke([HumanMessage(content=prompt)])
    content = response.content
    if isinstance(content, list):
        # Some models return a list of content blocks
        return " ".join(c.get("text", "") if isinstance(c, dict) else str(c) for c in content)
    return str(content).strip()


# ── Node 1: parse_jd_node ─────────────────────────────────────────────────────
# Purpose: Understand what kind of role is being described.
# Input:   state["job_description"]
# Output:  state["parsed_jd_summary"]  — a short plain-English summary
# Why:     Subsequent nodes reference the summary for context.  Also confirms
#          to the user that the agent correctly understood the JD.

def parse_jd_node(state: AgentState) -> dict:
    """Node 1 — Parse and summarise the job description."""
    jd = state.get("job_description", "")
    resumes_dir = state.get("resumes_dir", RESUMES_DIR)

    if not jd:
        return {
            "parsed_jd_summary": "⚠️ No job description provided.",
            "current_stage": "parse_jd",
            "messages": [AIMessage(content="I need a job description to start matching. Please paste one.")],
        }

    summary = _llm_ask(
        f"Summarise this job description in 3–4 sentences. Focus on: "
        f"the role title, key responsibilities, and the top 5 required skills.\n\n{jd}"
    )

    msg = f"📋 **Job Description Summary**\n\n{summary}\n\n_Proceeding to extract requirements..._"
    return {
        "parsed_jd_summary": summary,
        "current_stage": "parse_jd",
        "resumes_dir": resumes_dir,
        "messages": [AIMessage(content=msg)],
    }


# ── Node 2: extract_requirements_node ────────────────────────────────────────
# Purpose: Split the JD into must-have vs nice-to-have requirements.
# Input:   state["job_description"]
# Output:  state["requirements"]  — structured dict
# Why:     Structured requirements are used in Round 2 to score each candidate
#          precisely (did they tick every must-have box?).

def extract_requirements_node(state: AgentState) -> dict:
    """Node 2 — Extract structured requirements from the JD."""
    jd = state.get("job_description", "")
    feedback = state.get("feedback_history", [])

    # If there is feedback from a previous round, append it to the JD prompt
    # so the LLM re-extracts with the user's updated priorities.
    feedback_context = ""
    if feedback:
        latest_feedback = feedback[-1]
        feedback_context = f"\n\nUser has refined the requirements:\n{latest_feedback}"

    raw = extract_requirements.invoke({"job_description": jd + feedback_context})

    try:
        reqs = json.loads(raw)
    except Exception:
        reqs = {"must_have": [], "nice_to_have": [], "min_years": None, "education": None}

    must = "\n".join(f"  • {r}" for r in reqs.get("must_have", []))
    nice = "\n".join(f"  • {r}" for r in reqs.get("nice_to_have", []))
    msg = (
        f"✅ **Requirements Extracted**\n\n"
        f"**Must-have:**\n{must or '  (none detected)'}\n\n"
        f"**Nice-to-have:**\n{nice or '  (none detected)'}\n\n"
        f"_Starting resume search (Round 1)..._"
    )
    return {
        "requirements": reqs,
        "current_stage": "extract_requirements",
        "messages": [AIMessage(content=msg)],
    }


# ── Node 3: search_resumes_node ───────────────────────────────────────────────
# Purpose: Round 1 screening — fast semantic search to narrow 100 → top 10.
# Input:   state["job_description"], state["resumes_dir"]
# Output:  state["all_candidates"], state["shortlist"]
# Why:     Vector search is O(log n) and requires no LLM — it's very fast.
#          We use it to get a first cut before the expensive LLM deep-analysis.

def search_resumes_node(state: AgentState) -> dict:
    """Node 3 — Round 1: semantic search, get top 10 candidates."""
    jd          = state.get("job_description", "")
    resumes_dir = state.get("resumes_dir", RESUMES_DIR)

    raw = rag_search.invoke({"query": jd, "top_k": 20, "resumes_dir": resumes_dir})
    data = json.loads(raw)

    candidates = data.get("candidates", [])
    total      = data.get("total_found", len(candidates))
    shortlist  = candidates[:10]  # Round 1 cut: top 10

    if not candidates:
        msg = "⚠️ No resumes found. Make sure resumes are in the resumes/ folder and indexed."
    else:
        lines = "\n".join(
            f"  {i+1}. **{c['name']}** — score {c['match_score']}/100 "
            f"| {c.get('experience_years') or '?'} yrs | skills: {', '.join(c['skills'][:4])}"
            for i, c in enumerate(shortlist)
        )
        msg = (
            f"🔍 **Round 1 Screening Complete**\n\n"
            f"Searched {total} candidates via semantic search.\n"
            f"Top {len(shortlist)} shortlisted:\n\n{lines}\n\n"
            f"_Proceeding to deep analysis (Round 2)..._"
        )

    return {
        "all_candidates": candidates,
        "shortlist":      shortlist,
        "screening_round": 1,
        "current_stage":  "search_resumes",
        "messages":       [AIMessage(content=msg)],
    }


# ── Node 4: rank_candidates_node ──────────────────────────────────────────────
# Purpose: Round 2 — deep LLM analysis of the top 10.
#          For each candidate, the LLM reads their resume excerpt and scores
#          them against must-have requirements.
# Input:   state["shortlist"], state["requirements"]
# Output:  state["final_candidates"], state["ranking_reasoning"],
#          state["strengths_gaps"]

def rank_candidates_node(state: AgentState) -> dict:
    """Node 4 — Round 2: deep analysis of top 10, produce top 3."""
    shortlist = state.get("shortlist", [])
    reqs      = state.get("requirements", {})
    jd        = state.get("job_description", "")

    if not shortlist:
        return {
            "final_candidates": [],
            "ranking_reasoning": {},
            "strengths_gaps": {},
            "screening_round": 2,
            "current_stage": "rank_candidates",
            "messages": [AIMessage(content="⚠️ No candidates to analyse.")],
        }

    reasoning   = {}
    str_gaps    = {}
    scored      = []

    must_have_str = ", ".join(reqs.get("must_have", []))

    for candidate in shortlist:
        name        = candidate.get("name", "Unknown")
        skills      = ", ".join(candidate.get("skills", []))
        exp_years   = candidate.get("experience_years", "unknown")
        education   = ", ".join(candidate.get("education", []))
        base_score  = candidate.get("match_score", 0)
        resume_path = candidate.get("resume_path", "")

        # Ask the LLM to evaluate this candidate against must-haves
        analysis_prompt = (
            f"Evaluate this candidate for the following role.\n\n"
            f"Must-have requirements: {must_have_str or 'See JD'}\n"
            f"Job Description excerpt: {jd[:600]}\n\n"
            f"Candidate: {name}\n"
            f"Skills: {skills}\n"
            f"Experience: {exp_years} years\n"
            f"Education: {education}\n\n"
            f"Return a JSON object only (no markdown):\n"
            f'{{"adjusted_score": <0-100>, '
            f'"strengths": ["s1","s2"], '
            f'"gaps": ["g1","g2"], '
            f'"recommendation": "hire|maybe|no-hire", '
            f'"one_line_reason": "..."}}'
        )
        raw_analysis = _llm_ask(analysis_prompt)
        raw_analysis = re.sub(r"^```(?:json)?\s*", "", raw_analysis)
        raw_analysis = re.sub(r"\s*```$", "", raw_analysis)

        try:
            analysis = json.loads(raw_analysis)
        except Exception:
            analysis = {
                "adjusted_score": base_score,
                "strengths": candidate.get("skills", [])[:3],
                "gaps": [],
                "recommendation": "maybe",
                "one_line_reason": candidate.get("reasoning", ""),
            }

        final_score = (base_score * 0.4) + (analysis.get("adjusted_score", base_score) * 0.6)
        candidate["final_score"]     = round(final_score)
        candidate["recommendation"]  = analysis.get("recommendation", "maybe")
        reasoning[name]              = analysis.get("one_line_reason", "")
        str_gaps[name] = {
            "strengths": analysis.get("strengths", []),
            "gaps":      analysis.get("gaps", []),
        }
        scored.append(candidate)

    scored.sort(key=lambda c: c["final_score"], reverse=True)
    final_top3 = scored[:3]

    lines = "\n".join(
        f"  {i+1}. **{c['name']}** — final score {c['final_score']}/100 "
        f"| {c.get('recommendation','?').upper()} | {reasoning.get(c['name'],'')}"
        for i, c in enumerate(final_top3)
    )
    msg = (
        f"🏆 **Round 2 Analysis Complete**\n\n"
        f"Deep-analysed {len(shortlist)} candidates with the LLM.\n"
        f"Top 3 candidates:\n\n{lines}\n\n"
        f"_Generating final report (Round 3)..._"
    )
    return {
        "final_candidates": final_top3,
        "ranking_reasoning": reasoning,
        "strengths_gaps":   str_gaps,
        "screening_round":  2,
        "current_stage":    "rank_candidates",
        "messages":         [AIMessage(content=msg)],
    }


# ── Node 5: generate_report_node ──────────────────────────────────────────────
# Purpose: Round 3 — write a formal hire/no-hire report.
# Input:   state["final_candidates"], state["strengths_gaps"], state["requirements"]
# Output:  state["match_report"]

def generate_report_node(state: AgentState) -> dict:
    """Node 5 — Round 3: generate final match report with recommendations."""
    final   = state.get("final_candidates", [])
    sg      = state.get("strengths_gaps", {})
    reqs    = state.get("requirements", {})
    jd      = state.get("job_description", "")

    if not final:
        return {
            "match_report": "No candidates available for report.",
            "screening_round": 3,
            "current_stage": "generate_report",
            "messages": [AIMessage(content="⚠️ Cannot generate report — no candidates found.")],
        }

    candidates_detail = []
    for c in final:
        name = c.get("name", "Unknown")
        sg_data = sg.get(name, {})
        candidates_detail.append(
            f"Name: {name}\n"
            f"Score: {c.get('final_score', 0)}/100\n"
            f"Recommendation: {c.get('recommendation','?')}\n"
            f"Strengths: {', '.join(sg_data.get('strengths', []))}\n"
            f"Gaps: {', '.join(sg_data.get('gaps', []))}\n"
        )

    report_prompt = (
        f"Write a formal candidate match report for a recruiter.\n\n"
        f"Role requirements: {json.dumps(reqs, indent=2)}\n\n"
        f"Top candidates:\n\n{'---'.join(candidates_detail)}\n\n"
        f"The report should include:\n"
        f"1. Executive summary (2–3 sentences)\n"
        f"2. For each candidate: strengths, gaps, hire/no-hire recommendation\n"
        f"3. Final recommendation: who to hire and why\n"
        f"4. For borderline candidates: what would make them a strong hire\n\n"
        f"Use markdown formatting.  Be specific and actionable."
    )

    report = _llm_ask(report_prompt)

    return {
        "match_report":    report,
        "screening_round": 3,
        "current_stage":   "generate_report",
        "messages":        [AIMessage(content=f"📊 **Final Match Report**\n\n{report}")],
    }


# ── Node 6: human_feedback_node ───────────────────────────────────────────────
# Purpose: Pause the graph and surface results to the user.
#          After seeing the report, the user can:
#            • Ask to refine with new criteria
#            • Ask for a side-by-side comparison
#            • Ask for interview questions
#            • Accept results and end
#
# This node interprets the LAST human message and sets next_action.
# The conditional router in the graph reads next_action to decide where to go.

def human_feedback_node(state: AgentState) -> dict:
    """Node 6 — Parse user feedback and decide next action."""
    messages = state.get("messages", [])

    # Find the last human message
    last_human = ""
    for msg in reversed(messages):
        if isinstance(msg, HumanMessage):
            last_human = msg.content.lower()
            break

    # Default: if there's nothing from the human yet, just show the report
    if not last_human:
        return {
            "next_action": "wait",
            "current_stage": "human_feedback",
            "messages": [AIMessage(content=(
                "✅ Report ready!  What would you like to do next?\n\n"
                "• Type **refine** + new criteria to re-rank with updated requirements\n"
                "• Type **compare** to see a side-by-side comparison of top candidates\n"
                "• Type **questions** to generate interview questions\n"
                "• Type **done** or **end** to finish"
            ))],
        }

    # Interpret the user's intent from their message
    feedback_history = list(state.get("feedback_history", []))

    if any(kw in last_human for kw in ("refine", "update", "change", "adjust", "priorit", "focus on", "more weight")):
        feedback_history.append(last_human)
        return {
            "next_action":    "refine",
            "feedback_history": feedback_history,
            "current_stage":  "human_feedback",
            "messages":       [AIMessage(content="🔄 Got it! Re-ranking with your updated criteria...")],
        }

    if any(kw in last_human for kw in ("compare", "comparison", "side by side", "versus", "vs", "difference")):
        return {
            "next_action":   "compare",
            "current_stage": "human_feedback",
            "messages":      [AIMessage(content="📊 Generating side-by-side comparison...")],
        }

    if any(kw in last_human for kw in ("question", "interview", "screen", "ask", "screening")):
        return {
            "next_action":   "questions",
            "current_stage": "human_feedback",
            "messages":      [AIMessage(content="❓ Generating interview questions...")],
        }

    if any(kw in last_human for kw in ("done", "end", "finish", "exit", "quit", "thank", "ok", "okay", "great")):
        return {
            "next_action":   "end",
            "current_stage": "human_feedback",
            "messages":      [AIMessage(content="✅ Matching session complete!  Good luck with the hiring process.")],
        }

    # Unknown intent — ask the LLM to interpret it
    classification = _llm_ask(
        f"The user said: \"{last_human}\"\n\n"
        f"Classify their intent as exactly one word: "
        f"refine, compare, questions, or end.\n"
        f"Reply with only that one word."
    ).strip().lower()

    action = classification if classification in ("refine", "compare", "questions", "end") else "end"

    if action == "refine":
        feedback_history.append(last_human)

    return {
        "next_action":      action,
        "feedback_history": feedback_history,
        "current_stage":    "human_feedback",
        "messages":         [AIMessage(content=f"Processing: {action}...")],
    }


# ── Node 7: compare_candidates_node ──────────────────────────────────────────

def compare_candidates_node(state: AgentState) -> dict:
    """Node 7 — Generate a side-by-side markdown comparison of top candidates."""
    final = state.get("final_candidates", [])
    sg    = state.get("strengths_gaps", {})

    if not final:
        return {
            "comparison_data": "No candidates to compare.",
            "next_action": "end",
            "current_stage": "compare",
            "messages": [AIMessage(content="⚠️ No candidates available for comparison.")],
        }

    candidate_list = []
    for c in final:
        name = c.get("name", "Unknown")
        sg_data = sg.get(name, {})
        candidate_list.append({
            "name":             name,
            "match_score":      c.get("final_score", c.get("match_score", 0)),
            "skills":           c.get("skills", [])[:5],
            "experience_years": c.get("experience_years"),
            "education":        c.get("education", []),
            "strengths":        sg_data.get("strengths", []),
            "gaps":             sg_data.get("gaps", []),
        })

    comparison = compare_candidates.invoke({"candidate_data": json.dumps(candidate_list)})

    return {
        "comparison_data": comparison,
        "next_action": "wait",
        "current_stage": "compare",
        "messages": [AIMessage(content=f"📊 **Candidate Comparison**\n\n{comparison}\n\n"
                               f"What would you like to do next?  (refine / questions / done)")],
    }


# ── Node 8: interview_questions_node ─────────────────────────────────────────

def interview_questions_node(state: AgentState) -> dict:
    """Node 8 — Generate screening interview questions for each top candidate."""
    final = state.get("final_candidates", [])
    sg    = state.get("strengths_gaps", {})
    jd    = state.get("job_description", "")

    if not final:
        return {
            "interview_questions": {},
            "next_action": "end",
            "current_stage": "interview_questions",
            "messages": [AIMessage(content="⚠️ No candidates available for question generation.")],
        }

    all_questions: dict = {}
    output_parts: list  = []

    for c in final[:3]:  # Generate for top 3 only
        name    = c.get("name", "Unknown")
        sg_data = sg.get(name, {})
        info    = {
            "name":             name,
            "skills":           c.get("skills", []),
            "experience_years": c.get("experience_years"),
            "education":        c.get("education", []),
            "strengths":        sg_data.get("strengths", []),
            "gaps":             sg_data.get("gaps", []),
        }
        questions = generate_interview_questions.invoke({
            "candidate_info": json.dumps(info),
            "job_description": jd,
        })
        all_questions[name] = questions
        output_parts.append(f"### {name}\n{questions}")

    combined = "\n\n---\n\n".join(output_parts)
    return {
        "interview_questions": all_questions,
        "next_action": "wait",
        "current_stage": "interview_questions",
        "messages": [AIMessage(content=f"❓ **Interview Questions**\n\n{combined}\n\n"
                               f"What would you like to do next?  (compare / refine / done)")],
    }


# =============================================================================
# SECTION 5 — GRAPH CONSTRUCTION
# =============================================================================
# Here we wire all nodes into a directed graph and define the edges.
#
# How LangGraph graphs work:
#   StateGraph(AgentState)   — creates a new graph with our state type
#   .add_node(name, fn)      — registers a node
#   .add_edge(A, B)          — A always goes to B (unconditional)
#   .add_conditional_edges(  — A goes to one of several targets based on a
#       node, router, map)     function that reads state and returns a key
#   .set_entry_point(name)   — which node runs first
#   .compile(checkpointer)   — freeze the graph and attach a memory backend
#
# The MemorySaver checkpointer saves state after every node so:
#   • If a node crashes, state is not lost
#   • Multiple conversations can run simultaneously with different thread_ids
# =============================================================================

def _route_after_feedback(state: AgentState) -> str:
    """
    Router called after human_feedback_node.
    Reads state["next_action"] and returns the key LangGraph uses to
    look up the next node in the conditional edge map.
    """
    action = state.get("next_action", "end")
    # "wait" means the feedback node already sent a clarifying message
    # and we want to stay in the feedback loop (re-enter human_feedback)
    if action == "wait":
        return "human_feedback"
    return action  # one of: refine, compare, questions, end


def build_graph() -> tuple:
    """
    Build and compile the LangGraph state machine.

    Returns:
        (compiled_app, memory_saver)  — the app is the runnable graph;
        the memory_saver holds conversation state in RAM.
    """
    memory = MemorySaver()

    builder = StateGraph(AgentState)

    # ── Register all nodes ────────────────────────────────────────────────────
    builder.add_node("parse_jd",             parse_jd_node)
    builder.add_node("extract_requirements", extract_requirements_node)
    builder.add_node("search_resumes",       search_resumes_node)
    builder.add_node("rank_candidates",      rank_candidates_node)
    builder.add_node("generate_report",      generate_report_node)
    builder.add_node("human_feedback",       human_feedback_node)
    builder.add_node("compare_candidates",   compare_candidates_node)
    builder.add_node("interview_questions",  interview_questions_node)

    # ── Define the linear pipeline ────────────────────────────────────────────
    # This is the main "happy path": parse → extract → search → rank → report
    builder.add_edge(START,                  "parse_jd")
    builder.add_edge("parse_jd",             "extract_requirements")
    builder.add_edge("extract_requirements", "search_resumes")
    builder.add_edge("search_resumes",       "rank_candidates")
    builder.add_edge("rank_candidates",      "generate_report")
    builder.add_edge("generate_report",      "human_feedback")

    # ── Define the feedback loop ──────────────────────────────────────────────
    # After human_feedback, the router function decides what happens next.
    # The mapping says: if router returns "refine" → go to extract_requirements,
    # "compare" → compare_candidates, etc.
    builder.add_conditional_edges(
        "human_feedback",
        _route_after_feedback,
        {
            "refine":            "extract_requirements",   # loop back, re-rank
            "compare":           "compare_candidates",
            "questions":         "interview_questions",
            "human_feedback":    "human_feedback",         # wait / clarify
            "end":               END,
        }
    )

    # ── After compare/questions, go back to human_feedback ───────────────────
    # This creates the multi-turn loop: user can keep asking for different
    # things without restarting the whole pipeline.
    builder.add_edge("compare_candidates",  "human_feedback")
    builder.add_edge("interview_questions", "human_feedback")

    # ── Compile with interrupt_before ─────────────────────────────────────────
    # interrupt_before=["human_feedback"] is the KEY change that makes the
    # human-in-the-loop pattern work correctly.
    #
    # Without it:  graph runs parse_jd → ... → generate_report → human_feedback
    #              human_feedback_node sees the JD as the "last human message",
    #              classifies it as "end", graph terminates, and chat() can't
    #              continue — it restarts the pipeline from scratch.
    #
    # With it:     graph runs parse_jd → ... → generate_report, then PAUSES
    #              before executing human_feedback_node.  invoke() returns the
    #              current state to the caller.  When chat() is called next,
    #              it injects the real user message via update_state(), then
    #              calls invoke(None) which RESUMES from the pause point.
    #              Now human_feedback_node runs with the actual user feedback.
    #
    # This is the official LangGraph human-in-the-loop pattern.
    app = builder.compile(
        checkpointer=memory,
        interrupt_before=["human_feedback"],
    )
    return app, memory


# =============================================================================
# SECTION 6 — PUBLIC API
# =============================================================================
# MatchingAgent wraps the compiled graph with a clean Python interface.
# The Streamlit UI (chat_interface.py) and the CLI both use this class.
# =============================================================================

class MatchingAgent:
    """
    High-level interface to the LangGraph matching agent.

    How the interrupt pattern works
    ────────────────────────────────
    The graph is compiled with interrupt_before=["human_feedback"].
    This means every time the graph REACHES the human_feedback node,
    it PAUSES before executing that node and returns control here.

    start_session():
      • Calls invoke(initial_state) with the JD
      • Graph runs all pipeline nodes (parse → extract → search → rank → report)
      • Graph PAUSES before human_feedback
      • invoke() returns the current state
      • We extract and return all new AI messages (stages 1–5)

    chat(user_message):
      • Calls update_state() to inject the user's message into the paused state
      • Calls invoke(None) to RESUME from the pause point
      • human_feedback_node now runs with the real user message available
      • It routes to compare/questions/refine/end as appropriate
      • If compare or questions runs, graph flows back to human_feedback → pauses again
      • We extract and return only the NEW messages added since last invoke

    Usage:
        agent = MatchingAgent()
        responses = agent.start_session(jd_text)   # returns stage 1-5 messages
        responses = agent.chat("compare top 3")    # returns comparison
        responses = agent.chat("generate questions")
        responses = agent.chat("done")             # ends session
    """

    def __init__(self, thread_id: str = "default"):
        self.app, self.memory = build_graph()
        self.config = {"configurable": {"thread_id": thread_id}}
        self.started  = False
        self._msg_count = 0   # tracks how many messages were in state before invoke

    # ── Helpers ───────────────────────────────────────────────────────────────

    def _current_messages(self) -> list:
        """Read the current message list from the checkpointed state."""
        snapshot = self.app.get_state(self.config)
        if snapshot and snapshot.values:
            return snapshot.values.get("messages", [])
        return []

    def _ai_texts_since(self, prev_count: int) -> list[str]:
        """
        Return the text of all AI messages added AFTER index prev_count.

        This is how we avoid returning ALL messages on every chat() call —
        we only return what's new since the last invoke().
        """
        msgs = self._current_messages()
        texts = []
        for msg in msgs[prev_count:]:
            if isinstance(msg, AIMessage):
                content = msg.content
                if isinstance(content, list):
                    for block in content:
                        if isinstance(block, dict) and block.get("type") == "text":
                            if block["text"].strip():
                                texts.append(block["text"])
                elif isinstance(content, str) and content.strip():
                    texts.append(content)
        return texts

    # ── Public API ────────────────────────────────────────────────────────────

    def start_session(self, job_description: str,
                      resumes_dir: str = RESUMES_DIR) -> list[str]:
        """
        Begin a new matching session with a job description.
        Runs the full pipeline: parse → extract → search → rank → report,
        then PAUSES before human_feedback (waiting for your first chat() call).

        Returns all AI messages produced by the 5 pipeline stages.
        """
        initial_state: AgentState = {
            "messages":            [HumanMessage(content=job_description)],
            "job_description":     job_description,
            "parsed_jd_summary":   "",
            "requirements":        {},
            "all_candidates":      [],
            "shortlist":           [],
            "final_candidates":    [],
            "match_report":        "",
            "ranking_reasoning":   {},
            "strengths_gaps":      {},
            "comparison_data":     "",
            "interview_questions": {},
            "current_stage":       "start",
            "screening_round":     0,
            "feedback_history":    [],
            "resumes_dir":         resumes_dir,
            "next_action":         "",
        }

        prev_count = 0  # No prior messages
        self.app.invoke(initial_state, config=self.config)
        self.started     = True
        self._msg_count  = len(self._current_messages())
        return self._ai_texts_since(prev_count)

    def chat(self, user_message: str) -> list[str]:
        """
        Send a follow-up message after the pipeline has run.

        Internally:
          1. Records how many messages exist right now (prev_count)
          2. Injects the user message into the paused state via update_state()
          3. Resumes the graph from the pause point via invoke(None)
          4. Returns only messages added AFTER prev_count

        Args:
            user_message: Natural language command from the user

        Returns:
            List of new AI response strings generated in this turn
        """
        if not self.started:
            return ["Please start a session first by providing a job description."]

        # Step 1: snapshot current message count so we know what's "new"
        prev_count = len(self._current_messages())

        # Step 2: inject the user's message into the paused state
        # update_state() merges the provided dict into the current checkpoint.
        # Because messages uses add_messages, the HumanMessage is appended.
        self.app.update_state(
            self.config,
            {"messages": [HumanMessage(content=user_message)]},
        )

        # Step 3: resume the graph from the interrupt point (human_feedback)
        # Passing None as input means "continue with existing state, don't reset".
        self.app.invoke(None, config=self.config)

        # Step 4: return only the new AI messages
        self._msg_count = len(self._current_messages())
        return self._ai_texts_since(prev_count)

    def get_state(self) -> dict:
        """Return the current agent state snapshot (for debugging / UI)."""
        snapshot = self.app.get_state(self.config)
        return snapshot.values if snapshot and snapshot.values else {}


# =============================================================================
# SECTION 7 — CLI ENTRY POINT
# =============================================================================

def run_cli():
    """Simple command-line interface for the matching agent."""
    import uuid

    print("\n" + "="*65)
    print("  🤖  Resume Matching Agent  —  Milestone 3 (LangGraph + Ollama)")
    print("="*65)
    print(f"  Model:     {OLLAMA_MODEL}")
    print(f"  Ollama:    {OLLAMA_URL}")
    print(f"  Resumes:   {RESUMES_DIR}")
    print("="*65)
    print("\nPaste a job description below, then press Enter twice to submit.")
    print("Type 'quit' to exit.\n")

    agent = MatchingAgent(thread_id=str(uuid.uuid4()))

    # Collect multi-line JD
    print("📄 Job Description:")
    lines = []
    try:
        while True:
            line = input()
            if line.lower() == "quit":
                print("👋 Goodbye!")
                return
            if line == "" and lines and lines[-1] == "":
                break
            lines.append(line)
    except KeyboardInterrupt:
        print("\n👋 Goodbye!")
        return

    jd = "\n".join(lines).strip()
    if not jd:
        print("❌ No job description provided.")
        return

    print("\n⏳ Running pipeline (this takes a few minutes with local Ollama)...\n")
    responses = agent.start_session(jd, resumes_dir=RESUMES_DIR)
    for resp in responses:
        print(f"\n{resp}")

    # Feedback loop
    print("\n" + "─"*65)
    print("Pipeline complete!  You can now:")
    print("  • Type 'compare'   — side-by-side comparison")
    print("  • Type 'questions' — interview questions")
    print("  • Type 'refine <criteria>' — re-rank with new priorities")
    print("  • Type 'done'      — exit")
    print("─"*65)

    while True:
        try:
            user_input = input("\nYou: ").strip()
            if not user_input:
                continue
            if user_input.lower() in ("done", "exit", "quit"):
                print("\n👋 Session ended.  Good luck hiring!")
                break
            print("\n⏳ Processing...\n")
            responses = agent.chat(user_input)
            for resp in responses:
                print(f"\n{resp}")
        except KeyboardInterrupt:
            print("\n\n👋 Goodbye!")
            break


if __name__ == "__main__":
    run_cli()
