"""
LLM File Assistant with Function Calling — Ollama Edition (Milestone 1 updated)

Previously used Anthropic Claude via cloud API.
Now uses Ollama (local model) via LangChain's ChatOllama.

Why the change?
  Anthropic requires an API key and sends your data to the cloud.
  Ollama runs the model 100% locally — no key, no data leaving your machine.

How tool calling works with Ollama:
  1. We wrap each Python function as a LangChain @tool
  2. We bind the tools to the LLM with llm.bind_tools(tools)
  3. When the model wants to call a tool, it returns an AIMessage with
     tool_calls=[...] instead of plain text
  4. We detect that, execute the function, feed results back, and loop
     until the model produces a plain text reply
"""

import json
from typing import List, Dict, Any

from langchain_ollama import ChatOllama
from langchain_core.messages import HumanMessage, AIMessage, ToolMessage, SystemMessage
from langchain_core.tools import tool as lc_tool

# Import the raw Python functions from Milestone 1
from fs_tools import (
    read_file as _read_file,
    list_files as _list_files,
    write_file as _write_file,
    search_in_file as _search_in_file,
)

# ── Wrap each function as a LangChain tool ───────────────────────────────────
# @lc_tool turns a plain Python function into a Tool object that LangChain /
# Ollama can call.  The docstring becomes the description the model reads to
# decide WHEN to call the tool.

@lc_tool
def read_file(filepath: str) -> str:
    """Read and extract text content from a resume file (PDF, TXT, DOCX).
    Returns the file content along with metadata like filename and size."""
    result = _read_file(filepath)
    return json.dumps(result)

@lc_tool
def list_files(directory: str, extension: str = None) -> str:
    """List all files in a directory, optionally filtered by extension
    (e.g. '.pdf', '.txt'). Returns file names, sizes, and paths."""
    result = _list_files(directory, extension)
    return json.dumps(result)

@lc_tool
def write_file(filepath: str, content: str) -> str:
    """Write text content to a file, creating parent directories if needed.
    Returns success status and number of bytes written."""
    result = _write_file(filepath, content)
    return json.dumps(result)

@lc_tool
def search_in_file(filepath: str, keyword: str) -> str:
    """Search for a keyword inside a file (case-insensitive).
    Returns all matching lines with 2 lines of surrounding context."""
    result = _search_in_file(filepath, keyword)
    return json.dumps(result)

# The list of tool objects the LLM will know about
LC_TOOLS = [read_file, list_files, write_file, search_in_file]

# Map tool name → callable (needed to execute tool calls we receive)
TOOL_MAP = {t.name: t for t in LC_TOOLS}


class LLMFileAssistant:
    """
    File assistant powered by a local Ollama model with tool calling.

    Architecture:
      • ChatOllama  — the local LLM (qwen3-nothink by default)
      • bind_tools  — attaches the 4 file tools to the LLM so it can call them
      • message loop — we manually drive the tool-call → execute → continue loop
        so we can print verbose output at each step
    """

    SYSTEM_PROMPT = (
        "You are a helpful file assistant. You can read, list, write, and search "
        "files on the local file system using the provided tools. "
        "Always use the tools to fulfil the user's request — do not guess file "
        "contents. Summarise your findings clearly after using the tools."
    )

    def __init__(self, model: str = "qwen3-nothink:latest",
                 ollama_url: str = "http://localhost:11434"):
        """
        Initialise the assistant.

        Args:
            model:       Ollama model name (default: qwen3-nothink for speed)
            ollama_url:  Ollama server URL (default: local)
        """
        # ChatOllama is LangChain's wrapper around the Ollama HTTP API.
        # temperature=0 → deterministic outputs, good for tool calling.
        self.llm = ChatOllama(
            model=model,
            base_url=ollama_url,
            temperature=0,
        )

        # bind_tools tells the LLM the tool schemas so it can decide which
        # tool to call and with what arguments.
        self.llm_with_tools = self.llm.bind_tools(LC_TOOLS)

        # Conversation history: a list of LangChain message objects.
        # Each round we append the user message, any AI messages, any
        # ToolMessages (results), and the final AI reply.
        self.history: List = [SystemMessage(content=self.SYSTEM_PROMPT)]

    # ── Tool execution ────────────────────────────────────────────────────────

    def _execute_tool_call(self, tool_call: dict, verbose: bool) -> ToolMessage:
        """Execute one tool call returned by the model and return a ToolMessage."""
        name = tool_call["name"]
        args = tool_call["args"]
        call_id = tool_call["id"]

        if verbose:
            print(f"  🔧 Tool call: {name}({json.dumps(args, indent=6)})")

        if name not in TOOL_MAP:
            content = json.dumps({"success": False, "error": f"Unknown tool: {name}"})
        else:
            try:
                content = TOOL_MAP[name].invoke(args)
            except Exception as exc:
                content = json.dumps({"success": False, "error": str(exc)})

        if verbose:
            # Trim long output so the terminal stays readable
            preview = content if len(content) < 400 else content[:400] + " …"
            print(f"  📤 Result: {preview}\n")

        return ToolMessage(content=content, tool_call_id=call_id)

    # ── Main chat method ──────────────────────────────────────────────────────

    def chat(self, user_message: str, verbose: bool = True) -> str:
        """
        Send a message and return the assistant's reply.

        The method drives the full tool-call loop:
          1. Send current history to the LLM
          2. If the LLM responds with tool_calls → execute them, append results
          3. Repeat until the LLM produces a plain text reply
          4. Return that final reply

        Args:
            user_message: Natural language query from the user
            verbose:      Print tool calls and results to stdout
        Returns:
            The assistant's final natural-language response
        """
        self.history.append(HumanMessage(content=user_message))

        if verbose:
            print(f"\n{'='*60}")
            print(f"👤 User: {user_message}")
            print(f"{'='*60}")

        # Tool-call loop — runs until the model stops calling tools
        while True:
            response: AIMessage = self.llm_with_tools.invoke(self.history)
            self.history.append(response)

            # If no tool calls → model produced its final answer
            if not response.tool_calls:
                break

            if verbose:
                print(f"\n🤖 Model wants to call {len(response.tool_calls)} tool(s):")

            # Execute every tool the model requested in this round
            for tc in response.tool_calls:
                tool_msg = self._execute_tool_call(tc, verbose)
                self.history.append(tool_msg)

        # The final response content
        final_text = response.content if isinstance(response.content, str) else ""

        if verbose:
            print(f"\n🤖 Assistant: {final_text}")
            print(f"{'='*60}\n")

        return final_text

    def reset_conversation(self):
        """Clear conversation history (keeps system prompt)."""
        self.history = [SystemMessage(content=self.SYSTEM_PROMPT)]


def main():
    """Interactive CLI — powered by Ollama (no API key needed)."""
    print("🤖 LLM File Assistant  (Ollama edition — 100% local)")
    print("=" * 60)
    print("Model: qwen3-nothink:latest  |  No API key needed")
    print("\nExample queries:")
    print("  • List all PDF files in the resumes folder")
    print("  • Find resumes mentioning Python experience")
    print("  • Read john_doe.txt and give me a brief summary")
    print("  • Search for 'machine learning' across all resumes")
    print("\nCommands: 'quit'/'exit' to stop  |  'reset' to clear history\n")

    assistant = LLMFileAssistant()

    while True:
        try:
            user_input = input("You: ").strip()
            if not user_input:
                continue
            if user_input.lower() in ("quit", "exit"):
                print("\n👋 Goodbye!")
                break
            if user_input.lower() == "reset":
                assistant.reset_conversation()
                print("🔄 Conversation cleared.\n")
                continue
            assistant.chat(user_input, verbose=True)
        except KeyboardInterrupt:
            print("\n\n👋 Goodbye!")
            break
        except Exception as exc:
            print(f"\n❌ Error: {exc}\n")


def run_examples():
    """Run pre-defined examples to demo capabilities."""
    print("🚀 Running Example Queries  (Ollama edition)\n")
    assistant = LLMFileAssistant()

    examples = [
        "List all files in the resumes folder",
        "Read the first resume you find and give me a brief summary",
        "Search for 'Python' in all resume files and tell me which ones mention it",
    ]

    for i, query in enumerate(examples, 1):
        print(f"\n{'#'*60}")
        print(f"Example {i}/{len(examples)}")
        print(f"{'#'*60}")
        assistant.chat(query, verbose=True)
        if i < len(examples):
            input("\nPress Enter for next example...")


if __name__ == "__main__":
    import sys

    if len(sys.argv) > 1 and sys.argv[1] == "--examples":
        run_examples()
    else:
        main()
