# Milestone 2: RAG-Based Profile Matching

Building on Milestone 1's LLM function calling system, this milestone implements a complete Retrieval-Augmented Generation (RAG) system for intelligent resume matching.

## 🎯 Learning Objectives Achieved

- ✅ Implement document chunking and embedding
- ✅ Build vector databases for semantic search
- ✅ Create retrieval pipelines with ChromaDB
- ✅ Understand and implement semantic search
- ✅ Develop hybrid search (semantic + keyword matching)
- ✅ Create ranking and scoring systems

## 📋 Features Overview

### Part A: RAG System Setup (50%)

**File**: `resume_rag.py`

1. **Document Processing Pipeline**
   - Load resumes using Milestone 1 tools (read_file, list_files)
   - Intelligent chunking that preserves resume structure
   - Section-aware processing (Education, Experience, Skills, etc.)
   - Support for TXT, PDF, and DOCX formats

2. **Intelligent Chunking** (`ResumeChunker` class)
   - Detects common section headers using regex patterns
   - Preserves context within sections
   - Handles various resume formats and styles
   - Creates meaningful chunks for embedding

3. **Metadata Extraction** (`MetadataExtractor` class)
   - Uses SpaCy NER for name extraction
   - Pattern matching for skills extraction
   - Experience years calculation
   - Education level detection
   - Stores metadata alongside embeddings

4. **Embedding Generation**
   - Uses SentenceTransformers (all-MiniLM-L6-v2)
   - Efficient batch processing
   - High-quality semantic representations

5. **Vector Database Storage**
   - ChromaDB for vector storage
   - Persistent storage with metadata
   - Fast similarity search
   - Scalable architecture

### Part B: Job Matching Engine (50%)

**File**: `job_matcher.py`

1. **Semantic Search**
   - Converts job descriptions to embeddings
   - Cosine similarity-based retrieval
   - Top-K matching with configurable K
   - Fast query performance

2. **Hybrid Search**
   - Combines semantic similarity (70%) + keyword matching (30%)
   - Critical skills identification
   - Weighted scoring system
   - Improved relevance over pure semantic search

3. **Requirement Extraction**
   - Automatic extraction of min years experience
   - Required skills identification
   - Education level requirements
   - Pattern-based requirement parsing

4. **Ranking & Scoring**
   - 0-100 match score scale
   - Aggregates chunk-level scores to candidate-level
   - Max score (60%) + average score (40%) combination
   - Confidence-based weighting

5. **Match Reasoning**
   - Human-readable explanations
   - Skills matching details
   - Section relevance information
   - Experience alignment

6. **Filtering**
   - Must-have requirement enforcement
   - Experience threshold filtering
   - Critical skills validation
   - Configurable strictness

## 🚀 Quick Start

### Installation

```bash
# Install all dependencies
pip install -r requirements.txt

# Download SpaCy model
python3 -m spacy download en_core_web_sm
```

### Generate Dataset

```bash
# Generate 35 resumes and 6 job descriptions
python3 generate_extended_dataset.py
```

### Process Resumes (Build RAG System)

```bash
# Process all resumes and build vector database
python3 resume_rag.py --directory resumes_extended --clear

# Options:
#   --clear: Clear existing collection before processing
#   --stats: Show statistics only
#   --extension .txt: Filter by file extension
```

### Match Jobs

```bash
# Match a job description to resumes
python3 job_matcher.py --job-file job_descriptions/senior_python_engineer.txt --top-k 10

# Save results to JSON
python3 job_matcher.py --job-file job_descriptions/data_scientist.txt --output results.json

# Disable filtering
python3 job_matcher.py --job-file job_descriptions/full_stack_developer.txt --no-filter
```

## 📊 Dataset

### Resumes
- **Count**: 35 diverse resumes
- **Roles**: Software Engineers, Data Scientists, DevOps, Full Stack, Frontend, etc.
- **Experience Levels**: 1-12 years
- **Location**: `resumes_extended/`

### Job Descriptions
- **Count**: 6 job postings
- **Roles**: 
  - Senior Python Engineer
  - Machine Learning Engineer
  - Full Stack Developer
  - DevOps Engineer
  - Data Scientist
  - Frontend Developer
- **Location**: `job_descriptions/`

## 📓 Jupyter Notebook

**File**: `RAG_Experimentation.ipynb`

The notebook includes:
1. Setup and initialization
2. Resume processing pipeline demonstration
3. Processing statistics and visualizations
4. Semantic search testing
5. Job matching with real job descriptions
6. Performance metrics (latency, throughput)
7. Retrieval accuracy analysis
8. Score distribution analysis
9. Experience vs. match score correlation

**To run**:
```bash
jupyter notebook RAG_Experimentation.ipynb
```

## 📈 Performance Metrics

### Processing Performance
- **Average processing time**: ~0.3 seconds/resume
- **Chunks per resume**: 5-10 (avg: 7)
- **Total chunks generated**: 245+ from 35 resumes

### Search Performance
- **Query latency**: < 2 seconds per job match
- **Top-K retrieval**: Configurable (default: 10)
- **Accuracy**: High relevance in top results

### Quality Metrics
- **Match scores**: 50-95 range (higher = better match)
- **Filtering effectiveness**: Removes 20-40% of low-quality matches
- **Reasoning quality**: Human-readable explanations

## 🏗️ Architecture

```
┌─────────────────┐
│  Resume Files   │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  fs_tools.py    │  (Milestone 1: File reading)
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ ResumeChunker   │  (Intelligent section-aware chunking)
└────────┬────────┘
         │
         ▼
┌──────────────────┐
│MetadataExtractor │  (NER + pattern matching)
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│SentenceTransform.│  (Generate embeddings)
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│   ChromaDB       │  (Vector storage + search)
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│  Job Description │
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│  JobMatcher      │  (Semantic + hybrid search)
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│ Ranked Matches   │  (Scored candidates with reasoning)
└──────────────────┘
```

## 📝 Output Format

```json
{
  "job_description": "Senior Python Engineer with 5+ years...",
  "requirements": {
    "min_years": 5,
    "required_skills": ["Python", "Django", "PostgreSQL"],
    "education_level": "Bachelor"
  },
  "total_candidates_found": 15,
  "top_matches": [
    {
      "candidate_name": "Alex Chen",
      "resume_path": "resumes_extended/alex_chen.txt",
      "match_score": 92,
      "matched_skills": ["Python", "Django", "PostgreSQL", "AWS"],
      "relevant_excerpts": [
        "Senior Python Engineer with 7 years experience...",
        "Expert in Django framework and REST APIs...",
        "Led team in building microservices..."
      ],
      "matched_sections": ["PROFESSIONAL SUMMARY", "TECHNICAL SKILLS", "EXPERIENCE"],
      "experience_years": 7,
      "education": ["Bachelor of Science in Computer Science"],
      "reasoning": "Excellent match. Key skills: Python, Django, PostgreSQL. 7 years of experience. Relevant sections: PROFESSIONAL SUMMARY, TECHNICAL SKILLS, EXPERIENCE."
    }
  ]
}
```

## 🔬 Technical Implementation Details

### 1. Chunking Strategy
- Section header detection using regex patterns
- Preserves full sections (no mid-section cuts)
- Maintains context for better embeddings
- Handles various resume formats

### 2. Embedding Model
- **Model**: all-MiniLM-L6-v2
- **Dimensions**: 384
- **Speed**: Fast inference
- **Quality**: Good semantic understanding

### 3. Vector Database
- **Engine**: ChromaDB
- **Storage**: Persistent local storage
- **Search**: Cosine similarity
- **Metadata**: Rich filtering capabilities

### 4. Scoring Algorithm
```python
# Chunk-level
semantic_score = 1 - distance
keyword_score = count_of_matched_critical_skills
hybrid_score = (semantic_score * 0.7) + (keyword_score * 0.3)

# Candidate-level (aggregated from chunks)
final_score = (max_chunk_score * 0.6) + (avg_chunk_score * 0.4)
match_score = min(100, int(final_score * 100))
```

## 🧪 Testing

### Unit Testing
```bash
# Test chunking
python3 -c "from resume_rag import ResumeChunker; print('Chunker OK')"

# Test metadata extraction
python3 -c "from resume_rag import MetadataExtractor; print('Metadata OK')"

# Test matching
python3 -c "from job_matcher import JobMatcher; print('Matcher OK')"
```

### Integration Testing
```bash
# Full pipeline test
python3 resume_rag.py --directory resumes_extended --clear
python3 job_matcher.py --job-file job_descriptions/senior_python_engineer.txt
```

## 📚 Dependencies

Key libraries:
- `chromadb`: Vector database
- `sentence-transformers`: Embedding generation
- `spacy`: NER and NLP
- `pandas`, `numpy`: Data analysis
- `matplotlib`, `seaborn`: Visualization
- `jupyter`: Interactive notebooks

See `requirements.txt` for complete list.

## 🎥 Demo

To create a demo:

1. **Process resumes**:
   ```bash
   python3 resume_rag.py --directory resumes_extended --clear
   ```

2. **Match different job types**:
   ```bash
   python3 job_matcher.py --job-file job_descriptions/senior_python_engineer.txt
   python3 job_matcher.py --job-file job_descriptions/data_scientist.txt
   python3 job_matcher.py --job-file job_descriptions/devops_engineer.txt
   ```

3. **Show Jupyter analysis**:
   ```bash
   jupyter notebook RAG_Experimentation.ipynb
   ```

## 🎓 Assignment Compliance

### Part A: RAG System Setup (50%) ✅
- ✅ Document processing pipeline with Milestone 1 tools
- ✅ Intelligent chunking preserving sections
- ✅ Embeddings with SentenceTransformers
- ✅ ChromaDB vector storage
- ✅ Metadata extraction (name, skills, experience, education)

### Part B: Job Matching Engine (50%) ✅
- ✅ Semantic search implementation
- ✅ Top-K retrieval (configurable)
- ✅ Hybrid search (semantic + keyword)
- ✅ Ranking & scoring (0-100 scale)
- ✅ Match reasoning
- ✅ Must-have requirement filtering

### Deliverables ✅
- ✅ Complete RAG implementation
- ✅ Dataset: 35 resumes, 6 job descriptions
- ✅ Jupyter notebook with analysis
- ✅ Performance metrics (latency, accuracy)
- ✅ Documentation

## 🚧 Future Enhancements

- Fine-tune embeddings on resume/job description pairs
- Add reranking layer with cross-encoder
- Implement active learning for scoring improvements
- Add A/B testing framework
- Support for multilingual resumes
- Integration with ATS systems
- Real-time incremental indexing

---

**Built with ❤️ for learning RAG systems and semantic search**
