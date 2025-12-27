package logic

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScriptExecutorTest {
    private val executor = ScriptExecutor()

    @Test
    fun testExecuteSuccess() {
        val script = "println(\"Hello, World!\")"
        val result = executor.execute(script)
        assertEquals(0, result.exitCode)
        assertTrue(result.output.contains("Hello, World!"))
        assertTrue(result.error.isEmpty())
    }

    @Test
    fun testExecuteError() {
        val script = "this is not valid kotlin"
        val result = executor.execute(script)
        assertTrue(result.exitCode != 0)
        assertTrue(result.error.isNotEmpty())
    }

    @Test
    fun testGetStatusMessage() {
        val successResult = ScriptExecutor.ExecutionResult("out", "", 0)
        assertEquals("Success", executor.getStatusMessage(successResult))

        val failResult = ScriptExecutor.ExecutionResult("", "err", 1)
        assertEquals("Failed (exit code: 1)", executor.getStatusMessage(failResult))
    }
}
