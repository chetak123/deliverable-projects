# AI-Powered Resume Matching System

A comprehensive Python project demonstrating advanced AI concepts across two milestones:

1. **Milestone 1**: LLM Function Calling with File System Tools
2. **Milestone 2**: RAG-Based Profile Matching with Semantic Search

This project showcases practical applications of LLMs, vector databases, semantic search, and intelligent document processing for resume-to-job matching.

## 🏆 Project Milestones

### Milestone 1: LLM Function Calling
LLM-powered file operations using Claude's tool calling capabilities.
📖 See [QUICKSTART.md](QUICKSTART.md) for Milestone 1 details.

### Milestone 2: RAG-Based Profile Matching ⭐ NEW
Intelligent resume matching using semantic search and vector databases.
📖 See [README_MILESTONE2.md](README_MILESTONE2.md) for Milestone 2 details.

## 🎯 Combined Learning Objectives

### Milestone 1
- ✅ Understand LLM function calling/tool use
- ✅ Implement structured tool interfaces
- ✅ Handle file I/O operations programmatically
- ✅ Parse and validate documents (PDF, DOCX, TXT)

### Milestone 2
- ✅ Implement document chunking and embedding
- ✅ Build vector databases for semantic search
- ✅ Create retrieval pipelines
- ✅ Understand hybrid search (semantic + keyword)
- ✅ Develop ranking and scoring systems

## 🚀 Quick Navigation

- **New to the project?** Start with [Milestone 1 QUICKSTART.md](QUICKSTART.md)
- **Want RAG/Semantic Search?** See [Milestone 2 README_MILESTONE2.md](README_MILESTONE2.md)
- **Ready to demo?** Check [DEMO_MILESTONE2.md](DEMO_MILESTONE2.md)

## 📊 Project Overview

| Milestone | Focus | Key Technologies | Status |
|-----------|-------|------------------|--------|
| **1** | LLM Function Calling | Claude API, Tool Use, File I/O | ✅ Complete |
| **2** | RAG Profile Matching | ChromaDB, SentenceTransformers, Semantic Search | ✅ Complete |

---

## 📋 Milestone 1 Features

### Part A: Core File System Tools (60%)

The `fs_tools.py` module provides four main tools:

1. **`read_file(filepath: str)`**
   - Reads resume files in PDF, TXT, and DOCX formats
   - Extracts text content
   - Returns structured response with content and metadata
   - Handles errors gracefully

2. **`list_files(directory: str, extension: str = None)`**
   - Lists all files in a directory
   - Filters by extension (e.g., `.pdf`, `.txt`, `.docx`)
   - Returns file metadata (name, size, modified date)

3. **`write_file(filepath: str, content: str)`**
   - Writes content to file
   - Creates directories if needed
   - Returns success/failure status

4. **`search_in_file(filepath: str, keyword: str)`**
   - Searches for keywords in file content
   - Returns matches with surrounding context
   - Case-insensitive search

### Part B: LLM Integration (40%)

The `llm_file_assistant.py` module provides:

- Integration with Claude (Anthropic's LLM)
- Natural language interface for file operations
- Automatic tool calling based on user queries
- Conversation history management
- Interactive chat mode

## 🚀 Setup Instructions

### Prerequisites

- Python 3.8 or higher
- Anthropic API key ([Get one here](https://console.anthropic.com/))

### Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd deliverable-projects
   ```

2. **Create a virtual environment (recommended)**
   ```bash
   python -m venv venv
   source venv/bin/activate  # On Windows: venv\Scripts\activate
   ```

3. **Install dependencies**
   ```bash
   pip install -r requirements.txt
   ```

4. **Set up your API key**
   ```bash
   export ANTHROPIC_API_KEY='your-api-key-here'
   # On Windows: set ANTHROPIC_API_KEY=your-api-key-here
   ```

5. **Generate sample resume files**
   ```bash
   python generate_sample_resumes.py
   ```

   This will create a `resumes/` folder with 5 sample resumes in various formats (PDF, TXT, DOCX).

## 💻 Usage

### Interactive Mode

Run the assistant in interactive chat mode:

```bash
python llm_file_assistant.py
```

Example queries you can try:
- "Read all resumes in the resumes folder"
- "Find resumes mentioning Python experience"
- "Create a summary file for john_doe.pdf"
- "List all PDF files in the resumes directory"
- "Search for 'machine learning' in all resume files"

### Example Mode

Run pre-defined examples:

```bash
python llm_file_assistant.py --examples
```

### Using Tools Directly

You can also use the file system tools directly in your Python code:

```python
from fs_tools import read_file, list_files, write_file, search_in_file

# List all PDF files
result = list_files("resumes", extension=".pdf")
print(result)

# Read a specific file
result = read_file("resumes/john_doe.txt")
print(result['content'])

# Search for a keyword
result = search_in_file("resumes/jane_smith.txt", "Python")
print(f"Found {result['count']} matches")

# Write a summary
content = "Summary of resumes..."
result = write_file("outputs/summary.txt", content)
```

## 📁 Project Structure

```
deliverable-projects/
├── fs_tools.py                 # Core file system tools
├── llm_file_assistant.py       # LLM integration and chat interface
├── generate_sample_resumes.py  # Script to generate sample data
├── requirements.txt            # Python dependencies
├── README.md                   # This file
├── resumes/                    # Sample resume files (generated)
│   ├── john_doe.txt
│   ├── john_doe.pdf
│   ├── jane_smith.txt
│   ├── jane_smith.pdf
│   ├── mike_johnson.txt
│   ├── mike_johnson.docx
│   ├── sarah_williams.txt
│   ├── sarah_williams.docx
│   ├── alex_chen.txt
│   └── alex_chen.docx
└── outputs/                    # Output files (created as needed)
```

## 🔧 Technical Details

### Tool Schema

Each tool is defined with a structured schema that the LLM uses to understand:
- Tool name and description
- Input parameters (type, description, required/optional)
- Return format

Example tool definition:
```python
{
    "name": "read_file",
    "description": "Read and extract text content from resume files...",
    "input_schema": {
        "type": "object",
        "properties": {
            "filepath": {
                "type": "string",
                "description": "Path to the file to read"
            }
        },
        "required": ["filepath"]
    }
}
```

### How It Works

1. User sends a natural language query
2. LLM analyzes the query and determines which tool(s) to use
3. LLM generates structured tool calls with appropriate parameters
4. Python executes the tool functions
5. Results are sent back to the LLM
6. LLM synthesizes a natural language response

## 📊 Example Queries and Outputs

### Query 1: "List all resumes in the resumes folder"
```
🔧 Calling tool: list_files
   Input: {"directory": "resumes"}

Assistant: I found 10 resume files in the resumes folder:
- john_doe.txt (2.5 KB)
- john_doe.pdf (15.3 KB)
- jane_smith.txt (2.1 KB)
...
```

### Query 2: "Find resumes mentioning Python experience"
```
🔧 Calling tool: list_files
🔧 Calling tool: search_in_file (multiple calls)

Assistant: I found 3 resumes that mention Python:
1. john_doe.txt - "Languages: Python, JavaScript, TypeScript..."
2. jane_smith.txt - "Programming: Python, R, SQL, MATLAB"
3. sarah_williams.txt - "Scripting: Python, Bash, PowerShell"
```

## 🧪 Testing

To test the implementation:

1. **Test file system tools directly:**
   ```bash
   python -c "from fs_tools import *; print(list_files('resumes'))"
   ```

2. **Test LLM integration:**
   ```bash
   python llm_file_assistant.py
   # Try: "List all files in the resumes folder"
   ```

3. **Run example queries:**
   ```bash
   python llm_file_assistant.py --examples
   ```

## 🎥 Demo Video

A 2-3 minute demo video showing:
- Setup and installation
- Running the assistant in interactive mode
- Example queries demonstrating all tool capabilities
- Reading, searching, and writing files via natural language

## 🛠️ Dependencies

- **anthropic** (>=0.18.0): Claude AI SDK for LLM integration
- **PyPDF2** (>=3.0.0): PDF file reading
- **python-docx** (>=1.1.0): DOCX file processing
- **reportlab** (>=4.0.0): PDF file generation

## 🔍 Error Handling

All tools return structured responses with error handling:
- File not found errors
- Unsupported file format errors
- Permission errors
- Graceful degradation with helpful error messages

## 🚧 Troubleshooting

### API Key Not Set
```
Error: API key must be provided or set in ANTHROPIC_API_KEY environment variable
```
**Solution:** Set your API key as shown in Setup Instructions

### Module Not Found
```
ModuleNotFoundError: No module named 'anthropic'
```
**Solution:** Install dependencies with `pip install -r requirements.txt`

### File Not Found When Reading Resumes
```
Error: File not found: resumes/...
```
**Solution:** Run `python generate_sample_resumes.py` to create sample files

## 📝 Assignment Compliance

### Part A: Core File System Tools (60%) ✅
- ✅ `read_file()` - Reads PDF, TXT, DOCX with metadata
- ✅ `list_files()` - Lists files with filtering and metadata
- ✅ `write_file()` - Writes files with directory creation
- ✅ `search_in_file()` - Case-insensitive search with context

### Part B: LLM Integration (40%) ✅
- ✅ Integration with Claude (Anthropic)
- ✅ Tool calling based on natural language queries
- ✅ Example queries implemented and working
- ✅ Interactive chat interface

### Deliverables ✅
- ✅ Source code with documentation
- ✅ requirements.txt with dependencies
- ✅ Sample data: 10 dummy resume files (5 people × 2 formats each)
- ✅ README.md with setup and usage instructions
- ✅ Demo capability (run with --examples flag)

## 🎓 Learning Outcomes

By completing this project, you will:
1. Understand how LLMs use function calling to interact with external tools
2. Learn to design structured tool interfaces for AI systems
3. Gain experience with multiple file format parsing libraries
4. Practice error handling and robust API design
5. See practical applications of agentic AI systems

## 📄 License

This project is for educational purposes.

## 🤝 Contributing

This is an educational project. Feel free to fork and experiment!

## 📞 Support

For questions or issues:
1. Check the Troubleshooting section
2. Review the example code in `llm_file_assistant.py`
3. Ensure all dependencies are properly installed

---

**Built with ❤️ for learning LLM function calling and tool use**
