package org.codeblessing.tavnit.contentparsing.preprocessor

import org.codeblessing.tavnit.contentparsing.TemplateParsingErrorCode
import org.codeblessing.tavnit.contentparsing.TemplateParsingException
import org.codeblessing.tavnit.contentparsing.commandchain.ContentPartBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ContentPartsPreprocessorValidatorTest {

    @Nested
    inner class MoveCommentValidation {

        @Test
        fun `empty list passes validation and is returned unchanged`() {
            val input = ContentPartBuilder.create().build()

            val result = ContentPartsPreprocessorValidator.validatePreprocessing(input)

            assertEquals(input, result)
        }

        @Test
        fun `comment with no move-comment command passes validation`() {
            val input = ContentPartBuilder.create()
                .addTemplateComment().addIfCommand().end()
                .build()

            val result = ContentPartsPreprocessorValidator.validatePreprocessing(input)

            assertEquals(input, result)
        }

        @Test
        fun `comment with exactly one move-comment command passes validation`() {
            val input = ContentPartBuilder.create()
                .addTemplateComment().addMoveCommentForwardCommand().end()
                .build()

            val result = ContentPartsPreprocessorValidator.validatePreprocessing(input)

            assertEquals(input, result)
        }

        @Test
        fun `text parts are not validated and pass through unchanged`() {
            val input = ContentPartBuilder.create()
                .addText("some text")
                .addText("more text")
                .build()

            val result = ContentPartsPreprocessorValidator.validatePreprocessing(input)

            assertEquals(input, result)
        }

        @Test
        fun `comment with two move-comment commands throws exception`() {
            val input = ContentPartBuilder.create()
                .addTemplateComment()
                .addMoveCommentForwardCommand()
                .addMoveCommentBackwardCommand()
                .end()
                .build()

            val exception = assertThrows(TemplateParsingException::class.java) {
                ContentPartsPreprocessorValidator.validatePreprocessing(input)
            }
            assertEquals(TemplateParsingErrorCode.MULTIPLE_MOVE_COMMENT_COMMANDS, exception.errorCode)
        }

        @Test
        fun `comment with three move-comment commands throws exception`() {
            val input = ContentPartBuilder.create()
                .addTemplateComment()
                .addMoveCommentForwardCommand()
                .addMoveCommentForwardCommand()
                .addMoveCommentBackwardCommand()
                .end()
                .build()

            val exception = assertThrows(TemplateParsingException::class.java) {
                ContentPartsPreprocessorValidator.validatePreprocessing(input)
            }
            assertEquals(TemplateParsingErrorCode.MULTIPLE_MOVE_COMMENT_COMMANDS, exception.errorCode)
        }

        @Test
        fun `multiple comments each with at most one move-comment command passes validation`() {
            val input = ContentPartBuilder.create()
                .addTemplateComment().addMoveCommentForwardCommand().end()
                .addText("text")
                .addTemplateComment().addMoveCommentBackwardCommand().end()
                .build()

            val result = ContentPartsPreprocessorValidator.validatePreprocessing(input)

            assertEquals(input, result)
        }

        @Test
        fun `second comment with two move-comment commands throws exception`() {
            val input = ContentPartBuilder.create()
                .addTemplateComment().addMoveCommentForwardCommand().end()
                .addText("text")
                .addTemplateComment()
                .addMoveCommentForwardCommand()
                .addMoveCommentBackwardCommand()
                .end()
                .build()

            val exception = assertThrows(TemplateParsingException::class.java) {
                ContentPartsPreprocessorValidator.validatePreprocessing(input)
            }
            assertEquals(TemplateParsingErrorCode.MULTIPLE_MOVE_COMMENT_COMMANDS, exception.errorCode)
        }

        @Test
        fun `mixed text and comments where valid comment has one move-comment command passes validation`() {
            val input = ContentPartBuilder.create()
                .addText("before")
                .addTemplateComment().addMoveCommentForwardCommand(beforeFirstOccurrenceOf = "X").end()
                .addText("after")
                .build()

            val result = ContentPartsPreprocessorValidator.validatePreprocessing(input)

            assertEquals(input, result)
        }
    }

    @Nested
    inner class RemoveCommentValidation {

        @Test
        fun `comment with no remove-comment commands passes validation`() {
            val input = ContentPartBuilder.create()
                .addTemplateComment().addIfCommand().end()
                .build()

            val result = ContentPartsPreprocessorValidator.validatePreprocessing(input)

            assertEquals(input, result)
        }

        @Test
        fun `comment with one remove-blanks-after-comment passes validation`() {
            val input = ContentPartBuilder.create()
                .addTemplateComment().addRemoveBlanksAfterCommentCommand().end()
                .build()

            val result = ContentPartsPreprocessorValidator.validatePreprocessing(input)

            assertEquals(input, result)
        }

        @Test
        fun `comment with one remove-blanks-before-comment passes validation`() {
            val input = ContentPartBuilder.create()
                .addTemplateComment().addRemoveBlanksBeforeCommentCommand().end()
                .build()

            val result = ContentPartsPreprocessorValidator.validatePreprocessing(input)

            assertEquals(input, result)
        }

        @Test
        fun `comment with one before and one after remove-comment passes validation`() {
            val input = ContentPartBuilder.create()
                .addTemplateComment()
                .addRemoveBlanksAfterCommentCommand()
                .addRemoveBlanksBeforeCommentCommand()
                .end()
                .build()

            val result = ContentPartsPreprocessorValidator.validatePreprocessing(input)

            assertEquals(input, result)
        }

        @Test
        fun `comment with two after remove-comment commands throws exception`() {
            val input = ContentPartBuilder.create()
                .addTemplateComment()
                .addRemoveBlanksAfterCommentCommand()
                .addRemoveBlanksAndLinebreakAfterCommentCommand()
                .end()
                .build()

            val exception = assertThrows(TemplateParsingException::class.java) {
                ContentPartsPreprocessorValidator.validatePreprocessing(input)
            }
            assertEquals(TemplateParsingErrorCode.MULTIPLE_WHITESPACE_COMMENT_COMMANDS, exception.errorCode)
        }

        @Test
        fun `comment with two before remove-comment commands throws exception`() {
            val input = ContentPartBuilder.create()
                .addTemplateComment()
                .addRemoveBlanksBeforeCommentCommand()
                .addRemoveBlanksAndLinebreakBeforeCommentCommand()
                .end()
                .build()

            val exception = assertThrows(TemplateParsingException::class.java) {
                ContentPartsPreprocessorValidator.validatePreprocessing(input)
            }
            assertEquals(TemplateParsingErrorCode.MULTIPLE_WHITESPACE_COMMENT_COMMANDS, exception.errorCode)
        }

        @Test
        fun `comment with two after and one before remove-comment commands throws exception`() {
            val input = ContentPartBuilder.create()
                .addTemplateComment()
                .addRemoveBlanksAfterCommentCommand()
                .addRemoveBlanksAndLinebreakAfterCommentCommand()
                .addRemoveBlanksBeforeCommentCommand()
                .end()
                .build()

            val exception = assertThrows(TemplateParsingException::class.java) {
                ContentPartsPreprocessorValidator.validatePreprocessing(input)
            }
            assertEquals(TemplateParsingErrorCode.MULTIPLE_WHITESPACE_COMMENT_COMMANDS, exception.errorCode)
        }

        @Test
        fun `multiple comments each with at most one remove-comment per position passes validation`() {
            val input = ContentPartBuilder.create()
                .addTemplateComment().addRemoveBlanksAfterCommentCommand().end()
                .addText("text")
                .addTemplateComment().addRemoveBlanksBeforeCommentCommand().end()
                .build()

            val result = ContentPartsPreprocessorValidator.validatePreprocessing(input)

            assertEquals(input, result)
        }

        @Test
        fun `second comment with two after remove-comment commands throws exception`() {
            val input = ContentPartBuilder.create()
                .addTemplateComment().addRemoveBlanksAfterCommentCommand().end()
                .addText("text")
                .addTemplateComment()
                .addRemoveBlanksAfterCommentCommand()
                .addRemoveBlanksAndLinebreakAfterCommentCommand()
                .end()
                .build()

            val exception = assertThrows(TemplateParsingException::class.java) {
                ContentPartsPreprocessorValidator.validatePreprocessing(input)
            }
            assertEquals(TemplateParsingErrorCode.MULTIPLE_WHITESPACE_COMMENT_COMMANDS, exception.errorCode)
        }

        @Test
        fun `comment with one before and one after remove-comment command passes validation`() {
            val input = ContentPartBuilder.create()
                .addTemplateComment()
                .addRemoveBlanksBeforeCommentCommand()
                .addRemoveBlanksAfterCommentCommand()
                .end()
                .build()

            val result = ContentPartsPreprocessorValidator.validatePreprocessing(input)

            assertEquals(input, result)
        }
    }
}
