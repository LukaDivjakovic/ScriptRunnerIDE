package logic

data class Keywords @JvmOverloads constructor(val words: Set<String> = setOf("fun", "val", "var", "if", "else", "while", "for", "return"))
