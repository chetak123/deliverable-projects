# Setup Instructions

## Prerequisites

### Java Development Kit (JDK)
- **JDK Version Used**: JDK 11 or higher (Recommended: JDK 17 LTS)
- **Download Link**: [Oracle JDK](https://www.oracle.com/java/technologies/downloads/) or [OpenJDK](https://adoptium.net/)

## Installation Steps

### 1. Install JDK

#### On Windows:
1. Download the JDK installer from the link above
2. Run the installer and follow the installation wizard
3. Set the `JAVA_HOME` environment variable:
   - Right-click on 'This PC' → Properties → Advanced System Settings
   - Click 'Environment Variables'
   - Add new System Variable: `JAVA_HOME` = `C:\Program Files\Java\jdk-17` (adjust path as needed)
   - Add to PATH: `%JAVA_HOME%\bin`

#### On macOS:
```bash
# Using Homebrew
brew install openjdk@17

# Add to PATH (add to ~/.zshrc or ~/.bash_profile)
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
export PATH=$JAVA_HOME/bin:$PATH
```

#### On Linux:
```bash
# Ubuntu/Debian
sudo apt update
sudo apt install openjdk-17-jdk

# Verify installation
java -version
javac -version
```

### 2. Verify Installation

Open a terminal/command prompt and run:
```bash
java -version
javac -version
```

You should see output similar to:
```
java version "17.0.x" 2023-xx-xx LTS
Java(TM) SE Runtime Environment (build 17.0.x+xx-LTS-xxx)
Java HotSpot(TM) 64-Bit Server VM (build 17.0.x+xx-LTS-xxx, mixed mode, sharing)
```

## Compiling and Running LearnTrack

### Using Command Line

#### 1. Navigate to the project directory:
```bash
cd /path/to/deliverable-projects
```

#### 2. Compile all Java files:
```bash
# Compile from the project root
javac -d out src/com/airtribe/learntrack/**/*.java src/com/airtribe/learntrack/*.java
```

#### 3. Run the application:
```bash
# Run from the project root
java -cp out com.airtribe.learntrack.Main
```

### Using an IDE (Recommended)

#### IntelliJ IDEA:
1. Open IntelliJ IDEA
2. Click "Open" and select the project directory
3. Wait for the IDE to index the project
4. Right-click on `Main.java` → Run 'Main.main()'

#### Eclipse:
1. Open Eclipse
2. File → Import → Existing Projects into Workspace
3. Select the project directory
4. Right-click on `Main.java` → Run As → Java Application

#### VS Code:
1. Install the "Extension Pack for Java" from the marketplace
2. Open the project folder
3. Open `Main.java`
4. Click the "Run" button above the `main` method

## Hello World Test

To verify your Java setup, create a simple test file:

**HelloWorld.java:**
```java
public class HelloWorld {
    public static void main(String[] args) {
        System.out.println("Hello, World!");
    }
}
```

**Compile and Run:**
```bash
javac HelloWorld.java
java HelloWorld
```

**Expected Output:**
```
Hello, World!
```

## Troubleshooting

### Common Issues:

1. **"javac is not recognized as an internal or external command"**
   - Solution: Ensure JAVA_HOME is set correctly and added to PATH

2. **"Error: Could not find or load main class"**
   - Solution: Make sure you're running from the correct directory and the classpath is set properly

3. **"UnsupportedClassVersionError"**
   - Solution: Your Java version is too old. Upgrade to JDK 11 or higher

## Next Steps

Once your environment is set up:
1. Read `JVM_Basics.md` to understand how Java works
2. Read `Design_Notes.md` to understand the project architecture
3. Run the LearnTrack application and explore its features!

