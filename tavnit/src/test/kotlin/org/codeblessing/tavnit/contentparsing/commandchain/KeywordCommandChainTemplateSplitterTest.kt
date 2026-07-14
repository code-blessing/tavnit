package org.codeblessing.tavnit.contentparsing.commandchain

import org.codeblessing.tavnit.contentparsing.TemplateParsingErrorCode
import org.codeblessing.tavnit.contentparsing.TemplateParsingException
import org.codeblessing.tavnit.contentparsing.resolver.TemplateContentPart
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class KeywordCommandChainTemplateSplitterTest {

    private fun descriptionOf(
        className: String = "MyTemplateRendererClass",
        packageName: String = "org.example.template",
        templateChain: List<TemplateContentPart> = emptyList(),
    ): TemplateRendererDescription = TemplateRendererDescription(
        templateRendererClass = ClassDescription(className, packageName),
        templateRendererInterface = null,
        modelClasses = emptyList(),
        templateChain = templateChain,
    )

    private fun textPart(text: String): TemplateContentPart =
        ContentPartBuilder.create().addText(text).build().single()

    @Nested
    inner class EmptyAndIgnoredContent {

        @Test
        fun `empty list returns empty list`() {
            val input = ContentPartBuilder.create().build()

            val result = KeywordCommandChainTemplateSplitter.splitIntoTemplateRendererDescriptions(input)

            assertEquals(emptyList<TemplateRendererDescription>(), result)
        }

        @Test
        fun `content before first TEMPLATE_RENDERER is ignored`() {
            val input = ContentPartBuilder.create()
                .addText("ignored leading text")
                .addTemplateComment()
                    .addTemplateRendererCommand()
                    .end()
                .addTemplateComment()
                    .addEndTemplateRendererCommand()
                    .end()
                .build()

            val result = KeywordCommandChainTemplateSplitter.splitIntoTemplateRendererDescriptions(input)

            assertEquals(listOf(descriptionOf()), result)
        }

        @Test
        fun `content after last END_TEMPLATE_RENDERER is ignored`() {
            val input = ContentPartBuilder.create()
                .addTemplateComment()
                    .addTemplateRendererCommand()
                    .end()
                .addTemplateComment()
                    .addEndTemplateRendererCommand()
                    .end()
                .addText("ignored trailing text")
                .build()

            val result = KeywordCommandChainTemplateSplitter.splitIntoTemplateRendererDescriptions(input)

            assertEquals(listOf(descriptionOf()), result)
        }

        @Test
        fun `leading and trailing content is both ignored`() {
            val input = ContentPartBuilder.create()
                .addText("leading")
                .addTemplateComment()
                    .addTemplateRendererCommand()
                    .end()
                .addText("inside")
                .addTemplateComment()
                    .addEndTemplateRendererCommand()
                    .end()
                .addText("trailing")
                .build()

            val result = KeywordCommandChainTemplateSplitter.splitIntoTemplateRendererDescriptions(input)

            assertEquals(listOf(descriptionOf(templateChain = listOf(textPart("inside")))), result)
        }
    }

    @Nested
    inner class SingleTemplateRenderer {

        @Test
        fun `single TEMPLATE_RENDERER block with no content creates one description with empty chain`() {
            val input = ContentPartBuilder.create()
                .addTemplateComment()
                    .addTemplateRendererCommand()
                    .end()
                .addTemplateComment()
                    .addEndTemplateRendererCommand()
                    .end()
                .build()

            val result = KeywordCommandChainTemplateSplitter.splitIntoTemplateRendererDescriptions(input)

            assertEquals(listOf(descriptionOf()), result)
        }

        @Test
        fun `single TEMPLATE_RENDERER block with text content includes text in template chain`() {
            val input = ContentPartBuilder.create()
                .addTemplateComment()
                    .addTemplateRendererCommand()
                    .end()
                .addText("hello world")
                .addTemplateComment()
                    .addEndTemplateRendererCommand()
                    .end()
                .build()

            val result = KeywordCommandChainTemplateSplitter.splitIntoTemplateRendererDescriptions(input)

            assertEquals(listOf(descriptionOf(templateChain = listOf(textPart("hello world")))), result)
        }

        @Test
        fun `single TEMPLATE_RENDERER block reads class name and package from command attributes`() {
            val input = ContentPartBuilder.create()
                .addTemplateComment()
                    .addTemplateRendererCommand("CustomRenderer", "com.example.custom")
                    .end()
                .addTemplateComment()
                    .addEndTemplateRendererCommand()
                    .end()
                .build()

            val result = KeywordCommandChainTemplateSplitter.splitIntoTemplateRendererDescriptions(input)

            assertEquals(
                listOf(descriptionOf(className = "CustomRenderer", packageName = "com.example.custom")),
                result,
            )
        }

        @Test
        fun `TEMPLATE_RENDERER block with a model creates model description`() {
            val input = ContentPartBuilder.create()
                .addTemplateComment()
                    .addTemplateRendererCommandWithTemplateModel()
                        .addTemplateModel("model", "MyModel", "org.model")
                    .end()
                    .end()
                .addTemplateComment()
                    .addEndTemplateRendererCommand()
                    .end()
                .build()

            val result = KeywordCommandChainTemplateSplitter.splitIntoTemplateRendererDescriptions(input)

            val expected = TemplateRendererDescription(
                templateRendererClass = ClassDescription("MyTemplateRendererClass", "org.example.template"),
                templateRendererInterface = null,
                modelClasses = listOf(
                    ModelDescription(
                        modelClassDescription = ClassDescription("MyModel", "org.model"),
                        modelName = "model",
                        isList = false,
                    )
                ),
                templateChain = emptyList(),
            )
            assertEquals(listOf(expected), result)
        }

        @Test
        fun `TEMPLATE_RENDERER and END_TEMPLATE_RENDERER comment parts are filtered from template chain`() {
            val input = ContentPartBuilder.create()
                .addTemplateComment()
                    .addTemplateRendererCommand()
                    .end()
                .addTemplateComment()
                    .addIfCommand()
                    .end()
                .addTemplateComment()
                    .addEndIfCommand()
                    .end()
                .addTemplateComment()
                    .addEndTemplateRendererCommand()
                    .end()
                .build()

            val result = KeywordCommandChainTemplateSplitter.splitIntoTemplateRendererDescriptions(input)

            val expectedChain = ContentPartBuilder.create()
                .addTemplateComment()
                    .addIfCommand()
                    .end()
                .addTemplateComment()
                    .addEndIfCommand()
                    .end()
                .build()
            assertEquals(listOf(descriptionOf(templateChain = expectedChain)), result)
        }
    }

    @Nested
    inner class MultipleTopLevelRenderers {

        @Test
        fun `two consecutive top-level TEMPLATE_RENDERER blocks create two TemplateRendererDescriptions`() {
            val input = ContentPartBuilder.create()
                .addTemplateComment()
                    .addTemplateRendererCommand("FirstRenderer", "org.first")
                    .end()
                .addText("first content")
                .addTemplateComment()
                    .addEndTemplateRendererCommand()
                    .end()
                .addTemplateComment()
                    .addTemplateRendererCommand("SecondRenderer", "org.second")
                    .end()
                .addText("second content")
                .addTemplateComment()
                    .addEndTemplateRendererCommand()
                    .end()
                .build()

            val result = KeywordCommandChainTemplateSplitter.splitIntoTemplateRendererDescriptions(input)

            assertEquals(
                listOf(
                    descriptionOf("FirstRenderer", "org.first", listOf(textPart("first content"))),
                    descriptionOf("SecondRenderer", "org.second", listOf(textPart("second content"))),
                ),
                result,
            )
        }
    }

    @Nested
    inner class NestedTemplateRenderers {

        @Test
        fun `nested TEMPLATE_RENDERER block is removed from outer chain and returned as separate description`() {
            val input = ContentPartBuilder.create()
                .addTemplateComment()
                    .addTemplateRendererCommand("OuterRenderer", "org.outer")
                    .end()
                .addText("outer content")
                .addTemplateComment()
                    .addTemplateRendererCommand("InnerRenderer", "org.inner")
                    .end()
                .addText("inner content")
                .addTemplateComment()
                    .addEndTemplateRendererCommand()
                    .end()
                .addTemplateComment()
                    .addEndTemplateRendererCommand()
                    .end()
                .build()

            val result = KeywordCommandChainTemplateSplitter.splitIntoTemplateRendererDescriptions(input)

            assertEquals(
                listOf(
                    descriptionOf("OuterRenderer", "org.outer", listOf(textPart("outer content"))),
                    descriptionOf("InnerRenderer", "org.inner", listOf(textPart("inner content"))),
                ),
                result,
            )
        }

        @Test
        fun `two nested TEMPLATE_RENDERER blocks in same outer block are each returned as separate description`() {
            val input = ContentPartBuilder.create()
                .addTemplateComment()
                    .addTemplateRendererCommand("OuterRenderer", "org.outer")
                    .end()
                .addTemplateComment()
                    .addTemplateRendererCommand("FirstInner", "org.first")
                    .end()
                .addTemplateComment()
                    .addEndTemplateRendererCommand()
                    .end()
                .addTemplateComment()
                    .addTemplateRendererCommand("SecondInner", "org.second")
                    .end()
                .addTemplateComment()
                    .addEndTemplateRendererCommand()
                    .end()
                .addTemplateComment()
                    .addEndTemplateRendererCommand()
                    .end()
                .build()

            val result = KeywordCommandChainTemplateSplitter.splitIntoTemplateRendererDescriptions(input)

            assertEquals(
                listOf(
                    descriptionOf("OuterRenderer", "org.outer"),
                    descriptionOf("FirstInner", "org.first"),
                    descriptionOf("SecondInner", "org.second"),
                ),
                result,
            )
        }

        @Test
        fun `deeply nested TEMPLATE_RENDERER blocks are all returned as separate descriptions depth-first`() {
            val input = ContentPartBuilder.create()
                .addTemplateComment()
                    .addTemplateRendererCommand("Level1", "org.level1")
                    .end()
                .addText("level1 content")
                .addTemplateComment()
                    .addTemplateRendererCommand("Level2", "org.level2")
                    .end()
                .addText("level2 content")
                .addTemplateComment()
                    .addTemplateRendererCommand("Level3", "org.level3")
                    .end()
                .addText("level3 content")
                .addTemplateComment()
                    .addEndTemplateRendererCommand()
                    .end()
                .addTemplateComment()
                    .addEndTemplateRendererCommand()
                    .end()
                .addTemplateComment()
                    .addEndTemplateRendererCommand()
                    .end()
                .build()

            val result = KeywordCommandChainTemplateSplitter.splitIntoTemplateRendererDescriptions(input)

            assertEquals(
                listOf(
                    descriptionOf("Level1", "org.level1", listOf(textPart("level1 content"))),
                    descriptionOf("Level2", "org.level2", listOf(textPart("level2 content"))),
                    descriptionOf("Level3", "org.level3", listOf(textPart("level3 content"))),
                ),
                result,
            )
        }
    }

    @Nested
    inner class Errors {

        @Test
        fun `unclosed TEMPLATE_RENDERER throws TemplateParsingException`() {
            val input = ContentPartBuilder.create()
                .addTemplateComment()
                    .addTemplateRendererCommand()
                    .end()
                .addText("content without closing")
                .build()

            val exception = assertThrows(TemplateParsingException::class.java) {
                KeywordCommandChainTemplateSplitter.splitIntoTemplateRendererDescriptions(input)
            }
            assertEquals(TemplateParsingErrorCode.TEMPLATE_RENDERER_BLOCK_NOT_CLOSED, exception.errorCode)
        }

        @Test
        fun `outer TEMPLATE_RENDERER unclosed after inner block closes throws TemplateParsingException`() {
            val input = ContentPartBuilder.create()
                .addTemplateComment()
                    .addTemplateRendererCommand("OuterRenderer", "org.outer")
                    .end()
                .addTemplateComment()
                    .addTemplateRendererCommand("InnerRenderer", "org.inner")
                    .end()
                .addTemplateComment()
                    .addEndTemplateRendererCommand()
                    .end()
                // Missing END_TEMPLATE_RENDERER for outer
                .build()

            val exception = assertThrows(TemplateParsingException::class.java) {
                KeywordCommandChainTemplateSplitter.splitIntoTemplateRendererDescriptions(input)
            }
            assertEquals(TemplateParsingErrorCode.TEMPLATE_RENDERER_BLOCK_NOT_CLOSED, exception.errorCode)
        }
    }
}
