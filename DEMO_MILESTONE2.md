# 🎬 Milestone 2 Demo Script

This script guides you through demonstrating the RAG-based profile matching system for a 3-4 minute video.

## Preparation (Before Recording)

```bash
# 1. Generate dataset
python3 generate_extended_dataset.py

# 2. Install dependencies (if not already done)
pip install -r requirements.txt
python3 -m spacy download en_core_web_sm

# 3. Clear any previous ChromaDB data
rm -rf ./chroma_db

# 4. Test everything works
python3 resume_rag.py --directory resumes_extended --clear
python3 job_matcher.py --job-file job_descriptions/senior_python_engineer.txt --top-k 5
```

## Recording Script (3-4 minutes)

### Introduction (30 seconds)

> "Hello! Today I'm demonstrating a RAG-based profile matching system that uses semantic search to match job descriptions with candidate resumes. This builds on Milestone 1's LLM function calling by implementing document chunking, embeddings, vector databases, and intelligent ranking."

**Show**: Project structure in file explorer (resume_rag.py, job_matcher.py, resumes_extended/, job_descriptions/)

### Part 1: Dataset Overview (30 seconds)

```bash
ls -l resumes_extended/ | wc -l
ls -l job_descriptions/
```

> "I've generated 35 diverse resumes across various tech roles—software engineers, data scientists, DevOps engineers—with experience ranging from 1 to 12 years. I also have 6 different job descriptions to test against."

**Show**: Open 1-2 sample resumes and job descriptions in editor

### Part 2: Building the RAG System (60 seconds)

```bash
python3 resume_rag.py --directory resumes_extended --clear
```

> "Now I'll process all resumes through our RAG pipeline. Watch as it:"
> "1. Reads each resume using our Milestone 1 tools"
> "2. Intelligently chunks them while preserving sections like Experience and Education"
> "3. Extracts metadata using SpaCy NER—names, skills, experience years"
> "4. Generates embeddings using SentenceTransformers"
> "5. Stores everything in ChromaDB, our vector database"

**Wait for processing to complete, highlighting**:
- Candidate names being extracted
- Number of chunks created
- Skills found
- Final statistics

### Part 3: Semantic Search Demo (60 seconds)

```bash
python3 job_matcher.py --job-file job_descriptions/senior_python_engineer.txt --top-k 5
```

> "Let's match a Senior Python Engineer position. The system:"
> "1. Extracts requirements—5+ years experience, Python, Django skills"
> "2. Performs hybrid search combining semantic similarity and keyword matching"
> "3. Aggregates chunk-level results to candidate-level scores"
> "4. Applies filtering based on must-have requirements"
> "5. Returns top matches with match scores and reasoning"

**Highlight in output**:
- Extracted requirements
- Number of candidates found
- Top 3-5 matches with scores
- Skills matching
- Reasoning explanations

### Part 4: Different Job Types (45 seconds)

```bash
python3 job_matcher.py --job-file job_descriptions/data_scientist.txt --top-k 3
```

> "Let's try a completely different role—Data Scientist. Notice how the system finds different candidates with relevant skills like Python, TensorFlow, and statistical analysis."

**Show**: How results change for different job types

```bash
python3 job_matcher.py --job-file job_descriptions/devops_engineer.txt --top-k 3
```

> "And for DevOps Engineer, we get candidates with Kubernetes, Docker, and AWS experience."

### Part 5: Jupyter Notebook Analysis (30 seconds)

```bash
jupyter notebook RAG_Experimentation.ipynb
```

> "I've also created a Jupyter notebook with comprehensive analysis, including:"

**Quick scroll through notebook showing**:
- Processing statistics charts
- Performance metrics
- Score distributions
- Experience vs. match score correlations

> "The notebook shows our average query time is under 2 seconds, with strong semantic relevance in the top results."

### Conclusion (20 seconds)

```bash
cat job_descriptions/full_stack_developer.txt | head -15
python3 job_matcher.py --job-file job_descriptions/full_stack_developer.txt --output demo_results.json
cat demo_results.json | head -30
```

> "This RAG system demonstrates key concepts: intelligent document chunking, semantic embeddings, vector search, and hybrid ranking. All code is production-ready with comprehensive error handling and documentation."

**Show**: Output JSON file with structured results

> "Thank you for watching!"

## Alternative Demo Flow (If Time Permits)

### Show Filtering in Action

```bash
# Without filtering
python3 job_matcher.py --job-file job_descriptions/senior_python_engineer.txt --no-filter --top-k 10

# With filtering (default)
python3 job_matcher.py --job-file job_descriptions/senior_python_engineer.txt --top-k 10
```

> "Notice how filtering removes candidates who don't meet minimum requirements."

### Show Collection Statistics

```bash
python3 resume_rag.py --stats
```

> "We can query the vector database statistics anytime."

### Show Match Reasoning Quality

Pick one match and read the full reasoning aloud, explaining how the system:
- Identifies key skills
- Considers experience level
- Analyzes relevant resume sections

## Tips for Recording

### Screen Setup
- Terminal: Large font (16-18pt), high contrast theme
- Editor: Show code/files side-by-side with terminal
- Resolution: 1080p minimum

### Pacing
- Speak clearly but not too slowly
- Let commands run fully (don't speed up too much in editing)
- Pause 1-2 seconds after important outputs

### Highlights to Emphasize
1. **Intelligence**: Section-aware chunking preserves resume structure
2. **Speed**: Sub-2-second query times even with 35 resumes
3. **Accuracy**: Top matches are highly relevant
4. **Hybrid Approach**: Semantic + keyword better than either alone
5. **Reasoning**: Human-readable explanations for each match

### Common Gotchas
- Ensure ChromaDB directory is clean before demo
- Have dependencies pre-installed
- Test all commands beforehand
- Keep terminal output readable (not too much scrolling)

## Quick Test Commands

Before recording, test these work:

```bash
# Test 1: Resume processing
python3 resume_rag.py --directory resumes_extended --clear

# Test 2: Job matching
python3 job_matcher.py --job-file job_descriptions/senior_python_engineer.txt

# Test 3: Stats
python3 resume_rag.py --stats

# Test 4: Output to JSON
python3 job_matcher.py --job-file job_descriptions/data_scientist.txt --output test.json

# Test 5: Jupyter (opens browser)
jupyter notebook RAG_Experimentation.ipynb
```

## File to Submit

- **Format**: MP4 or MOV
- **Length**: 3-4 minutes
- **Resolution**: 720p minimum (1080p preferred)
- **Audio**: Clear voice recording
- **Editing**: Optional but nice: add title slides, speed up slow parts

## Post-Recording Checklist

- [ ] Showed dataset (35 resumes, 6 jobs)
- [ ] Demonstrated resume processing with RAG pipeline
- [ ] Showed semantic search in action
- [ ] Highlighted different job types finding different candidates
- [ ] Displayed performance metrics
- [ ] Showed Jupyter notebook
- [ ] Explained key technical concepts
- [ ] Output was clearly visible
- [ ] Audio is clear

---

**Good luck with your demo!** 🎥✨

For questions, refer to README_MILESTONE2.md for complete documentation.
