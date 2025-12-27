package logic

import kotlin.test.Test
import kotlin.test.assertEquals

class KeywordProcessorTest {
    private val keywords = Keywords()
    private val processor = KeywordProcessor(keywords)

    @Test
    fun testProcessLineWithKeywords() {
        val line = "val x = 10"
        val matches = processor.processLine(line, 0)
        assertEquals(1, matches.size)
        assertEquals(0, matches[0].start)
        assertEquals(3, matches[0].length)
    }

    @Test
    fun testProcessLineWithMultipleKeywords() {
        val line = "fun main() { val x = 10 }"
        val matches = processor.processLine(line, 0)
        assertEquals(2, matches.size)
        
        assertEquals(0, matches[0].start)
        assertEquals(3, matches[0].length) // fun
        
        assertEquals(13, matches[1].start)
        assertEquals(3, matches[1].length) // val
    }

    @Test
    fun testProcessLineWithOffset() {
        val line = "val x = 10"
        val offset = 100
        val matches = processor.processLine(line, offset)
        assertEquals(1, matches.size)
        assertEquals(100, matches[0].start)
    }

    @Test
    fun testProcessLineNoKeywords() {
        val line = "x = y + z"
        val matches = processor.processLine(line, 0)
        assertEquals(0, matches.size)
    }
}
