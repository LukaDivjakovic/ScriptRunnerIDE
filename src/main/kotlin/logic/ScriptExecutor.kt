package logic

import java.io.File
import java.io.InputStream
import java.nio.file.Files
import java.util.concurrent.TimeUnit

class ScriptExecutor {
    data class ExecutionResult(val output: String, val error: String, val exitCode: Int)

    fun getStatusMessage(result: ExecutionResult): String {
        return if (result.exitCode == 0) "Success" else "Failed (exit code: ${result.exitCode})"
    }

    fun execute(
        scriptContent: String,
        onOutput: (String) -> Unit = {},
        onError: (String) -> Unit = {}
    ): ExecutionResult {
        val tempFile = Files.createTempFile("script", ".kts").toFile()
        try {
            tempFile.writeText(scriptContent)

            val isWindows = System.getProperty("os.name").lowercase().contains("win")
            val command = if (isWindows) listOf("cmd", "/c", "kotlinc", "-script", tempFile.absolutePath)
            else listOf("kotlinc", "-script", tempFile.absolutePath)

            val process = ProcessBuilder(command)
                .start()

            val outputStringBuilder = StringBuilder()
            val errorStringBuilder = StringBuilder()

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

            outputThread.start()
            errorThread.start()

            process.waitFor()
            val exitCode = process.exitValue()
            outputThread.join()
            errorThread.join()

            return ExecutionResult(outputStringBuilder.toString(), errorStringBuilder.toString(), exitCode)
        } catch (e: Exception) {
            return ExecutionResult("", e.message ?: "Unknown error occurred", -1)
        } finally {
            if (tempFile.exists()) {
                tempFile.delete()
            }
        }
    }
}
