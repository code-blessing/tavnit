package org.codeblessing.tavnit

import java.nio.file.Path

/**
 * Configures where generated renderer classes are written.
 *
 * @property templateRendererTargetSourceBasePath the base source directory into which renderer
 *   classes are generated. Each template's declared package (`templateRendererPackageName`) is
 *   appended to this path, so it should point at a source root (e.g. `src/generated/kotlin`).
 */
data class TemplateRendererConfiguration(
    val templateRendererTargetSourceBasePath: Path,
)
