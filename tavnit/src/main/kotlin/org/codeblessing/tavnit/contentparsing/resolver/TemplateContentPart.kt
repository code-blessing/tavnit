package org.codeblessing.tavnit.contentparsing.resolver

import org.codeblessing.tavnit.contentparsing.linenumbers.LineNumbers

sealed interface TemplateContentPart {
    val lineNumbers: LineNumbers
}
