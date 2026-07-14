package org.codeblessing.tavnit.templaterenderer

import org.codeblessing.tavnit.CommentStyles.KOTLIN_COMMENT_STYLES
import org.codeblessing.tavnit.RelativeFile
import org.codeblessing.tavnit.TavnitException
import org.codeblessing.tavnit.contentparsing.ContentParser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class AddImportToRendererTest {

    @Test
    fun `two imports with the same simple name from different packages fail with a clear error`() {
        val contentToParse = """
            /* @tt{{{
                @template-renderer [ templateRendererClassName="MyRenderer" templateRendererPackageName="examples" ]
                @add-import-to-renderer [ importClassName="Foo" importPackageName="first.pkg" ]
                                        [ importClassName="Foo" importPackageName="second.pkg" ]
            }}}@ */
            const val x = ""
        """.trimIndent()

        val exception = assertThrows<TavnitException> {
            renderClassContent(contentToParse)
        }
        val message = requireNotNull(exception.message)
        assertTrue(message.contains("simple name 'Foo'"), "unexpected message: $message")
        assertTrue(message.contains("first.pkg.Foo") && message.contains("second.pkg.Foo"), "unexpected message: $message")
    }

    @Test
    fun `two imports with the same simple name and identical package produce a single import`() {
        val contentToParse = """
            /* @tt{{{
                @template-renderer [ templateRendererClassName="MyRenderer" templateRendererPackageName="examples" ]
                @add-import-to-renderer [ importClassName="Foo" importPackageName="same.pkg" ]
                                        [ importClassName="Foo" importPackageName="same.pkg" ]
            }}}@ */
            const val x = ""
        """.trimIndent()

        val classContent = renderClassContent(contentToParse)

        assertEquals(
            1,
            Regex("^import same\\.pkg\\.Foo$", RegexOption.MULTILINE).findAll(classContent).count(),
            "expected exactly one import, but was:\n$classContent",
        )
    }

    @Test
    fun `add-import-to-renderer adds the imports to the generated renderer class`() {
        val contentToParse = """
            /* @tt{{{
                @template-renderer [ templateRendererClassName="MyRenderer" templateRendererPackageName="examples" ]
                @add-import-to-renderer [ importClassName="DayOfWeek.WEDNESDAY" importPackageName="java.time" ]
                                        [ importClassName="MyConstant" importPackageName="examples.constants" ]
            }}}@ */
            const val day = "DayOfWeek.WEDNESDAY"
        """.trimIndent()

        val classContent = renderClassContent(contentToParse)

        assertTrue(
            classContent.contains("import java.time.DayOfWeek.WEDNESDAY"),
            "expected the qualified import to be present, but was:\n$classContent",
        )
        assertTrue(
            classContent.contains("import examples.constants.MyConstant"),
            "expected the second import to be present, but was:\n$classContent",
        )
    }

    @Test
    fun `add-import-to-renderer without package name imports the class name as-is`() {
        val contentToParse = """
            /* @tt{{{
                @template-renderer [ templateRendererClassName="MyRenderer" templateRendererPackageName="examples" ]
                @add-import-to-renderer [ importClassName="examples.already.Qualified" ]
            }}}@ */
            const val x = ""
        """.trimIndent()

        val classContent = renderClassContent(contentToParse)

        assertTrue(
            classContent.contains("import examples.already.Qualified"),
            "expected the unqualified import to be present, but was:\n$classContent",
        )
    }

    private fun renderClassContent(contentToParse: String): String {
        val templates = ContentParser.parseContent(content = contentToParse, KOTLIN_COMMENT_STYLES)
        assertEquals(1, templates.size)
        val template = templates.single()
        val filepath = RelativeFile.fromRelativeString("dummy-dir/dummy.txt")
        val methodContent = TemplateRendererContentCreator.createMultilineStringTemplateContent(filepath, template)
        return TemplateRendererClassContentCreator.wrapInKotlinClassContent(filepath, template, methodContent)
    }
}
