package org.codeblessing.tavnit.config

import org.codeblessing.tavnit.CommentType
import org.codeblessing.tavnit.TavnitException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.Properties

class TavnitConfigReaderTest {

    @Test
    fun `a valid block comment style is read`() {
        val properties = propertiesOf(
            "commentStyle.html.startOfComment" to "<!--",
            "commentStyle.html.endOfComment" to "-->",
            "commentStyle.html.commentType" to "BLOCK_COMMENT",
            "extension.html" to "html",
        )

        val config = TavnitConfigReader.readConfiguration(properties)

        val style = config.namedCommentStyles.getValue("html")
        assertEquals("<!--", style.startOfComment)
        assertEquals("-->", style.endOfComment)
        assertEquals(CommentType.BLOCK_COMMENT, style.commentType)
    }

    @Test
    fun `a block comment style without an end delimiter fails with a clear error`() {
        val properties = propertiesOf(
            "commentStyle.broken.startOfComment" to "<!--",
            "commentStyle.broken.endOfComment" to "",
            "commentStyle.broken.commentType" to "BLOCK_COMMENT",
        )

        val exception = assertThrows<TavnitException> {
            TavnitConfigReader.readConfiguration(properties)
        }
        assertTrue(
            requireNotNull(exception.message).contains("block comment requires an end delimiter"),
            "unexpected message: ${exception.message}",
        )
    }

    @Test
    fun `an unknown comment type fails with a clear error listing the allowed values`() {
        val properties = propertiesOf(
            "commentStyle.weird.startOfComment" to "<!--",
            "commentStyle.weird.endOfComment" to "-->",
            "commentStyle.weird.commentType" to "PARAGRAPH_COMMENT",
        )

        val exception = assertThrows<TavnitException> {
            TavnitConfigReader.readConfiguration(properties)
        }
        assertTrue(
            requireNotNull(exception.message).contains("BLOCK_COMMENT"),
            "expected the allowed values in the message, but was: ${exception.message}",
        )
    }

    @Test
    fun `a missing startOfComment fails with a clear error`() {
        val properties = propertiesOf(
            "commentStyle.nostart.endOfComment" to "-->",
            "commentStyle.nostart.commentType" to "BLOCK_COMMENT",
        )

        val exception = assertThrows<TavnitException> {
            TavnitConfigReader.readConfiguration(properties)
        }
        assertTrue(requireNotNull(exception.message).contains("nostart"), "unexpected message: ${exception.message}")
    }

    @Test
    fun `an extension referencing an unknown comment style fails with a clear error`() {
        val properties = propertiesOf(
            "commentStyle.html.startOfComment" to "<!--",
            "commentStyle.html.endOfComment" to "-->",
            "commentStyle.html.commentType" to "BLOCK_COMMENT",
            "extension.html" to "html,doesNotExist",
        )

        val exception = assertThrows<TavnitException> {
            TavnitConfigReader.readConfiguration(properties)
        }
        assertTrue(
            requireNotNull(exception.message).contains("doesNotExist"),
            "unexpected message: ${exception.message}",
        )
    }

    private fun propertiesOf(vararg pairs: Pair<String, String>): Properties {
        val properties = Properties()
        pairs.forEach { (key, value) -> properties.setProperty(key, value) }
        return properties
    }
}
