"""
Generate extended dataset of 30+ resumes and 5+ job descriptions for RAG testing.
"""

import random
from pathlib import Path


# Skills pools
PROGRAMMING_LANGUAGES = ['Python', 'Java', 'JavaScript', 'TypeScript', 'C++', 'C#', 'Go', 'Rust', 'Ruby', 'PHP', 'Swift', 'Kotlin', 'Scala']
FRAMEWORKS = ['React', 'Angular', 'Vue.js', 'Node.js', 'Django', 'Flask', 'Spring Boot', 'Express', 'FastAPI', 'Next.js', 'Ruby on Rails']
DATABASES = ['PostgreSQL', 'MySQL', 'MongoDB', 'Redis', 'Elasticsearch', 'Cassandra', 'DynamoDB', 'Oracle', 'SQL Server']
CLOUD = ['AWS', 'Azure', 'Google Cloud', 'Docker', 'Kubernetes', 'Terraform', 'Ansible']
ML_AI = ['Machine Learning', 'Deep Learning', 'NLP', 'Computer Vision', 'TensorFlow', 'PyTorch', 'Scikit-learn', 'Keras']
TOOLS = ['Git', 'Jenkins', 'GitLab CI', 'GitHub Actions', 'Jira', 'Confluence', 'Tableau', 'Power BI']

# Job titles and roles
ROLES = [
    ('Software Engineer', ['Python', 'Java', 'JavaScript'], ['Django', 'React', 'Spring Boot'], ['PostgreSQL', 'MongoDB']),
    ('Senior Backend Developer', ['Python', 'Go', 'Java'], ['Django', 'Flask', 'Spring Boot'], ['PostgreSQL', 'Redis']),
    ('Frontend Developer', ['JavaScript', 'TypeScript'], ['React', 'Vue.js', 'Angular'], []),
    ('Full Stack Developer', ['JavaScript', 'TypeScript', 'Python'], ['React', 'Node.js', 'Django'], ['PostgreSQL', 'MongoDB']),
    ('Data Scientist', ['Python', 'R'], ['TensorFlow', 'PyTorch', 'Scikit-learn'], ['PostgreSQL']),
    ('Machine Learning Engineer', ['Python'], ['TensorFlow', 'PyTorch', 'Scikit-learn'], []),
    ('DevOps Engineer', ['Python', 'Go'], ['Terraform', 'Ansible'], ['PostgreSQL']),
    ('Cloud Architect', ['Python', 'Java'], ['Terraform'], []),
    ('Mobile Developer', ['Swift', 'Kotlin'], ['React Native', 'Flutter'], []),
    ('QA Engineer', ['Python', 'JavaScript'], ['Selenium', 'Jest'], []),
]

FIRST_NAMES = ['Alex', 'Sam', 'Jordan', 'Taylor', 'Morgan', 'Casey', 'Riley', 'Avery', 'Quinn', 'Reese',
               'Blake', 'Cameron', 'Dakota', 'Emerson', 'Finley', 'Gray', 'Hayden', 'India', 'Jamie', 'Kennedy',
               'Logan', 'Mason', 'Noel', 'Oakley', 'Parker', 'Quinn', 'Rory', 'Sage', 'Tatum', 'Uma', 'Val']

LAST_NAMES = ['Smith', 'Johnson', 'Williams', 'Brown', 'Jones', 'Garcia', 'Miller', 'Davis', 'Rodriguez', 'Martinez',
              'Chen', 'Kumar', 'Patel', 'Anderson', 'Taylor', 'Thomas', 'Moore', 'Jackson', 'Martin', 'Lee',
              'Thompson', 'White', 'Harris', 'Sanchez', 'Clark', 'Ramirez', 'Lewis', 'Robinson', 'Walker', 'Young']

UNIVERSITIES = ['MIT', 'Stanford', 'UC Berkeley', 'Carnegie Mellon', 'Georgia Tech', 'University of Washington',
                'University of Texas', 'University of Michigan', 'Cornell', 'Columbia', 'NYU']

COMPANIES = ['Tech Corp', 'Innovation Labs', 'Digital Solutions', 'Cloud Systems Inc', 'Data Insights Co',
             'Web Services LLC', 'Mobile Apps Co', 'AI Research Inc', 'Enterprise Software Group', 'StartUp Ventures']


def generate_resume(role_idx: int, experience_years: int) -> dict:
    """Generate a resume based on role and experience."""
    role, primary_langs, frameworks_list, databases_list = ROLES[role_idx % len(ROLES)]
    
    name = f"{random.choice(FIRST_NAMES)} {random.choice(LAST_NAMES)}"
    email = f"{name.lower().replace(' ', '.')}@email.com"
    
    # Select skills
    languages = primary_langs + random.sample([l for l in PROGRAMMING_LANGUAGES if l not in primary_langs], k=min(2, len(PROGRAMMING_LANGUAGES)))
    frameworks = frameworks_list + random.sample([f for f in FRAMEWORKS if f not in frameworks_list], k=min(2, len(FRAMEWORKS)))
    databases = databases_list + random.sample([d for d in DATABASES if d not in databases_list], k=min(2, len(DATABASES)))
    cloud_tools = random.sample(CLOUD, k=random.randint(2, 4))
    tools = random.sample(TOOLS, k=random.randint(2, 4))
    
    # Add ML/AI for relevant roles
    ml_skills = []
    if 'Data Scientist' in role or 'Machine Learning' in role:
        ml_skills = random.sample(ML_AI, k=random.randint(3, 5))
    
    # Generate work experience
    companies = random.sample(COMPANIES, k=min(experience_years // 2 + 1, 3))
    current_year = 2024
    
    experiences = []
    years_left = experience_years
    for i, company in enumerate(companies):
        years_at_company = min(random.randint(2, 4), years_left)
        end_year = current_year - sum([random.randint(2, 4) for _ in range(i)])
        start_year = end_year - years_at_company
        
        level = 'Senior' if experience_years > 5 else 'Junior' if experience_years < 3 else ''
        title = f"{level} {role}".strip()
        
        experiences.append({
            'title': title,
            'company': company,
            'period': f"{start_year} - {end_year if i == 0 else 'Present'}",
            'achievements': [
                f"Led development of {random.choice(['microservices', 'web applications', 'mobile apps', 'data pipelines'])}",
                f"Improved {random.choice(['performance', 'scalability', 'user experience'])} by {random.randint(20, 60)}%",
                f"Mentored {random.randint(2, 8)} junior developers" if experience_years > 3 else "Collaborated with cross-functional teams"
            ]
        })
        
        years_left -= years_at_company
        if years_left <= 0:
            break
    
    # Generate education
    degree_types = ['Bachelor of Science', 'Master of Science'] if experience_years > 3 else ['Bachelor of Science']
    education = []
    for degree in degree_types:
        field = random.choice(['Computer Science', 'Software Engineering', 'Data Science', 'Information Technology'])
        university = random.choice(UNIVERSITIES)
        grad_year = 2024 - experience_years - (4 if 'Master' in degree else 0)
        education.append(f"{degree} in {field}, {university}, {grad_year}")
    
    # Build resume
    resume_text = f"""{name}
{role}

Email: {email} | Phone: (555) {random.randint(100,999)}-{random.randint(1000,9999)}

PROFESSIONAL SUMMARY
{role} with {experience_years}+ years of experience in software development and technology solutions.
Proven track record of delivering high-quality applications and driving innovation.

TECHNICAL SKILLS
- Languages: {', '.join(languages[:5])}
- Frameworks: {', '.join(frameworks[:5])}"""
    
    if databases:
        resume_text += f"\n- Databases: {', '.join(databases[:4])}"
    if cloud_tools:
        resume_text += f"\n- Cloud/DevOps: {', '.join(cloud_tools)}"
    if ml_skills:
        resume_text += f"\n- ML/AI: {', '.join(ml_skills)}"
    if tools:
        resume_text += f"\n- Tools: {', '.join(tools[:4])}"
    
    resume_text += "\n\nPROFESSIONAL EXPERIENCE\n"
    
    for exp in experiences:
        resume_text += f"\n{exp['title']} | {exp['company']} | {exp['period']}\n"
        for achievement in exp['achievements']:
            resume_text += f"- {achievement}\n"
    
    resume_text += "\n\nEDUCATION\n"
    for edu in education:
        resume_text += f"{edu}\n"
    
    # Add certifications for senior roles
    if experience_years > 5:
        resume_text += "\nCERTIFICATIONS\n"
        certs = random.sample(['AWS Certified Solutions Architect', 'Google Cloud Professional', 
                              'Certified Kubernetes Administrator', 'PMP Certified'], k=random.randint(1, 2))
        for cert in certs:
            resume_text += f"- {cert}\n"
    
    return {
        'filename': f"{name.lower().replace(' ', '_')}.txt",
        'content': resume_text
    }


JOB_DESCRIPTIONS = [
    {
        'filename': 'senior_python_engineer.txt',
        'content': """Senior Python Engineer

About the Role:
We are seeking a Senior Python Engineer to join our growing team. You will be responsible for designing and implementing scalable backend systems.

Required Qualifications:
- 5+ years of professional software development experience
- Strong expertise in Python
- Experience with Django or Flask framework
- Proficiency in SQL databases (PostgreSQL preferred)
- Experience with RESTful API design
- Strong understanding of software design patterns

Preferred Qualifications:
- Experience with AWS or other cloud platforms
- Knowledge of Docker and Kubernetes
- Experience with CI/CD pipelines
- Familiarity with microservices architecture
- Experience mentoring junior developers

Responsibilities:
- Design and develop high-quality Python applications
- Collaborate with cross-functional teams
- Participate in code reviews and technical discussions
- Optimize application performance and scalability
- Mentor junior team members

We offer competitive salary, health benefits, and remote work options.
"""
    },
    {
        'filename': 'machine_learning_engineer.txt',
        'content': """Machine Learning Engineer

Company Overview:
Join our AI team to build cutting-edge machine learning solutions.

Requirements:
- Master's degree in Computer Science, Statistics, or related field
- 3+ years of experience in machine learning
- Strong programming skills in Python
- Experience with TensorFlow or PyTorch
- Solid understanding of ML algorithms and deep learning
- Experience with NLP or Computer Vision
- Proficiency in data preprocessing and feature engineering

Nice to Have:
- PhD in Machine Learning or related field
- Publications in top-tier conferences
- Experience with MLOps and model deployment
- Knowledge of distributed training
- Experience with cloud ML platforms (AWS SageMaker, Azure ML)

What You'll Do:
- Develop and deploy machine learning models
- Research and implement state-of-the-art algorithms
- Work with large-scale datasets
- Optimize model performance and accuracy
- Collaborate with data scientists and engineers

Competitive compensation package with equity options.
"""
    },
    {
        'filename': 'full_stack_developer.txt',
        'content': """Full Stack Developer

Position: Full Stack Developer (Mid to Senior Level)

Required Skills:
- 4+ years of full stack development experience
- Proficiency in JavaScript/TypeScript
- Strong experience with React or Vue.js
- Backend development with Node.js
- Experience with RESTful APIs and GraphQL
- Database experience (PostgreSQL, MongoDB)
- Version control with Git

Preferred Skills:
- Experience with Next.js or modern React frameworks
- Knowledge of TypeScript
- CI/CD pipeline experience
- Docker and containerization
- AWS or cloud platform experience
- Agile/Scrum methodology

Responsibilities:
- Build and maintain web applications
- Develop responsive user interfaces
- Design and implement APIs
- Write clean, maintainable code
- Collaborate with designers and product managers
- Participate in sprint planning and standups

Benefits include health insurance, 401k matching, and flexible hours.
"""
    },
    {
        'filename': 'devops_engineer.txt',
        'content': """DevOps Engineer

We're looking for an experienced DevOps Engineer to enhance our infrastructure.

Minimum Requirements:
- 5+ years in DevOps or similar role
- Strong experience with AWS (EC2, S3, RDS, Lambda)
- Expertise in Kubernetes and Docker
- Infrastructure as Code (Terraform preferred)
- CI/CD pipeline design and implementation
- Scripting skills (Python, Bash)
- Experience with monitoring tools (Prometheus, Grafana)

Additional Requirements:
- Jenkins or GitLab CI experience
- Configuration management (Ansible, Chef, or Puppet)
- Security best practices
- Performance optimization skills
- Strong troubleshooting abilities

Key Responsibilities:
- Manage and scale Kubernetes clusters
- Automate infrastructure provisioning
- Implement and maintain CI/CD pipelines
- Monitor system performance and reliability
- Ensure security and compliance
- Collaborate with development teams

Excellent benefits package with unlimited PTO.
"""
    },
    {
        'filename': 'data_scientist.txt',
        'content': """Data Scientist

Company: Leading Analytics Firm

Required Qualifications:
- 3+ years of data science experience
- Strong Python programming skills
- Experience with pandas, NumPy, Scikit-learn
- Proficiency in statistical analysis and hypothesis testing
- SQL expertise for data extraction
- Data visualization skills (Matplotlib, Seaborn, Tableau)
- Machine learning model development experience

Preferred Qualifications:
- Master's or PhD in quantitative field
- Experience with big data technologies (Spark, Hadoop)
- Deep learning experience (TensorFlow, PyTorch)
- A/B testing and experimental design
- Business intelligence tools
- Experience in specific domain (finance, healthcare, etc.)

What You'll Do:
- Analyze large datasets to extract insights
- Build predictive models and ML algorithms
- Create data visualizations and dashboards
- Collaborate with stakeholders on data-driven decisions
- Present findings to non-technical audiences
- Maintain and improve existing models

Competitive salary with performance bonuses and stock options.
"""
    },
    {
        'filename': 'frontend_developer.txt',
        'content': """Frontend Developer

Position: Senior Frontend Developer

Must Have:
- 4+ years of frontend development experience
- Expert-level knowledge of React
- Strong JavaScript and TypeScript skills
- HTML5, CSS3, and responsive design
- State management (Redux, Context API)
- RESTful API integration
- Git version control

Nice to Have:
- Next.js or Gatsby experience
- UI/UX design skills
- Testing frameworks (Jest, React Testing Library)
- Webpack or build tools configuration
- Performance optimization experience
- Accessibility (WCAG) knowledge

Responsibilities:
- Develop modern, responsive web applications
- Implement pixel-perfect designs
- Optimize application performance
- Write clean, reusable components
- Collaborate with backend developers
- Participate in code reviews

Great work culture with remote-first approach and professional development budget.
"""
    }
]


def main():
    """Generate extended dataset."""
    print("📄 Generating Extended Dataset (30+ Resumes, 5+ Job Descriptions)")
    print("=" * 70)

    # Create directories
    resumes_dir = Path("resumes_extended")
    jobs_dir = Path("job_descriptions")
    resumes_dir.mkdir(exist_ok=True)
    jobs_dir.mkdir(exist_ok=True)

    # Generate 35 resumes with varying experience levels
    print("\n Generating Resumes...")
    experience_distribution = ([1, 2, 3] * 5) + ([4, 5, 6, 7] * 4) + ([8, 9, 10, 11, 12] * 2)

    for i in range(35):
        role_idx = i % len(ROLES)
        experience = random.choice(experience_distribution)

        resume = generate_resume(role_idx, experience)
        filepath = resumes_dir / resume['filename']

        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(resume['content'])

        print(f"  ✓ Created: {resume['filename']}")

    # Generate job descriptions
    print(f"\n💼 Generating Job Descriptions...")
    for job in JOB_DESCRIPTIONS:
        filepath = jobs_dir / job['filename']

        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(job['content'])

        print(f"  ✓ Created: {job['filename']}")

    print(f"\n{'='*70}")
    print(f"✅ Dataset Generation Complete!")
    print(f"   Resumes: {resumes_dir.absolute()} (35 files)")
    print(f"   Job Descriptions: {jobs_dir.absolute()} (6 files)")
    print(f"\n💡 Next steps:")
    print(f"   1. Install dependencies: pip install -r requirements.txt")
    print(f"   2. Process resumes: python3 resume_rag.py --directory resumes_extended --clear")
    print(f"   3. Match jobs: python3 job_matcher.py --job-file job_descriptions/senior_python_engineer.txt")
    print(f"{'='*70}")


if __name__ == "__main__":
    main()
