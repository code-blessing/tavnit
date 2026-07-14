package org.codeblessing.tavnit.contentparsing.linenumbers

import org.codeblessing.tavnit.CommentStyles.KOTLIN_COMMENT_STYLES
import org.codeblessing.tavnit.contentparsing.tokenizer.ContentType
import org.codeblessing.tavnit.contentparsing.tokenizer.FileContentTokenizer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Verifies that line numbers computed from the real tokenizer output point to the
 * correct source lines.
 */
class LineNumberIntegrationTest {

    @Test
    fun `line numbers are not shifted by preceding line comments`() {
        val content = "code line 1\n" +
                "// @tt{{{ @remark [ text=\"one\" ] }}}@\n" +
                "code line 3\n" +
                "code line 4\n" +
                "// @tt{{{ @remark [ text=\"two\" ] }}}@"

        val lineNumbers = lineNumbersOfComment(content, commentIndex = 1)

        assertEquals(5, lineNumbers.startLineNumber)
        assertEquals(5, lineNumbers.endLineNumber)
    }

    @Test
    fun `identical comments get their own line numbers`() {
        val content = "// @tt{{{ @remark [ text=\"same\" ] }}}@\n" +
                "// @tt{{{ @remark [ text=\"same\" ] }}}@"

        val lineNumbers = lineNumbersOfComment(content, commentIndex = 1)

        assertEquals(2, lineNumbers.startLineNumber)
    }

    @Test
    fun `carriage return line breaks are counted`() {
        val content = "code line 1\rcode line 2\r// @tt{{{ @remark [ text=\"x\" ] }}}@"

        val lineNumbers = lineNumbersOfComment(content, commentIndex = 0)

        assertEquals(3, lineNumbers.startLineNumber)
    }

    @Test
    fun `windows line breaks are counted once`() {
        val content = "code line 1\r\ncode line 2\r\n// @tt{{{ @remark [ text=\"x\" ] }}}@"

        val lineNumbers = lineNumbersOfComment(content, commentIndex = 0)

        assertEquals(3, lineNumbers.startLineNumber)
    }

    private fun lineNumbersOfComment(content: String, commentIndex: Int): LineNumbers {
        val allParts = FileContentTokenizer.tokenizeContent(content, KOTLIN_COMMENT_STYLES)
        val commentPart = allParts.filter { it.contentType == ContentType.TEMPLATE_COMMENT }[commentIndex]
        return LineNumberCalculator.calculateLineNumbers(commentPart, allParts)
    }
}
