package org.codeblessing.tavnit.templaterenderer

import org.codeblessing.tavnit.CommentStyles.KOTLIN_COMMENT_STYLES
import org.codeblessing.tavnit.RelativeFile
import org.codeblessing.tavnit.contentparsing.ContentParser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class OptionalPackageNameTest {

    @Test
    fun `renderer without package name generates no package line`() {
        val contentToParse = """
            /* @tt{{{
                @template-renderer [ templateRendererClassName="MyRenderer" ]
            }}}@ */
            some text
        """.trimIndent()

        val classContent = renderClassContent(contentToParse)

        assertFalse(
            classContent.contains("package "),
            "expected no package line for a renderer without package name, but was:\n$classContent",
        )
        assertTrue(
            classContent.contains("object MyRenderer"),
            "expected the renderer class declaration, but was:\n$classContent",
        )
    }

    @Test
    fun `model without package name generates no import`() {
        val contentToParse = """
            /* @tt{{{
                @template-renderer [ templateRendererClassName="MyRenderer" templateRendererPackageName="examples" ]
                                    [ modelName="model" modelClassName="MyModel" ]
            }}}@ */
            some text
        """.trimIndent()

        val classContent = renderClassContent(contentToParse)

        assertFalse(
            classContent.contains("import ."),
            "expected no import with an empty package prefix, but was:\n$classContent",
        )
        assertFalse(
            classContent.contains("import MyModel"),
            "expected no import for a model in the renderer package, but was:\n$classContent",
        )
        assertTrue(
            classContent.contains("renderTemplate(model: MyModel)"),
            "expected the model to be used with its simple name, but was:\n$classContent",
        )
    }

    @Test
    fun `interface without package name generates no import`() {
        val contentToParse = """
            /* @tt{{{
                @template-renderer [ templateRendererClassName="MyRenderer" templateRendererPackageName="examples" templateRendererInterfaceName="MyRendererInterface" ]
            }}}@ */
            some text
        """.trimIndent()

        val classContent = renderClassContent(contentToParse)

        assertFalse(
            classContent.contains("import ."),
            "expected no import with an empty package prefix, but was:\n$classContent",
        )
        assertTrue(
            classContent.contains("object MyRenderer : MyRendererInterface"),
            "expected the interface to be used with its simple name, but was:\n$classContent",
        )
    }

    @Test
    fun `model with package name still generates the import`() {
        val contentToParse = """
            /* @tt{{{
                @template-renderer [ templateRendererClassName="MyRenderer" templateRendererPackageName="examples" ]
                                    [ modelName="model" modelClassName="MyModel" modelPackageName="examples.model" ]
            }}}@ */
            some text
        """.trimIndent()

        val classContent = renderClassContent(contentToParse)

        assertTrue(
            classContent.contains("import examples.model.MyModel"),
            "expected the qualified model import, but was:\n$classContent",
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
