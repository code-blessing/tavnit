package org.codeblessing.tavnit.contentparsing

import org.codeblessing.tavnit.contentparsing.linenumbers.LineNumbers
import org.codeblessing.tavnit.contentparsing.linenumbers.LineNumbers.Companion.EMPTY_LINE_NUMBERS

class TemplateParsingException(
    val lineNumbers: LineNumbers = EMPTY_LINE_NUMBERS,
    val errorCode: TemplateParsingErrorCode,
    val msg: String,
    cause: Exception? = null
): RuntimeException(msg, cause) {
    companion object {
        fun <T> reThrowWithLineNumbers(lineNumbers: LineNumbers, block: () -> T): T {
            try {
                return block()
            } catch (e: TemplateParsingException) {
                throw TemplateParsingException(lineNumbers = lineNumbers, errorCode = e.errorCode, msg = e.msg, cause = e)
            }
        }
    }
}


