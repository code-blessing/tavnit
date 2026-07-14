package org.codeblessing.tavnit.contentparsing.preprocessor

import org.codeblessing.tavnit.CommandAttributeKey
import org.codeblessing.tavnit.CommandKey
import org.codeblessing.tavnit.contentparsing.KeywordCommand
import org.codeblessing.tavnit.contentparsing.TemplateParsingErrorCode
import org.codeblessing.tavnit.contentparsing.TemplateParsingException
import org.codeblessing.tavnit.contentparsing.resolver.TemplateCommentContentPart
import org.codeblessing.tavnit.contentparsing.resolver.TemplateContentPart
import org.codeblessing.tavnit.contentparsing.resolver.TextContentPart

object ContentPartsMoveCommentPreprocessor {

    private val moveCommandKeys = setOf(CommandKey.MOVE_COMMENT_FORWARD, CommandKey.MOVE_COMMENT_BACKWARD)

    fun runPreprocessing(templateContentParts: List<TemplateContentPart>): List<TemplateContentPart> {
        val result = mutableListOf<TemplateContentPart>()
        val consumed = mutableSetOf<Int>()

        for (i in templateContentParts.indices) {
            if (i in consumed) continue

            val part = templateContentParts[i]
            if (part is TemplateCommentContentPart) {
                val moveCommand = part.keywordCommands.firstOrNull { it.commandKey in moveCommandKeys }
                if (moveCommand != null) {
                    if (moveCommand.commandKey == CommandKey.MOVE_COMMENT_FORWARD) {
                        val nextIndex = i + 1
                        if (nextIndex < templateContentParts.size && templateContentParts[nextIndex] is TextContentPart) {
                            val textPart = templateContentParts[nextIndex] as TextContentPart
                            result.addAll(applyMove(part, moveCommand, textPart, CommandKey.MOVE_COMMENT_FORWARD))
                            consumed.add(nextIndex)
                            continue
                        }
                    } else if (moveCommand.commandKey == CommandKey.MOVE_COMMENT_BACKWARD) {
                        val prevIndex = i - 1
                        if (prevIndex >= 0 && prevIndex !in consumed && templateContentParts[prevIndex] is TextContentPart) {
                            val textPart = templateContentParts[prevIndex] as TextContentPart
                            result.removeAt(result.lastIndex)
                            result.addAll(applyMove(part, moveCommand, textPart, CommandKey.MOVE_COMMENT_BACKWARD))
                            continue
                        }
                    }
                    result.add(stripMoveCommand(part))
                    continue
                }
            }
            result.add(part)
        }

        return result
    }

    private fun stripMoveCommand(commentPart: TemplateCommentContentPart): TemplateCommentContentPart {
        return commentPart.copy(
            keywordCommands = commentPart.keywordCommands.filter { it.commandKey !in moveCommandKeys }
        )
    }

    private fun applyMove(
        commentPart: TemplateCommentContentPart,
        moveCommand: KeywordCommand,
        textPart: TextContentPart,
        moveCommandKey: CommandKey,
    ): List<TemplateContentPart> {
        val processedComment = stripMoveCommand(commentPart)

        val attributeGroup = moveCommand.attributeGroups.singleOrNull()
        val beforeFirstOf = attributeGroup?.attributeOptional(CommandAttributeKey.BEFORE_FIRST_OCCURRENCE_OF)
        val afterFirstOf = attributeGroup?.attributeOptional(CommandAttributeKey.AFTER_FIRST_OCCURRENCE_OF)
        val beforeLastOf = attributeGroup?.attributeOptional(CommandAttributeKey.BEFORE_LAST_OCCURRENCE_OF)
        val afterLastOf = attributeGroup?.attributeOptional(CommandAttributeKey.AFTER_LAST_OCCURRENCE_OF)

        val text = textPart.text
        val splitIndex: Int
            if(beforeFirstOf != null) {
            val idx = text.indexOf(beforeFirstOf).indexOrException(beforeFirstOf, commentPart)
            splitIndex = idx
        } else if (afterFirstOf != null) {
            val idx = text.indexOf(afterFirstOf).indexOrException(afterFirstOf, commentPart)
            splitIndex = idx + afterFirstOf.length
        } else if(beforeLastOf != null) {
            val idx = text.lastIndexOf(beforeLastOf).indexOrException(beforeLastOf, commentPart)
            splitIndex = idx
        } else if(afterLastOf != null) {
            val idx = text.lastIndexOf(afterLastOf).indexOrException(afterLastOf, commentPart)
            splitIndex = idx + afterLastOf.length
        } else {
            // all are null
            return if (moveCommandKey == CommandKey.MOVE_COMMENT_FORWARD) listOf(textPart, processedComment) else listOf(processedComment, textPart)
        }

        val leftText = text.substring(0, splitIndex)
        val rightText = text.substring(splitIndex)

        return listOfNotNull(
            TextContentPart(textPart.lineNumbers, leftText).takeIf { leftText.isNotEmpty() },
            processedComment,
            TextContentPart(textPart.lineNumbers, rightText).takeIf { rightText.isNotEmpty() }
        )
    }

    private fun Int.indexOrException(searchToken: String, commentPart: TemplateCommentContentPart): Int {
        if (this < 0) throw TemplateParsingException(
            lineNumbers = commentPart.lineNumbers,
            errorCode = TemplateParsingErrorCode.SEARCH_TOKEN_NOT_FOUND,
            msg = TemplateParsingErrorCode.SEARCH_TOKEN_NOT_FOUND.resolve("searchToken" to searchToken),
        )
        return this
    }
}
