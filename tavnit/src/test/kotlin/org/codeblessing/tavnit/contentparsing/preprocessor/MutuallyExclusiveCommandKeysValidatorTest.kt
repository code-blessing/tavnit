package org.codeblessing.tavnit.contentparsing.preprocessor

import org.codeblessing.tavnit.contentparsing.TemplateParsingErrorCode
import org.codeblessing.tavnit.contentparsing.TemplateParsingException
import org.codeblessing.tavnit.contentparsing.commandchain.ContentPartBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class MutuallyExclusiveCommandKeysValidatorTest {

    @Nested
    inner class PassingCases {

        @Test
        fun `empty list passes validation and is returned unchanged`() {
            val input = ContentPartBuilder.create().build()

            val result = MutuallyExclusiveCommandKeysValidator.validate(input)

            assertEquals(input, result)
        }

        @Test
        fun `text parts are not validated and pass through unchanged`() {
            val input = ContentPartBuilder.create()
                .addText("some text")
                .addText("more text")
                .build()

            val result = MutuallyExclusiveCommandKeysValidator.validate(input)

            assertEquals(input, result)
        }

        @Test
        fun `comment with a single command passes validation`() {
            val input = ContentPartBuilder.create()
                .addTemplateComment().addMoveCommentForwardCommand().end()
                .build()

            val result = MutuallyExclusiveCommandKeysValidator.validate(input)

            assertEquals(input, result)
        }

        @Test
        fun `comment combining non-exclusive commands passes validation`() {
            val input = ContentPartBuilder.create()
                .addTemplateComment()
                .addMoveCommentForwardCommand()
                .addRemoveBlanksBeforeCommentCommand()
                .addRemoveBlanksAfterCommentCommand()
                .end()
                .build()

            val result = MutuallyExclusiveCommandKeysValidator.validate(input)

            assertEquals(input, result)
        }

        @Test
        fun `before and after whitespace commands do not exclude each other`() {
            val input = ContentPartBuilder.create()
                .addTemplateComment()
                .addRemoveBlanksBeforeCommentCommand()
                .addRemoveBlanksAfterCommentCommand()
                .end()
                .build()

            val result = MutuallyExclusiveCommandKeysValidator.validate(input)

            assertEquals(input, result)
        }

        @Test
        fun `same exclusive command keys in separate comments pass validation`() {
            val input = ContentPartBuilder.create()
                .addTemplateComment().addMoveCommentForwardCommand().end()
                .addText("text")
                .addTemplateComment().addMoveCommentBackwardCommand().end()
                .build()

            val result = MutuallyExclusiveCommandKeysValidator.validate(input)

            assertEquals(input, result)
        }
    }

    @Nested
    inner class FailingCases {

        @Test
        fun `move-comment-forward and move-comment-backward in the same comment throw`() {
            val input = ContentPartBuilder.create()
                .addTemplateComment()
                .addMoveCommentForwardCommand()
                .addMoveCommentBackwardCommand()
                .end()
                .build()

            val exception = assertThrows(TemplateParsingException::class.java) {
                MutuallyExclusiveCommandKeysValidator.validate(input)
            }
            assertEquals(TemplateParsingErrorCode.MUTUALLY_EXCLUSIVE_COMMAND_KEYS, exception.errorCode)
        }

        @Test
        fun `remove-blanks-before and remove-blanks-and-linebreak-before throw`() {
            val input = ContentPartBuilder.create()
                .addTemplateComment()
                .addRemoveBlanksBeforeCommentCommand()
                .addRemoveBlanksAndLinebreakBeforeCommentCommand()
                .end()
                .build()

            val exception = assertThrows(TemplateParsingException::class.java) {
                MutuallyExclusiveCommandKeysValidator.validate(input)
            }
            assertEquals(TemplateParsingErrorCode.MUTUALLY_EXCLUSIVE_COMMAND_KEYS, exception.errorCode)
        }

        @Test
        fun `remove-blanks-after and remove-blanks-and-linebreak-after throw`() {
            val input = ContentPartBuilder.create()
                .addTemplateComment()
                .addRemoveBlanksAfterCommentCommand()
                .addRemoveBlanksAndLinebreakAfterCommentCommand()
                .end()
                .build()

            val exception = assertThrows(TemplateParsingException::class.java) {
                MutuallyExclusiveCommandKeysValidator.validate(input)
            }
            assertEquals(TemplateParsingErrorCode.MUTUALLY_EXCLUSIVE_COMMAND_KEYS, exception.errorCode)
        }

        @Test
        fun `error message names both conflicting commands`() {
            val input = ContentPartBuilder.create()
                .addTemplateComment()
                .addMoveCommentForwardCommand()
                .addMoveCommentBackwardCommand()
                .end()
                .build()

            val exception = assertThrows(TemplateParsingException::class.java) {
                MutuallyExclusiveCommandKeysValidator.validate(input)
            }
            val message = requireNotNull(exception.message)
            assert(message.contains("move-comment-forward")) { "Expected message to name move-comment-forward but was: $message" }
            assert(message.contains("move-comment-backward")) { "Expected message to name move-comment-backward but was: $message" }
        }

        @Test
        fun `conflict only in the second comment throws`() {
            val input = ContentPartBuilder.create()
                .addTemplateComment().addMoveCommentForwardCommand().end()
                .addText("text")
                .addTemplateComment()
                .addRemoveBlanksAfterCommentCommand()
                .addRemoveBlanksAndLinebreakAfterCommentCommand()
                .end()
                .build()

            val exception = assertThrows(TemplateParsingException::class.java) {
                MutuallyExclusiveCommandKeysValidator.validate(input)
            }
            assertEquals(TemplateParsingErrorCode.MUTUALLY_EXCLUSIVE_COMMAND_KEYS, exception.errorCode)
        }
    }
}
