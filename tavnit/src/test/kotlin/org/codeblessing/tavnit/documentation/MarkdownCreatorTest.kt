package org.codeblessing.tavnit.documentation

import org.junit.jupiter.api.Test

class MarkdownCreatorTest {
    @Test
    fun `print command reference markdown text`() {
        println(CommandReferenceMarkdownCreator.createMarkdownDocumentation())
    }

    @Test
    fun `print main function usage markdown text`() {
        println(MainFunctionUsageMarkdownCreator.createMarkdownDocumentation())
    }
}
