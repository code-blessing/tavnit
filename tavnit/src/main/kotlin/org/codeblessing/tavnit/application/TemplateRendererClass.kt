package org.codeblessing.tavnit.application

import org.codeblessing.tavnit.contentparsing.commandchain.TemplateRendererDescription
import java.nio.file.Path

data class TemplateRendererClass(
    val templateRendererDescription: TemplateRendererDescription,
    val templateRendererClassContent: String,
    val templateRendererClassFilePath: Path
)
