package logic

import java.nio.file.Files

class ScriptExecutor {
    // Data class representing the result of script execution
    data class ExecutionResult(val output: String, val error: String, val exitCode: Int)

    // Generate a human-readable status message from execution result
    fun getStatusMessage(result: ExecutionResult): String {
        return if (result.exitCode == 0) "Success" else "Failed (exit code: ${result.exitCode})"
    }

    // Execute the provided script content and handle output/errors via callbacks
    fun execute(
        scriptContent: String,
        onOutput: (String) -> Unit = {},
        onError: (String) -> Unit = {}
    ): ExecutionResult {
        val tempFile = Files.createTempFile("script", ".kts").toFile()
        try {
            // Write script content to a temporary file
            tempFile.writeText(scriptContent)

            // Determine command based on operating system
            val isWindows = System.getProperty("os.name").lowercase().contains("win")
            val command = if (isWindows) listOf("cmd", "/c", "kotlinc", "-script", tempFile.absolutePath)
            else listOf("kotlinc", "-script", tempFile.absolutePath)

            // Start the compilation and execution process
            val process = ProcessBuilder(command)
                .start()

            val outputStringBuilder = StringBuilder()
            val errorStringBuilder = StringBuilder()

            // Thread to process standard output stream
            val outputThread = Thread {
                process.inputStream.use { input ->
                    val buffer = ByteArray(1024)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        val text = String(buffer, 0, bytesRead)
                        outputStringBuilder.append(text)
                        onOutput(text)
                    }
                }
            }

            // Thread to process error stream
            val errorThread = Thread {
                process.errorStream.use { input ->
                    val buffer = ByteArray(1024)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        val text = String(buffer, 0, bytesRead)
                        errorStringBuilder.append(text)
                        onError(text)
                    }
                }
            }

            // Execute processing threads
            outputThread.start()
            errorThread.start()

            // Wait for process completion and threads to finish
            process.waitFor()
            val exitCode = process.exitValue()
            outputThread.join()
            errorThread.join()

            return ExecutionResult(outputStringBuilder.toString(), errorStringBuilder.toString(), exitCode)
        } catch (e: Exception) {
            // Handle execution exceptions
            return ExecutionResult("", e.message ?: "Unknown error occurred", -1)
        } finally {
            // Ensure temporary file cleanup
            if (tempFile.exists()) {
                tempFile.delete()
            }
        }
    }
}
