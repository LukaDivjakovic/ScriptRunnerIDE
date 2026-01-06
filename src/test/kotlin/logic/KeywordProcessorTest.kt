package logic

import kotlin.test.Test
import kotlin.test.assertEquals

class KeywordProcessorTest {
    // Keywords and processor instances for testing
    private val keywords = Keywords()
    private val processor = KeywordProcessor(keywords)

    @Test
    fun testProcessLineWithKeywords() {
        // Verify that a single keyword in a line is correctly identified
        val line = "val x = 10"
        val matches = processor.processLine(line, 0)
        assertEquals(1, matches.size)
        assertEquals(0, matches[0].start)
        assertEquals(3, matches[0].length)
    }

    @Test
    fun testProcessLineWithMultipleKeywords() {
        // Verify that multiple keywords in a line are correctly identified
        val line = "fun main() { val x = 10 }"
        val matches = processor.processLine(line, 0)
        assertEquals(2, matches.size)
        
        assertEquals(0, matches[0].start)
        assertEquals(3, matches[0].length) 
        
        assertEquals(13, matches[1].start)
        assertEquals(3, matches[1].length) 
    }

    @Test
    fun testProcessLineWithOffset() {
        // Verify that keyword positions are correctly offset
        val line = "val x = 10"
        val offset = 100
        val matches = processor.processLine(line, offset)
        assertEquals(1, matches.size)
        assertEquals(100, matches[0].start)
    }

    @Test
    fun testProcessLineNoKeywords() {
        // Verify that lines without keywords result in no matches
        val line = "x = y + z"
        val matches = processor.processLine(line, 0)
        assertEquals(0, matches.size)
    }
}
