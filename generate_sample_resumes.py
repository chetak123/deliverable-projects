"""
Generate sample resume files for testing the LLM File Assistant.
Creates resumes in TXT, PDF, and DOCX formats.
"""

from pathlib import Path
from docx import Document
from reportlab.lib.pagesizes import letter
from reportlab.platypus import SimpleDocTemplate, Paragraph, Spacer
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib.units import inch


# Sample resume data
RESUMES = [
    {
        "filename": "john_doe",
        "content": """JOHN DOE
Software Engineer

Email: john.doe@email.com | Phone: (555) 123-4567
LinkedIn: linkedin.com/in/johndoe | GitHub: github.com/johndoe

SUMMARY
Experienced software engineer with 5+ years of expertise in Python, JavaScript, and cloud technologies.
Passionate about building scalable applications and mentoring junior developers.

TECHNICAL SKILLS
- Languages: Python, JavaScript, TypeScript, Java, SQL
- Frameworks: Django, Flask, React, Node.js, Express
- Tools: Docker, Kubernetes, Git, Jenkins, AWS
- Databases: PostgreSQL, MongoDB, Redis

EXPERIENCE

Senior Software Engineer | Tech Corp | 2021 - Present
- Led development of microservices architecture using Python and Docker
- Improved system performance by 40% through optimization
- Mentored team of 5 junior developers

Software Engineer | StartUp Inc | 2019 - 2021
- Built REST APIs using Flask and PostgreSQL
- Implemented CI/CD pipelines with Jenkins
- Developed React frontend applications

EDUCATION
Bachelor of Science in Computer Science
University of Technology | 2015 - 2019

CERTIFICATIONS
- AWS Certified Solutions Architect
- Python Professional Certification
"""
    },
    {
        "filename": "jane_smith",
        "content": """JANE SMITH
Data Scientist

Contact: jane.smith@email.com | (555) 987-6543
Portfolio: janesmith.dev

PROFESSIONAL SUMMARY
Data scientist with 4 years of experience in machine learning, statistical analysis, and data visualization.
Skilled in Python, R, and SQL with a proven track record of delivering insights that drive business decisions.

SKILLS
- Programming: Python, R, SQL, MATLAB
- ML/AI: TensorFlow, PyTorch, Scikit-learn, Keras
- Data Viz: Tableau, Power BI, Matplotlib, Seaborn
- Big Data: Spark, Hadoop, Hive

WORK HISTORY

Data Scientist | Analytics Solutions | 2022 - Present
- Developed predictive models achieving 92% accuracy
- Created automated reporting dashboards using Python and Tableau
- Analyzed large datasets (10M+ records) using Spark

Junior Data Analyst | Data Insights Co | 2020 - 2022
- Performed statistical analysis using R and Python
- Built ETL pipelines for data processing
- Collaborated with stakeholders to define KPIs

EDUCATION
Master of Science in Data Science
Data University | 2018 - 2020

Bachelor of Science in Statistics
Math College | 2014 - 2018

PUBLICATIONS
- "Machine Learning Applications in Finance" - Data Journal 2023
"""
    },
    {
        "filename": "mike_johnson",
        "content": """MIKE JOHNSON
Full Stack Developer

mike.johnson@email.com | +1-555-456-7890
GitHub: github.com/mikej | Portfolio: mikejohnson.io

ABOUT ME
Full stack developer with 6 years of experience building web applications.
Expertise in JavaScript ecosystem and cloud platforms.

TECHNICAL EXPERTISE
- Frontend: React, Vue.js, Angular, HTML5, CSS3, TypeScript
- Backend: Node.js, Express, Python, Django
- Databases: MySQL, PostgreSQL, MongoDB
- Cloud: AWS, Google Cloud, Azure
- DevOps: Docker, Kubernetes, Terraform

PROFESSIONAL EXPERIENCE

Lead Full Stack Developer | WebTech Solutions | 2020 - Present
- Architected and deployed cloud-native applications on AWS
- Led team of 8 developers using Agile methodologies
- Reduced infrastructure costs by 30% through optimization

Full Stack Developer | Digital Agency | 2018 - 2020
- Developed e-commerce platforms using React and Node.js
- Integrated payment systems (Stripe, PayPal)
- Implemented automated testing (Jest, Cypress)

EDUCATION
Bachelor of Engineering in Software Engineering
Engineering Institute | 2014 - 2018

ACHIEVEMENTS
- Winner of HackTech 2022
- Open source contributor (500+ GitHub stars)
"""
    },
    {
        "filename": "sarah_williams",
        "content": """SARAH WILLIAMS
DevOps Engineer

sarah.williams@email.com | (555) 234-5678

OBJECTIVE
DevOps engineer with strong background in infrastructure automation, CI/CD, and cloud platforms.
Seeking to leverage expertise in Kubernetes and cloud technologies.

CORE COMPETENCIES
- Container Orchestration: Kubernetes, Docker, Docker Swarm
- CI/CD: Jenkins, GitLab CI, GitHub Actions, CircleCI
- Cloud Platforms: AWS, Azure, GCP
- IaC: Terraform, Ansible, CloudFormation
- Monitoring: Prometheus, Grafana, ELK Stack
- Scripting: Python, Bash, PowerShell

EMPLOYMENT HISTORY

Senior DevOps Engineer | Cloud Systems Inc | 2021 - Present
- Managed Kubernetes clusters serving 1M+ daily users
- Automated infrastructure provisioning using Terraform
- Reduced deployment time by 70% with CI/CD pipelines

DevOps Engineer | Tech Startup | 2019 - 2021
- Built and maintained AWS infrastructure
- Implemented monitoring and alerting systems
- Developed Python automation scripts

EDUCATION
Bachelor of Science in Information Technology
IT University | 2015 - 2019

CERTIFICATIONS
- Certified Kubernetes Administrator (CKA)
- AWS DevOps Engineer Professional
- HashiCorp Terraform Certified
"""
    },
    {
        "filename": "alex_chen",
        "content": """ALEX CHEN
Mobile App Developer

Email: alex.chen@email.com | Phone: (555) 345-6789
LinkedIn: linkedin.com/in/alexchen

PROFILE
Mobile application developer specializing in iOS and Android development.
5 years of experience creating user-friendly mobile applications.

TECHNICAL SKILLS
- Mobile: Swift, Kotlin, React Native, Flutter
- Languages: JavaScript, TypeScript, Python
- Backend: Firebase, Node.js, GraphQL
- Tools: Xcode, Android Studio, Git, Fastlane
- Testing: XCTest, Espresso, Jest

EXPERIENCE

Senior Mobile Developer | Mobile Apps Co | 2021 - Present
- Developed iOS apps with Swift reaching 500K+ downloads
- Led migration from native to React Native
- Implemented push notifications and in-app purchases
- Improved app performance and reduced crash rate by 60%

Mobile Developer | App Studio | 2019 - 2021
- Built Android applications using Kotlin
- Integrated RESTful APIs and GraphQL
- Collaborated with designers on UI/UX

EDUCATION
Bachelor of Science in Computer Science
Tech University | 2015 - 2019

PROJECTS
- TaskMaster App: Productivity app with 100K+ users
- FitTracker: Health and fitness tracking application
"""
    }
]


def create_text_resume(resume_data, output_dir):
    """Create a TXT resume file."""
    filepath = output_dir / f"{resume_data['filename']}.txt"
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(resume_data['content'])
    print(f"✓ Created: {filepath}")


def create_pdf_resume(resume_data, output_dir):
    """Create a PDF resume file."""
    filepath = output_dir / f"{resume_data['filename']}.pdf"
    
    doc = SimpleDocTemplate(str(filepath), pagesize=letter)
    styles = getSampleStyleSheet()
    story = []
    
    # Split content into paragraphs
    paragraphs = resume_data['content'].split('\n\n')
    
    for para in paragraphs:
        if para.strip():
            # Use different style for headers (all caps lines)
            if para.strip().isupper() and len(para.strip()) < 50:
                p = Paragraph(para.replace('\n', '<br/>'), styles['Heading1'])
            else:
                p = Paragraph(para.replace('\n', '<br/>'), styles['Normal'])
            story.append(p)
            story.append(Spacer(1, 0.2 * inch))
    
    doc.build(story)
    print(f"✓ Created: {filepath}")


def create_docx_resume(resume_data, output_dir):
    """Create a DOCX resume file."""
    filepath = output_dir / f"{resume_data['filename']}.docx"

    doc = Document()

    # Split content into paragraphs
    paragraphs = resume_data['content'].split('\n\n')

    for para in paragraphs:
        if para.strip():
            doc.add_paragraph(para.strip())

    doc.save(str(filepath))
    print(f"✓ Created: {filepath}")


def main():
    """Generate all sample resume files."""
    print("📄 Generating Sample Resume Files")
    print("=" * 60)

    # Create output directory
    output_dir = Path("resumes")
    output_dir.mkdir(exist_ok=True)

    # Generate resumes in different formats
    for i, resume in enumerate(RESUMES, 1):
        print(f"\nGenerating resume {i}/{len(RESUMES)}: {resume['filename']}")

        # Create TXT version
        create_text_resume(resume, output_dir)

        # Create PDF version for first 3 resumes
        if i <= 3:
            create_pdf_resume(resume, output_dir)

        # Create DOCX version for last 3 resumes
        if i >= 3:
            create_docx_resume(resume, output_dir)

    print(f"\n{'='*60}")
    print(f"✅ Successfully generated {len(RESUMES)} resumes in multiple formats")
    print(f"   Location: {output_dir.absolute()}")
    print(f"{'='*60}")


if __name__ == "__main__":
    main()
