package org.codeblessing.tavnit

import java.nio.file.Path

/**
 * A directory to search for template source files, together with a filename filter.
 *
 * @property rootDirectoryToSearch the directory that is searched recursively for template files.
 * @property filenameMatchingPattern a [Regex] matched against each candidate file name (not a glob).
 *   For example, use `Regex(".*\\.kt")` to match Kotlin files. Note that the command-line interface
 *   (`--search <path>:<pattern>`) instead expects a glob such as `*.kt`.
 */
data class FileSearchLocation(
    val rootDirectoryToSearch: Path,
    val filenameMatchingPattern: Regex,
)
