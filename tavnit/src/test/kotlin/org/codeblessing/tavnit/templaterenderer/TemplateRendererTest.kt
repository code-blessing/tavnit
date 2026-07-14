package org.codeblessing.tavnit.templaterenderer

import org.codeblessing.tavnit.ClasspathResourceLoader
import org.codeblessing.tavnit.RelativeFile
import org.codeblessing.tavnit.contentparsing.ContentParser
import org.codeblessing.tavnit.CommentStyles.KOTLIN_COMMENT_STYLES
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TemplateRendererTest {

    @Test
    fun `create template content from file`() {
        val contentToParse = ClasspathResourceLoader.loadClasspathResource(
            classpathResourcePath = "org/codeblessing/tavnit/templaterenderer/TemplateRendererTest-content-to-parse.kt.txt",
        )

        val expectedContent = ClasspathResourceLoader.loadClasspathResource(
            classpathResourcePath = "org/codeblessing/tavnit/templaterenderer/TemplateRendererTest-expected-content.txt",
        )


        val templates = ContentParser.parseContent(content = contentToParse, KOTLIN_COMMENT_STYLES)

        assertEquals(1, templates.size)

        val template = templates.single()
        val filepath = RelativeFile.fromRelativeString("dummy-dir/author-subdir/dummy-Author.txt")
        val kotlinTemplateContent = TemplateRendererContentCreator.createMultilineStringTemplateContent(filepath, template)
        val kotlinTemplateRendererClassContent = TemplateRendererClassContentCreator.wrapInKotlinClassContent(filepath, template, kotlinTemplateContent)

        println("--- Kotlin Template ------------------")
        println(kotlinTemplateRendererClassContent)
        println("--------------------------------------")
        assertEquals(expectedContent, kotlinTemplateRendererClassContent)
    }
}
