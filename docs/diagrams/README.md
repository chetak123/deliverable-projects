# LearnTrack Diagrams

This folder contains visual diagrams that illustrate the architecture and design of the LearnTrack application.

## Available Diagrams

### 1. [Architecture Diagram](Architecture.md)
Shows the layered architecture of the application:
- UI Layer (Main.java)
- Service Layer (Business Logic)
- Repository Layer (Data Storage)
- Entity Layer (Domain Models)
- Support Components (Utilities, Constants, Enums, Exceptions)

**View this diagram to understand:**
- How different layers interact
- The flow of data through the application
- Separation of concerns

### 2. [OOP Concepts Diagram](OOP_Concepts.md)
Illustrates the key Object-Oriented Programming principles used in the project:
- Inheritance (Person → Student)
- Encapsulation (Private fields, Public methods)
- Polymorphism (Method overriding)
- Static Members (IdGenerator)
- Exception Handling (Custom exceptions)
- Collections (ArrayList usage)

**View this diagram to understand:**
- How OOP principles are applied
- Code examples for each concept
- Design decisions

### 3. [Class Diagram](Class_Diagram.md)
Detailed UML-style class diagram showing:
- All classes with their fields and methods
- Relationships between classes (inheritance, composition, dependencies)
- Enums and their values
- Exception hierarchy

**View this diagram to understand:**
- Complete class structure
- Method signatures
- Class relationships

## How to View These Diagrams

### On GitHub
GitHub automatically renders Mermaid diagrams in markdown files. Simply click on any of the diagram files above to view them.

### In VS Code
Install the "Markdown Preview Mermaid Support" extension to view diagrams directly in VS Code.

### Online Mermaid Editor
1. Copy the Mermaid code from any diagram file
2. Go to [Mermaid Live Editor](https://mermaid.live/)
3. Paste the code to view and edit the diagram
4. Export as PNG or SVG if needed

### Convert to Images

You can convert these Mermaid diagrams to images using:

#### Using Mermaid CLI:
```bash
# Install mermaid-cli
npm install -g @mermaid-js/mermaid-cli

# Convert to PNG
mmdc -i Architecture.md -o Architecture.png
mmdc -i OOP_Concepts.md -o OOP_Concepts.png
mmdc -i Class_Diagram.md -o Class_Diagram.png
```

#### Using Online Tools:
1. [Mermaid Live Editor](https://mermaid.live/) - Copy code, export as PNG/SVG
2. [Kroki](https://kroki.io/) - API for diagram generation
3. [Mermaid Chart](https://www.mermaidchart.com/) - Online editor with export options

## Diagram Legend

### Colors Used

#### Architecture Diagram:
- 🔵 **Blue** (#e1f5ff): UI Layer
- 🟡 **Yellow** (#fff4e1): Service Layer
- 🟢 **Green** (#e8f5e9): Repository Layer
- 🟣 **Purple** (#f3e5f5): Entity Layer

#### OOP Concepts Diagram:
- 🟣 **Purple** (#e1bee7, #ce93d8): Inheritance
- 🟠 **Orange** (#ffccbc, #ff8a65): Encapsulation
- 🟢 **Green** (#c5e1a5, #9ccc65): Polymorphism
- 🔵 **Blue** (#b3e5fc, #4fc3f7): Static Members
- 🟡 **Yellow** (#fff9c4, #fff59d): Exception Handling
- 🔴 **Pink** (#f8bbd0, #f48fb1, #f06292): Collections

### Relationship Types

- **Solid Arrow (→)**: Direct dependency or usage
- **Dotted Arrow (-.->)**: Inheritance or conceptual relationship
- **Dashed Line**: Weak dependency or throws relationship

## Contributing

If you'd like to add more diagrams or improve existing ones:
1. Use Mermaid syntax for consistency
2. Add clear descriptions and legends
3. Update this README with the new diagram information

## Resources

- [Mermaid Documentation](https://mermaid.js.org/)
- [Mermaid Syntax Guide](https://mermaid.js.org/intro/syntax-reference.html)
- [GitHub Mermaid Support](https://github.blog/2022-02-14-include-diagrams-markdown-files-mermaid/)

