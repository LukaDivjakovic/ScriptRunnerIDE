package logic

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScriptExecutorTest {
    // Instance of the executor to be tested
    private val executor = ScriptExecutor()

    @Test
    fun testExecuteSuccess() {
        // Verify that a valid script executes successfully and returns expected output
        val script = "println(\"Hello, World!\")"
        val result = executor.execute(script)
        assertEquals(0, result.exitCode)
        assertTrue(result.output.contains("Hello, World!"))
        assertTrue(result.error.isEmpty())
    }

    @Test
    fun testExecuteError() {
        // Verify that an invalid script returns a non-zero exit code and error message
        val script = "this is not valid kotlin"
        val result = executor.execute(script)
        assertTrue(result.exitCode != 0)
        assertTrue(result.error.isNotEmpty())
    }

    @Test
    fun testGetStatusMessage() {
        // Verify status message generation for both success and failure results
        val successResult = ScriptExecutor.ExecutionResult("out", "", 0)
        assertEquals("Success", executor.getStatusMessage(successResult))

        val failResult = ScriptExecutor.ExecutionResult("", "err", 1)
        assertEquals("Failed (exit code: 1)", executor.getStatusMessage(failResult))
    }
}
