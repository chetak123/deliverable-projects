"""
chat_interface.py — Streamlit Chat UI for the LangGraph Matching Agent
=======================================================================

Run with:
    cd deliverable-projects
    source venv/bin/activate
    streamlit run chat_interface.py

How Streamlit works (quick primer):
  • Every user interaction (button click, text input) triggers a full
    re-run of this script from top to bottom.
  • st.session_state is a persistent dict that survives re-runs.
    We use it to store: the agent object, chat history, and pipeline state.
  • @st.cache_resource means "create this object ONCE and reuse it".
    We use it so the agent isn't re-created on every re-run.
  • st.chat_message() renders chat bubbles.
  • st.chat_input() renders the message box at the bottom.
"""

import uuid
import streamlit as st

# ── Page config — must be the FIRST Streamlit call ───────────────────────────
st.set_page_config(
    page_title="AI Resume Matching Agent",
    page_icon="🤖",
    layout="wide",
)

# ── Import the agent (inside try so bad imports show nicely) ──────────────────
try:
    from matching_agent import MatchingAgent, OLLAMA_MODEL, OLLAMA_URL, RESUMES_DIR
    IMPORT_OK = True
except Exception as e:
    IMPORT_OK = False
    IMPORT_ERROR = str(e)

# =============================================================================
# SESSION STATE INITIALISATION
# =============================================================================
# st.session_state is like a Python dict that persists across Streamlit re-runs.
# We initialise keys here so the rest of the script can safely read them.

if "agent" not in st.session_state:
    st.session_state.agent = None          # MatchingAgent instance
if "thread_id" not in st.session_state:
    st.session_state.thread_id = str(uuid.uuid4())
if "chat_history" not in st.session_state:
    st.session_state.chat_history = []     # list of {"role": "user"|"assistant", "content": str}
if "pipeline_done" not in st.session_state:
    st.session_state.pipeline_done = False
if "jd_submitted" not in st.session_state:
    st.session_state.jd_submitted = False

# =============================================================================
# SIDEBAR — CONFIGURATION
# =============================================================================

with st.sidebar:
    st.title("⚙️ Configuration")

    if IMPORT_OK:
        st.success("✅ Agent loaded")
        st.info(f"**Model:** {OLLAMA_MODEL}\n\n**Ollama:** {OLLAMA_URL}")
    else:
        st.error("❌ Import failed")
        st.code(IMPORT_ERROR)

    st.divider()
    st.subheader("📁 Resumes Directory")
    resumes_dir = st.text_input("Path to resumes folder", value=RESUMES_DIR)

    st.divider()
    st.subheader("🎯 Quick Actions")
    st.write("After the pipeline runs, type one of:")
    st.markdown("""
    - `compare` — side-by-side candidate table
    - `questions` — interview questions for top 3
    - `refine <criteria>` — re-rank with new priorities
    - `done` / `end` — finish the session
    """)

    st.divider()
    if st.button("🔄 New Session", use_container_width=True):
        st.session_state.agent = None
        st.session_state.thread_id = str(uuid.uuid4())
        st.session_state.chat_history = []
        st.session_state.pipeline_done = False
        st.session_state.jd_submitted = False
        st.rerun()

# =============================================================================
# MAIN AREA
# =============================================================================

st.title("🤖 AI Resume Matching Agent")
st.caption(f"Powered by LangGraph + Ollama ({OLLAMA_MODEL}) — 100% local, no API key needed")

if not IMPORT_OK:
    st.error(f"Cannot start: {IMPORT_ERROR}")
    st.stop()

# ── Step 1: Job description input (shown only before pipeline starts) ─────────

if not st.session_state.jd_submitted:
    st.subheader("📋 Step 1: Paste your Job Description")
    st.write("The agent will run through the full 3-round screening pipeline automatically.")

    jd_text = st.text_area(
        "Job Description",
        height=300,
        placeholder=(
            "Senior Python Developer\n\n"
            "We are looking for a Senior Python Developer with 5+ years of experience...\n"
            "Requirements:\n"
            "• Python 3.8+\n"
            "• Django or FastAPI\n"
            "• AWS experience\n"
            "• PostgreSQL\n"
            "..."
        ),
    )

    col1, col2 = st.columns([3, 1])
    with col1:
        run_pipeline = st.button(
            "🚀 Start Matching Pipeline",
            type="primary",
            use_container_width=True,
            disabled=(not jd_text.strip()),
        )
    with col2:
        st.info(f"📁 {resumes_dir}")

    if run_pipeline and jd_text.strip():
        # Create the agent with a fresh thread_id
        st.session_state.agent = MatchingAgent(thread_id=st.session_state.thread_id)
        st.session_state.jd_submitted = True

        # Add the JD as a user message in the chat
        st.session_state.chat_history.append({
            "role": "user",
            "content": f"**Job Description submitted:**\n\n{jd_text[:300]}{'...' if len(jd_text) > 300 else ''}",
        })

        # Run the full pipeline (blocking — shows spinner while running)
        with st.spinner("⏳ Running 3-round screening pipeline (this takes a few minutes with local Ollama)..."):
            try:
                responses = st.session_state.agent.start_session(jd_text, resumes_dir=resumes_dir)
                for resp in responses:
                    if resp.strip():
                        st.session_state.chat_history.append({
                            "role": "assistant",
                            "content": resp,
                        })
                st.session_state.pipeline_done = True
            except Exception as exc:
                st.session_state.chat_history.append({
                    "role": "assistant",
                    "content": f"❌ Pipeline error: {exc}\n\nMake sure:\n"
                               f"1. Ollama is running (`ollama serve`)\n"
                               f"2. Model is available (`ollama pull {OLLAMA_MODEL}`)\n"
                               f"3. Resumes directory exists: `{resumes_dir}`",
                })

        st.rerun()

# ── Step 2: Show chat history ─────────────────────────────────────────────────

if st.session_state.chat_history:
    st.subheader("💬 Conversation")
    for entry in st.session_state.chat_history:
        with st.chat_message(entry["role"]):
            st.markdown(entry["content"])

# ── Step 3: Follow-up chat input (only after pipeline has run) ────────────────

if st.session_state.pipeline_done and st.session_state.agent:
    user_input = st.chat_input(
        "Ask a follow-up: compare / questions / refine <criteria> / done"
    )

    if user_input:
        # Show user message immediately
        st.session_state.chat_history.append({"role": "user", "content": user_input})
        with st.chat_message("user"):
            st.markdown(user_input)

        # Get agent response
        with st.spinner("🤔 Thinking..."):
            try:
                responses = st.session_state.agent.chat(user_input)
                for resp in responses:
                    if resp.strip():
                        st.session_state.chat_history.append({
                            "role": "assistant",
                            "content": resp,
                        })
                        with st.chat_message("assistant"):
                            st.markdown(resp)
            except Exception as exc:
                err_msg = f"❌ Error: {exc}"
                st.session_state.chat_history.append({"role": "assistant", "content": err_msg})
                with st.chat_message("assistant"):
                    st.error(err_msg)

        st.rerun()

elif st.session_state.jd_submitted and not st.session_state.pipeline_done:
    st.info("⏳ Pipeline is running...")
