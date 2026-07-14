package org.codeblessing.tavnit.contentparsing.resolver

import org.codeblessing.tavnit.CommandAttributeKey
import org.codeblessing.tavnit.CommandKey
import org.codeblessing.tavnit.contentparsing.TemplateParsingErrorCode
import org.codeblessing.tavnit.contentparsing.TemplateParsingException
import org.codeblessing.tavnit.contentparsing.commentparser.CommandStructure
import org.codeblessing.tavnit.contentparsing.commentparser.TemplateCommentParser
import org.codeblessing.tavnit.contentparsing.linenumbers.LineNumbers
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class KeywordCommandFactoryTest {

    @Test
    fun `valid command content part is created`() {
        val commandStructure = createSingleTemplateComment(
            comment = """ 
                        @template-renderer [
                            templateRendererClassName="MyTemplate"
                            templateRendererPackageName="org.codeblessing.tavnit.examples"
                        ]
            """.trimIndent()
        )

        val keywordCommand = KeywordCommandFactory.createKeywordCommand(commandStructure, stubLineNumbers)
        Assertions.assertEquals(CommandKey.TEMPLATE_RENDERER, keywordCommand.commandKey)
        Assertions.assertEquals(
            "MyTemplate",
            keywordCommand.attribute(CommandAttributeKey.TEMPLATE_RENDERER_CLASS_NAME)
        )
        Assertions.assertEquals(
            "org.codeblessing.tavnit.examples",
            keywordCommand.attribute(CommandAttributeKey.TEMPLATE_RENDERER_PACKAGE_NAME)
        )
    }

    @Test
    fun `throws for unknown keyword`() {
        val commandStructure = createSingleTemplateComment(
            comment = """
                        @unknown
            """.trimIndent()
        )

        val exception = assertThrows<TemplateParsingException> {
            KeywordCommandFactory.createKeywordCommand(commandStructure, stubLineNumbers)
        }
        Assertions.assertEquals(TemplateParsingErrorCode.UNKNOWN_KEYWORD, exception.errorCode)
    }

    @Test
    fun `throws for too few attribute groups`() {
        val commandStructure = createSingleTemplateComment(
            comment = """
                        @template-renderer
            """.trimIndent()
        )

        val exception = assertThrows<TemplateParsingException> {
            KeywordCommandFactory.createKeywordCommand(commandStructure, stubLineNumbers)
        }
        Assertions.assertEquals(TemplateParsingErrorCode.TOO_FEW_ATTRIBUTE_GROUPS, exception.errorCode)
    }

    @Test
    fun `valid template-renderer with renderer and model attribute groups`() {
        val commandStructure = createSingleTemplateComment(
            comment = """
                        @template-renderer [
                            templateRendererClassName="MyTemplate"
                            templateRendererPackageName="org.codeblessing.tavnit.examples"
                        ][
                            modelName="myModel"
                            modelClassName="MyModelClass"
                        ]
            """.trimIndent()
        )

        val keywordCommand = KeywordCommandFactory.createKeywordCommand(commandStructure, stubLineNumbers)
        Assertions.assertEquals(CommandKey.TEMPLATE_RENDERER, keywordCommand.commandKey)
        Assertions.assertEquals(2, keywordCommand.attributeGroups.size)
        Assertions.assertEquals("MyTemplate", keywordCommand.attribute(0, CommandAttributeKey.TEMPLATE_RENDERER_CLASS_NAME))
        Assertions.assertEquals("myModel", keywordCommand.attribute(1, CommandAttributeKey.TEMPLATE_MODEL_NAME))
    }

    @Test
    fun `throws when model attribute group contains renderer attributes`() {
        val commandStructure = createSingleTemplateComment(
            comment = """
                        @template-renderer [
                            templateRendererClassName="MyTemplate"
                            templateRendererPackageName="org.codeblessing.tavnit.examples"
                        ][
                            templateRendererClassName="MyTemplate"
                        ]
            """.trimIndent()
        )

        val exception = assertThrows<TemplateParsingException> {
            KeywordCommandFactory.createKeywordCommand(commandStructure, stubLineNumbers)
        }
        Assertions.assertEquals(TemplateParsingErrorCode.ATTRIBUTE_KEY_NOT_ALLOWED, exception.errorCode)
    }

    @Test
    fun `throws for unknown attribute key`() {
        val commandStructure = createSingleTemplateComment(
            comment = """
                        @template-renderer [
                            templateRendererClassNameUnknown="MyTemplate"
                            templateRendererPackageName="org.codeblessing.tavnit.examples"
                        ]
            """.trimIndent()
        )

        val exception = assertThrows<TemplateParsingException> {
            KeywordCommandFactory.createKeywordCommand(commandStructure, stubLineNumbers)
        }
        Assertions.assertEquals(TemplateParsingErrorCode.UNKNOWN_ATTRIBUTE_KEY, exception.errorCode)
    }

    @Test
    fun `throws for unallowed attribute key`() {
        val commandStructure = createSingleTemplateComment(
            comment = """
                        @template-renderer [
                            templateRendererClassName="MyTemplate"
                            templateRendererPackageName="org.codeblessing.tavnit.examples"
                            replacement="foo"
                        ]
            """.trimIndent()
        )

        val exception = assertThrows<TemplateParsingException> {
            KeywordCommandFactory.createKeywordCommand(commandStructure, stubLineNumbers)
        }
        Assertions.assertEquals(TemplateParsingErrorCode.UNKNOWN_ATTRIBUTE_KEY, exception.errorCode)
    }

    @Test
    fun `throws for unallowed attribute value`() {
        val commandStructure = createSingleTemplateComment(
            comment = """
                        @template-renderer [
                            templateRendererClassName="MyTemplate"
                            templateRendererPackageName="org.codeblessing.tavnit.examples"
                        ][
                            modelName="myTemplateModel"
                            modelClassName="MyTemplateModel"
                            modelPackageName="org.codeblessing.tavnit.examples"
                            isList="foo"
                        ]
            """.trimIndent()
        )

        val exception = assertThrows<TemplateParsingException> {
            KeywordCommandFactory.createKeywordCommand(commandStructure, stubLineNumbers)
        }
        Assertions.assertEquals(TemplateParsingErrorCode.ATTRIBUTE_VALUE_NOT_ALLOWED, exception.errorCode)
    }

    @Test
    fun `throws for blank attribute value when not allowed`() {
        val commandStructure = createSingleTemplateComment(
            comment = """
                        @template-renderer [
                            templateRendererClassName=""
                            templateRendererPackageName="org.codeblessing.tavnit.examples"
                        ]
            """.trimIndent()
        )

        val exception = assertThrows<TemplateParsingException> {
            KeywordCommandFactory.createKeywordCommand(commandStructure, stubLineNumbers)
        }
        Assertions.assertEquals(TemplateParsingErrorCode.BLANK_ATTRIBUTE_VALUE, exception.errorCode)
    }

    @Test
    fun `throws for missing required attribute`() {
        val commandStructure = createSingleTemplateComment(
            comment = """
                        @template-renderer [
                            templateRendererPackageName="org.codeblessing.tavnit.examples"
                        ]
            """.trimIndent()
        )

        val exception = assertThrows<TemplateParsingException> {
            KeywordCommandFactory.createKeywordCommand(commandStructure, stubLineNumbers)
        }
        Assertions.assertEquals(TemplateParsingErrorCode.MISSING_REQUIRED_ATTRIBUTES, exception.errorCode)
    }

    @Test
    fun `valid command with no attribute constraints`() {
        val commandStructure = createSingleTemplateComment(
            comment = "@end-template-renderer"
        )

        val keywordCommand = KeywordCommandFactory.createKeywordCommand(commandStructure, stubLineNumbers)
        Assertions.assertEquals(CommandKey.END_TEMPLATE_RENDERER, keywordCommand.commandKey)
        Assertions.assertTrue(keywordCommand.attributeGroups.isEmpty())
    }

    @Test
    fun `valid MANY attribute group command with single group`() {
        val commandStructure = createSingleTemplateComment(
            comment = """
                @replace-value-by-expression [
                    searchValue="old"
                    replaceByExpression="new"
                ]
            """.trimIndent()
        )

        val keywordCommand = KeywordCommandFactory.createKeywordCommand(commandStructure, stubLineNumbers)
        Assertions.assertEquals(CommandKey.REPLACE_VALUE_BY_EXPRESSION, keywordCommand.commandKey)
        Assertions.assertEquals("old", keywordCommand.attribute(CommandAttributeKey.SEARCH_VALUE))
        Assertions.assertEquals("new", keywordCommand.attribute(CommandAttributeKey.REPLACE_BY_EXPRESSION))
    }

    @Test
    fun `valid MANY attribute group command with multiple groups`() {
        val commandStructure = createSingleTemplateComment(
            comment = """
                @replace-value-by-expression [
                    searchValue="old"
                    replaceByExpression="new"
                ][
                    searchValue="old2"
                    replaceByExpression="new2"
                ]
            """.trimIndent()
        )

        val keywordCommand = KeywordCommandFactory.createKeywordCommand(commandStructure, stubLineNumbers)
        Assertions.assertEquals(CommandKey.REPLACE_VALUE_BY_EXPRESSION, keywordCommand.commandKey)
        Assertions.assertEquals(2, keywordCommand.attributeGroups.size)
        Assertions.assertEquals("old", keywordCommand.attribute(0, CommandAttributeKey.SEARCH_VALUE))
        Assertions.assertEquals("new", keywordCommand.attribute(0, CommandAttributeKey.REPLACE_BY_EXPRESSION))
        Assertions.assertEquals("old2", keywordCommand.attribute(1, CommandAttributeKey.SEARCH_VALUE))
        Assertions.assertEquals("new2", keywordCommand.attribute(1, CommandAttributeKey.REPLACE_BY_EXPRESSION))
    }

    @Test
    fun `valid multi-constraint command with one repeatable group`() {
        val commandStructure = createSingleTemplateComment(
            comment = """
                @render-template [
                    templateRendererClassName="MyRenderer"
                ][
                    modelName="myModel"
                    modelExpression="modelVar"
                ]
            """.trimIndent()
        )

        val keywordCommand = KeywordCommandFactory.createKeywordCommand(commandStructure, stubLineNumbers)
        Assertions.assertEquals(CommandKey.RENDER_TEMPLATE, keywordCommand.commandKey)
        Assertions.assertEquals(2, keywordCommand.attributeGroups.size)
        Assertions.assertEquals("MyRenderer", keywordCommand.attribute(0, CommandAttributeKey.TEMPLATE_RENDERER_CLASS_NAME))
        Assertions.assertEquals("myModel", keywordCommand.attribute(1, CommandAttributeKey.TEMPLATE_MODEL_NAME))
        Assertions.assertEquals("modelVar", keywordCommand.attribute(1, CommandAttributeKey.MODEL_EXPRESSION))
    }

    @Test
    fun `valid multi-constraint command with extra MANY groups`() {
        val commandStructure = createSingleTemplateComment(
            comment = """
                @render-template [
                    templateRendererClassName="MyRenderer"
                ][
                    modelName="model"
                    modelExpression="modelVar"
                ][
                    modelName="model2"
                    modelExpression="modelVar2"
                ]
            """.trimIndent()
        )

        val keywordCommand = KeywordCommandFactory.createKeywordCommand(commandStructure, stubLineNumbers)
        Assertions.assertEquals(CommandKey.RENDER_TEMPLATE, keywordCommand.commandKey)
        Assertions.assertEquals(3, keywordCommand.attributeGroups.size)
    }

    @Test
    fun `valid render-template with only renderer group and no model groups`() {
        // A template-renderer may declare zero models, so render-template must be able to call
        // such a renderer with no model groups at all.
        val commandStructure = createSingleTemplateComment(
            comment = """
                @render-template [
                    templateRendererClassName="MyRenderer"
                ]
            """.trimIndent()
        )

        val keywordCommand = KeywordCommandFactory.createKeywordCommand(commandStructure, stubLineNumbers)
        Assertions.assertEquals(CommandKey.RENDER_TEMPLATE, keywordCommand.commandKey)
        Assertions.assertEquals(1, keywordCommand.attributeGroups.size)
        Assertions.assertEquals("MyRenderer", keywordCommand.attribute(0, CommandAttributeKey.TEMPLATE_RENDERER_CLASS_NAME))
    }

    @Test
    fun `throws when first group has attributes from second constraint`() {
        val commandStructure = createSingleTemplateComment(
            comment = """
                @render-template [
                    modelName="myModel"
                    modelExpression="modelVar"
                ][
                    templateRendererClassName="MyRenderer"
                ]
            """.trimIndent()
        )

        val exception = assertThrows<TemplateParsingException> {
            KeywordCommandFactory.createKeywordCommand(commandStructure, stubLineNumbers)
        }
        Assertions.assertEquals(TemplateParsingErrorCode.ATTRIBUTE_KEY_NOT_ALLOWED, exception.errorCode)
    }

    @Test
    fun `throws when MANY group has attributes from first constraint`() {
        val commandStructure = createSingleTemplateComment(
            comment = """
                @render-template [
                    templateRendererClassName="MyRenderer"
                ][
                    templateRendererClassName="MyRenderer"
                ]
            """.trimIndent()
        )

        val exception = assertThrows<TemplateParsingException> {
            KeywordCommandFactory.createKeywordCommand(commandStructure, stubLineNumbers)
        }
        Assertions.assertEquals(TemplateParsingErrorCode.ATTRIBUTE_KEY_NOT_ALLOWED, exception.errorCode)
    }

    @Test
    fun `throws for missing required attribute in second attribute group`() {
        val commandStructure = createSingleTemplateComment(
            comment = """
                @render-template [
                    templateRendererClassName="MyRenderer"
                ][
                    modelName="myModel"
                ]
            """.trimIndent()
        )

        val exception = assertThrows<TemplateParsingException> {
            KeywordCommandFactory.createKeywordCommand(commandStructure, stubLineNumbers)
        }
        Assertions.assertEquals(TemplateParsingErrorCode.MISSING_REQUIRED_ATTRIBUTES, exception.errorCode)
    }

    @Test
    fun `valid move-comment-forward without attributes`() {
        val commandStructure = createSingleTemplateComment(
            comment = "@move-comment-forward"
        )

        val keywordCommand = KeywordCommandFactory.createKeywordCommand(commandStructure, stubLineNumbers)
        Assertions.assertEquals(CommandKey.MOVE_COMMENT_FORWARD, keywordCommand.commandKey)
        Assertions.assertTrue(keywordCommand.attributeGroups.isEmpty())
    }

    @Test
    fun `valid move-comment-backward with one occurrence attribute`() {
        val commandStructure = createSingleTemplateComment(
            comment = """
                @move-comment-backward [
                    beforeFirstOccurrenceOf="someText"
                ]
            """.trimIndent()
        )

        val keywordCommand = KeywordCommandFactory.createKeywordCommand(commandStructure, stubLineNumbers)
        Assertions.assertEquals(CommandKey.MOVE_COMMENT_BACKWARD, keywordCommand.commandKey)
        Assertions.assertEquals("someText", keywordCommand.attribute(CommandAttributeKey.BEFORE_FIRST_OCCURRENCE_OF))
    }

    @Test
    fun `throws when move-comment-forward has two mutually exclusive occurrence attributes`() {
        val commandStructure = createSingleTemplateComment(
            comment = """
                @move-comment-forward [
                    beforeFirstOccurrenceOf="someText"
                    afterFirstOccurrenceOf="otherText"
                ]
            """.trimIndent()
        )

        val exception = assertThrows<TemplateParsingException> {
            KeywordCommandFactory.createKeywordCommand(commandStructure, stubLineNumbers)
        }
        Assertions.assertEquals(TemplateParsingErrorCode.MUTUALLY_EXCLUSIVE_ATTRIBUTES, exception.errorCode)
    }

    @Test
    fun `valid remove-blanks-after-comment`() {
        val commandStructure = createSingleTemplateComment(
            comment = """
                @remove-blanks-after-comment
            """.trimIndent()
        )

        val keywordCommand = KeywordCommandFactory.createKeywordCommand(commandStructure, stubLineNumbers)
        Assertions.assertEquals(CommandKey.REMOVE_BLANKS_AFTER_COMMENT, keywordCommand.commandKey)
    }

    @Test
    fun `valid remove-blanks-and-linebreak-before-comment`() {
        val commandStructure = createSingleTemplateComment(
            comment = """
                @remove-blanks-and-linebreak-before-comment
            """.trimIndent()
        )

        val keywordCommand = KeywordCommandFactory.createKeywordCommand(commandStructure, stubLineNumbers)
        Assertions.assertEquals(CommandKey.REMOVE_BLANKS_AND_LINEBREAK_BEFORE_COMMENT, keywordCommand.commandKey)
    }

    @Test
    fun `alias for command without attributes resolves to the same command key`() {
        val commandStructure = createSingleTemplateComment(comment = "@rba")

        val keywordCommand = KeywordCommandFactory.createKeywordCommand(commandStructure, stubLineNumbers)
        Assertions.assertEquals(CommandKey.REMOVE_BLANKS_AFTER_COMMENT, keywordCommand.commandKey)
    }

    @Test
    fun `alias enforces same attribute constraints as keyword`() {
        val commandStructure = createSingleTemplateComment(
            comment = """
                        @mvf [
                            beforeFirstOccurrenceOf="Foo"
                            afterFirstOccurrenceOf="Bar"
                        ]
            """.trimIndent()
        )

        val exception = assertThrows<TemplateParsingException> {
            KeywordCommandFactory.createKeywordCommand(commandStructure, stubLineNumbers)
        }
        Assertions.assertEquals(TemplateParsingErrorCode.MUTUALLY_EXCLUSIVE_ATTRIBUTES, exception.errorCode)
    }

    @Test
    fun `alias rve resolves to replace-value-by-expression`() {
        val commandStructure = createSingleTemplateComment(
            comment = """@re [ searchValue="News" replaceByExpression="model.title" ]"""
        )

        val keywordCommand = KeywordCommandFactory.createKeywordCommand(commandStructure, stubLineNumbers)
        Assertions.assertEquals(CommandKey.REPLACE_VALUE_BY_EXPRESSION, keywordCommand.commandKey)
    }

    @Test
    fun `alias ervv resolves to end-replace-value-by-value`() {
        val commandStructure = createSingleTemplateComment(comment = "@end-rv")

        val keywordCommand = KeywordCommandFactory.createKeywordCommand(commandStructure, stubLineNumbers)
        Assertions.assertEquals(CommandKey.END_REPLACE_VALUE_BY_VALUE, keywordCommand.commandKey)
    }

    private val stubLineNumbers = LineNumbers.EMPTY_LINE_NUMBERS

    private fun createSingleTemplateComment(comment: String): CommandStructure {
        return TemplateCommentParser.parseComment(comment).single()
    }
}
