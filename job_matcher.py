"""
Job Matching Engine with Semantic Search
Implements hybrid search, ranking, and scoring for resume-job matching.
"""

import json
import re
from typing import List, Dict, Any, Optional
from collections import defaultdict

from sentence_transformers import SentenceTransformer
import chromadb
from chromadb.config import Settings


class JobMatcher:
    """
    Semantic search and ranking engine for matching resumes to job descriptions.
    """
    
    def __init__(self,
                 collection_name: str = "resumes",
                 embedding_model: str = "all-MiniLM-L6-v2",
                 persist_directory: str = "./chroma_db"):
        """
        Initialize job matcher.
        
        Args:
            collection_name: Name of the resume collection
            embedding_model: SentenceTransformer model name
            persist_directory: Directory where vector DB is persisted
        """
        self.collection_name = collection_name
        
        # Load embedding model
        print(f"Loading embedding model: {embedding_model}")
        self.embedding_model = SentenceTransformer(embedding_model)
        
        # Connect to ChromaDB
        self.client = chromadb.Client(Settings(
            persist_directory=persist_directory,
            anonymized_telemetry=False
        ))
        
        try:
            self.collection = self.client.get_collection(name=collection_name)
            print(f"✓ Connected to collection: {collection_name}")
            print(f"✓ Total chunks in database: {self.collection.count()}")
        except Exception as e:
            raise Exception(f"Collection '{collection_name}' not found. Run resume_rag.py first. Error: {e}")
    
    def extract_must_have_requirements(self, job_description: str) -> Dict[str, Any]:
        """
        Extract must-have requirements from job description.
        
        Args:
            job_description: Job description text
            
        Returns:
            Dictionary of must-have requirements
        """
        requirements = {
            'min_years': None,
            'required_skills': [],
            'education_level': None
        }
        
        # Extract minimum years of experience
        year_patterns = [
            r'(\d+)\+?\s*years?\s+(?:of\s+)?experience',
            r'minimum\s+of\s+(\d+)\s+years?',
            r'at\s+least\s+(\d+)\s+years?',
        ]
        
        for pattern in year_patterns:
            match = re.search(pattern, job_description, re.IGNORECASE)
            if match:
                requirements['min_years'] = int(match.group(1))
                break
        
        # Extract required skills (look for "required", "must have", etc.)
        required_section_patterns = [
            r'(?:required|must have|mandatory)[:\s]+([^\n]+(?:\n[^\n]+)*?)(?:\n\n|requirements|qualifications|$)',
        ]
        
        for pattern in required_section_patterns:
            match = re.search(pattern, job_description, re.IGNORECASE | re.DOTALL)
            if match:
                section_text = match.group(1)
                # Extract common technical skills
                skill_patterns = [
                    r'\b(Python|Java|JavaScript|TypeScript|C\+\+|C#|Ruby|Go|Rust|PHP|Swift|Kotlin)\b',
                    r'\b(React|Angular|Vue|Node\.js|Django|Flask|Spring|Express)\b',
                    r'\b(AWS|Azure|GCP|Google Cloud|Docker|Kubernetes)\b',
                    r'\b(SQL|PostgreSQL|MySQL|MongoDB|Redis)\b',
                    r'\b(Machine Learning|ML|AI|Deep Learning|NLP)\b',
                ]
                
                for skill_pattern in skill_patterns:
                    matches = re.findall(skill_pattern, section_text, re.IGNORECASE)
                    requirements['required_skills'].extend(matches)
        
        # Extract education requirements
        education_patterns = [
            r'\b(Bachelor|BS|BA|B\.S\.|B\.A\.)',
            r'\b(Master|MS|MA|M\.S\.|M\.A\.)',
            r'\b(PhD|Ph\.D\.|Doctorate)',
        ]
        
        for pattern in education_patterns:
            if re.search(pattern, job_description, re.IGNORECASE):
                requirements['education_level'] = re.search(pattern, job_description, re.IGNORECASE).group(1)
                break
        
        return requirements
    
    def semantic_search(self, 
                       query: str, 
                       top_k: int = 50,
                       where_filter: Optional[Dict] = None) -> List[Dict[str, Any]]:
        """
        Perform semantic search on resume collection.
        
        Args:
            query: Search query (job description)
            top_k: Number of results to return
            where_filter: Optional metadata filter
            
        Returns:
            List of search results with metadata
        """
        # Generate query embedding
        query_embedding = self.embedding_model.encode(query).tolist()
        
        # Search in vector database
        results = self.collection.query(
            query_embeddings=[query_embedding],
            n_results=top_k,
            where=where_filter
        )
        
        # Format results
        formatted_results = []
        for idx in range(len(results['ids'][0])):
            formatted_results.append({
                'id': results['ids'][0][idx],
                'document': results['documents'][0][idx],
                'metadata': results['metadatas'][0][idx],
                'distance': results['distances'][0][idx] if 'distances' in results else None,
            })
        
        return formatted_results

    def hybrid_search(self,
                     query: str,
                     critical_skills: List[str],
                     top_k: int = 50) -> List[Dict[str, Any]]:
        """
        Hybrid search combining semantic similarity and keyword matching.

        Args:
            query: Search query
            critical_skills: List of critical skills to boost
            top_k: Number of results

        Returns:
            List of results with hybrid scores
        """
        # Get semantic search results
        semantic_results = self.semantic_search(query, top_k=top_k)

        # Add keyword scores for critical skills
        for result in semantic_results:
            keyword_score = 0
            doc_lower = result['document'].lower()
            metadata = result['metadata']

            # Check skills in metadata (already extracted)
            try:
                candidate_skills = json.loads(metadata.get('skills', '[]'))
            except:
                candidate_skills = []

            # Score based on critical skills match
            for skill in critical_skills:
                skill_lower = skill.lower()
                if skill_lower in doc_lower or any(skill_lower in cs.lower() for cs in candidate_skills):
                    keyword_score += 1

            # Combine semantic and keyword scores.
            # ChromaDB returns L2 distances (range 0..2 for unit-normed vectors).
            # Normalise to [0,1]: distance=0 → score=1, distance=2 → score=0.
            raw_dist = result['distance'] if result['distance'] is not None else 1.0
            semantic_score = max(0.0, 1.0 - raw_dist / 2.0)
            result['keyword_score'] = keyword_score
            result['semantic_score'] = semantic_score
            result['hybrid_score'] = (semantic_score * 0.7) + (keyword_score * 0.3)

        # Sort by hybrid score
        semantic_results.sort(key=lambda x: x['hybrid_score'], reverse=True)

        return semantic_results

    def aggregate_candidate_results(self, chunk_results: List[Dict[str, Any]]) -> List[Dict[str, Any]]:
        """
        Aggregate chunk-level results to candidate-level results.

        Args:
            chunk_results: List of chunk search results

        Returns:
            List of candidate-level aggregated results
        """
        candidates = defaultdict(lambda: {
            'chunks': [],
            'scores': [],
            'relevant_excerpts': [],
            'matched_sections': set(),
            'metadata': {}
        })

        for result in chunk_results:
            metadata = result['metadata']
            filepath = metadata.get('filepath', 'unknown')

            # Aggregate by candidate
            candidate_data = candidates[filepath]
            candidate_data['chunks'].append(result)
            candidate_data['scores'].append(result.get('hybrid_score', 0))
            candidate_data['relevant_excerpts'].append(result['document'][:200] + '...')
            candidate_data['matched_sections'].add(metadata.get('section', 'unknown'))

            # Store metadata (from first chunk)
            if not candidate_data['metadata']:
                candidate_data['metadata'] = metadata

        # Convert to list and calculate aggregate scores
        aggregated = []
        for filepath, data in candidates.items():
            # Calculate aggregate score (max + average)
            max_score = max(data['scores']) if data['scores'] else 0
            avg_score = sum(data['scores']) / len(data['scores']) if data['scores'] else 0
            final_score = (max_score * 0.6) + (avg_score * 0.4)

            metadata = data['metadata']

            try:
                skills = json.loads(metadata.get('skills', '[]'))
            except:
                skills = []

            aggregated.append({
                'candidate_name': metadata.get('name', 'Unknown'),
                'resume_path': filepath,
                'match_score': min(100, int(final_score * 100)),
                'matched_skills': skills[:10],  # Top 10 skills
                'relevant_excerpts': data['relevant_excerpts'][:3],  # Top 3 excerpts
                'matched_sections': list(data['matched_sections']),
                'num_matching_chunks': len(data['chunks']),
                'experience_years': metadata.get('experience_years'),
                'education': json.loads(metadata.get('education', '[]')) if metadata.get('education') else []
            })

        # Sort by match score
        aggregated.sort(key=lambda x: x['match_score'], reverse=True)

        return aggregated

    def filter_by_requirements(self,
                               candidates: List[Dict[str, Any]],
                               requirements: Dict[str, Any]) -> List[Dict[str, Any]]:
        """
        Filter candidates by must-have requirements.

        Args:
            candidates: List of candidate matches
            requirements: Dictionary of requirements

        Returns:
            Filtered list of candidates
        """
        filtered = []

        for candidate in candidates:
            passes = True
            rejection_reasons = []

            # Check minimum years of experience
            if requirements['min_years'] is not None:
                exp_years = candidate.get('experience_years')
                if exp_years is None or exp_years < requirements['min_years']:
                    passes = False
                    rejection_reasons.append(f"Insufficient experience (requires {requirements['min_years']}+ years)")

            # Check required skills
            if requirements['required_skills']:
                candidate_skills = [s.lower() for s in candidate.get('matched_skills', [])]
                missing_skills = []
                for req_skill in requirements['required_skills']:
                    if not any(req_skill.lower() in cs for cs in candidate_skills):
                        missing_skills.append(req_skill)

                if missing_skills:
                    # Allow some leniency: reject only if more than 50% skills missing
                    if len(missing_skills) > len(requirements['required_skills']) * 0.5:
                        passes = False
                        rejection_reasons.append(f"Missing critical skills: {', '.join(missing_skills[:3])}")

            if passes:
                filtered.append(candidate)
            else:
                candidate['filtered_out'] = True
                candidate['rejection_reasons'] = rejection_reasons

        return filtered

    def generate_match_reasoning(self, candidate: Dict[str, Any], job_description: str) -> str:
        """
        Generate human-readable reasoning for the match.

        Args:
            candidate: Candidate match data
            job_description: Original job description

        Returns:
            Reasoning string
        """
        reasoning_parts = []

        score = candidate['match_score']
        if score >= 85:
            reasoning_parts.append("Excellent match.")
        elif score >= 70:
            reasoning_parts.append("Strong match.")
        elif score >= 50:
            reasoning_parts.append("Good match.")
        else:
            reasoning_parts.append("Moderate match.")

        # Skills match
        matched_skills = candidate.get('matched_skills', [])
        if matched_skills:
            reasoning_parts.append(f"Key skills: {', '.join(matched_skills[:5])}.")

        # Experience
        if candidate.get('experience_years'):
            reasoning_parts.append(f"{candidate['experience_years']} years of experience.")

        # Matching sections
        sections = candidate.get('matched_sections', [])
        if sections:
            reasoning_parts.append(f"Relevant sections: {', '.join(sections[:3])}.")

        return " ".join(reasoning_parts)

    def match_job(self,
                  job_description: str,
                  top_k: int = 10,
                  apply_filters: bool = True) -> Dict[str, Any]:
        """
        Match job description to resumes.

        Args:
            job_description: Job description text
            top_k: Number of top matches to return
            apply_filters: Whether to apply must-have filters

        Returns:
            Dictionary with job description and top matches
        """
        print(f"\n🔍 Matching job description...")
        print("=" * 60)

        # Extract requirements
        requirements = self.extract_must_have_requirements(job_description)
        print(f"📋 Extracted Requirements:")
        print(f"   Min Years: {requirements['min_years'] or 'Not specified'}")
        print(f"   Required Skills: {requirements['required_skills'] or 'Not specified'}")
        print(f"   Education: {requirements['education_level'] or 'Not specified'}")

        # Perform hybrid search
        print(f"\n🔎 Performing hybrid search...")
        chunk_results = self.hybrid_search(
            query=job_description,
            critical_skills=requirements['required_skills'],
            top_k=100  # Get more chunks to aggregate
        )

        print(f"   Found {len(chunk_results)} matching chunks")

        # Aggregate to candidate level
        print(f"📊 Aggregating results by candidate...")
        candidates = self.aggregate_candidate_results(chunk_results)
        print(f"   Found {len(candidates)} unique candidates")

        # Apply filters if requested
        if apply_filters and (requirements['min_years'] or requirements['required_skills']):
            print(f"\n🔬 Applying filters...")
            original_count = len(candidates)
            candidates = self.filter_by_requirements(candidates, requirements)
            filtered_count = original_count - len(candidates)
            print(f"   Filtered out {filtered_count} candidates")

        # Take top K
        top_matches = candidates[:top_k]

        # Generate reasoning for each match
        for candidate in top_matches:
            candidate['reasoning'] = self.generate_match_reasoning(candidate, job_description)

        result = {
            'job_description': job_description[:500] + '...' if len(job_description) > 500 else job_description,
            'requirements': requirements,
            'total_candidates_found': len(candidates),
            'top_matches': top_matches
        }

        print(f"\n✅ Match complete!")
        print(f"   Top {len(top_matches)} matches ready")

        return result


def main():
    """Main function for CLI usage."""
    import argparse

    parser = argparse.ArgumentParser(description="Job Matching Engine")
    parser.add_argument('--job-description', type=str,
                       help='Job description text or file path')
    parser.add_argument('--job-file', type=str,
                       help='Path to job description file')
    parser.add_argument('--top-k', type=int, default=10,
                       help='Number of top matches to return (default: 10)')
    parser.add_argument('--no-filter', action='store_true',
                       help='Disable must-have requirement filtering')
    parser.add_argument('--output', type=str,
                       help='Output JSON file path')
    parser.add_argument('--collection', type=str, default='resumes',
                       help='Collection name (default: resumes)')

    args = parser.parse_args()

    # Get job description
    job_description = None
    if args.job_file:
        with open(args.job_file, 'r') as f:
            job_description = f.read()
    elif args.job_description:
        job_description = args.job_description
    else:
        print("❌ Please provide --job-description or --job-file")
        return

    # Initialize matcher
    print("🚀 Initializing Job Matcher")
    print("=" * 60)
    matcher = JobMatcher(collection_name=args.collection)

    # Perform matching
    result = matcher.match_job(
        job_description=job_description,
        top_k=args.top_k,
        apply_filters=not args.no_filter
    )

    # Print results
    print(f"\n📊 Top {len(result['top_matches'])} Matches")
    print("=" * 60)

    for idx, match in enumerate(result['top_matches'], 1):
        print(f"\n{idx}. {match['candidate_name']} - Score: {match['match_score']}/100")
        print(f"   📄 Resume: {match['resume_path']}")
        print(f"   💼 Experience: {match['experience_years'] or 'N/A'} years")
        print(f"   🎓 Education: {', '.join(match['education'][:2]) or 'N/A'}")
        print(f"   🔧 Skills: {', '.join(match['matched_skills'][:5])}")
        print(f"   📝 Reasoning: {match['reasoning']}")

    # Save to file if requested
    if args.output:
        with open(args.output, 'w') as f:
            json.dump(result, f, indent=2)
        print(f"\n💾 Results saved to: {args.output}")


if __name__ == "__main__":
    main()
