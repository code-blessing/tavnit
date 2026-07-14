package org.codeblessing.tavnit.contentparsing.commandchain

import org.codeblessing.tavnit.CommandKey
import org.codeblessing.tavnit.contentparsing.KeywordCommand
import org.codeblessing.tavnit.contentparsing.TemplateParsingErrorCode
import org.codeblessing.tavnit.contentparsing.TemplateParsingException
import org.codeblessing.tavnit.contentparsing.linenumbers.LineNumbers
import org.codeblessing.tavnit.contentparsing.linenumbers.LineNumbers.Companion.EMPTY_LINE_NUMBERS
import org.codeblessing.tavnit.contentparsing.resolver.TemplateCommentContentPart
import org.codeblessing.tavnit.contentparsing.resolver.TemplateContentPart

object KeywordCommandChainNestingHandler {

    private class OpenCommand(
        val commandKey: CommandKey,
        val lineNumbers: LineNumbers,
        var hasElseClause: Boolean = false,
    )

    fun validateAndHandleNestingStructure(templateContentParts: List<TemplateContentPart>): List<TemplateContentPart> {
        val result = mutableListOf<TemplateContentPart>()
        val openingCommandsStack = mutableListOf<OpenCommand>()

        for (part in templateContentParts) {
            if (part is TemplateCommentContentPart) {
                val autoClosePartsToInsert = mutableListOf<TemplateCommentContentPart>()

                for (keywordCommand in part.keywordCommands) {
                    val commandKey = keywordCommand.commandKey

                    if (commandKey.isTriggerAutoclose) {
                        autoClosePartsToInsert.addAll(
                            collectAutocloseCommands(commandKey, openingCommandsStack, part.lineNumbers)
                        )
                    }

                    if (commandKey.isOpeningCommand) {
                        openingCommandsStack.add(OpenCommand(commandKey, part.lineNumbers))
                    } else if (commandKey.isClosingCommand) {
                        validateClosingCommand(part, keywordCommand, openingCommandsStack)
                        openingCommandsStack.removeLast()
                    }

                    if (commandKey.isRequiredDirectlyNestedInOtherCommand) {
                        validateDirectlyNestedCommand(part, keywordCommand, openingCommandsStack)
                    }

                    if (commandKey == CommandKey.ELSE_CLAUSE || commandKey == CommandKey.ELSE_IF_CONDITION) {
                        validateAndTrackElseClause(part, keywordCommand, openingCommandsStack)
                    }
                }

                result.addAll(autoClosePartsToInsert)
                result.add(part)
            } else {
                result.add(part)
            }
        }

        closeRemainingOpenCommands(openingCommandsStack, result)

        return result
    }

    private fun validateAndTrackElseClause(
        commandFragment: TemplateCommentContentPart,
        keywordCommand: KeywordCommand,
        openingCommandsStack: List<OpenCommand>,
    ) {
        // the directly-nested validation already guarantees that the top of the stack is the if command
        val enclosingIfCommand = openingCommandsStack.last()
        if (enclosingIfCommand.hasElseClause) {
            throw TemplateParsingException(
                lineNumbers = commandFragment.lineNumbers,
                errorCode = TemplateParsingErrorCode.COMMAND_AFTER_ELSE_CLAUSE,
                msg = TemplateParsingErrorCode.COMMAND_AFTER_ELSE_CLAUSE.resolve(
                    "command" to keywordCommand.commandKey.keyword,
                    "elseCommand" to CommandKey.ELSE_CLAUSE.keyword,
                    "ifCommand" to CommandKey.IF_CONDITION.keyword,
                ),
            )
        }
        if (keywordCommand.commandKey == CommandKey.ELSE_CLAUSE) {
            enclosingIfCommand.hasElseClause = true
        }
    }

    private fun collectAutocloseCommands(
        triggerCommandKey: CommandKey,
        openingCommandsStack: MutableList<OpenCommand>,
        lineNumbers: LineNumbers,
    ): List<TemplateCommentContentPart> {
        val collected = mutableListOf<TemplateCommentContentPart>()
        val correspondingOpeningKey = requireNotNull(triggerCommandKey.correspondingOpeningCommandKeyForAutoclose)

        while (openingCommandsStack.isNotEmpty()) {
            val lastCommandKey = openingCommandsStack.last().commandKey
            if (lastCommandKey == correspondingOpeningKey) break
            if (!lastCommandKey.isAutoclosingSupported) break
            val closingKey = requireNotNull(lastCommandKey.correspondingClosingCommandKey)
            collected.add(createClosingCommandPart(closingKey, lineNumbers))
            openingCommandsStack.removeLast()
        }

        return collected
    }

    private fun closeRemainingOpenCommands(
        openingCommandsStack: MutableList<OpenCommand>,
        result: MutableList<TemplateContentPart>,
    ) {
        while (openingCommandsStack.isNotEmpty()) {
            val openCommand = openingCommandsStack.last()
            val commandKey = openCommand.commandKey
            if (!commandKey.isAutoclosingSupported) {
                throw TemplateParsingException(
                    lineNumbers = openCommand.lineNumbers,
                    errorCode = TemplateParsingErrorCode.UNCLOSED_OPENING_COMMAND,
                    msg = TemplateParsingErrorCode.UNCLOSED_OPENING_COMMAND.resolve(
                        "openingCommand" to commandKey.keyword,
                        "closingCommand" to commandKey.correspondingClosingCommandKey?.keyword.toString(),
                    ),
                )
            }
            val closingKey = requireNotNull(commandKey.correspondingClosingCommandKey)
            result.add(createClosingCommandPart(closingKey, EMPTY_LINE_NUMBERS))
            openingCommandsStack.removeLast()
        }
    }

    private fun validateClosingCommand(
        commandFragment: TemplateCommentContentPart,
        keywordCommand: KeywordCommand,
        openingCommandsStack: List<OpenCommand>,
    ) {
        val closingCommandKey = keywordCommand.commandKey
        val correspondingOpeningCommandKey = requireNotNull(closingCommandKey.correspondingOpeningCommandKey)
        val lastCommandKey = openingCommandsStack.lastOrNull()?.commandKey
        if (lastCommandKey == null || lastCommandKey != correspondingOpeningCommandKey) {
            throw TemplateParsingException(
                lineNumbers = commandFragment.lineNumbers,
                errorCode = TemplateParsingErrorCode.MISMATCHED_CLOSING_COMMAND,
                msg = TemplateParsingErrorCode.MISMATCHED_CLOSING_COMMAND.resolve(
                    "closingCommand" to closingCommandKey.keyword,
                    "openingCommand" to correspondingOpeningCommandKey.keyword,
                ),
            )
        }
    }

    private fun validateDirectlyNestedCommand(
        commandFragment: TemplateCommentContentPart,
        keywordCommand: KeywordCommand,
        openingCommandsStack: List<OpenCommand>,
    ) {
        val commandKey = keywordCommand.commandKey
        val requiredEnclosingCommandKey = requireNotNull(commandKey.directlyNestedInsideCommandKey)
        if (openingCommandsStack.lastOrNull()?.commandKey != requiredEnclosingCommandKey) {
            throw TemplateParsingException(
                lineNumbers = commandFragment.lineNumbers,
                errorCode = TemplateParsingErrorCode.COMMAND_NOT_DIRECTLY_NESTED,
                msg = TemplateParsingErrorCode.COMMAND_NOT_DIRECTLY_NESTED.resolve(
                    "command" to commandKey.keyword,
                    "enclosingCommand" to requiredEnclosingCommandKey.keyword,
                ),
            )
        }
    }

    private fun createClosingCommandPart(
        closingCommandKey: CommandKey,
        lineNumbers: LineNumbers,
    ): TemplateCommentContentPart {
        return TemplateCommentContentPart(
            lineNumbers = lineNumbers,
            keywordCommands = listOf(KeywordCommand(closingCommandKey, emptyList())),
        )
    }
}
