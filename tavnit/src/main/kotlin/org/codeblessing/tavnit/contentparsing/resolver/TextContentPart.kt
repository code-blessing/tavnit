package org.codeblessing.tavnit.contentparsing.resolver

import org.codeblessing.tavnit.contentparsing.linenumbers.LineNumbers

data class TextContentPart(
    override val lineNumbers: LineNumbers,
    val text: String
) : TemplateContentPart
