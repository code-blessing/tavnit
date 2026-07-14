package org.codeblessing.tavnit

data class CommentStyle(
    val startOfComment: String,
    val endOfComment: String?, // not necessary for line comments
    val commentType: CommentType,
)

