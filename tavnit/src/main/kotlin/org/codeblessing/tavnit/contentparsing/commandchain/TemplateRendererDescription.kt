package org.codeblessing.tavnit.contentparsing.commandchain

import org.codeblessing.tavnit.contentparsing.resolver.TemplateContentPart

data class TemplateRendererDescription(
    val templateRendererClass: ClassDescription,
    val templateRendererInterface: ClassDescription?,
    val modelClasses: List<ModelDescription>,
    val templateChain: List<TemplateContentPart>,
)
