package logic

import kotlin.test.Test
import kotlin.test.assertEquals

class ErrorLinkProcessorTest {
    // Instance of the processor to be tested
    private val processor = ErrorLinkProcessor()

    @Test
    fun testFindLinks() {
        // Verify that a single error link can be correctly identified
        val text = "script123.kts:10:5: error: message"
        val links = processor.findLinks(text)
        assertEquals(1, links.size)
        assertEquals(0, links[0].start)
        assertEquals(19, links[0].end)
        assertEquals(10, links[0].line)
        assertEquals(5, links[0].column)
    }

    @Test
    fun testFindMultipleLinks() {
        // Verify that multiple error links across lines are correctly identified
        val text = "script123.kts:10:5: error: first\nscript456.kts:20:8: error: second"
        val links = processor.findLinks(text)
        assertEquals(2, links.size)
        
        assertEquals(10, links[0].line)
        assertEquals(5, links[0].column)
        
        assertEquals(20, links[1].line)
        assertEquals(8, links[1].column)
    }

    @Test
    fun testNoLinks() {
        // Verify that no links are found in text without the expected format
        val text = "some random error message"
        val links = processor.findLinks(text)
        assertEquals(0, links.size)
    }
}
