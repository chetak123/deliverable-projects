# 🎬 Demo Script for Video Recording

Use this script to create your 2-3 minute demo video.

## Preparation (Before Recording)

```bash
# 1. Generate sample files
python3 generate_sample_resumes_simple.py

# 2. Install dependencies
pip install -r requirements.txt

# 3. Set API key
export ANTHROPIC_API_KEY='your-api-key-here'

# 4. Clear outputs folder
rm -rf outputs/
mkdir outputs/
```

## Recording Script

### Introduction (15 seconds)

> "Hi! Today I'm demonstrating an LLM-powered file assistant that uses function calling to perform file operations through natural language. This project showcases how to integrate AI tools with file system operations."

**Show**: Project structure in file explorer

### Part 1: Show the Sample Data (15 seconds)

```bash
ls -lh resumes/
```

> "I've generated 5 sample resume files for testing. Let's see what we can do with them using natural language."

### Part 2: Start the Assistant (30 seconds)

```bash
python3 llm_file_assistant.py
```

**Query 1**: "List all files in the resumes folder"

> "First, let's list all the resume files. Notice how the LLM calls the list_files tool automatically."

**Wait for response, show tool call**

### Part 3: Read and Analyze (45 seconds)

**Query 2**: "Read john_doe.txt and give me a brief summary of his experience"

> "Now let's read a specific resume. The assistant uses the read_file tool to extract the content."

**Wait for response**

**Query 3**: "Find all resumes that mention Python or JavaScript"

> "Let's search across multiple files. Watch how it uses search_in_file multiple times."

**Wait for response**

### Part 4: Generate Output (45 seconds)

**Query 4**: "Create a summary of all candidates in a file called outputs/candidates_summary.txt, including their roles and key skills"

> "Finally, let's have the AI create a summary file. It will read all resumes, analyze them, and write a new file."

**Wait for response**

```bash
cat outputs/candidates_summary.txt
```

> "And here's the generated summary file!"

### Conclusion (15 seconds)

```bash
ls -l outputs/
```

> "This project demonstrates LLM function calling with structured tools, file I/O operations, and document parsing. All code is available in the repository with comprehensive documentation. Thanks for watching!"

## Alternative Queries (If Time Permits)

- "Which candidate has the most years of experience?"
- "List all unique programming languages mentioned across all resumes"
- "Create a comparison table of all candidates"
- "Find resumes mentioning cloud technologies like AWS, Azure, or GCP"

## Tips for Recording

1. **Screen Setup**:
   - Use a clean terminal with large font (16-18pt)
   - Dark theme is easier on the eyes
   - Full screen or well-sized window

2. **Pacing**:
   - Speak clearly and not too fast
   - Pause briefly after each tool call completes
   - Show the results for 2-3 seconds

3. **Editing** (Optional):
   - Speed up wait times between queries (1.5-2x)
   - Add text overlays for key points
   - Background music (quiet, non-distracting)

4. **Tools**:
   - **macOS**: QuickTime Player (File → New Screen Recording)
   - **Windows**: Xbox Game Bar (Win + G)
   - **Linux**: OBS Studio, SimpleScreenRecorder
   - **All**: Loom (web-based, easy sharing)

## File to Submit

Export as MP4 or MOV format, 720p or higher resolution.

## Checklist Before Recording

- [ ] Sample resumes generated
- [ ] Dependencies installed
- [ ] API key configured
- [ ] Outputs folder empty
- [ ] Terminal font size increased
- [ ] Test run completed
- [ ] Queries planned out
- [ ] Screen recording software ready

---

**Good luck with your demo!** 🎥✨
