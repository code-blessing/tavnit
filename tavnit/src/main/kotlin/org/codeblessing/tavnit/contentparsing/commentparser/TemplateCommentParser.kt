package org.codeblessing.tavnit.contentparsing.commentparser

import org.codeblessing.tavnit.contentparsing.TemplateParsingErrorCode
import org.codeblessing.tavnit.contentparsing.TemplateParsingException

/**
 * Transform a template comment string (a list of keywords with attributes) to its structure.
 * A valid structure is something like one or many of
 * @<keyword>[<attribute1>="<value1>" <attribute2>="<value2>"][<attribute1>="<value3>"]
 */
object TemplateCommentParser {

    private val attributeKeyPattern = Regex("""[a-zA-Z]+""")
    private val attributeValuePattern = Regex("""(?:[^"\\]|\\.)*""")

    private val attributePairPattern = Regex("""${attributeKeyPattern}="$attributeValuePattern"""")

    private val attributePairsGroupingPattern = Regex("""(${attributeKeyPattern})="($attributeValuePattern)"""")
    private val keywordPattern = Regex("""[a-z][a-z\\-]+""")

    private val singleCommandKeywordAndAttributesGroupingPattern = Regex("""\s*@(${keywordPattern})\s*((?:\[(?:\s*$attributePairPattern\s+)*\s*$attributePairPattern\s*]\s*)*)""", RegexOption.MULTILINE)
    private val bracketsGroupingPattern = Regex("""\[((?:\s*$attributePairPattern\s*)*)]\s*""", RegexOption.MULTILINE)

    private val multiCommandValidationPattern = Regex("""\s*(${singleCommandKeywordAndAttributesGroupingPattern.pattern}\s*)+\s*""", RegexOption.MULTILINE)

    fun parseComment(comment: String): List<CommandStructure> {
        if(!multiCommandValidationPattern.matches(comment)) {
            throw TemplateParsingException(
                errorCode = TemplateParsingErrorCode.INVALID_COMMENT_STRUCTURE,
                msg = TemplateParsingErrorCode.INVALID_COMMENT_STRUCTURE.resolve(),
            )
        }

        return singleCommandKeywordAndAttributesGroupingPattern
            .findAll(comment)
            .map { match -> match.value }
            .map { command -> parseSingleCommand(command) }
            .toList()
    }

    private fun parseSingleCommand(command: String): CommandStructure {
        val match = requireNotNull(singleCommandKeywordAndAttributesGroupingPattern.find(command))
        val keyword = match.groupValues[1]
        val bracketsString = match.groupValues[2]

        val bracketsContent = bracketsGroupingPattern
            .findAll(bracketsString)
            .map { it.groupValues[1].trim() }
            .toList()

        return CommandStructure(
            keyword = keyword,
            brackets = bracketsContent
                .map { parseBracketContent(it) },
        )
    }

    private fun parseBracketContent(bracketContent: String): Map<String, String> {
        val keyValuePairs = mutableMapOf<String, String>()

        val attributePairMatches = attributePairsGroupingPattern.findAll(bracketContent)
        for (attributePairMatch in attributePairMatches) {
            val attributeName = attributePairMatch.groupValues[1]
            val attributeValue = attributePairMatch.groupValues[2]

            if(attributeName.isEmpty()) {
                throw TemplateParsingException(
                    errorCode = TemplateParsingErrorCode.EMPTY_ATTRIBUTE_KEY,
                    msg = TemplateParsingErrorCode.EMPTY_ATTRIBUTE_KEY.resolve(),
                )
            }
            if(attributeName in keyValuePairs) {
                throw TemplateParsingException(
                    errorCode = TemplateParsingErrorCode.DUPLICATE_ATTRIBUTE_KEY,
                    msg = TemplateParsingErrorCode.DUPLICATE_ATTRIBUTE_KEY.resolve("attributeName" to attributeName),
                )
            }
            keyValuePairs[attributeName] = decodeAttributeValue(attributeValue)
        }
        
        return keyValuePairs
    }

    private fun decodeAttributeValue(rawValue: String): String {
        val sb = StringBuilder()
        var i = 0
        while (i < rawValue.length) {
            if (rawValue[i] == '\\' && i + 1 < rawValue.length) {
                when (rawValue[i + 1]) {
                    '"'  -> { sb.append('"');  i += 2 }
                    '\\' -> { sb.append('\\'); i += 2 }
                    else -> { sb.append('\\'); sb.append(rawValue[i + 1]); i += 2 }
                }
            } else {
                sb.append(rawValue[i])
                i++
            }
        }
        return sb.toString()
    }
}
