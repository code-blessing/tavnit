package org.codeblessing.tavnit.filesearch

import org.codeblessing.tavnit.FileSearchLocation
import org.codeblessing.tavnit.TavnitException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.createFile

class FileTraversalTest {

    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `a nonexistent search root directory fails with a clear error`() {
        val missing = tempDir.resolve("does-not-exist")

        val exception = assertThrows<TavnitException> {
            FileTraversal.searchFiles(
                listOf(FileSearchLocation(rootDirectoryToSearch = missing, filenameMatchingPattern = Regex(".*")))
            )
        }
        assertTrue(
            requireNotNull(exception.message).contains("does not exist or is not a directory"),
            "unexpected message: ${exception.message}",
        )
    }

    @Test
    fun `a search root that is a file rather than a directory fails with a clear error`() {
        val file = tempDir.resolve("a-file.html").createFile()

        assertThrows<TavnitException> {
            FileTraversal.searchFiles(
                listOf(FileSearchLocation(rootDirectoryToSearch = file, filenameMatchingPattern = Regex(".*")))
            )
        }
    }

    @Test
    fun `an existing directory returns its matching files`() {
        tempDir.resolve("first.html").createFile()
        tempDir.resolve("second.kt").createFile()

        val found = FileTraversal.searchFiles(
            listOf(FileSearchLocation(rootDirectoryToSearch = tempDir, filenameMatchingPattern = Regex(".*\\.html")))
        )

        assertEquals(1, found.size)
        assertEquals("first.html", found.single().filePath.fileName.toString())
    }
}
