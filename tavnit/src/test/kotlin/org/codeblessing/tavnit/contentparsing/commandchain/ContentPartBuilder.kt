package org.codeblessing.tavnit.contentparsing.commandchain

import org.codeblessing.tavnit.AttributeGroup
import org.codeblessing.tavnit.AttributeValue
import org.codeblessing.tavnit.CommandAttributeKey
import org.codeblessing.tavnit.CommandKey
import org.codeblessing.tavnit.IsListValue
import org.codeblessing.tavnit.contentparsing.KeywordCommand
import org.codeblessing.tavnit.contentparsing.resolver.TemplateCommentContentPart
import org.codeblessing.tavnit.contentparsing.resolver.TemplateContentPart
import org.codeblessing.tavnit.contentparsing.resolver.TextContentPart
import org.codeblessing.tavnit.contentparsing.linenumbers.LineNumbers

class ContentPartBuilder private constructor() {
    companion object {
        fun create(): ContentPartBuilder = ContentPartBuilder()
    }

    private val contentParts: MutableList<TemplateContentPart> = mutableListOf()

    fun addText(text: String): ContentPartBuilder {
        contentParts.add(TextContentPart(createLineNumbers(), text))
        return this
    }

    fun addTemplateComment(): TemplateCommentBuilder {
        return TemplateCommentBuilder(this)
    }

    internal fun finishTemplateComment(keywordCommands: List<KeywordCommand>) {
        contentParts.add(TemplateCommentContentPart(createLineNumbers(), keywordCommands))
    }

    fun build(): List<TemplateContentPart> = contentParts

    private fun createLineNumbers(): LineNumbers = LineNumbers.EMPTY_LINE_NUMBERS

    class TemplateCommentBuilder(private val contentPartBuilder: ContentPartBuilder) {
        private val keywordCommands: MutableList<KeywordCommand> = mutableListOf()

        internal fun addKeywordCommand(keywordCommand: KeywordCommand): TemplateCommentBuilder {
            keywordCommands.add(keywordCommand)
            return this
        }

        fun createCommand(commandKey: CommandKey): KeywordCommandBuilder {
            return KeywordCommandBuilder(this, commandKey)
        }

        fun end(): ContentPartBuilder {
            contentPartBuilder.finishTemplateComment(keywordCommands)
            return contentPartBuilder
        }

        fun addTemplateRendererCommand(
            templateRendererClassName: String = "MyTemplateRendererClass",
            templateRendererPackageName: String = "org.example.template",
            templateRendererInterfaceName: String? = null,
            templateRendererInterfacePackageName: String? = null,
        ): TemplateCommentBuilder {
            return addTemplateRendererCommandWithTemplateModel(
                templateRendererClassName = templateRendererClassName,
                templateRendererPackageName = templateRendererPackageName,
                templateRendererInterfaceName = templateRendererInterfaceName,
                templateRendererInterfacePackageName = templateRendererInterfacePackageName,
            ).end()
        }

        fun addTemplateRendererCommandWithTemplateModel(
            templateRendererClassName: String = "MyTemplateRendererClass",
            templateRendererPackageName: String = "org.example.template",
            templateRendererInterfaceName: String? = null,
            templateRendererInterfacePackageName: String? = null,
        ): TemplateRendererBuilder {
            return TemplateRendererBuilder(
                this,
                templateRendererClassName,
                templateRendererPackageName,
                templateRendererInterfaceName,
                templateRendererInterfacePackageName,
            )
        }

        fun addReplaceValueByExpressionCommand(
            searchValue: String = "search",
            replaceByExpression: String = "myField",
        ): TemplateCommentBuilder {
            return this.createCommand(CommandKey.REPLACE_VALUE_BY_EXPRESSION)
                .withAttribute(CommandAttributeKey.SEARCH_VALUE, searchValue)
                .withAttribute(CommandAttributeKey.REPLACE_BY_EXPRESSION, replaceByExpression)
                .addCommandToChain()
        }

        fun addReplaceValueByExpressionCommand(
            vararg replacements: Pair<String, String>,
        ): TemplateCommentBuilder {
            var builder = this.createCommand(CommandKey.REPLACE_VALUE_BY_EXPRESSION)
            replacements.forEach { (searchValue, replaceByExpression) ->
                builder = builder
                    .withAttribute(CommandAttributeKey.SEARCH_VALUE, searchValue)
                    .withAttribute(CommandAttributeKey.REPLACE_BY_EXPRESSION, replaceByExpression)
                    .nextAttributeGroup()
            }
            return builder.addCommandToChain()
        }

        fun addEndReplaceValueByExpressionCommand(): TemplateCommentBuilder {
            return this.createCommand(CommandKey.END_REPLACE_VALUE_BY_EXPRESSION).addCommandToChain()
        }

        fun addReplaceValueByValueCommand(
            searchValue: String = "search",
            replaceByValue: String = "replacement",
        ): TemplateCommentBuilder {
            return this.createCommand(CommandKey.REPLACE_VALUE_BY_VALUE)
                .withAttribute(CommandAttributeKey.SEARCH_VALUE, searchValue)
                .withAttribute(CommandAttributeKey.REPLACE_BY_VALUE, replaceByValue)
                .addCommandToChain()
        }

        fun addReplaceValueByValueCommand(
            vararg replacements: Pair<String, String>,
        ): TemplateCommentBuilder {
            var builder = this.createCommand(CommandKey.REPLACE_VALUE_BY_VALUE)
            replacements.forEach { (searchValue, replaceByValue) ->
                builder = builder
                    .withAttribute(CommandAttributeKey.SEARCH_VALUE, searchValue)
                    .withAttribute(CommandAttributeKey.REPLACE_BY_VALUE, replaceByValue)
                    .nextAttributeGroup()
            }
            return builder.addCommandToChain()
        }

        fun addEndReplaceValueByValueCommand(): TemplateCommentBuilder {
            return this.createCommand(CommandKey.END_REPLACE_VALUE_BY_VALUE).addCommandToChain()
        }

        fun addIfCommand(
            conditionExpression: String = "myConditionExpression",
        ): TemplateCommentBuilder {
            return this.createCommand(CommandKey.IF_CONDITION)
                .withAttribute(CommandAttributeKey.CONDITION_EXPRESSION, conditionExpression)
                .addCommandToChain()
        }

        fun addElseIfCommand(
            conditionExpression: String = "myOtherConditionExpression",
        ): TemplateCommentBuilder {
            return this.createCommand(CommandKey.ELSE_IF_CONDITION)
                .withAttribute(CommandAttributeKey.CONDITION_EXPRESSION, conditionExpression)
                .addCommandToChain()
        }

        fun addElseCommand(): TemplateCommentBuilder {
            return this.createCommand(CommandKey.ELSE_CLAUSE).addCommandToChain()
        }

        fun addEndIfCommand(): TemplateCommentBuilder {
            return this.createCommand(CommandKey.END_IF_CONDITION).addCommandToChain()
        }

        fun addForeachCommand(
            loopVariable: String = "item",
            iteratorExpression: String = "myList",
        ): TemplateCommentBuilder {
            return this.createCommand(CommandKey.FOREACH)
                .withAttribute(CommandAttributeKey.LOOP_VARIABLE_NAME, loopVariable)
                .withAttribute(CommandAttributeKey.LOOP_ITERABLE_EXPRESSION, iteratorExpression)
                .addCommandToChain()
        }

        fun addEndForeachCommand(): TemplateCommentBuilder {
            return this.createCommand(CommandKey.END_FOREACH).addCommandToChain()
        }

        fun addIgnoreTextCommand(): TemplateCommentBuilder {
            return this.createCommand(CommandKey.IGNORE_TEXT).addCommandToChain()
        }

        fun addEndIgnoreTextCommand(): TemplateCommentBuilder {
            return this.createCommand(CommandKey.END_IGNORE_TEXT).addCommandToChain()
        }

        fun addEndTemplateRendererCommand(): TemplateCommentBuilder {
            return this.createCommand(CommandKey.END_TEMPLATE_RENDERER).addCommandToChain()
        }

        fun addMoveCommentForwardCommand(
            beforeFirstOccurrenceOf: String? = null,
            afterFirstOccurrenceOf: String? = null,
            beforeLastOccurrenceOf: String? = null,
            afterLastOccurrenceOf: String? = null,
        ): TemplateCommentBuilder = addMoveCommentCommand(
            CommandKey.MOVE_COMMENT_FORWARD,
            beforeFirstOccurrenceOf,
            afterFirstOccurrenceOf,
            beforeLastOccurrenceOf,
            afterLastOccurrenceOf,
        )

        fun addMoveCommentBackwardCommand(
            beforeFirstOccurrenceOf: String? = null,
            afterFirstOccurrenceOf: String? = null,
            beforeLastOccurrenceOf: String? = null,
            afterLastOccurrenceOf: String? = null,
        ): TemplateCommentBuilder = addMoveCommentCommand(
            CommandKey.MOVE_COMMENT_BACKWARD,
            beforeFirstOccurrenceOf,
            afterFirstOccurrenceOf,
            beforeLastOccurrenceOf,
            afterLastOccurrenceOf,
        )

        private fun addMoveCommentCommand(
            commandKey: CommandKey,
            beforeFirstOccurrenceOf: String?,
            afterFirstOccurrenceOf: String?,
            beforeLastOccurrenceOf: String?,
            afterLastOccurrenceOf: String?,
        ): TemplateCommentBuilder {
            var builder = createCommand(commandKey)
            beforeFirstOccurrenceOf?.let { builder = builder.withAttribute(CommandAttributeKey.BEFORE_FIRST_OCCURRENCE_OF, it) }
            afterFirstOccurrenceOf?.let { builder = builder.withAttribute(CommandAttributeKey.AFTER_FIRST_OCCURRENCE_OF, it) }
            beforeLastOccurrenceOf?.let { builder = builder.withAttribute(CommandAttributeKey.BEFORE_LAST_OCCURRENCE_OF, it) }
            afterLastOccurrenceOf?.let { builder = builder.withAttribute(CommandAttributeKey.AFTER_LAST_OCCURRENCE_OF, it) }
            return builder.addCommandToChain()
        }

        fun addRemoveBlanksBeforeCommentCommand(): TemplateCommentBuilder {
            return createCommand(CommandKey.REMOVE_BLANKS_BEFORE_COMMENT).addCommandToChain()
        }

        fun addRemoveBlanksAfterCommentCommand(): TemplateCommentBuilder {
            return createCommand(CommandKey.REMOVE_BLANKS_AFTER_COMMENT).addCommandToChain()
        }

        fun addRemoveBlanksAndLinebreakBeforeCommentCommand(): TemplateCommentBuilder {
            return createCommand(CommandKey.REMOVE_BLANKS_AND_LINEBREAK_BEFORE_COMMENT).addCommandToChain()
        }

        fun addRemoveBlanksAndLinebreakAfterCommentCommand(): TemplateCommentBuilder {
            return createCommand(CommandKey.REMOVE_BLANKS_AND_LINEBREAK_AFTER_COMMENT).addCommandToChain()
        }

        fun addModifyProvidedFilenameByReplacementsCommand(): TemplateCommentBuilder {
            return createCommand(CommandKey.MODIFY_PROVIDED_FILENAME_BY_REPLACEMENTS).addCommandToChain()
        }

        fun addPrintTextCommand(text: String): TemplateCommentBuilder {
            return this.createCommand(CommandKey.PRINT_TEXT)
                .withAttribute(CommandAttributeKey.TEXT, text)
                .addCommandToChain()
        }

        fun addRenderTemplateCommand(
            templateRendererClassName: String = "MyTemplateRendererClass",
            templateRendererPackageName: String? = "org.example.template",
        ): RenderTemplateBuilder {
            return RenderTemplateBuilder(this, templateRendererClassName, templateRendererPackageName)
        }

        class TemplateRendererBuilder(
            private val templateCommentBuilder: TemplateCommentBuilder,
            private val templateRendererClassName: String,
            private val templateRendererPackageName: String,
            private val templateRendererInterfaceName: String?,
            private val templateRendererInterfacePackageName: String?,
        ) {
            private val modelAttributeGroups: MutableList<AttributeGroup> = mutableListOf()

            fun addTemplateModel(
                modelName: String = "model",
                modelClassName: String = "MyModelClass",
                modelPackageName: String = "org.example.model",
                isList: Boolean = false,
            ): TemplateRendererBuilder {
                val modelAttributes = mutableMapOf<CommandAttributeKey, AttributeValue>(
                    CommandAttributeKey.TEMPLATE_MODEL_NAME to modelName,
                    CommandAttributeKey.TEMPLATE_MODEL_CLASS_NAME to modelClassName,
                    CommandAttributeKey.TEMPLATE_MODEL_PACKAGE_NAME to modelPackageName,
                )
                if (isList) {
                    modelAttributes[CommandAttributeKey.TEMPLATE_MODEL_IS_LIST] = IsListValue.YES.value
                }
                modelAttributeGroups.add(AttributeGroup(modelAttributes))
                return this
            }

            fun end(): TemplateCommentBuilder {
                val rendererAttributes = mutableMapOf<CommandAttributeKey, AttributeValue>(
                    CommandAttributeKey.TEMPLATE_RENDERER_CLASS_NAME to templateRendererClassName,
                    CommandAttributeKey.TEMPLATE_RENDERER_PACKAGE_NAME to templateRendererPackageName,
                )
                templateRendererInterfaceName?.let {
                    rendererAttributes[CommandAttributeKey.TEMPLATE_RENDERER_INTERFACE_NAME] = it
                }
                templateRendererInterfacePackageName?.let {
                    rendererAttributes[CommandAttributeKey.TEMPLATE_RENDERER_INTERFACE_PACKAGE_NAME] = it
                }
                val allAttributeGroups = listOf(AttributeGroup(rendererAttributes)) + modelAttributeGroups
                val keywordCommand = KeywordCommand(CommandKey.TEMPLATE_RENDERER, allAttributeGroups)
                return templateCommentBuilder.addKeywordCommand(keywordCommand)
            }
        }

        class RenderTemplateBuilder(
            private val templateCommentBuilder: TemplateCommentBuilder,
            private val templateRendererClassName: String,
            private val templateRendererPackageName: String?,
        ) {
            private val modelAttributeGroups: MutableList<AttributeGroup> = mutableListOf()

            fun addTemplateModel(
                modelName: String = "model",
                modelExpression: String = "myModelExpression",
            ): RenderTemplateBuilder {
                modelAttributeGroups.add(AttributeGroup(mapOf(
                    CommandAttributeKey.TEMPLATE_MODEL_NAME to modelName,
                    CommandAttributeKey.MODEL_EXPRESSION to modelExpression,
                )))
                return this
            }

            fun end(): TemplateCommentBuilder {
                require(modelAttributeGroups.isNotEmpty()) {
                    "RENDER_TEMPLATE requires at least one template model — call addTemplateModel(...) before end()"
                }
                val rendererAttributes = mutableMapOf<CommandAttributeKey, AttributeValue>(
                    CommandAttributeKey.TEMPLATE_RENDERER_CLASS_NAME to templateRendererClassName,
                )
                templateRendererPackageName?.let {
                    rendererAttributes[CommandAttributeKey.TEMPLATE_RENDERER_PACKAGE_NAME] = it
                }
                val allAttributeGroups = listOf(AttributeGroup(rendererAttributes)) + modelAttributeGroups
                val keywordCommand = KeywordCommand(CommandKey.RENDER_TEMPLATE, allAttributeGroups)
                return templateCommentBuilder.addKeywordCommand(keywordCommand)
            }
        }
    }

    class KeywordCommandBuilder(
        private val templateCommentBuilder: TemplateCommentBuilder,
        private val commandKey: CommandKey,
    ) {
        val attributeGroups: MutableList<AttributeGroup> = mutableListOf()
        var attributes: MutableMap<CommandAttributeKey, AttributeValue> = mutableMapOf()

        fun nextAttributeGroup(): KeywordCommandBuilder {
            attributeGroups.add(AttributeGroup(attributes))
            attributes = mutableMapOf()
            return this
        }

        fun withAttribute(attribute: CommandAttributeKey, value: AttributeValue): KeywordCommandBuilder {
            attributes[attribute] = value
            return this
        }

        fun addCommandToChain(): TemplateCommentBuilder {
            if (attributes.isNotEmpty()) {
                nextAttributeGroup()
            }
            val keywordCommand = KeywordCommand(commandKey, attributeGroups)
            return templateCommentBuilder.addKeywordCommand(keywordCommand)
        }
    }
}
