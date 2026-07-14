package org.codeblessing.tavnit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class TavnitCliTest {

    @Test
    fun `a valid argument set is parsed and the search glob becomes a regex`() {
        val configuration = parseConfiguration(
            arrayOf(
                "--template-renderer", "/out",
                "--search", "/src/main/kotlin:*.kt",
            )
        )

        assertEquals("/out", configuration.templateRendererConfiguration.templateRendererTargetSourceBasePath.toString())
        val location = configuration.fileSearchLocations.single()
        assertEquals("/src/main/kotlin", location.rootDirectoryToSearch.toString())
        val pattern = location.filenameMatchingPattern
        assertTrue(pattern.matches("Foo.kt"), "expected the glob *.kt to match Foo.kt, but pattern was ${pattern.pattern}")
        assertTrue(!pattern.matches("Foo.html"), "expected the glob *.kt not to match Foo.html")
    }

    @Test
    fun `repeated search flags produce multiple search locations`() {
        val configuration = parseConfiguration(
            arrayOf(
                "--template-renderer", "/out",
                "--search", "/a:*.kt",
                "--search", "/b:*.html",
            )
        )

        assertEquals(2, configuration.fileSearchLocations.size)
    }

    @Test
    fun `a missing template-renderer argument fails with a clear message`() {
        val exception = assertFailsWith<IllegalArgumentException> {
            parseConfiguration(arrayOf("--search", "/src:*.kt"))
        }
        assertTrue(requireNotNull(exception.message).contains("--template-renderer"), "unexpected: ${exception.message}")
    }

    @Test
    fun `a missing search argument fails with a clear message`() {
        val exception = assertFailsWith<IllegalArgumentException> {
            parseConfiguration(arrayOf("--template-renderer", "/out"))
        }
        assertTrue(requireNotNull(exception.message).contains("--search"), "unexpected: ${exception.message}")
    }

    @Test
    fun `a search value without a pattern separator fails with a clear message`() {
        val exception = assertFailsWith<IllegalArgumentException> {
            parseConfiguration(arrayOf("--template-renderer", "/out", "--search", "/src-without-colon"))
        }
        assertTrue(requireNotNull(exception.message).contains("expected <path>:<pattern>"), "unexpected: ${exception.message}")
    }
}
