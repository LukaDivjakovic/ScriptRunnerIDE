package logic

data class ErrorLink(val start: Int, val end: Int, val line: Int, val column: Int)

class ErrorLinkProcessor {
    private val regex = Regex("""(.*?\.kts):(\d+):(\d+):""")

    fun findLinks(text: String): List<ErrorLink> {
        return regex.findAll(text).map { matchResult ->
            val line = matchResult.groupValues[2].toInt()
            val column = matchResult.groupValues[3].toInt()
            ErrorLink(matchResult.range.first, matchResult.range.last + 1, line, column)
        }.toList()
    }
}
