package org.codeblessing.tavnit.filemapping

import org.codeblessing.tavnit.CommentStyle
import org.codeblessing.tavnit.config.TavnitConfigProvider
import java.nio.file.Path
import kotlin.io.path.extension

object ContentMapper {

    fun mapContent(file: Path): List<CommentStyle> {
        val configuration = TavnitConfigProvider.getConfiguration()
        val fileExtension = file.extension

        return configuration.fileExtensionCommentStyles
            .filter { fileExtension.equals(it.extension, ignoreCase = true) }
            .flatMap { it.commentStyles }
    }
}
