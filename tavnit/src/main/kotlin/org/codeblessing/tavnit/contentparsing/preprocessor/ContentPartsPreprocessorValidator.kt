package org.codeblessing.tavnit.contentparsing.preprocessor

import org.codeblessing.tavnit.CommandKey
import org.codeblessing.tavnit.contentparsing.TemplateParsingErrorCode
import org.codeblessing.tavnit.contentparsing.TemplateParsingException
import org.codeblessing.tavnit.contentparsing.resolver.TemplateCommentContentPart
import org.codeblessing.tavnit.contentparsing.resolver.TemplateContentPart

object ContentPartsPreprocessorValidator {

    private val BEFORE_WHITESPACE_COMMENT_COMMAND_KEYS = setOf(
        CommandKey.REMOVE_BLANKS_BEFORE_COMMENT,
        CommandKey.REMOVE_BLANKS_AND_LINEBREAK_BEFORE_COMMENT,
    )
    private val AFTER_WHITESPACE_COMMENT_COMMAND_KEYS = setOf(
        CommandKey.REMOVE_BLANKS_AFTER_COMMENT,
        CommandKey.REMOVE_BLANKS_AND_LINEBREAK_AFTER_COMMENT,
    )

    fun validatePreprocessing(templateContentParts: List<TemplateContentPart>): List<TemplateContentPart> {
        for (part in templateContentParts) {
            if (part is TemplateCommentContentPart) {
                validateMoveComment(part)
                validateWhitespaceComment(part, "before", BEFORE_WHITESPACE_COMMENT_COMMAND_KEYS)
                validateWhitespaceComment(part, "after", AFTER_WHITESPACE_COMMENT_COMMAND_KEYS)
            }
        }
        return templateContentParts
    }

    private fun validateMoveComment(part: TemplateCommentContentPart) {
        val moveCommentCount = part.keywordCommands.count {
            it.commandKey == CommandKey.MOVE_COMMENT_FORWARD || it.commandKey == CommandKey.MOVE_COMMENT_BACKWARD
        }
        if (moveCommentCount > 1) {
            throw TemplateParsingException(
                lineNumbers = part.lineNumbers,
                errorCode = TemplateParsingErrorCode.MULTIPLE_MOVE_COMMENT_COMMANDS,
                msg = TemplateParsingErrorCode.MULTIPLE_MOVE_COMMENT_COMMANDS.resolve(
                    "count" to moveCommentCount.toString(),
                ),
            )
        }
    }

    private fun validateWhitespaceComment(part: TemplateCommentContentPart, position: String, commandKeys: Set<CommandKey>) {
        val count = part.keywordCommands.count { it.commandKey in commandKeys }
        if (count > 1) {
            throw TemplateParsingException(
                lineNumbers = part.lineNumbers,
                errorCode = TemplateParsingErrorCode.MULTIPLE_WHITESPACE_COMMENT_COMMANDS,
                msg = TemplateParsingErrorCode.MULTIPLE_WHITESPACE_COMMENT_COMMANDS.resolve(
                    "position" to position,
                    "count" to count.toString(),
                ),
            )
        }
    }
}
