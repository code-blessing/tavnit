package org.codeblessing.tavnit.contentparsing.preprocessor

import org.codeblessing.tavnit.CommandKey
import org.codeblessing.tavnit.contentparsing.TemplateParsingErrorCode
import org.codeblessing.tavnit.contentparsing.TemplateParsingException
import org.codeblessing.tavnit.contentparsing.resolver.TemplateCommentContentPart
import org.codeblessing.tavnit.contentparsing.resolver.TemplateContentPart

/**
 * Validates that no two mutually exclusive command keys are used together in the same template comment.
 *
 * Which command keys exclude each other is declared per command key in [CommandKey.mutuallyExclusiveCommandKeys]
 * (e.g. moving a comment backward and forward at the same time, or removing only the blanks and removing the
 * blanks together with the line break on the same side of a comment). This class does nothing but this single validation.
 */
object MutuallyExclusiveCommandKeysValidator {

    fun validate(templateContentParts: List<TemplateContentPart>): List<TemplateContentPart> {
        for (part in templateContentParts) {
            if (part is TemplateCommentContentPart) {
                validateComment(part)
            }
        }
        return templateContentParts
    }

    private fun validateComment(part: TemplateCommentContentPart) {
        val presentCommandKeys = part.keywordCommands.map { it.commandKey }
        for (commandKey in presentCommandKeys) {
            val conflictingCommandKey = presentCommandKeys
                .firstOrNull { it in commandKey.mutuallyExclusiveCommandKeys }
                ?: continue
            throw TemplateParsingException(
                lineNumbers = part.lineNumbers,
                errorCode = TemplateParsingErrorCode.MUTUALLY_EXCLUSIVE_COMMAND_KEYS,
                msg = TemplateParsingErrorCode.MUTUALLY_EXCLUSIVE_COMMAND_KEYS.resolve(
                    "command" to commandKey.keyword,
                    "conflictingCommand" to conflictingCommandKey.keyword,
                ),
            )
        }
    }
}
