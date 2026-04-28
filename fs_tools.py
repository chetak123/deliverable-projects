"""
File System Tools for LLM Function Calling
This module provides structured tools for file operations that can be used by LLMs.
"""

import os
import re
from pathlib import Path
from datetime import datetime
from typing import Optional, Dict, List, Any

# Optional imports for file processing
try:
    import PyPDF2
    PYPDF2_AVAILABLE = True
except ImportError:
    PYPDF2_AVAILABLE = False

try:
    import docx
    DOCX_AVAILABLE = True
except ImportError:
    DOCX_AVAILABLE = False


def read_file(filepath: str) -> Dict[str, Any]:
    """
    Read resume files (PDF, TXT, DOCX) and extract text content.
    
    Args:
        filepath: Path to the file to read
        
    Returns:
        dict: Structured response with content and metadata
        {
            'success': bool,
            'content': str,
            'metadata': {
                'filename': str,
                'extension': str,
                'size_bytes': int,
                'modified_date': str
            },
            'error': str (if success=False)
        }
    """
    try:
        file_path = Path(filepath)
        
        if not file_path.exists():
            return {
                'success': False,
                'error': f'File not found: {filepath}'
            }
        
        # Get metadata
        stat = file_path.stat()
        metadata = {
            'filename': file_path.name,
            'extension': file_path.suffix.lower(),
            'size_bytes': stat.st_size,
            'modified_date': datetime.fromtimestamp(stat.st_mtime).isoformat()
        }
        
        # Extract content based on file type
        content = ''
        extension = file_path.suffix.lower()

        if extension == '.pdf':
            if not PYPDF2_AVAILABLE:
                return {
                    'success': False,
                    'error': 'PyPDF2 not installed. Install with: pip install PyPDF2'
                }
            with open(file_path, 'rb') as f:
                pdf_reader = PyPDF2.PdfReader(f)
                for page in pdf_reader.pages:
                    content += page.extract_text() + '\n'

        elif extension == '.docx':
            if not DOCX_AVAILABLE:
                return {
                    'success': False,
                    'error': 'python-docx not installed. Install with: pip install python-docx'
                }
            doc = docx.Document(file_path)
            content = '\n'.join([paragraph.text for paragraph in doc.paragraphs])

        elif extension == '.txt':
            with open(file_path, 'r', encoding='utf-8') as f:
                content = f.read()

        else:
            return {
                'success': False,
                'error': f'Unsupported file format: {extension}. Supported: .pdf, .docx, .txt'
            }
        
        return {
            'success': True,
            'content': content.strip(),
            'metadata': metadata
        }
        
    except Exception as e:
        return {
            'success': False,
            'error': f'Error reading file: {str(e)}'
        }


def list_files(directory: str, extension: Optional[str] = None) -> Dict[str, Any]:
    """
    List all files in a directory with optional extension filtering.
    
    Args:
        directory: Path to the directory
        extension: Optional file extension to filter (e.g., '.pdf', 'pdf')
        
    Returns:
        dict: List of files with metadata
        {
            'success': bool,
            'files': [
                {
                    'name': str,
                    'path': str,
                    'size_bytes': int,
                    'modified_date': str,
                    'extension': str
                }
            ],
            'count': int,
            'error': str (if success=False)
        }
    """
    try:
        dir_path = Path(directory)
        
        if not dir_path.exists():
            return {
                'success': False,
                'error': f'Directory not found: {directory}'
            }
        
        if not dir_path.is_dir():
            return {
                'success': False,
                'error': f'Path is not a directory: {directory}'
            }
        
        # Normalize extension if provided
        if extension:
            if not extension.startswith('.'):
                extension = f'.{extension}'
            extension = extension.lower()
        
        files = []
        for item in dir_path.iterdir():
            if item.is_file():
                # Filter by extension if specified
                if extension and item.suffix.lower() != extension:
                    continue
                
                stat = item.stat()
                files.append({
                    'name': item.name,
                    'path': str(item),
                    'size_bytes': stat.st_size,
                    'modified_date': datetime.fromtimestamp(stat.st_mtime).isoformat(),
                    'extension': item.suffix.lower()
                })
        
        # Sort by name
        files.sort(key=lambda x: x['name'])
        
        return {
            'success': True,
            'files': files,
            'count': len(files)
        }
        
    except Exception as e:
        return {
            'success': False,
            'error': f'Error listing files: {str(e)}'
        }


def write_file(filepath: str, content: str) -> Dict[str, Any]:
    """
    Write content to a file, creating directories if needed.

    Args:
        filepath: Path where file should be written
        content: Content to write to the file

    Returns:
        dict: Success/failure status
        {
            'success': bool,
            'filepath': str,
            'bytes_written': int,
            'error': str (if success=False)
        }
    """
    try:
        file_path = Path(filepath)

        # Create parent directories if they don't exist
        file_path.parent.mkdir(parents=True, exist_ok=True)

        # Write content
        with open(file_path, 'w', encoding='utf-8') as f:
            f.write(content)

        # Get file size
        size = file_path.stat().st_size

        return {
            'success': True,
            'filepath': str(file_path),
            'bytes_written': size
        }

    except Exception as e:
        return {
            'success': False,
            'error': f'Error writing file: {str(e)}'
        }


def search_in_file(filepath: str, keyword: str) -> Dict[str, Any]:
    """
    Search for keywords in file content with context.

    Args:
        filepath: Path to the file to search
        keyword: Keyword to search for (case-insensitive)

    Returns:
        dict: Matches with surrounding context
        {
            'success': bool,
            'matches': [
                {
                    'line_number': int,
                    'line': str,
                    'context_before': str,
                    'context_after': str,
                    'matched_text': str
                }
            ],
            'count': int,
            'error': str (if success=False)
        }
    """
    try:
        # First read the file
        file_data = read_file(filepath)

        if not file_data['success']:
            return file_data  # Return the error from read_file

        content = file_data['content']
        lines = content.split('\n')

        matches = []
        keyword_lower = keyword.lower()
        context_lines = 2  # Number of lines before/after to include

        for i, line in enumerate(lines):
            if keyword_lower in line.lower():
                # Find the matched text (preserve original case)
                match_start = line.lower().find(keyword_lower)
                matched_text = line[match_start:match_start + len(keyword)]

                # Get context
                context_before = '\n'.join(lines[max(0, i - context_lines):i])
                context_after = '\n'.join(lines[i + 1:min(len(lines), i + context_lines + 1)])

                matches.append({
                    'line_number': i + 1,
                    'line': line,
                    'context_before': context_before,
                    'context_after': context_after,
                    'matched_text': matched_text
                })

        return {
            'success': True,
            'matches': matches,
            'count': len(matches),
            'keyword': keyword
        }

    except Exception as e:
        return {
            'success': False,
            'error': f'Error searching file: {str(e)}'
        }


# Tool definitions for LLM function calling
TOOLS = [
    {
        "name": "read_file",
        "description": "Read and extract text content from resume files (PDF, TXT, DOCX). Returns the file content along with metadata like filename, size, and modification date.",
        "input_schema": {
            "type": "object",
            "properties": {
                "filepath": {
                    "type": "string",
                    "description": "Path to the file to read (relative or absolute)"
                }
            },
            "required": ["filepath"]
        }
    },
    {
        "name": "list_files",
        "description": "List all files in a directory with optional filtering by file extension. Returns file metadata including name, size, and modification date.",
        "input_schema": {
            "type": "object",
            "properties": {
                "directory": {
                    "type": "string",
                    "description": "Path to the directory to list"
                },
                "extension": {
                    "type": "string",
                    "description": "Optional file extension to filter by (e.g., '.pdf', 'txt')"
                }
            },
            "required": ["directory"]
        }
    },
    {
        "name": "write_file",
        "description": "Write content to a file. Creates parent directories automatically if they don't exist. Returns success status and bytes written.",
        "input_schema": {
            "type": "object",
            "properties": {
                "filepath": {
                    "type": "string",
                    "description": "Path where the file should be written"
                },
                "content": {
                    "type": "string",
                    "description": "Content to write to the file"
                }
            },
            "required": ["filepath", "content"]
        }
    },
    {
        "name": "search_in_file",
        "description": "Search for keywords in file content (case-insensitive). Returns all matches with surrounding context lines for better understanding.",
        "input_schema": {
            "type": "object",
            "properties": {
                "filepath": {
                    "type": "string",
                    "description": "Path to the file to search in"
                },
                "keyword": {
                    "type": "string",
                    "description": "Keyword to search for (search is case-insensitive)"
                }
            },
            "required": ["filepath", "keyword"]
        }
    }
]


# Map tool names to functions
TOOL_FUNCTIONS = {
    "read_file": read_file,
    "list_files": list_files,
    "write_file": write_file,
    "search_in_file": search_in_file
}
