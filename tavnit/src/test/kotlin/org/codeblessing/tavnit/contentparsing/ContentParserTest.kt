package org.codeblessing.tavnit.contentparsing

import org.codeblessing.tavnit.CommentStyles.HTML_COMMENT_STYLES
import org.codeblessing.tavnit.CommentStyles.SCSS_COMMENT_STYLES
import org.codeblessing.tavnit.TavnitException
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.nio.charset.StandardCharsets
import kotlin.test.assertEquals

class ContentParserTest {
    @Test
    fun `parse the template of a example html file should render the template`() {
        val resourcePath = "/org/codeblessing/tavnit/contentparsing/my-address-form.html"
        val resource = requireNotNull(this.javaClass.getResourceAsStream(resourcePath)) {
            "Resource $resourcePath not found"
        }

        val htmlContent = resource.readBytes().toString(StandardCharsets.UTF_8)

        val templates = ContentParser.parseContent(htmlContent, HTML_COMMENT_STYLES)
        assertEquals(1, templates.size)
    }

    @Test
    fun `parse the template of a empty file should not fail`() {
        val emptyContent = ""

        val templates = ContentParser.parseContent(emptyContent, SCSS_COMMENT_STYLES)
        assertEquals(0, templates.size)
    }

    @Test
    fun `parse the template of a file with empty comment styles should not fail`() {
        val myContent = "foo bar"

        val templates = ContentParser.parseContent(myContent, emptyList())
        assertEquals(0, templates.size)
    }

    @Test
    fun `an unclosed foreach reports the line number of the opening command`() {
        val content = """
            <p>some text</p>
            <!-- @tt{{{ @foreach [ iteratorExpression="items" loopVariable="item" ] }}}@ -->
            <p>more text</p>
        """.trimIndent()

        val exception = assertThrows<TavnitException> {
            ContentParser.parseContent(content, HTML_COMMENT_STYLES)
        }
        val message = requireNotNull(exception.message)
        assertTrue(message.contains("Lines 2-2"), "expected the line of the opening command, but was: $message")
        assertFalse(message.contains("<no line numbers available>"), "expected line numbers, but was: $message")
    }

    @Test
    fun `an unterminated tavnit comment start marker fails with a clear error`() {
        val content = """
            <!-- @tt{{{ @remark [ text="valid comment" ] }}}@ -->
            <p>some text</p>
            <!-- @tt{{{ @foreach [ iteratorExpression="items" loopVariable="item" ]
        """.trimIndent()

        val exception = assertThrows<TavnitException> {
            ContentParser.parseContent(content, HTML_COMMENT_STYLES)
        }
        val message = requireNotNull(exception.message)
        assertTrue(message.contains("missing its end marker"), "expected an unterminated comment error, but was: $message")
        assertTrue(message.contains("Lines 3-3"), "expected the line of the start marker, but was: $message")
    }

    @Test
    fun `a file without tavnit comments that mentions the start marker is ignored`() {
        val content = """val marker = "@tt{{{""""

        val templates = ContentParser.parseContent(content, SCSS_COMMENT_STYLES)
        assertEquals(0, templates.size)
    }

}
