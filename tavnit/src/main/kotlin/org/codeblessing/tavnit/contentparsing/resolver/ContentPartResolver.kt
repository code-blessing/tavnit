package org.codeblessing.tavnit.contentparsing.resolver

import org.codeblessing.tavnit.contentparsing.TemplateParsingException.Companion.reThrowWithLineNumbers
import org.codeblessing.tavnit.contentparsing.commentparser.TemplateCommentParser
import org.codeblessing.tavnit.contentparsing.linenumbers.LineNumberCalculator
import org.codeblessing.tavnit.contentparsing.tokenizer.ContentType
import org.codeblessing.tavnit.contentparsing.tokenizer.RawContentPart

/**
 * Splits the tavnit comment text into a list of structured commands.
 */
object ContentPartResolver {

    fun createContentParts(contentParts: List<RawContentPart>): List<TemplateContentPart> {
        return contentParts.map { contentPart ->
            val lineNumbers = LineNumberCalculator.calculateLineNumbers(contentPart, contentParts)
            reThrowWithLineNumbers(lineNumbers) {
                when (contentPart.contentType) {
                    ContentType.PLAIN_TEXT -> TextContentPart(
                        lineNumbers = lineNumbers,
                        text = contentPart.content,
                    )
                    ContentType.TEMPLATE_COMMENT -> TemplateCommentContentPart(
                        lineNumbers = lineNumbers,
                        keywordCommands = TemplateCommentParser.parseComment(contentPart.content)
                            .map { commandStructure ->
                                KeywordCommandFactory.createKeywordCommand(
                                    commandStructure = commandStructure,
                                    lineNumbers = lineNumbers,
                                )
                            },
                    )
                }
            }
        }
    }
}
