"""
RAG System for Resume Processing
Implements document chunking, embedding generation, and vector database storage.
"""

import os
import re
from pathlib import Path
from typing import List, Dict, Any, Optional
import hashlib
import json

# NLP and Embeddings
import spacy
from sentence_transformers import SentenceTransformer

# Vector Database
import chromadb
from chromadb.config import Settings

# Import from Milestone 1
from fs_tools import read_file, list_files


class ResumeChunker:
    """
    Intelligent document chunking that preserves resume structure.
    """
    
    # Common resume section headers
    SECTION_PATTERNS = [
        r'(?i)^(PROFESSIONAL\s+)?SUMMARY\s*$',
        r'(?i)^(PROFESSIONAL\s+)?EXPERIENCE\s*$',
        r'(?i)^(WORK\s+)?HISTORY\s*$',
        r'(?i)^(EMPLOYMENT\s+)?HISTORY\s*$',
        r'(?i)^EDUCATION\s*$',
        r'(?i)^(TECHNICAL\s+)?SKILLS?\s*$',
        r'(?i)^(CORE\s+)?COMPETENCIES\s*$',
        r'(?i)^CERTIFICATIONS?\s*$',
        r'(?i)^PROJECTS?\s*$',
        r'(?i)^PUBLICATIONS?\s*$',
        r'(?i)^ACHIEVEMENTS?\s*$',
        r'(?i)^(LANGUAGES?|PROGRAMMING\s+LANGUAGES?)\s*$',
        r'(?i)^OBJECTIVE\s*$',
        r'(?i)^ABOUT\s+(ME|MYSELF)\s*$',
        r'(?i)^PROFILE\s*$',
        r'(?i)^CONTACT(\s+INFORMATION)?\s*$',
    ]
    
    def __init__(self):
        self.section_regex = [re.compile(pattern) for pattern in self.SECTION_PATTERNS]
    
    def is_section_header(self, line: str) -> bool:
        """Check if a line is a section header."""
        line = line.strip()
        if not line:
            return False
        
        for pattern in self.section_regex:
            if pattern.match(line):
                return True
        
        # Additional heuristics: short lines in all caps
        if len(line) < 50 and line.isupper() and len(line.split()) <= 5:
            return True
        
        return False
    
    def chunk_resume(self, content: str, metadata: Dict[str, Any]) -> List[Dict[str, Any]]:
        """
        Chunk resume into sections while preserving context.
        
        Args:
            content: Resume text content
            metadata: File metadata
            
        Returns:
            List of chunks with metadata
        """
        lines = content.split('\n')
        chunks = []
        current_section = "HEADER"
        current_content = []
        
        for line in lines:
            if self.is_section_header(line):
                # Save previous section
                if current_content:
                    chunk_text = '\n'.join(current_content).strip()
                    if chunk_text:
                        chunks.append({
                            'text': chunk_text,
                            'section': current_section,
                            'metadata': metadata.copy()
                        })
                
                # Start new section
                current_section = line.strip()
                current_content = [line]
            else:
                current_content.append(line)
        
        # Save last section
        if current_content:
            chunk_text = '\n'.join(current_content).strip()
            if chunk_text:
                chunks.append({
                    'text': chunk_text,
                    'section': current_section,
                    'metadata': metadata.copy()
                })
        
        return chunks


class MetadataExtractor:
    """
    Extract structured metadata from resumes using NER and pattern matching.
    """
    
    def __init__(self):
        """Initialize NER model."""
        try:
            self.nlp = spacy.load('en_core_web_sm')
        except OSError:
            print("⚠️  SpaCy model not found. Installing...")
            os.system("python3 -m spacy download en_core_web_sm")
            self.nlp = spacy.load('en_core_web_sm')
    
    def extract_name(self, content: str) -> Optional[str]:
        """Extract candidate name from resume."""
        # Usually the name is in the first few lines
        lines = content.split('\n')[:5]
        
        for line in lines:
            line = line.strip()
            if not line or '@' in line or 'phone' in line.lower():
                continue
            
            doc = self.nlp(line)
            for ent in doc.ents:
                if ent.label_ == 'PERSON':
                    return ent.text
            
            # Fallback: if line looks like a name (2-4 words, capitalized)
            words = line.split()
            if 2 <= len(words) <= 4 and all(w[0].isupper() for w in words if w):
                return line

        return None

    def extract_skills(self, content: str) -> List[str]:
        """Extract skills from resume."""
        skills = []

        # Common technical skills to look for
        skill_patterns = [
            r'\b(Python|Java|JavaScript|TypeScript|C\+\+|C#|Ruby|Go|Rust|PHP|Swift|Kotlin)\b',
            r'\b(React|Angular|Vue|Node\.js|Django|Flask|Spring|Express|FastAPI)\b',
            r'\b(AWS|Azure|GCP|Google Cloud|Docker|Kubernetes|Jenkins|CI/CD)\b',
            r'\b(PostgreSQL|MySQL|MongoDB|Redis|Elasticsearch|DynamoDB)\b',
            r'\b(Machine Learning|ML|AI|Deep Learning|NLP|Computer Vision|TensorFlow|PyTorch)\b',
            r'\b(Git|GitHub|GitLab|Agile|Scrum|REST|GraphQL|Microservices)\b',
        ]

        for pattern in skill_patterns:
            matches = re.findall(pattern, content, re.IGNORECASE)
            skills.extend(matches)

        # Remove duplicates, preserve case of first occurrence
        seen = set()
        unique_skills = []
        for skill in skills:
            skill_lower = skill.lower()
            if skill_lower not in seen:
                seen.add(skill_lower)
                unique_skills.append(skill)

        return unique_skills

    def extract_experience_years(self, content: str) -> Optional[float]:
        """Extract years of experience from resume."""
        # Look for patterns like "5+ years", "5 years", "5-7 years"
        patterns = [
            r'(\d+)\+?\s*years?\s+(?:of\s+)?experience',
            r'experience[:\s]+(\d+)\+?\s*years?',
            r'(\d+)-(\d+)\s*years?',
        ]

        years = []
        for pattern in patterns:
            matches = re.findall(pattern, content, re.IGNORECASE)
            if matches:
                for match in matches:
                    if isinstance(match, tuple):
                        # Range: take average
                        nums = [int(x) for x in match if x.isdigit()]
                        if nums:
                            years.append(sum(nums) / len(nums))
                    else:
                        years.append(int(match))

        return max(years) if years else None

    def extract_education(self, content: str) -> List[str]:
        """Extract education information."""
        education = []

        # Look for degree patterns
        degree_patterns = [
            r'\b(Bachelor|BS|BA|B\.S\.|B\.A\.)\s+(?:of\s+)?(?:Science|Arts)?\s+in\s+([A-Za-z\s]+)',
            r'\b(Master|MS|MA|M\.S\.|M\.A\.)\s+(?:of\s+)?(?:Science|Arts)?\s+in\s+([A-Za-z\s]+)',
            r'\b(PhD|Ph\.D\.|Doctorate)\s+in\s+([A-Za-z\s]+)',
            r'\b(MBA|M\.B\.A\.)',
        ]

        for pattern in degree_patterns:
            matches = re.findall(pattern, content, re.IGNORECASE)
            for match in matches:
                if isinstance(match, tuple):
                    degree = ' '.join(filter(None, match))
                else:
                    degree = match
                education.append(degree.strip())

        return education

    def extract_metadata(self, content: str, filepath: str) -> Dict[str, Any]:
        """Extract all metadata from resume."""
        return {
            'filepath': filepath,
            'name': self.extract_name(content) or "Unknown",
            'skills': self.extract_skills(content),
            'experience_years': self.extract_experience_years(content),
            'education': self.extract_education(content),
        }


class ResumeRAGSystem:
    """
    Complete RAG system for resume processing and retrieval.
    """

    def __init__(self,
                 collection_name: str = "resumes",
                 embedding_model: str = "all-MiniLM-L6-v2",
                 persist_directory: str = "./chroma_db"):
        """
        Initialize RAG system.

        Args:
            collection_name: Name for the vector database collection
            embedding_model: SentenceTransformer model name
            persist_directory: Directory to persist vector database
        """
        self.collection_name = collection_name
        self.persist_directory = persist_directory

        # Initialize components
        self.chunker = ResumeChunker()
        self.metadata_extractor = MetadataExtractor()

        # Initialize embedding model
        print(f"Loading embedding model: {embedding_model}")
        self.embedding_model = SentenceTransformer(embedding_model)

        # Initialize ChromaDB
        self.client = chromadb.Client(Settings(
            persist_directory=persist_directory,
            anonymized_telemetry=False
        ))

        # Get or create collection
        try:
            self.collection = self.client.get_collection(name=collection_name)
            print(f"✓ Loaded existing collection: {collection_name}")
        except:
            self.collection = self.client.create_collection(
                name=collection_name,
                metadata={"description": "Resume embeddings for semantic search"}
            )
            print(f"✓ Created new collection: {collection_name}")

    def _generate_chunk_id(self, filepath: str, section: str, index: int) -> str:
        """Generate unique ID for a chunk."""
        content = f"{filepath}_{section}_{index}"
        return hashlib.md5(content.encode()).hexdigest()

    def process_resume(self, filepath: str) -> Dict[str, Any]:
        """
        Process a single resume: read, chunk, extract metadata, generate embeddings.

        Args:
            filepath: Path to resume file

        Returns:
            Processing result with stats
        """
        # Read file
        result = read_file(filepath)
        if not result['success']:
            return {
                'success': False,
                'filepath': filepath,
                'error': result['error']
            }

        content = result['content']
        file_metadata = result['metadata']

        # Extract metadata
        extracted_metadata = self.metadata_extractor.extract_metadata(content, filepath)

        # Chunk document
        chunks = self.chunker.chunk_resume(content, file_metadata)

        if not chunks:
            return {
                'success': False,
                'filepath': filepath,
                'error': 'No chunks generated from resume'
            }

        # Generate embeddings and store
        chunk_ids = []
        embeddings = []
        metadatas = []
        documents = []

        for idx, chunk in enumerate(chunks):
            chunk_id = self._generate_chunk_id(filepath, chunk['section'], idx)
            chunk_ids.append(chunk_id)
            documents.append(chunk['text'])

            # Combine extracted metadata with chunk metadata
            combined_metadata = {
                **extracted_metadata,
                'section': chunk['section'],
                'chunk_index': idx,
                'filename': file_metadata['filename']
            }
            # Convert lists to JSON strings for ChromaDB
            combined_metadata['skills'] = json.dumps(combined_metadata.get('skills', []))
            combined_metadata['education'] = json.dumps(combined_metadata.get('education', []))

            metadatas.append(combined_metadata)

        # Generate embeddings in batch
        embeddings = self.embedding_model.encode(documents).tolist()

        # Add to collection
        self.collection.add(
            ids=chunk_ids,
            embeddings=embeddings,
            metadatas=metadatas,
            documents=documents
        )

        return {
            'success': True,
            'filepath': filepath,
            'candidate_name': extracted_metadata['name'],
            'chunks_created': len(chunks),
            'skills': extracted_metadata['skills'],
            'experience_years': extracted_metadata['experience_years']
        }

    def process_resume_directory(self, directory: str, extension: str = None) -> List[Dict[str, Any]]:
        """
        Process all resumes in a directory.

        Args:
            directory: Directory containing resumes
            extension: Optional file extension filter

        Returns:
            List of processing results
        """
        # List files
        files_result = list_files(directory, extension)

        if not files_result['success']:
            return [{
                'success': False,
                'error': files_result['error']
            }]

        results = []
        total_files = files_result['count']

        print(f"\n📁 Processing {total_files} resumes from {directory}")
        print("=" * 60)

        for idx, file_info in enumerate(files_result['files'], 1):
            filepath = file_info['path']
            print(f"\n[{idx}/{total_files}] Processing: {file_info['name']}")

            result = self.process_resume(filepath)
            results.append(result)

            if result['success']:
                print(f"  ✓ Candidate: {result['candidate_name']}")
                print(f"  ✓ Chunks: {result['chunks_created']}")
                print(f"  ✓ Skills: {len(result['skills'])} found")
                if result['experience_years']:
                    print(f"  ✓ Experience: {result['experience_years']} years")
            else:
                print(f"  ✗ Error: {result['error']}")

        print(f"\n{'='*60}")
        successful = sum(1 for r in results if r['success'])
        print(f"✅ Successfully processed {successful}/{total_files} resumes")

        return results

    def get_stats(self) -> Dict[str, Any]:
        """Get statistics about the RAG system."""
        count = self.collection.count()

        # Get sample metadata to analyze
        if count > 0:
            sample = self.collection.get(limit=min(100, count))
            unique_resumes = set(m['filepath'] for m in sample['metadatas'])

            return {
                'total_chunks': count,
                'total_resumes': len(unique_resumes),
                'collection_name': self.collection_name,
                'embedding_model': self.embedding_model.__class__.__name__
            }

        return {
            'total_chunks': 0,
            'total_resumes': 0,
            'collection_name': self.collection_name
        }

    def clear_collection(self):
        """Clear all data from the collection."""
        try:
            self.client.delete_collection(name=self.collection_name)
            self.collection = self.client.create_collection(
                name=self.collection_name,
                metadata={"description": "Resume embeddings for semantic search"}
            )
            print(f"✓ Cleared collection: {self.collection_name}")
        except Exception as e:
            print(f"✗ Error clearing collection: {e}")


def main():
    """Main function for CLI usage."""
    import argparse

    parser = argparse.ArgumentParser(description="RAG System for Resume Processing")
    parser.add_argument('--directory', type=str, default='resumes',
                       help='Directory containing resumes (default: resumes)')
    parser.add_argument('--extension', type=str, default=None,
                       help='File extension filter (e.g., .txt, .pdf)')
    parser.add_argument('--clear', action='store_true',
                       help='Clear existing collection before processing')
    parser.add_argument('--stats', action='store_true',
                       help='Show statistics only')
    parser.add_argument('--collection', type=str, default='resumes',
                       help='Collection name (default: resumes)')

    args = parser.parse_args()

    # Initialize RAG system
    print("🚀 Initializing Resume RAG System")
    print("=" * 60)
    rag = ResumeRAGSystem(collection_name=args.collection)

    # Clear if requested
    if args.clear:
        print("\n🗑️  Clearing existing collection...")
        rag.clear_collection()

    # Show stats if requested
    if args.stats:
        print("\n📊 Collection Statistics")
        print("=" * 60)
        stats = rag.get_stats()
        for key, value in stats.items():
            print(f"  {key}: {value}")
        return

    # Process resumes
    results = rag.process_resume_directory(args.directory, args.extension)

    # Show final stats
    print("\n📊 Final Statistics")
    print("=" * 60)
    stats = rag.get_stats()
    for key, value in stats.items():
        print(f"  {key}: {value}")

    print("\n✅ Resume processing complete!")
    print(f"   Ready for semantic search in job_matcher.py")


if __name__ == "__main__":
    main()
