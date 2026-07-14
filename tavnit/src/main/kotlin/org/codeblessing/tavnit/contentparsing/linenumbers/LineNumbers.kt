package org.codeblessing.tavnit.contentparsing.linenumbers

data class LineNumbers(
    val startLineNumber: Int,
    val endLineNumber: Int,
    val context: String,
    val formattedDescription: String,
) {
    companion object {
        val EMPTY_LINE_NUMBERS = LineNumbers(
            startLineNumber = 0,
            endLineNumber = 0,
            context = "",
            formattedDescription = "<no line numbers available>",
        )
    }
}

