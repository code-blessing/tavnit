package org.codeblessing.tavnit.filemapping

import org.codeblessing.tavnit.filemapping.FileEndings.HTML_FILENAME_REGEX
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ContentMapperTest {

    @Test
    fun `assert that files ending with html are matching HTML_FILENAME_REGEX`() {
        assertEquals(true, HTML_FILENAME_REGEX.matches("asdf.html"))
        assertEquals(true, HTML_FILENAME_REGEX.matches("my-address-form.html"))
        assertEquals(true, HTML_FILENAME_REGEX.matches("asdf.xhtml"))
        assertEquals(true, HTML_FILENAME_REGEX.matches("my-address-form.component.xhtml"))
    }

    @Test
    fun `assert that files not ending with html are not matching HTML_FILENAME_REGEX`() {
        assertEquals(false, HTML_FILENAME_REGEX.matches("asdf.scss"))
    }

}
