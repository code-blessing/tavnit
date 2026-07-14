package org.codeblessing.tavnit.contentparsing.tokenizer

import org.codeblessing.tavnit.CommentStyle
import org.codeblessing.tavnit.CommentType

/**
 * Split a text content into chunks of
 * a. content of tavnit comments, i.e. the content inside "@tt{{{" and "}}}@"
 * and
 * b. plain text (which includes non-tavnit comments)
 */
object FileContentTokenizer {

    const val TT_COMMAND_LIST_START = "@tt{{{"
    const val TT_COMMAND_LIST_END = "}}}@"
    const val ALL_LINE_BREAKS = "\\r\\n|\\r|\\n"
    const val ALL_LINE_BREAKS_OR_END_OF_FILE = "$ALL_LINE_BREAKS|\\z"

    fun tokenizeContent(content: String, supportedCommentStyles: List<CommentStyle>): List<RawContentPart> {
        val commentPatterns = supportedCommentStyles.map { style ->
            val startOfCommentRegex = Regex.escape(style.startOfComment)
            val endOfCommentRegex = when(style.commentType) {
                CommentType.BLOCK_COMMENT -> "${Regex.escape(requireNotNull(style.endOfComment))}()"
                CommentType.LINE_COMMENT -> "(${ALL_LINE_BREAKS_OR_END_OF_FILE})"
            }

            val startOfCommandList = Regex.escape(TT_COMMAND_LIST_START)
            val endOfCommandList = Regex.escape(TT_COMMAND_LIST_END)
            "(?:$startOfCommentRegex)\\s*(?:$startOfCommandList)(.*?)(?:$endOfCommandList)\\s*(?:$endOfCommentRegex)"
        }

        val regexPattern = commentPatterns.joinToString("|") { "(?:$it)" }
        val regex = Regex(regexPattern, setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.MULTILINE))
        val result = mutableListOf<RawContentPart>()
        var lastIndex = 0
        var leftoverContent = ""
        for (match in regex.findAll(content)) {
            val contentBeforeCommand = if (match.range.first > lastIndex) {
                content.substring(lastIndex, match.range.first)
            } else ""

            result.addPlainContentContentPart(leftoverContent + contentBeforeCommand)

            val strippedCommand = match.groupValueSegment( offset = 0, numberOfGroupsPerExpression = 2)
            leftoverContent = match.groupValueSegment(offset = 1, numberOfGroupsPerExpression = 2)

            // the leftover (the line break terminating a line comment) is carried over to the
            // next plain text part, so it must not also be part of this pristine content
            // (otherwise the line break is counted twice when calculating line numbers)
            result.addTemplateCommentContentPart(
                content = strippedCommand,
                pristineContent = match.value.removeSuffix(leftoverContent),
            )

            lastIndex = match.range.last + 1
        }


        if (leftoverContent.isNotEmpty() || lastIndex < content.length) {
            result.addPlainContentContentPart(leftoverContent + content.substring(lastIndex))
        }
        return result
    }

    private fun MatchResult.groupValueSegment(
        offset: Int,
        numberOfGroupsPerExpression: Int,
    ): String {
        var content = ""
        for (index in 1 + offset until this.groupValues.size step numberOfGroupsPerExpression) {
            content += this.groupValues[index]

        }
        return content
    }

    private fun MutableList<RawContentPart>.addPlainContentContentPart(content: String) {
        if(content.isNotEmpty()) {
            add(RawContentPart(contentType = ContentType.PLAIN_TEXT, content = content, pristineContent = content))
        }
    }

    private fun MutableList<RawContentPart>.addTemplateCommentContentPart(content: String, pristineContent: String) {
        add(RawContentPart(contentType = ContentType.TEMPLATE_COMMENT, content = content, pristineContent = pristineContent))
    }
}
