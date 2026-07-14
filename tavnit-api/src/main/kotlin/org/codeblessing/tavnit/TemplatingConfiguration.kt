package org.codeblessing.tavnit

/**
 * One self-contained tavnit run: where to look for annotated template files and where to write the
 * generated renderer classes.
 *
 * @property fileSearchLocations the directories to scan (each with its own filename filter) for
 *   source files containing `@tt{{{ ... }}}@` commands.
 * @property templateRendererConfiguration where and how the generated renderer classes are written.
 */
data class TemplatingConfiguration(
    val fileSearchLocations: List<FileSearchLocation>,
    val templateRendererConfiguration: TemplateRendererConfiguration,
)
