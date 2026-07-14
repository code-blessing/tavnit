package org.codeblessing.tavnit.filemapping

object FileEndings {
    val HTML_FILENAME_REGEX: Regex = Regex(".*\\.(html|xhtml)")
    val KOTLIN_FILENAME_REGEX: Regex = Regex(".*\\.kt")
    val TYPESCRIPT_FILENAME_REGEX: Regex = Regex(".*\\.ts")
    val SCSS_FILENAME_REGEX: Regex = Regex(".*\\.scss")
}
