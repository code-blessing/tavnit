package org.codeblessing.tavnit.contentparsing.linenumbers

import org.codeblessing.tavnit.contentparsing.tokenizer.FileContentTokenizer
import org.codeblessing.tavnit.contentparsing.tokenizer.RawContentPart

object LineNumberCalculator {

    fun calculateLineNumbers(
        contentPart: RawContentPart,
        allContentParts: List<RawContentPart>
    ): LineNumbers {

        var previousContentPartEndLineNumber = 0

        for(currentContentPart in allContentParts) {
            // compare by identity: parts with equal content may occur more than once
            if(currentContentPart !== contentPart) {
                previousContentPartEndLineNumber += currentContentPart.pristineContent.countLines()
            } else {
                break
            }
        }

        val contentPartStartLineNumber = previousContentPartEndLineNumber + 1
        val contentPartEndLineNumber = (previousContentPartEndLineNumber + contentPart.pristineContent.countLines())
            .coerceAtLeast(contentPartStartLineNumber)

        return LineNumbers(
            startLineNumber = contentPartStartLineNumber,
            endLineNumber = contentPartEndLineNumber,
            context = contentPart.pristineContent,
            formattedDescription = "Lines ${contentPartStartLineNumber}-${contentPartEndLineNumber}: '${contentPart.pristineContent}'",
        )
    }

    private val lineBreakPattern = Regex(FileContentTokenizer.ALL_LINE_BREAKS)

    private fun String.countLines(): Int {
        return lineBreakPattern.findAll(this).count()
    }
}
