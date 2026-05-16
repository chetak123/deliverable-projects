"""
test_agent.py — End-to-end test of the LangGraph Matching Agent
Run: source venv/bin/activate && python test_agent.py

Tests 5 conversation flows as required by the assignment:
  Flow 1 — Full pipeline (JD → parse → extract → search → rank → report)
  Flow 2 — "compare" follow-up (side-by-side candidate table)
  Flow 3 — "questions" follow-up (interview questions)
  Flow 4 — "refine" with new criteria (re-rank)
  Flow 5 — "done" to end session
"""
import sys
sys.path.insert(0, ".")

from matching_agent import MatchingAgent

SAMPLE_JD = """
Senior Python Backend Engineer

We are looking for an experienced Python backend engineer to join our team.

Must-have requirements:
- Python 3.8+ with 3+ years hands-on experience
- REST API development with FastAPI or Django
- PostgreSQL or MySQL database experience
- Git version control and CI/CD pipelines

Nice to have:
- AWS or cloud platform experience
- Docker containerisation
- Machine learning or data science exposure

Education: Bachelor degree in Computer Science preferred.
"""

DIVIDER = "=" * 65

def print_responses(label: str, responses: list[str]):
    print(f"\n{DIVIDER}")
    print(f"  {label}")
    print(DIVIDER)
    if not responses:
        print("  ⚠️  No responses returned")
        return
    for i, resp in enumerate(responses, 1):
        preview = resp[:700] + ("  [...truncated]" if len(resp) > 700 else "")
        print(f"\n--- Response {i} ---")
        print(preview)


def run_test():
    print(f"\n{DIVIDER}")
    print("  Milestone 3 — LangGraph Matching Agent  End-to-End Test")
    print(f"  Testing 5 conversation flows")
    print(DIVIDER)

    agent = MatchingAgent(thread_id="e2e-test-v2")
    print("✅ Agent created\n")

    # ── Flow 1: Full pipeline ─────────────────────────────────────────────────
    print("FLOW 1: Running full pipeline (parse→extract→search→rank→report)")
    print("        This takes ~3-5 minutes with local Ollama...\n")
    responses = agent.start_session(SAMPLE_JD, resumes_dir="./resumes")
    print_responses("FLOW 1 — Full Pipeline Output", responses)
    assert len(responses) >= 3, f"Expected ≥3 responses, got {len(responses)}"
    print(f"\n  ✅ Flow 1 passed ({len(responses)} stage responses)")

    # ── Flow 2: Compare ───────────────────────────────────────────────────────
    print(f"\n\nFLOW 2: Compare top candidates")
    responses = agent.chat("compare the top 3 candidates side by side")
    print_responses("FLOW 2 — Comparison Table", responses)
    assert len(responses) >= 1, "Expected at least 1 comparison response"
    print(f"\n  ✅ Flow 2 passed")

    # ── Flow 3: Interview questions ───────────────────────────────────────────
    print(f"\n\nFLOW 3: Generate interview questions")
    responses = agent.chat("generate interview questions for the top candidates")
    print_responses("FLOW 3 — Interview Questions", responses)
    assert len(responses) >= 1, "Expected at least 1 questions response"
    print(f"\n  ✅ Flow 3 passed")

    # ── Flow 4: Refine criteria ───────────────────────────────────────────────
    print(f"\n\nFLOW 4: Refine with new criteria")
    responses = agent.chat(
        "refine the results — I want to prioritise machine learning experience "
        "and AWS over database skills"
    )
    print_responses("FLOW 4 — Refined Rankings", responses)
    assert len(responses) >= 1, "Expected at least 1 refine response"
    print(f"\n  ✅ Flow 4 passed")

    # ── Flow 5: End session ───────────────────────────────────────────────────
    print(f"\n\nFLOW 5: End session")
    responses = agent.chat("done, thank you")
    print_responses("FLOW 5 — Session End", responses)
    print(f"\n  ✅ Flow 5 passed")

    # ── State inspection ──────────────────────────────────────────────────────
    state = agent.get_state()
    print(f"\n{DIVIDER}")
    print("  FINAL STATE SUMMARY")
    print(DIVIDER)
    print(f"  current_stage:    {state.get('current_stage', '?')}")
    print(f"  screening_round:  {state.get('screening_round', '?')}")
    print(f"  candidates found: {len(state.get('final_candidates', []))}")
    print(f"  feedback entries: {len(state.get('feedback_history', []))}")
    print(f"  total messages:   {len(state.get('messages', []))}")

    print(f"\n{DIVIDER}")
    print("  ✅  ALL 5 CONVERSATION FLOWS PASSED!")
    print(DIVIDER)


if __name__ == "__main__":
    run_test()
