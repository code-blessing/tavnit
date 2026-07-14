package org.codeblessing.tavnit.contentparsing

import org.codeblessing.tavnit.CommentStyle
import org.codeblessing.tavnit.TavnitException
import org.codeblessing.tavnit.contentparsing.commandchain.KeywordCommandChainCustomValidation
import org.codeblessing.tavnit.contentparsing.resolver.ContentPartResolver
import org.codeblessing.tavnit.contentparsing.commandchain.KeywordCommandChainNestingHandler
import org.codeblessing.tavnit.contentparsing.commandchain.KeywordCommandChainTemplateSplitter
import org.codeblessing.tavnit.contentparsing.commandchain.TemplateRendererDescription
import org.codeblessing.tavnit.contentparsing.preprocessor.ContentPartsExpandCommentPreprocessor
import org.codeblessing.tavnit.contentparsing.preprocessor.ContentPartsMoveCommentPreprocessor
import org.codeblessing.tavnit.contentparsing.preprocessor.ContentPartsPreprocessorValidator
import org.codeblessing.tavnit.contentparsing.preprocessor.MutuallyExclusiveCommandKeysValidator
import org.codeblessing.tavnit.contentparsing.linenumbers.LineNumberCalculator
import org.codeblessing.tavnit.contentparsing.linenumbers.LineNumbers
import org.codeblessing.tavnit.contentparsing.resolver.TemplateContentPart
import org.codeblessing.tavnit.contentparsing.tokenizer.FileContentTokenizer
import org.codeblessing.tavnit.contentparsing.tokenizer.ContentType
import org.codeblessing.tavnit.contentparsing.tokenizer.RawContentPart

object ContentParser {

    // A file saved as UTF-8 with a byte-order mark keeps the BOM as the first character after
    // decoding. Stripping it here keeps it out of the first plain-text part (and out of line-number
    // counting) so it cannot leak into the generated renderer output.
    private const val UTF8_BOM = "\uFEFF"

    fun parseContent(content: String, supportedCommentStyles: List<CommentStyle>): List<TemplateRendererDescription> {
        if(supportedCommentStyles.isEmpty()) {
            return emptyList()
        }
        val contentWithoutBom = content.removePrefix(UTF8_BOM)
        try {
            val rawContentParts = FileContentTokenizer.tokenizeContent(contentWithoutBom, supportedCommentStyles)

            if(rawContentParts.none { it.contentType == ContentType.TEMPLATE_COMMENT }) {
                // the file does not contain any tavnit commands and can be ignored.
                return emptyList()
            }

            verifyNoUnterminatedTavnitComments(rawContentParts)

            val templateContentParts = ContentPartResolver.createContentParts(rawContentParts)
                .pipe(MutuallyExclusiveCommandKeysValidator::validate)
                .pipe(ContentPartsPreprocessorValidator::validatePreprocessing)
                .pipe(ContentPartsExpandCommentPreprocessor::runPreprocessing)
                .pipe(ContentPartsMoveCommentPreprocessor::runPreprocessing)
                .pipe(KeywordCommandChainNestingHandler::validateAndHandleNestingStructure)
                .pipe( KeywordCommandChainCustomValidation::validate )
            return KeywordCommandChainTemplateSplitter.splitIntoTemplateRendererDescriptions(templateContentParts)
        } catch (ex: TemplateParsingException) {
            throw TavnitException(
                "Failed to parse at line ${ex.lineNumbers.formattedDescription}: ${ex.message}"
            )
        }
    }

    private fun List<TemplateContentPart>.pipe(
        function: (templateContentParts: List<TemplateContentPart>) -> List<TemplateContentPart>,
    ): List<TemplateContentPart> {
        return function(this)
    }

    /**
     * A tavnit comment start marker in plain text means a tavnit comment is missing its
     * end marker (the tokenizer then treats the whole comment as plain text). Without this
     * check, the half-written commands would silently leak into the rendered output.
     */
    private fun verifyNoUnterminatedTavnitComments(rawContentParts: List<RawContentPart>) {
        for (rawContentPart in rawContentParts) {
            if (rawContentPart.contentType != ContentType.PLAIN_TEXT) continue
            if (!rawContentPart.content.contains(FileContentTokenizer.TT_COMMAND_LIST_START)) continue
            throw TemplateParsingException(
                lineNumbers = startMarkerLineNumbers(rawContentPart, rawContentParts),
                errorCode = TemplateParsingErrorCode.UNTERMINATED_TEMPLATE_COMMENT,
                msg = TemplateParsingErrorCode.UNTERMINATED_TEMPLATE_COMMENT.resolve(
                    "startMarker" to FileContentTokenizer.TT_COMMAND_LIST_START,
                    "endMarker" to FileContentTokenizer.TT_COMMAND_LIST_END,
                ),
            )
        }
    }

    private fun startMarkerLineNumbers(
        rawContentPart: RawContentPart,
        allRawContentParts: List<RawContentPart>,
    ): LineNumbers {
        val startMarker = FileContentTokenizer.TT_COMMAND_LIST_START
        val partLineNumbers = LineNumberCalculator.calculateLineNumbers(rawContentPart, allRawContentParts)
        val contentBeforeMarker = rawContentPart.content.substringBefore(startMarker)
        val lineBreakPattern = Regex(FileContentTokenizer.ALL_LINE_BREAKS)
        val markerLineNumber = partLineNumbers.startLineNumber + lineBreakPattern.findAll(contentBeforeMarker).count()
        val markerLineText = (
                contentBeforeMarker.lines().last()
                + startMarker
                + rawContentPart.content.substringAfter(startMarker).lines().first()
        ).trim()
        return LineNumbers(
            startLineNumber = markerLineNumber,
            endLineNumber = markerLineNumber,
            context = markerLineText,
            formattedDescription = "Lines $markerLineNumber-$markerLineNumber: '$markerLineText'",
        )
    }
}
