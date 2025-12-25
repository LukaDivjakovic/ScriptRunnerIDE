# Script Runner IDE

A lightweight, Swing-based Integrated Development Environment (IDE) for writing and executing Kotlin scripts (`.kts`). This tool provides real-time feedback, syntax highlighting, and interactive error navigation.

## Features

- **Dynamic Output Streaming**: See the output of your scripts in real-time as they execute, without waiting for the process to finish.
- **Syntax Highlighting**: Core Kotlin keywords are highlighted in the editor for better readability.
- **Interactive Error Navigation**: Compilation errors in the error pane are clickable. Clicking an error link will jump directly to the problematic line and column in the editor.
- **Responsive UI**: Long-running scripts won't freeze the application, as execution happens in a background thread.
- **Maximized Start**: The IDE automatically launches in a maximized window to provide a full-screen workspace.

## Requirements

To run this project, you need the following installed on your system:

1.  **JDK 11 or higher**: Required for running the IDE and the Kotlin compiler.
2.  **Kotlin Compiler (`kotlinc`)**: The system must have `kotlinc` available in the `PATH`. The IDE uses this to execute the scripts.
3.  **Gradle**: Used for building and managing dependencies.

## How to Run

### From the command line

1.  Clone or download the project to your local machine.
2.  Navigate to the project root directory.
3.  Run the application using Gradle:

    ```bash
    ./gradlew run
    ```

    (On Windows, use `gradlew.bat run`)

### Using an IDE (like IntelliJ IDEA)

1.  Open the project in IntelliJ IDEA.
2.  Wait for Gradle to sync dependencies.
3.  Run the `main` function located in `src/main/kotlin/Main.kt`.

## Project Structure

- `src/main/kotlin/Main.kt`: Entry point of the application.
- `src/main/java/forms/MainScreen.java`: The main UI implementation using Swing.
- `src/main/kotlin/logic/`: Contains the core logic of the IDE.
    - `ScriptExecutor.kt`: Handles the execution of Kotlin scripts as external processes.
    - `KeywordProcessor.kt`: Manages syntax highlighting logic.
    - `ErrorLinkProcessor.kt`: Parses compiler output to create clickable links.
- `build.gradle.kts`: Gradle build configuration.

## Usage

1.  Type your Kotlin code into the editor pane on the left.
2.  Click the **Run** button.
3.  View the standard output in the top-right pane and errors in the bottom-right pane.
4.  If an error occurs, click on the highlighted file path in the error pane to navigate to the source of the error.
