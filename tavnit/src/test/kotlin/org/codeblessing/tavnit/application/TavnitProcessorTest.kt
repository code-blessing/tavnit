package org.codeblessing.tavnit.application

import org.codeblessing.tavnit.FileSearchLocation
import org.codeblessing.tavnit.TavnitException
import org.codeblessing.tavnit.TemplateRendererConfiguration
import org.codeblessing.tavnit.TemplatingConfiguration
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText

class TavnitProcessorTest {

    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `two templates generating the same renderer class file fail with a clear error`() {
        val sourceDirectory = tempDir.resolve("source").createDirectories()
        writeTemplateFile(sourceDirectory, "first.html", rendererClassName = "MyRenderer")
        writeTemplateFile(sourceDirectory, "second.html", rendererClassName = "MyRenderer")

        val exception = assertThrows<TavnitException> {
            TavnitProcessor().processTavnit(listOf(templatingConfiguration(sourceDirectory)))
        }
        assertTrue(
            requireNotNull(exception.message).contains("was already generated from the template file"),
            "expected a duplicate generated file error, but was: ${exception.message}",
        )
    }

    @Test
    fun `a parse error is reported with the file name and line in a single message`() {
        val sourceDirectory = tempDir.resolve("source").createDirectories()
        sourceDirectory.resolve("broken.html").writeText(
            """
                <!-- @tt{{{ @template-renderer [ templateRendererClassName="R" templateRendererPackageName="p" ] }}}@ -->
                <p>text</p>
                <!-- @tt{{{ @foreach [ iteratorExpression="items" loopVariable="item" ] }}}@ -->
                <li>an item</li>
            """.trimIndent()
        )

        val exception = assertThrows<TavnitException> {
            TavnitProcessor().processTavnit(listOf(templatingConfiguration(sourceDirectory)))
        }
        val message = requireNotNull(exception.message)
        assertTrue(message.contains("broken.html"), "expected the file name in the message, but was: $message")
        assertTrue(message.contains("Lines 3"), "expected the line of the unclosed command, but was: $message")
        assertTrue(message.contains("end-foreach"), "expected the reason in the message, but was: $message")
    }

    @Test
    fun `overlapping search locations process a template file only once`() {
        val sourceDirectory = tempDir.resolve("source").createDirectories()
        writeTemplateFile(sourceDirectory, "first.html", rendererClassName = "MyRenderer")

        val configuration = templatingConfiguration(sourceDirectory, sourceDirectory)

        val generatedFiles = TavnitProcessor().processTavnit(listOf(configuration))

        assertEquals(1, generatedFiles.getValue(configuration).size)
    }

    private fun writeTemplateFile(directory: Path, filename: String, rendererClassName: String) {
        directory.resolve(filename).writeText(
            """
                <!-- @tt{{{ @template-renderer [ templateRendererClassName="$rendererClassName" templateRendererPackageName="examples" ] }}}@ -->
                <p>hello</p>
            """.trimIndent()
        )
    }

    private fun templatingConfiguration(vararg sourceDirectories: Path): TemplatingConfiguration {
        return TemplatingConfiguration(
            fileSearchLocations = sourceDirectories.map { sourceDirectory ->
                FileSearchLocation(
                    rootDirectoryToSearch = sourceDirectory,
                    filenameMatchingPattern = Regex(".*\\.html"),
                )
            },
            templateRendererConfiguration = TemplateRendererConfiguration(
                templateRendererTargetSourceBasePath = tempDir.resolve("generated"),
            ),
        )
    }
}
