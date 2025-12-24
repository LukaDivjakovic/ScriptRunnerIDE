package logic

data class KeywordMatch(val start: Int, val length: Int)

class KeywordProcessor(private val keywords: Keywords) {
    fun processLine(lineText: String, lineOffset: Int): List<KeywordMatch> {
        val matches = mutableListOf<KeywordMatch>()
        val wordsInLine = lineText.split(Regex("\\W+"))
        
        var currentIndex = 0
        for (word in wordsInLine) {
            if (word.isEmpty()) continue
            
            val wordStart = lineText.indexOf(word, currentIndex)
            if (keywords.words.contains(word)) {
                matches.add(KeywordMatch(lineOffset + wordStart, word.length))
            }
            currentIndex = wordStart + word.length
        }
        
        return matches
    }
}
