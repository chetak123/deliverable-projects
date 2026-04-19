"""
LLM File Assistant with Function Calling
Integrates file system tools with LLM for natural language file operations.
"""

import os
import json
from typing import List, Dict, Any
from anthropic import Anthropic
from fs_tools import TOOLS, TOOL_FUNCTIONS


class LLMFileAssistant:
    """
    Assistant that uses LLM with function calling to perform file operations.
    """
    
    def __init__(self, api_key: str = None):
        """
        Initialize the LLM File Assistant.
        
        Args:
            api_key: Anthropic API key (or set ANTHROPIC_API_KEY env var)
        """
        self.api_key = api_key or os.environ.get('ANTHROPIC_API_KEY')
        if not self.api_key:
            raise ValueError("API key must be provided or set in ANTHROPIC_API_KEY environment variable")
        
        self.client = Anthropic(api_key=self.api_key)
        self.model = "claude-3-5-sonnet-20241022"
        self.conversation_history = []
        
    def process_tool_call(self, tool_name: str, tool_input: Dict[str, Any]) -> Dict[str, Any]:
        """
        Execute a tool call and return the result.
        
        Args:
            tool_name: Name of the tool to call
            tool_input: Input parameters for the tool
            
        Returns:
            dict: Result from the tool execution
        """
        if tool_name not in TOOL_FUNCTIONS:
            return {
                'success': False,
                'error': f'Unknown tool: {tool_name}'
            }
        
        tool_func = TOOL_FUNCTIONS[tool_name]
        
        try:
            result = tool_func(**tool_input)
            return result
        except Exception as e:
            return {
                'success': False,
                'error': f'Error executing {tool_name}: {str(e)}'
            }
    
    def chat(self, user_message: str, verbose: bool = True) -> str:
        """
        Send a message to the assistant and get a response.
        
        Args:
            user_message: The user's message/query
            verbose: Whether to print tool calls and results
            
        Returns:
            str: The assistant's response
        """
        # Add user message to history
        self.conversation_history.append({
            "role": "user",
            "content": user_message
        })
        
        if verbose:
            print(f"\n{'='*60}")
            print(f"User: {user_message}")
            print(f"{'='*60}\n")
        
        # Make API call with tools
        response = self.client.messages.create(
            model=self.model,
            max_tokens=4096,
            tools=TOOLS,
            messages=self.conversation_history
        )
        
        # Process response and handle tool calls
        while response.stop_reason == "tool_use":
            # Extract tool uses from response
            assistant_content = []
            tool_results = []
            
            for block in response.content:
                if block.type == "text":
                    assistant_content.append(block)
                    if verbose and block.text:
                        print(f"Assistant: {block.text}\n")
                        
                elif block.type == "tool_use":
                    assistant_content.append(block)
                    tool_name = block.name
                    tool_input = block.input
                    
                    if verbose:
                        print(f"🔧 Calling tool: {tool_name}")
                        print(f"   Input: {json.dumps(tool_input, indent=2)}")
                    
                    # Execute the tool
                    result = self.process_tool_call(tool_name, tool_input)
                    
                    if verbose:
                        print(f"   Result: {json.dumps(result, indent=2)}\n")
                    
                    # Add tool result
                    tool_results.append({
                        "type": "tool_result",
                        "tool_use_id": block.id,
                        "content": json.dumps(result)
                    })
            
            # Add assistant message to history
            self.conversation_history.append({
                "role": "assistant",
                "content": assistant_content
            })
            
            # Add tool results to history
            self.conversation_history.append({
                "role": "user",
                "content": tool_results
            })
            
            # Continue the conversation
            response = self.client.messages.create(
                model=self.model,
                max_tokens=4096,
                tools=TOOLS,
                messages=self.conversation_history
            )
        
        # Extract final text response
        final_response = ""
        for block in response.content:
            if block.type == "text":
                final_response += block.text
        
        # Add final assistant message to history
        self.conversation_history.append({
            "role": "assistant",
            "content": response.content
        })
        
        if verbose:
            print(f"Assistant: {final_response}")
            print(f"\n{'='*60}\n")
        
        return final_response
    
    def reset_conversation(self):
        """Reset the conversation history."""
        self.conversation_history = []


def main():
    """
    Main function demonstrating the LLM File Assistant.
    """
    print("🤖 LLM File Assistant")
    print("=" * 60)
    print("This assistant can help you with file operations using natural language.")
    print("\nExample queries:")
    print("  - Read all resumes in the resumes folder")
    print("  - Find resumes mentioning Python experience")
    print("  - Create a summary file for resume_john_doe.pdf")
    print("  - List all PDF files in the resumes directory")
    print("\nType 'quit' or 'exit' to stop, 'reset' to clear conversation history.\n")

    # Initialize assistant
    try:
        assistant = LLMFileAssistant()
    except ValueError as e:
        print(f"❌ Error: {e}")
        print("\nPlease set your ANTHROPIC_API_KEY environment variable:")
        print("  export ANTHROPIC_API_KEY='your-api-key-here'")
        return

    # Interactive loop
    while True:
        try:
            user_input = input("\nYou: ").strip()

            if not user_input:
                continue

            if user_input.lower() in ['quit', 'exit']:
                print("\n👋 Goodbye!")
                break

            if user_input.lower() == 'reset':
                assistant.reset_conversation()
                print("🔄 Conversation history cleared.")
                continue

            # Process the query
            response = assistant.chat(user_input, verbose=True)

        except KeyboardInterrupt:
            print("\n\n👋 Goodbye!")
            break
        except Exception as e:
            print(f"\n❌ Error: {e}")


def run_examples():
    """
    Run example queries to demonstrate capabilities.
    """
    print("🚀 Running Example Queries\n")

    try:
        assistant = LLMFileAssistant()
    except ValueError as e:
        print(f"❌ Error: {e}")
        print("\nPlease set your ANTHROPIC_API_KEY environment variable:")
        print("  export ANTHROPIC_API_KEY='your-api-key-here'")
        return

    example_queries = [
        "List all files in the resumes folder",
        "Read the first resume you find and give me a brief summary",
        "Search for 'Python' in all resume files and tell me which ones mention it"
    ]

    for i, query in enumerate(example_queries, 1):
        print(f"\n{'#'*60}")
        print(f"Example {i}/{len(example_queries)}")
        print(f"{'#'*60}")
        assistant.chat(query, verbose=True)

        if i < len(example_queries):
            input("\nPress Enter to continue to next example...")


if __name__ == "__main__":
    import sys

    if len(sys.argv) > 1 and sys.argv[1] == "--examples":
        run_examples()
    else:
        main()
