package org.codeblessing.tavnit.blackboxtests

import org.codeblessing.tavnit.ClasspathResourceLoader
import org.codeblessing.tavnit.ClasspathResourceWriter
import org.junit.jupiter.api.Assertions.assertEquals
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.exists
import kotlin.io.path.isReadable
import kotlin.io.path.readText
import kotlin.test.assertTrue

private const val CREATE_FILE_IF_NOT_EXISTS = false // do only active during development
private const val OVERWRITE_EXPECTED_TEXT = false // do only active during development
private const val VERBOSE = false // do only active during development
private const val BASE_PROJECT_PATH_PROPERTY = "blackbox.baseProjectPath"
private const val CLASSPATH_RESOURCE_PATH = "org/codeblessing/tavnit/blackboxtests"

abstract class AbstractBlackboxTest {

    private fun baseProjectPath(): Path {
        return Paths.get(System.getProperty(BASE_PROJECT_PATH_PROPERTY, System.getProperty("user.dir")))
    }

    fun htmlSourcePath(): Path {
        return baseProjectPath().resolve("src/main/resources/html")
    }

    fun htmlGeneratedPath(): Path {
        return baseProjectPath().resolve("src/generated/resources/html")
    }

    fun kotlinSourcePath(): Path {
        return baseProjectPath().resolve("src/main/kotlin")
    }

    fun kotlinGeneratedPath(): Path {
        return baseProjectPath().resolve("src/generated/kotlin")
    }

    fun assertSameContent(fileWithTavnitSyntax: Path, generatedFile: Path, expectedContentResourceName: String) {
        assertTrue(fileWithTavnitSyntax.exists(), "$fileWithTavnitSyntax doesn't exist")

        assertTrue(generatedFile.exists(), "$generatedFile doesn't exist")
        assertTrue(generatedFile.isReadable(), "$generatedFile exists but is not readable")
        val actualContent = generatedFile.readText(Charsets.UTF_8)

        val classpathResourcePath = "$CLASSPATH_RESOURCE_PATH/$expectedContentResourceName"
        val expectedContent = if (OVERWRITE_EXPECTED_TEXT) {

            ClasspathResourceWriter.writeClasspathResource(
                classpathResourcePath = classpathResourcePath,
                text = actualContent,
                createFileIfNotExists = CREATE_FILE_IF_NOT_EXISTS
            )
            actualContent
        } else {
            ClasspathResourceLoader.loadClasspathResource(
                classpathResourcePath = "$CLASSPATH_RESOURCE_PATH/$expectedContentResourceName",
                suffixToRemove = ""
            )
        }

        if (VERBOSE) {
            println("----------------------------------------------")
            println("---- FILE WITH TAVNIT SYNTAX")
            println("----------------------------------------------")
            println(fileWithTavnitSyntax.readText(Charsets.UTF_8))
            println("----------------------------------------------")
            println("---- GENERATED CONTENT")
            println("----------------------------------------------")
            println(actualContent)
            println("----------------------------------------------")
            println("---- EXPECTED CONTENT")
            println("----------------------------------------------")
            if(actualContent == expectedContent) {
                println("<same as generated content>")
            } else {
                println(expectedContent)
            }
            println("----------------------------------------------")
            println("---- $expectedContentResourceName")
            println("----------------------------------------------")
            println()
            println()
        }

        assertEquals(expectedContent, actualContent)
    }
}
