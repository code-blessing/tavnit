package org.codeblessing.tavnit.config

import org.codeblessing.tavnit.CommentStyle

data class TavnitConfig(
    val namedCommentStyles: Map<String, CommentStyle>,
    val fileExtensionCommentStyles: List<FileExtensionCommentStyles>,
)
