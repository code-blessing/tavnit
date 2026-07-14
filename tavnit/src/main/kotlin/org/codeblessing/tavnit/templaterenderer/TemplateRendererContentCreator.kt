package org.codeblessing.tavnit.templaterenderer

import org.codeblessing.tavnit.CommandAttributeKey
import org.codeblessing.tavnit.CommandKey
import org.codeblessing.tavnit.RelativeFile
import org.codeblessing.tavnit.contentparsing.KeywordCommand
import org.codeblessing.tavnit.contentparsing.commandchain.TemplateRendererDescription
import org.codeblessing.tavnit.contentparsing.resolver.TemplateCommentContentPart
import org.codeblessing.tavnit.contentparsing.resolver.TextContentPart

object TemplateRendererContentCreator {

    private const val NO_CONTENT_TO_WRITE = ""
    private const val LINE_BREAK = "\n"
    private const val MULTILINE_STRING_DELIMITER = "\"\"\""
    private val THREE_OR_MORE_DOUBLE_QUOTES = Regex("\"{3,}")

    fun createMultilineStringTemplateContent(filepath: RelativeFile, templateRendererDescription: TemplateRendererDescription): KotlinTemplateRendererMethodContent {
        val ctx = TemplateCreationContext(
            filepathWithModifications = filepath.relativeToRootDirectory().escapeKotlinStringLiteral(),
            templateRendererDescription = templateRendererDescription,
        )
        val sb = StringBuilder("|")
        templateRendererDescription.templateChain.forEach { chainItem ->
            when (chainItem) {
                is TextContentPart -> sb.append(rawContent(ctx, chainItem))
                is TemplateCommentContentPart -> chainItem.keywordCommands.forEach { sb.append(commandContent(ctx, it)) }
            }
        }
        return KotlinTemplateRendererMethodContent(
            rendererCode = sb.toString(),
            filepath = ctx.filepathWithModifications,
        )
    }

    private fun rawContent(ctx: TemplateCreationContext, textContentPart: TextContentPart): String {
        if(ctx.nestingStack.isInIgnoreMode()) {
            return NO_CONTENT_TO_WRITE
        }
        return ctx.nestingStack
            .replaceInString(textContentPart.text.escapeKotlinSpecialCharacters()) { it.escapeKotlinSpecialCharacters() }
            .addMargin(ctx)
    }

    private fun String.escapeKotlinSpecialCharacters(): String {
        return this
            .replace("$", $$"${\"$\"}")
            .replace(THREE_OR_MORE_DOUBLE_QUOTES) { quoteRun ->
                // a run of three or more double quotes would terminate the generated
                // multiline string, so emit the quotes through an interpolated expression
                val escapedQuotes = "\\\"".repeat(quoteRun.value.length)
                $$"${\"$$escapedQuotes\"}"
            }
    }

    /** Escape a value that is embedded in a generated regular (single-line) Kotlin string literal. */
    private fun String.escapeKotlinStringLiteral(): String {
        return this
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("$", "\\$")
    }

    private fun commandContent(ctx: TemplateCreationContext, keywordCommand: KeywordCommand): String {
        return when (val commandKey = keywordCommand.commandKey) {
            CommandKey.MOVE_COMMENT_FORWARD,
            CommandKey.MOVE_COMMENT_BACKWARD,
            CommandKey.REMOVE_BLANKS_BEFORE_COMMENT,
            CommandKey.REMOVE_BLANKS_AFTER_COMMENT,
            CommandKey.REMOVE_BLANKS_AND_LINEBREAK_BEFORE_COMMENT,
            CommandKey.REMOVE_BLANKS_AND_LINEBREAK_AFTER_COMMENT,
            CommandKey.NO_DEFAULT_WHITESPACE_REMOVE,
            CommandKey.TEMPLATE_RENDERER,
            CommandKey.END_TEMPLATE_RENDERER,
                 -> throw IllegalArgumentException("Command '$commandKey' not allowed here")
            CommandKey.REPLACE_VALUE_BY_EXPRESSION -> processReplaceValueByExpression(ctx, keywordCommand)
            CommandKey.END_REPLACE_VALUE_BY_EXPRESSION -> processEndReplaceValueByExpression(ctx)
            CommandKey.REPLACE_VALUE_BY_VALUE -> processReplaceValueByValue(ctx, keywordCommand)
            CommandKey.END_REPLACE_VALUE_BY_VALUE -> processEndReplaceValueByValue(ctx)
            CommandKey.IF_CONDITION -> processIfCondition(ctx, keywordCommand)
            CommandKey.ELSE_IF_CONDITION -> processElseIfCondition(ctx, keywordCommand)
            CommandKey.ELSE_CLAUSE -> processElseCondition(ctx)
            CommandKey.END_IF_CONDITION -> processEndIf(ctx)
            CommandKey.FOREACH -> processForeach(ctx, keywordCommand)
            CommandKey.END_FOREACH -> processEndForeach(ctx)
            CommandKey.IGNORE_TEXT -> processIgnoreText(ctx, keywordCommand)
            CommandKey.END_IGNORE_TEXT -> processEndIgnoreText(ctx)
            CommandKey.PRINT_TEXT -> processPrintText(ctx, keywordCommand)
            CommandKey.REMARK -> NO_CONTENT_TO_WRITE
            CommandKey.MODIFY_PROVIDED_FILENAME_BY_REPLACEMENTS -> processProvideModifiedFilename(ctx)
            CommandKey.RENDER_TEMPLATE -> processRenderTemplate(ctx, keywordCommand)
            CommandKey.ADD_IMPORT_TO_RENDERER -> NO_CONTENT_TO_WRITE
        }
    }

    private fun processReplaceValueByExpression(ctx: TemplateCreationContext, command: KeywordCommand): String {
        val replacements: Map<String, Replacement> = command.attributeGroups.fold(emptyMap()) { resultMap, attributeGroup ->
            val searchValue = attributeGroup.attribute(CommandAttributeKey.SEARCH_VALUE)
            val placeholderExpression = createExpressionPlaceholder(
                expression = attributeGroup.attribute(CommandAttributeKey.REPLACE_BY_EXPRESSION),
            )
            resultMap + (searchValue to ExpressionReplacement(placeholderExpression))
        }
        ctx.nestingStack.pushNestingContext(CommandNestingContext(command, replacements))
        return NO_CONTENT_TO_WRITE
    }

    private fun processEndReplaceValueByExpression(ctx: TemplateCreationContext): String {
        ctx.nestingStack.popNestingContext(CommandKey.END_REPLACE_VALUE_BY_EXPRESSION)
        return NO_CONTENT_TO_WRITE
    }

    private fun processReplaceValueByValue(ctx: TemplateCreationContext, command: KeywordCommand): String {
        val replacements: Map<String, Replacement> = command.attributeGroups.fold(emptyMap()) { resultMap, attributeGroup ->
            val searchValue = attributeGroup.attribute(CommandAttributeKey.SEARCH_VALUE)
            val replacementValue = attributeGroup.attribute(CommandAttributeKey.REPLACE_BY_VALUE)
            resultMap + (searchValue to ValueReplacement(replacementValue))
        }
        ctx.nestingStack.pushNestingContext(CommandNestingContext(command, replacements))
        return NO_CONTENT_TO_WRITE
    }

    private fun processEndReplaceValueByValue(ctx: TemplateCreationContext): String {
        ctx.nestingStack.popNestingContext(CommandKey.END_REPLACE_VALUE_BY_VALUE)
        return NO_CONTENT_TO_WRITE
    }

    private fun processIfCondition(
        ctx: TemplateCreationContext,
        command: KeywordCommand,
    ): String {
        ctx.nestingStack.pushNestingContext(CommandNestingContext(command))
        return startStatementInMultilineText(
            ctx = ctx,
            statement = "if(${command.attribute(CommandAttributeKey.CONDITION_EXPRESSION)}) {",
        )
    }

    private fun processElseIfCondition(
        ctx: TemplateCreationContext,
        keywordCommand: KeywordCommand,
    ): String {
        return intermediateStatementInMultilineText(
            ctx = ctx,
            statement = "} else if(${keywordCommand.attribute(CommandAttributeKey.CONDITION_EXPRESSION)}) {",
        )
    }

    private fun processElseCondition(
        ctx: TemplateCreationContext,
    ): String {
        ctx.nestingStack.markLastElementHasElseClause()
        return intermediateStatementInMultilineText(
            ctx = ctx,
            statement = "} else {"
        )
    }

    private fun processEndIf(
        ctx: TemplateCreationContext,
    ): String {
        val hasElseClause = ctx.nestingStack.hasElseClause()
        ctx.nestingStack.popNestingContext(CommandKey.END_IF_CONDITION)
        if(hasElseClause) {
            return endStatementInMultilineText(ctx = ctx, statement = "}")
        } else {
            val elseClause = intermediateStatementInMultilineText(
                ctx = ctx,
                statement = "} else {"
            )
            val endIfClause = endStatementInMultilineText(ctx = ctx, statement = "}")

            return elseClause + endIfClause
        }
    }

    private fun processForeach(
        ctx: TemplateCreationContext,
        command: KeywordCommand,
    ): String {
        ctx.nestingStack.pushNestingContext(CommandNestingContext(command))
        return startStatementInMultilineText(
            ctx = ctx,
            statement = "${command.attribute(CommandAttributeKey.LOOP_ITERABLE_EXPRESSION)}.joinToString(\"\") { ${command.attribute(CommandAttributeKey.LOOP_VARIABLE_NAME)} -> "
        )
    }

    private fun processEndForeach(
        ctx: TemplateCreationContext,
    ): String {
        ctx.nestingStack.popNestingContext(CommandKey.END_FOREACH)
        return endStatementInMultilineText(ctx = ctx, statement = "}")
    }

    private fun processIgnoreText(
        ctx: TemplateCreationContext,
        command: KeywordCommand,
    ): String {
        ctx.nestingStack.pushNestingContext(CommandNestingContext(command, isInIgnoreMode = true))
        return NO_CONTENT_TO_WRITE
    }

    private fun processPrintText(
        ctx: TemplateCreationContext,
        command: KeywordCommand,
    ): String {
        return command.attribute(CommandAttributeKey.TEXT).escapeKotlinSpecialCharacters().addMargin(ctx)
    }

    private fun processProvideModifiedFilename(
        ctx: TemplateCreationContext,
    ): String {
        // the file path is already escaped for a single-line Kotlin string literal; expression
        // replacements insert interpolation expressions that must stay intact, while plain-value
        // replacements must be escaped for that same single-line string literal
        ctx.filepathWithModifications = ctx.nestingStack
            .replaceInString(ctx.filepathWithModifications) { it.escapeKotlinStringLiteral() }
        return NO_CONTENT_TO_WRITE
    }

    private fun processRenderTemplate(ctx: TemplateCreationContext, command: KeywordCommand): String {
        val rendererClassName = command.attribute(0, CommandAttributeKey.TEMPLATE_RENDERER_CLASS_NAME)

        val modelArguments = command.attributeGroupIndices()
            .drop(1)
            .map { groupIndex ->
                val modelName = command.attribute(groupIndex, CommandAttributeKey.TEMPLATE_MODEL_NAME)
                val modelExpression = command.attribute(groupIndex, CommandAttributeKey.MODEL_EXPRESSION)
                "$modelName = $modelExpression"
            }
            .joinToString(", ")

        return createExpressionPlaceholder("$rendererClassName.renderTemplate($modelArguments)").addMargin(ctx)
    }

    private fun processEndIgnoreText(
        ctx: TemplateCreationContext,
    ): String {
        ctx.nestingStack.popNestingContext(CommandKey.END_IGNORE_TEXT)
        return NO_CONTENT_TO_WRITE
    }

    private fun startStatementInMultilineText(ctx: TemplateCreationContext, statement: String): String {
        return $$"${ $$statement $${startExpressionBlockWithText(ctx)}"
    }

    private fun intermediateStatementInMultilineText(ctx: TemplateCreationContext, statement: String): String {
        return $$"$${endExpressionBlockWithText(ctx)} $$statement $${startExpressionBlockWithText(ctx)}"
    }

    private fun endStatementInMultilineText(ctx: TemplateCreationContext, statement: String): String {
        return $$"$${endExpressionBlockWithText(ctx)} $$statement }"
    }

    private fun startExpressionBlockWithText(ctx: TemplateCreationContext): String {
        ctx.identLevel.increaseLevel()
        return MULTILINE_STRING_DELIMITER
    }

    private fun endExpressionBlockWithText(ctx: TemplateCreationContext): String {
        ctx.identLevel.decreaseLevel()
        return $$"$$MULTILINE_STRING_DELIMITER"
    }

    private fun createExpressionPlaceholder(expression: String): String {
        return $$"${$${expression}}"
    }

    private fun String.addMargin(ctx: TemplateCreationContext): String {
        return this.lines()
            .joinToString("\n${identAndMarker(ctx)}")
    }
    private fun identAndMarker(ctx: TemplateCreationContext, marginSymbol: String = "|"): String {
        return "${" ".repeat(4 * ctx.identLevel.identLevel)}$marginSymbol"
    }

    private data class TemplateCreationContext(
        var filepathWithModifications: String,
        val templateRendererDescription: TemplateRendererDescription,
        val identLevel: IdentLevel = IdentLevel(),
        val nestingStack: CommandNestingContextStack = CommandNestingContextStack()
    )

    private class IdentLevel {
        private var level = 0

        val identLevel: Int
            get() = level

        fun increaseLevel(): IdentLevel {
            level++
            return this
        }

        fun decreaseLevel(): IdentLevel {
            level--
            return this
        }
    }

    private class CommandNestingContextStack {
        private val nestingStack: MutableList<CommandNestingContext> = mutableListOf()

        fun pushNestingContext(ctx: CommandNestingContext) {
            require(ctx.command.commandKey.isOpeningCommand)
            nestingStack.add(ctx)
        }

        fun popNestingContext(closingCommandKey: CommandKey) {
            val correspondingOpeningCommandKey = requireNotNull(closingCommandKey.correspondingOpeningCommandKey)
            while (nestingStack.isNotEmpty()) {
                val last = nestingStack.removeLast()
                val lastCommandKey = last.command.commandKey
                if(lastCommandKey == correspondingOpeningCommandKey) {
                    return
                }
            }
        }

        /**
         * Apply all active replacements to [text], which has already been escaped for its target
         * string literal. [escapeValue] is the same escaping used on [text]; it is applied both to
         * the search token (so a token containing special characters still matches the escaped
         * text) and to plain replacement values (so they cannot break out of the string literal).
         */
        fun replaceInString(text: String, escapeValue: (String) -> String): String {
            var resultText = text

            for((searchValue, replacement) in createReplacementMap()) {
                resultText = resultText.replace(escapeValue(searchValue), replacement.render(escapeValue))
            }
            return resultText
        }

        private fun createReplacementMap(): Map<String, Replacement> {
            // Apply the replacements of the innermost commands first, then the outer
            // (parent/grand-parent) commands. On a duplicate searchValue the innermost
            // command wins, as it is seen first.
            val replacements = LinkedHashMap<String, Replacement>()
            nestingStack.asReversed().forEach { nestingCtx ->
                nestingCtx.replacements.forEach { (searchValue, replacement) ->
                    replacements.putIfAbsent(searchValue, replacement)
                }
            }
            return replacements
        }

        fun markLastElementHasElseClause() {
            nestingStack.last().markLastElementHasElseClause()
        }


        fun hasElseClause(): Boolean {
            return nestingStack.last().hasElseClause
        }

        fun isInIgnoreMode(): Boolean {
            return nestingStack.any { it.isInIgnoreMode }
        }

    }

    /**
     * A value substituted into a generated string literal by a `replace-value-by-*` command.
     * How it must be escaped depends on the kind of string literal it is inserted into, so
     * rendering is deferred until the target is known (see [CommandNestingContextStack.replaceInString]).
     */
    private sealed interface Replacement {
        fun render(escapeValue: (String) -> String): String
    }

    /** A model expression, emitted as a Kotlin interpolation that is valid in any string literal. */
    private class ExpressionReplacement(private val placeholder: String) : Replacement {
        override fun render(escapeValue: (String) -> String): String = placeholder
    }

    /** A plain value, escaped for the concrete string literal it is inserted into. */
    private class ValueReplacement(private val rawValue: String) : Replacement {
        override fun render(escapeValue: (String) -> String): String = escapeValue(rawValue)
    }

    private class CommandNestingContext(
        val command: KeywordCommand,
        val replacements: Map<String, Replacement> = emptyMap(),
        var hasElseClause: Boolean = false,
        var isInIgnoreMode: Boolean = false,
    ) {
        fun markLastElementHasElseClause() {
            require(command.commandKey == CommandKey.IF_CONDITION) {
                "try to change the 'hasElseClause' flag " +
                        "but nesting element is not ${CommandKey.IF_CONDITION} but ${command.commandKey}"
            }
            hasElseClause = true
        }

    }
}
