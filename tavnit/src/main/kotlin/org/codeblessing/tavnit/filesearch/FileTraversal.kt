package org.codeblessing.tavnit.filesearch

import org.codeblessing.tavnit.FileSearchLocation
import org.codeblessing.tavnit.RelativeFile
import org.codeblessing.tavnit.TavnitException
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.isDirectory
import kotlin.io.path.name


object FileTraversal {
    fun searchFiles(fileSearchLocations: List<FileSearchLocation>): List<RelativeFile> {
        return fileSearchLocations
            .flatMap { fileSearchLocation -> searchRecursivelyInFileLocation(fileSearchLocation) }
            .distinct() // overlapping search locations must not process the same file twice
    }

    private fun searchRecursivelyInFileLocation(fileSearchLocation: FileSearchLocation): List<RelativeFile> {
        val rootDirectory = fileSearchLocation.rootDirectoryToSearch
        if (!rootDirectory.isDirectory()) {
            // fail loudly instead of silently producing zero renderers on a mistyped path
            throw TavnitException(
                "The configured search root directory '${rootDirectory.absolutePathString()}' does not exist " +
                "or is not a directory."
            )
        }
        return walkTopDown(
            rootDirectory = rootDirectory,
            filenameMatchingPattern = fileSearchLocation.filenameMatchingPattern
        )
    }

    private fun walkTopDown(rootDirectory: Path, filenameMatchingPattern: Regex): List<RelativeFile> {
        return rootDirectory
            .toFile()
            .walkTopDown()
            .filter { it.isFile }
            .map { it.toPath() }
            .filter { isFileMatching(it, filenameMatchingPattern) }
            .map { RelativeFile(filePath = it, rootDirectory = rootDirectory,) }
            .toList()
    }

    private fun isFileMatching(file: Path, filenamePattern: Regex): Boolean {
        return filenamePattern.matches(file.name)
    }
}
