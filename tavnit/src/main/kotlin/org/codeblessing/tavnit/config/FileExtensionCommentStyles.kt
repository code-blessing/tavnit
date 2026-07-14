package org.codeblessing.tavnit.config

import org.codeblessing.tavnit.CommentStyle

data class FileExtensionCommentStyles(
    val extension: String,
    val commentStyles: List<CommentStyle>,
)
