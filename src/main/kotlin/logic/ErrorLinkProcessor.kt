package logic

// Represent a clickable link in the error output
data class ErrorLink(val start: Int, val end: Int, val line: Int, val column: Int)

class ErrorLinkProcessor {
    // Regex for matching Kotlin compiler error messages with file, line, and column
    private val regex = Regex("""(.*?\.kts):(\d+):(\d+):""")

    // Extract clickable error links from the provided text
    fun findLinks(text: String): List<ErrorLink> {
        return regex.findAll(text).map { matchResult ->
            // Parse line and column numbers from the match
            val line = matchResult.groupValues[2].toInt()
            val column = matchResult.groupValues[3].toInt()
            ErrorLink(matchResult.range.first, matchResult.range.last + 1, line, column)
        }.toList()
    }
}
