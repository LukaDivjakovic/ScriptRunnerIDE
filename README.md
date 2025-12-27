# Script Runner IDE

A modern, lightweight, Swing-based Integrated Development Environment (IDE) for writing and executing Kotlin scripts (`.kts`). Designed with a professional dark theme and a responsive layout, it provides a seamless experience for quick Kotlin prototyping and experimentation.

## Key Features

- **Modern Dark Interface**: Features a professional dark theme palette inspired by modern IDEs.
- **Dynamic Layout**: A flexible, split-pane layout that allows you to resize the editor, output, and error panels.
- **Real-Time Output Streaming**: See your script's output immediately as it's generated
- **Syntax Highlighting**: Core Kotlin keywords are intelligently highlighted for better code readability.
- **Interactive Error Navigation**: Clickable error links in the output pane jump directly to where an issue occurred.
- **Visual Status Feedback**: A dedicated, color-coded status badge provides clear "READY", "RUNNING", "SUCCESS", and "FAILED" states.
- **Responsive Execution**: Scripts run in background threads, keeping the UI fully interactive even during long-running tasks.

## Requirements

To run this project, you need the following:

1.  **JDK 23**: The project is configured to use Java 23 (via Gradle toolchain).
2.  **Kotlin Compiler (`kotlinc`)**: Ensure `kotlinc` is installed and available in your system's `PATH`.
3.  **Gradle**: Required for building and dependency management.

## Getting Started

### From the Command Line

1.  Clone the repository.
2.  Navigate to the root directory.
3.  Execute using the Gradle wrapper:
    ```bash
    ./gradlew run
    ```
    *(Windows users: use `gradlew.bat run`)*

### Using IntelliJ IDEA

1.  Open the project in IntelliJ IDEA.
2.  Allow Gradle to sync dependencies.
3.  Run the `main` function in `src/main/kotlin/Main.kt`.

## Usage Guide

1.  **Write Code**: Enter your Kotlin script in the editor (left pane).
2.  **Execute**: Click the prominent green **RUN** button in the top toolbar.
3.  **Monitor Status**: Check the status badge for execution progress and results.
4.  **Analyze Output**: Standard output appears in the top-right pane; errors appear in the bottom-right.
5.  **Debug**: Click on underlined error paths to navigate directly to the problematic code line.

## Project Structure

- `src/main/kotlin/Main.kt`: Application entry point.
- `src/main/java/forms/MainScreen.java`: Main UI implementation and component styling.
- `src/main/kotlin/logic/`:
    - `ScriptExecutor.kt`: Manages script execution processes.
    - `KeywordProcessor.kt`: Handles syntax highlighting logic.
    - `ErrorLinkProcessor.kt`: Processes compiler output for interactive links.
- `build.gradle.kts`: Project build and dependency configuration.
