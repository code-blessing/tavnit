package org.codeblessing.tavnit.contentparsing.preprocessor

import org.codeblessing.tavnit.CommandKey
import org.codeblessing.tavnit.contentparsing.resolver.TemplateCommentContentPart
import org.codeblessing.tavnit.contentparsing.resolver.TemplateContentPart
import org.codeblessing.tavnit.contentparsing.resolver.TextContentPart

/**
 * Removes whitespace around tavnit comments.
 *
 * The default decision looks only at the text directly before the comment (up to the start of its
 * line) and directly after the comment (up to the end of its line) and distinguishes four cases:
 *
 * 1. Non-blank text before the comment, only blanks (and then the line break) after it: everything
 *    before the comment is kept, but the blanks between the last non-blank and the comment are
 *    removed; the blanks after the comment are removed too but the line break is kept (a trailing
 *    comment is removed without leaving trailing blanks behind on either side).
 * 2. Only blanks before the comment, non-blank text after it: nothing is removed; only the comment
 *    itself disappears (a leading comment keeps its indentation and the following text).
 * 3. Only blanks before the comment and only blanks after it: the comment stands alone on its line,
 *    so the blanks before it are removed (the preceding line break is kept) and the blanks together
 *    with the trailing line break after it are removed, collapsing the whole comment line.
 * 4. Any other case (non-blank text on both sides): nothing is removed.
 *
 * The before side and the after side are then processed on their own:
 *
 * - A remove-blanks command overrides the default decision for its own side only (and never
 *   touches the other side).
 * - The no-default-whitespace-remove command switches the default decision off for both sides, so
 *   that nothing but the comment itself is removed. Explicit remove-blanks commands still override
 *   that (now disabled) default for their own side.
 *
 * Conflicting remove commands on the same side cannot occur (they are rejected earlier by
 * the validators), and a command that does the same thing as the default decision is simply a
 * no-op. Every comment is considered on its own, looking only at the text directly before and
 * after it.
 */
object ContentPartsExpandCommentPreprocessor {

    private enum class WhitespaceAction { KEEP, STRIP_BLANKS, STRIP_BLANKS_AND_LINEBREAK }

    private val BEFORE_COMMAND_ACTIONS = mapOf(
        CommandKey.REMOVE_BLANKS_BEFORE_COMMENT to WhitespaceAction.STRIP_BLANKS,
        CommandKey.REMOVE_BLANKS_AND_LINEBREAK_BEFORE_COMMENT to WhitespaceAction.STRIP_BLANKS_AND_LINEBREAK,
    )
    private val AFTER_COMMAND_ACTIONS = mapOf(
        CommandKey.REMOVE_BLANKS_AFTER_COMMENT to WhitespaceAction.STRIP_BLANKS,
        CommandKey.REMOVE_BLANKS_AND_LINEBREAK_AFTER_COMMENT to WhitespaceAction.STRIP_BLANKS_AND_LINEBREAK,
    )

    /** All commands that influence the whitespace handling and therefore must be stripped from the comment. */
    private val WHITESPACE_COMMENT_COMMAND_KEYS =
        BEFORE_COMMAND_ACTIONS.keys + AFTER_COMMAND_ACTIONS.keys + CommandKey.NO_DEFAULT_WHITESPACE_REMOVE

    fun runPreprocessing(templateContentParts: List<TemplateContentPart>): List<TemplateContentPart> {
        val parts = templateContentParts.toMutableList()

        var index = 0
        while (index < parts.size) {
            if (parts[index] is TemplateCommentContentPart) {
                applyWhitespaceHandling(parts, index)
            }
            index++
        }

        return parts.filter { it !is TextContentPart || it.text.isNotEmpty() }
    }

    private fun applyWhitespaceHandling(parts: MutableList<TemplateContentPart>, index: Int) {
        val comment = parts[index] as TemplateCommentContentPart
        val precedingIndex = index - 1
        val followingIndex = index + 1
        val precedingText = parts.getOrNull(precedingIndex) as? TextContentPart
        val followingText = parts.getOrNull(followingIndex) as? TextContentPart

        // Step 1: classify the text directly before and after the comment.
        val onlyBlanksBefore = precedingText == null || onlyBlanksUpToLineStart(precedingText.text)
        val onlyBlanksAfter = followingText == null || !firstLineHasNonBlank(followingText.text)

        // The no-default-whitespace-remove command disables the default whitespace handling
        // entirely, so that only the comment itself is removed and no surrounding blanks or line
        // breaks. Explicit remove-blanks commands still override the (now disabled) default per side.
        val defaultRemovalEnabled =
            comment.keywordCommands.none { it.commandKey == CommandKey.NO_DEFAULT_WHITESPACE_REMOVE }

        // Step 2: derive the default decision per side from the four cases (see the class
        // documentation). The before side strips the trailing blanks whenever no non-blank text
        // follows the comment on its line (so a trailing or standalone comment leaves no blanks
        // behind); the after side strips the trailing blanks whenever no non-blank text follows on
        // the line, and also the trailing line break when the comment stands alone.
        val beforeDefault = when {
            !defaultRemovalEnabled -> WhitespaceAction.KEEP
            onlyBlanksAfter -> WhitespaceAction.STRIP_BLANKS
            else -> WhitespaceAction.KEEP
        }
        val afterDefault = when {
            !defaultRemovalEnabled -> WhitespaceAction.KEEP
            !onlyBlanksAfter -> WhitespaceAction.KEEP
            onlyBlanksBefore -> WhitespaceAction.STRIP_BLANKS_AND_LINEBREAK
            else -> WhitespaceAction.STRIP_BLANKS
        }

        // Steps 3-4: a command overrides the default for its own side only
        val beforeAction = comment.commandAction(BEFORE_COMMAND_ACTIONS) ?: beforeDefault
        val afterAction = comment.commandAction(AFTER_COMMAND_ACTIONS) ?: afterDefault

        parts[index] = comment.copy(
            keywordCommands = comment.keywordCommands.filter { it.commandKey !in WHITESPACE_COMMENT_COMMAND_KEYS }
        )

        if (precedingText != null && beforeAction != WhitespaceAction.KEEP) {
            parts[precedingIndex] = precedingText.copy(
                text = stripFromEnd(precedingText.text, beforeAction == WhitespaceAction.STRIP_BLANKS_AND_LINEBREAK)
            )
        }
        if (followingText != null && afterAction != WhitespaceAction.KEEP) {
            parts[followingIndex] = followingText.copy(
                text = stripFromStart(followingText.text, afterAction == WhitespaceAction.STRIP_BLANKS_AND_LINEBREAK)
            )
        }
    }

    private fun TemplateCommentContentPart.commandAction(actions: Map<CommandKey, WhitespaceAction>): WhitespaceAction? =
        keywordCommands.firstNotNullOfOrNull { actions[it.commandKey] }

    /** Whether the first line of [text] contains a non-blank character before any line break. */
    private fun firstLineHasNonBlank(text: String): Boolean {
        for (char in text) {
            if (char == '\n' || char == '\r') return false
            if (char != ' ' && char != '\t') return true
        }
        return false
    }

    /** Whether the text ends with only blanks back to the start of its last line. */
    private fun onlyBlanksUpToLineStart(text: String): Boolean {
        for (index in text.indices.reversed()) {
            val char = text[index]
            if (char == '\n' || char == '\r') return true
            if (char != ' ' && char != '\t') return false
        }
        return true
    }

    private fun stripFromStart(text: String, stripLinebreak: Boolean): String {
        var i = 0
        while (i < text.length && (text[i] == ' ' || text[i] == '\t')) {
            i++
        }
        if (stripLinebreak && i < text.length) {
            if (text[i] == '\r' && i + 1 < text.length && text[i + 1] == '\n') {
                i += 2
            } else if (text[i] == '\r' || text[i] == '\n') {
                i++
            }
        }
        return text.substring(i)
    }

    private fun stripFromEnd(text: String, stripLinebreak: Boolean): String {
        var i = text.length - 1
        while (i >= 0 && (text[i] == ' ' || text[i] == '\t')) {
            i--
        }
        if (stripLinebreak && i >= 0) {
            if (text[i] == '\n' && i > 0 && text[i - 1] == '\r') {
                i -= 2
            } else if (text[i] == '\n' || text[i] == '\r') {
                i--
            }
        }
        return text.substring(0, i + 1)
    }
}
