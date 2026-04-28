# 🚀 Quick Start Guide

Get up and running with the LLM File Assistant in 5 minutes!

## Step 1: Generate Sample Data (No Dependencies Needed!)

```bash
python3 generate_sample_resumes_simple.py
```

This creates 5 sample resume files in the `resumes/` folder.

## Step 2: Test the Tools (Optional but Recommended)

```bash
python3 test_fs_tools.py
```

This verifies all file system tools are working correctly.

## Step 3: Install Dependencies

```bash
pip install -r requirements.txt
```

Or if you prefer using a virtual environment:

```bash
python3 -m venv venv
source venv/bin/activate  # On Windows: venv\Scripts\activate
pip install -r requirements.txt
```

## Step 4: Set Up Your API Key

Get your API key from [Anthropic Console](https://console.anthropic.com/)

```bash
export ANTHROPIC_API_KEY='your-api-key-here'
```

On Windows:
```cmd
set ANTHROPIC_API_KEY=your-api-key-here
```

## Step 5: Run the Assistant!

### Interactive Mode (Recommended)

```bash
python3 llm_file_assistant.py
```

Try these queries:
- "List all files in the resumes folder"
- "Read john_doe.txt and give me a summary"
- "Find all resumes that mention Python"
- "Create a summary of all resumes in outputs/all_resumes_summary.txt"

### Example Mode

```bash
python3 llm_file_assistant.py --examples
```

## 🎯 Example Workflow

Here's a complete example workflow:

```bash
# 1. Generate sample resumes
python3 generate_sample_resumes_simple.py

# 2. Test the tools
python3 test_fs_tools.py

# 3. Install dependencies (one-time setup)
pip install -r requirements.txt

# 4. Set API key (once per session)
export ANTHROPIC_API_KEY='sk-ant-...'

# 5. Run the assistant
python3 llm_file_assistant.py
```

## 💡 Sample Queries to Try

Once you're in the interactive assistant:

1. **List files**: "Show me all the resume files"
2. **Read content**: "Read the resume of Jane Smith"
3. **Search**: "Which resumes mention Docker or Kubernetes?"
4. **Analyze**: "Compare the experience levels of all candidates"
5. **Create summary**: "Create a summary file comparing all candidates"

## 🔧 Troubleshooting

### "Module not found" errors
```bash
pip install -r requirements.txt
```

### "API key not set"
```bash
export ANTHROPIC_API_KEY='your-key-here'
```

### No resume files found
```bash
python3 generate_sample_resumes_simple.py
```

## 📚 Next Steps

- Check out the full [README.md](README.md) for detailed documentation
- Generate PDF and DOCX versions: `python3 generate_sample_resumes.py`
- Write your own queries and experiment!

## 🎥 Recording a Demo

To create your demo video:

1. Start the assistant: `python3 llm_file_assistant.py`
2. Show these features:
   - Listing files
   - Reading a resume
   - Searching for keywords (e.g., "Python")
   - Creating a summary file
3. Show the generated output file: `cat outputs/summary.txt`

Screen recording tools:
- macOS: QuickTime Player (built-in)
- Windows: Xbox Game Bar (built-in)
- Linux: SimpleScreenRecorder, OBS Studio

---

**Ready to go!** 🎉

For more details, see the complete [README.md](README.md)
