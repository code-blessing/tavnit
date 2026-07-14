package org.codeblessing.tavnit.documentation

import org.codeblessing.tavnit.CommentStyle
import org.codeblessing.tavnit.CommentType
import org.codeblessing.tavnit.config.TavnitConfigReader

object SupportedFileFormatsMarkdownCreator {

    private val STATIC_INTRODUCTION = """
        # Supported file formats

        tavnit recognizes the template commands (```@tt{{{ ... }}}@```) inside the comments of the
        processed source files. Which comment syntax is understood depends on the file extension.

        The following table lists every file extension that is configured out of the box together with the
        comment formats that are supported for it. A file format can support several comment formats; in that
        case any of them can be used to host the template commands.
        
        Supported comment styles are defined in a classpath resource file 
        [tavnit-config.properties](tavnit/src/main/resources/tavnit-config.properties)
        and can be extended by providing an individual
        classpath resource `/tavnit-config-overwrite.properties`.

        ### Configuring your own comment styles and file extensions

        The overwrite file uses the same property format as the built-in configuration. Declare one or
        more named comment styles and then map file extensions to them:

        ```properties
        # A line comment style: give it a unique id (here "lua") and its start delimiter.
        # commentType is either LINE_COMMENT or BLOCK_COMMENT.
        commentStyle.lua.startOfComment=--
        commentStyle.lua.commentType=LINE_COMMENT

        # A block comment style additionally needs an end delimiter.
        commentStyle.luaBlock.startOfComment=--[[
        commentStyle.luaBlock.endOfComment=]]
        commentStyle.luaBlock.commentType=BLOCK_COMMENT

        # Map a file extension (without the leading dot) to one or more comment style ids (comma-separated).
        extension.lua=lua,luaBlock
        ```

        An entry in the overwrite file replaces the entry with the same key in the built-in
        configuration, so you can also redefine the comment styles of an already-supported extension.

    """.trimIndent()

    fun createMarkdownDocumentation(): String {
        val sb = StringBuilder()
        sb.appendLine(STATIC_INTRODUCTION)
        sb.appendLine()
        sb.appendLine("| File Extension | Supported Comment Format |")
        sb.appendLine("| --- | --- |")
        for (fileExtensionCommentStyles in sortedFileExtensionCommentStyles()) {
            val extension = "`.${fileExtensionCommentStyles.extension}`"
            val commentFormats = fileExtensionCommentStyles.commentStyles
                .joinToString(" and ") { it.formatAsMarkdown() }
            sb.appendLine("| $extension | $commentFormats |")
        }
        return sb.toString()
    }

    private fun sortedFileExtensionCommentStyles() =
        TavnitConfigReader.readConfiguration()
            .fileExtensionCommentStyles
            .sortedBy { it.extension }

    private fun CommentStyle.formatAsMarkdown(): String = when (commentType) {
        CommentType.LINE_COMMENT -> "`$startOfComment`"
        CommentType.BLOCK_COMMENT -> "`$startOfComment $endOfComment`"
    }
}
