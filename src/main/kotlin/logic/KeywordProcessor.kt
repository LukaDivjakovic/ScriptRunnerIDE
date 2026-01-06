package logic

// Represent a range of text that matches a keyword
data class KeywordMatch(val start: Int, val length: Int)

class KeywordProcessor(private val keywords: Keywords) {
    // Identify keywords in a line of text and return their positions
    fun processLine(lineText: String, lineOffset: Int): List<KeywordMatch> {
        val matches = mutableListOf<KeywordMatch>()
        // Split line into words using non-word characters as delimiters
        val wordsInLine = lineText.split(Regex("\\W+"))
        
        var currentIndex = 0
        for (word in wordsInLine) {
            // Skip empty strings resulting from delimiters
            if (word.isEmpty()) continue
            
            // Find the starting index of the word in the line
            val wordStart = lineText.indexOf(word, currentIndex)
            // Check if the word is a known Kotlin keyword
            if (keywords.words.contains(word)) {
                matches.add(KeywordMatch(lineOffset + wordStart, word.length))
            }
            currentIndex = wordStart + word.length
        }
        
        return matches
    }
}
