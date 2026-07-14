package org.codeblessing.tavnit.templaterenderer

import org.codeblessing.tavnit.CommentStyles.KOTLIN_COMMENT_STYLES
import org.codeblessing.tavnit.RelativeFile
import org.codeblessing.tavnit.contentparsing.ContentParser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class KotlinSpecialCharacterEscapingTest {

    private val tripleQuote = "\"\"\""

    @Test
    fun `three double quotes in template text are escaped in the generated renderer`() {
        val contentToParse = """
            /* @tt{{{
                @template-renderer [ templateRendererClassName="MyRenderer" templateRendererPackageName="examples" ]
            }}}@ */
            val doc = ${tripleQuote}raw text$tripleQuote
        """.trimIndent()

        val classContent = renderClassContent(contentToParse)

        val expectedLine = "val doc = ${expectedEscapedQuoteRun(3)}raw text${expectedEscapedQuoteRun(3)}"
        assertTrue(
            classContent.contains(expectedLine),
            "expected the quote runs to be escaped, but was:\n$classContent",
        )
        assertFalse(
            classContent.contains("${tripleQuote}raw text"),
            "expected no unescaped quote run in the template text, but was:\n$classContent",
        )
    }

    @Test
    fun `a run of more than three double quotes is escaped as a whole`() {
        val fiveQuotes = "\"".repeat(5)
        val contentToParse = """
            /* @tt{{{
                @template-renderer [ templateRendererClassName="MyRenderer" templateRendererPackageName="examples" ]
            }}}@ */
            quotes: $fiveQuotes end
        """.trimIndent()

        val classContent = renderClassContent(contentToParse)

        assertTrue(
            classContent.contains("quotes: ${expectedEscapedQuoteRun(5)} end"),
            "expected the whole quote run to be escaped, but was:\n$classContent",
        )
    }

    @Test
    fun `dollar sign in print-text is escaped in the generated renderer`() {
        val contentToParse = """
            /* @tt{{{
                @template-renderer [ templateRendererClassName="MyRenderer" templateRendererPackageName="examples" ]
                @print-text [ text="price: ${'$'}total" ]
            }}}@ */
            some text
        """.trimIndent()

        val classContent = renderClassContent(contentToParse)

        assertTrue(
            classContent.contains("price: ${expectedEscapedDollar()}total"),
            "expected the dollar sign to be escaped, but was:\n$classContent",
        )
    }

    @Test
    fun `double quotes in print-text are escaped in the generated renderer`() {
        val contentToParse = """
            /* @tt{{{
                @template-renderer [ templateRendererClassName="MyRenderer" templateRendererPackageName="examples" ]
                @print-text [ text="quoted \"\"\" text" ]
            }}}@ */
            some text
        """.trimIndent()

        val classContent = renderClassContent(contentToParse)

        assertTrue(
            classContent.contains("quoted ${expectedEscapedQuoteRun(3)} text"),
            "expected the quote run in print-text to be escaped, but was:\n$classContent",
        )
    }

    @Test
    fun `special characters in the template file path are escaped in the generated filePath function`() {
        val contentToParse = """
            /* @tt{{{
                @template-renderer [ templateRendererClassName="MyRenderer" templateRendererPackageName="examples" ]
            }}}@ */
            some text
        """.trimIndent()

        val classContent = renderClassContent(contentToParse, relativeFilePath = "dummy-dir/my${'$'}file.txt")

        assertTrue(
            classContent.contains("return \"dummy-dir/my\\\$file.txt\""),
            "expected the dollar sign in the file path to be escaped, but was:\n$classContent",
        )
    }

    @Test
    fun `dollar sign in replace-value-by-value is escaped in the generated renderer`() {
        val contentToParse = """
            /* @tt{{{
                @template-renderer [ templateRendererClassName="MyRenderer" templateRendererPackageName="examples" ]
                @replace-value-by-value [ searchValue="PRICE" replaceByValue="${'$'}total" ]
            }}}@ */
            PRICE
        """.trimIndent()

        val classContent = renderClassContent(contentToParse)

        assertTrue(
            classContent.contains("${expectedEscapedDollar()}total"),
            "expected the dollar sign in the replacement value to be escaped, but was:\n$classContent",
        )
        assertFalse(
            classContent.contains("\$total"),
            "expected no unescaped dollar sign in the replacement value, but was:\n$classContent",
        )
    }

    @Test
    fun `three double quotes in replace-value-by-value are escaped in the generated renderer`() {
        val contentToParse = """
            /* @tt{{{
                @template-renderer [ templateRendererClassName="MyRenderer" templateRendererPackageName="examples" ]
                @replace-value-by-value [ searchValue="PRICE" replaceByValue="a \"\"\" b" ]
            }}}@ */
            PRICE
        """.trimIndent()

        val classContent = renderClassContent(contentToParse)

        assertTrue(
            classContent.contains("a ${expectedEscapedQuoteRun(3)} b"),
            "expected the quote run in the replacement value to be escaped, but was:\n$classContent",
        )
    }

    @Test
    fun `a search value containing a dollar sign still matches the escaped template text`() {
        val contentToParse = """
            /* @tt{{{
                @template-renderer [ templateRendererClassName="MyRenderer" templateRendererPackageName="examples" ]
                @replace-value-by-value [ searchValue="${'$'}PRICE" replaceByValue="cost" ]
            }}}@ */
            ${'$'}PRICE
        """.trimIndent()

        val classContent = renderClassContent(contentToParse)

        assertTrue(
            classContent.contains("cost"),
            "expected the dollar-prefixed search value to be replaced, but was:\n$classContent",
        )
        assertFalse(
            classContent.contains("${expectedEscapedDollar()}PRICE"),
            "expected the search value to be consumed by the replacement, but was:\n$classContent",
        )
    }

    @Test
    fun `a leading UTF-8 BOM is stripped and does not leak into a renderer spanning the file start`() {
        val bom = "\uFEFF"
        val template = """
            /* @tt{{{
                @move-comment-backward
                @template-renderer [ templateRendererClassName="MyRenderer" templateRendererPackageName="examples" ]
            }}}@ */val x = 1
        """.trimIndent()

        val classContentWithBom = renderClassContent(bom + template)

        assertFalse(
            classContentWithBom.contains(bom),
            "expected the BOM to be stripped, but it was present in:\n$classContentWithBom",
        )
        assertEquals(
            renderClassContent(template),
            classContentWithBom,
            "a leading BOM must not affect the generated renderer",
        )
    }

    /** The generated escape for a run of double quotes, e.g. `${"\"\"\""}` for a run of three. */
    private fun expectedEscapedQuoteRun(length: Int): String =
        "\${\"" + "\\\"".repeat(length) + "\"}"

    /** The generated escape for a dollar sign: `${"$"}` */
    private fun expectedEscapedDollar(): String = "\${\"\$\"}"

    private fun renderClassContent(contentToParse: String, relativeFilePath: String = "dummy-dir/dummy.txt"): String {
        val templates = ContentParser.parseContent(content = contentToParse, KOTLIN_COMMENT_STYLES)
        assertEquals(1, templates.size)
        val template = templates.single()
        val filepath = RelativeFile.fromRelativeString(relativeFilePath)
        val methodContent = TemplateRendererContentCreator.createMultilineStringTemplateContent(filepath, template)
        return TemplateRendererClassContentCreator.wrapInKotlinClassContent(filepath, template, methodContent)
    }
}
