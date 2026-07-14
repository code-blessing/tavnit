package org.codeblessing.tavnit

import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.pathString
import kotlin.io.path.relativeTo

data class RelativeFile(
    val filePath: Path,
    val rootDirectory: Path,
) {
    fun relativeToRootDirectory(): String {
        return filePath.relativeTo(rootDirectory).normalize().pathString
    }

    companion object {
        fun fromRelativeString(relativePath: String): RelativeFile {
            return RelativeFile(filePath = Paths.get(relativePath), rootDirectory = Paths.get(""))
        }
    }
}
