package org.codeblessing.tavnit.contentparsing.resolver

import org.codeblessing.tavnit.contentparsing.KeywordCommand
import org.codeblessing.tavnit.contentparsing.linenumbers.LineNumbers

data class TemplateCommentContentPart(
    override val lineNumbers: LineNumbers,
    val keywordCommands: List<KeywordCommand>
) : TemplateContentPart
