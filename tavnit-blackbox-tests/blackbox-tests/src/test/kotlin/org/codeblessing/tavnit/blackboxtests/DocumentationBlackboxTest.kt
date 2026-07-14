package org.codeblessing.tavnit.blackboxtests

import org.junit.jupiter.api.Test

class DocumentationBlackboxTest : AbstractBlackboxTest() {

    @Test
    fun `test output of kotlin enum template commands`() {
        assertSameContent(
            htmlSourcePath().resolve("documentation/news.html"),
            htmlGeneratedPath().resolve("documentation/today-news.html"),
            "documentation/today-news.expectation.html",
        )
    }
}
