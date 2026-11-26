ONLINE EXAMINATION SYSTEM - MINI PROJECT
Technologies: HTML, CSS, Java (built-in HttpServer; no external libraries)\n
REQUIREMENTS
- JDK 17 or newer recommended
- Windows / macOS / Linux

HOW TO RUN (Windows)
1. Extract this ZIP folder.
2. Open the OnlineExaminationSystem folder in VS Code or File Explorer.
3. Open Terminal/CMD in this folder (the folder containing src and web).
4. Compile:
   javac -d out src\ExamServer.java
5. Start:
   java -cp out ExamServer
6. Open browser:
   http://localhost:8080

HOW TO RUN (Linux/macOS)
From the project root:
  javac -d out src/ExamServer.java
  java -cp out ExamServer
Then browse to http://localhost:8080

FEATURES
- Student name entry
- Five MCQ questions
- 5-minute countdown
- Auto-submit when timer ends
- Instant score and percentage
- Retake option

DEMO NOTE
This is a simple mini-project demonstration. Login only checks that a name is entered; it is not a production authentication system. Questions and answer keys are stored in the Java source for easy beginner study.
