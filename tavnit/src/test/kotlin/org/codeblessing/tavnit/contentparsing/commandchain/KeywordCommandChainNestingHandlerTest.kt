package org.codeblessing.tavnit.contentparsing.commandchain

import org.codeblessing.tavnit.contentparsing.TemplateParsingErrorCode
import org.codeblessing.tavnit.contentparsing.TemplateParsingException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class KeywordCommandChainNestingHandlerTest {

    @Nested
    inner class ValidStructure {

        @Test
        fun `empty list returns empty list`() {
            val input = ContentPartBuilder.create().build()

            val result = KeywordCommandChainNestingHandler.validateAndHandleNestingStructure(input)

            assertEquals(input, result)
        }

        @Test
        fun `text-only parts are passed through unchanged`() {
            val input = ContentPartBuilder.create()
                .addText("first")
                .addText("second")
                .build()

            val result = KeywordCommandChainNestingHandler.validateAndHandleNestingStructure(input)

            assertEquals(input, result)
        }

        @Test
        fun `properly paired if and end-if returns list unchanged`() {
            val input = ContentPartBuilder.create()
                .addTemplateComment()
                    .addIfCommand()
                    .end()
                .addText("inside if")
                .addTemplateComment()
                    .addEndIfCommand()
                    .end()
                .build()

            val result = KeywordCommandChainNestingHandler.validateAndHandleNestingStructure(input)

            assertEquals(input, result)
        }

        @Test
        fun `properly paired foreach and end-foreach returns list unchanged`() {
            val input = ContentPartBuilder.create()
                .addTemplateComment()
                    .addForeachCommand()
                    .end()
                .addText("inside foreach")
                .addTemplateComment()
                    .addEndForeachCommand()
                    .end()
                .build()

            val result = KeywordCommandChainNestingHandler.validateAndHandleNestingStructure(input)

            assertEquals(input, result)
        }

        @Test
        fun `properly paired ignore-text and end-ignore-text returns list unchanged`() {
            val input = ContentPartBuilder.create()
                .addTemplateComment()
                    .addIgnoreTextCommand()
                    .end()
                .addText("ignored text")
                .addTemplateComment()
                    .addEndIgnoreTextCommand()
                    .end()
                .build()

            val result = KeywordCommandChainNestingHandler.validateAndHandleNestingStructure(input)

            assertEquals(input, result)
        }
    }

    @Nested
    inner class AutoclosingSupported {

        @Test
        fun `unclosed ignore-text at end of input is auto-closed`() {
            val input = ContentPartBuilder.create()
                .addTemplateComment()
                    .addIgnoreTextCommand()
                    .end()
                .build()

            val expected = ContentPartBuilder.create()
                .addTemplateComment()
                    .addIgnoreTextCommand()
                    .end()
                .addTemplateComment()
                    .addEndIgnoreTextCommand()
                    .end()
                .build()

            val result = KeywordCommandChainNestingHandler.validateAndHandleNestingStructure(input)

            assertEquals(expected, result)
        }

        @Test
        fun `unclosed replace-value-by-expression at end of input is auto-closed`() {
            val input = ContentPartBuilder.create()
                .addTemplateComment()
                    .addReplaceValueByExpressionCommand()
                    .end()
                .build()

            val expected = ContentPartBuilder.create()
                .addTemplateComment()
                    .addReplaceValueByExpressionCommand()
                    .end()
                .addTemplateComment()
                    .addEndReplaceValueByExpressionCommand()
                    .end()
                .build()

            val result = KeywordCommandChainNestingHandler.validateAndHandleNestingStructure(input)

            assertEquals(expected, result)
        }

        @Test
        fun `multiple nested unclosed autoclosing commands are auto-closed in reverse order`() {
            val input = ContentPartBuilder.create()
                .addTemplateComment()
                    .addReplaceValueByExpressionCommand()
                    .end()
                .addTemplateComment()
                    .addIgnoreTextCommand()
                    .end()
                .build()

            val expected = ContentPartBuilder.create()
                .addTemplateComment()
                    .addReplaceValueByExpressionCommand()
                    .end()
                .addTemplateComment()
                    .addIgnoreTextCommand()
                    .end()
                .addTemplateComment()
                    .addEndIgnoreTextCommand()
                    .end()
                .addTemplateComment()
                    .addEndReplaceValueByExpressionCommand()
                    .end()
                .build()

            val result = KeywordCommandChainNestingHandler.validateAndHandleNestingStructure(input)

            assertEquals(expected, result)
        }

        @Test
        fun `inner autoclosing command is auto-closed before else-clause and inserted before it in the result`() {
            val input = ContentPartBuilder.create()
                .addTemplateComment()
                    .addIfCommand()
                    .end()
                .addTemplateComment()
                    .addReplaceValueByExpressionCommand()
                    .end()
                .addTemplateComment()
                    .addElseCommand()
                    .end()
                .addTemplateComment()
                    .addEndIfCommand()
                    .end()
                .build()

            val expected = ContentPartBuilder.create()
                .addTemplateComment()
                    .addIfCommand()
                    .end()
                .addTemplateComment()
                    .addReplaceValueByExpressionCommand()
                    .end()
                .addTemplateComment()
                    .addEndReplaceValueByExpressionCommand()
                    .end()
                .addTemplateComment()
                    .addElseCommand()
                    .end()
                .addTemplateComment()
                    .addEndIfCommand()
                    .end()
                .build()

            val result = KeywordCommandChainNestingHandler.validateAndHandleNestingStructure(input)

            assertEquals(expected, result)
        }

        @Test
        fun `inner autoclosing command is auto-closed before else-if-clause and inserted before it in the result`() {
            val input = ContentPartBuilder.create()
                .addTemplateComment()
                    .addIfCommand()
                    .end()
                .addTemplateComment()
                    .addIgnoreTextCommand()
                    .end()
                .addTemplateComment()
                    .addElseIfCommand()
                    .end()
                .addTemplateComment()
                    .addEndIfCommand()
                    .end()
                .build()

            val expected = ContentPartBuilder.create()
                .addTemplateComment()
                    .addIfCommand()
                    .end()
                .addTemplateComment()
                    .addIgnoreTextCommand()
                    .end()
                .addTemplateComment()
                    .addEndIgnoreTextCommand()
                    .end()
                .addTemplateComment()
                    .addElseIfCommand()
                    .end()
                .addTemplateComment()
                    .addEndIfCommand()
                    .end()
                .build()

            val result = KeywordCommandChainNestingHandler.validateAndHandleNestingStructure(input)

            assertEquals(expected, result)
        }
    }

    @Nested
    inner class AutoclosingNotSupported {

        @Test
        fun `unclosed if-condition throws TemplateParsingException`() {
            val input = ContentPartBuilder.create()
                .addTemplateComment()
                    .addIfCommand()
                    .end()
                .addText("inside if")
                .build()

            val exception = assertThrows(TemplateParsingException::class.java) {
                KeywordCommandChainNestingHandler.validateAndHandleNestingStructure(input)
            }
            assertEquals(TemplateParsingErrorCode.UNCLOSED_OPENING_COMMAND, exception.errorCode)
        }

        @Test
        fun `unclosed foreach throws TemplateParsingException`() {
            val input = ContentPartBuilder.create()
                .addTemplateComment()
                    .addForeachCommand()
                    .end()
                .addText("inside foreach")
                .build()

            val exception = assertThrows(TemplateParsingException::class.java) {
                KeywordCommandChainNestingHandler.validateAndHandleNestingStructure(input)
            }
            assertEquals(TemplateParsingErrorCode.UNCLOSED_OPENING_COMMAND, exception.errorCode)
        }
    }

    @Nested
    inner class DirectlyNestedCommandViolations {

        @Test
        fun `else nested inside replace-value-by-expression inside if throws TemplateParsingException`() {
            val input = ContentPartBuilder.create()
                .addTemplateComment()
                    .addIfCommand()
                    .end()
                .addTemplateComment()
                    .addReplaceValueByExpressionCommand()
                    .end()
                .addTemplateComment()
                    .addElseCommand()
                    .end()
                .addTemplateComment()
                    .addEndReplaceValueByExpressionCommand()
                    .end()
                .addTemplateComment()
                    .addEndIfCommand()
                    .end()
                .build()

            val exception = assertThrows(TemplateParsingException::class.java) {
                KeywordCommandChainNestingHandler.validateAndHandleNestingStructure(input)
            }
            assertEquals(TemplateParsingErrorCode.MISMATCHED_CLOSING_COMMAND, exception.errorCode)
        }

        @Test
        fun `else without if throws TemplateParsingException`() {
            val input = ContentPartBuilder.create()
                .addTemplateComment()
                    .addElseCommand()
                    .end()
                .build()

            val exception = assertThrows(TemplateParsingException::class.java) {
                KeywordCommandChainNestingHandler.validateAndHandleNestingStructure(input)
            }
            assertEquals(TemplateParsingErrorCode.COMMAND_NOT_DIRECTLY_NESTED, exception.errorCode)
        }
    }

    @Nested
    inner class ClosingWithoutOpening {

        @Test
        fun `end-if without preceding if throws TemplateParsingException`() {
            val input = ContentPartBuilder.create()
                .addTemplateComment()
                    .addEndIfCommand()
                    .end()
                .build()

            val exception = assertThrows(TemplateParsingException::class.java) {
                KeywordCommandChainNestingHandler.validateAndHandleNestingStructure(input)
            }
            assertEquals(TemplateParsingErrorCode.MISMATCHED_CLOSING_COMMAND, exception.errorCode)
        }

        @Test
        fun `end-foreach without preceding foreach throws TemplateParsingException`() {
            val input = ContentPartBuilder.create()
                .addTemplateComment()
                    .addEndForeachCommand()
                    .end()
                .build()

            val exception = assertThrows(TemplateParsingException::class.java) {
                KeywordCommandChainNestingHandler.validateAndHandleNestingStructure(input)
            }
            assertEquals(TemplateParsingErrorCode.MISMATCHED_CLOSING_COMMAND, exception.errorCode)
        }

        @Test
        fun `end-if when innermost open command is foreach throws TemplateParsingException`() {
            val input = ContentPartBuilder.create()
                .addTemplateComment()
                    .addIfCommand()
                    .end()
                .addTemplateComment()
                    .addForeachCommand()
                    .end()
                .addTemplateComment()
                    .addEndIfCommand()
                    .end()
                .build()

            val exception = assertThrows(TemplateParsingException::class.java) {
                KeywordCommandChainNestingHandler.validateAndHandleNestingStructure(input)
            }
            assertEquals(TemplateParsingErrorCode.MISMATCHED_CLOSING_COMMAND, exception.errorCode)
        }

        @Test
        fun `end-ignore-text without preceding ignore-text throws TemplateParsingException`() {
            val input = ContentPartBuilder.create()
                .addTemplateComment()
                    .addEndIgnoreTextCommand()
                    .end()
                .build()

            val exception = assertThrows(TemplateParsingException::class.java) {
                KeywordCommandChainNestingHandler.validateAndHandleNestingStructure(input)
            }
            assertEquals(TemplateParsingErrorCode.MISMATCHED_CLOSING_COMMAND, exception.errorCode)
        }
    }

    @Nested
    inner class ElseClauseOrdering {

        @Test
        fun `if with else-if and else in order passes validation`() {
            val input = ContentPartBuilder.create()
                .addTemplateComment().addIfCommand().end()
                .addText("if branch")
                .addTemplateComment().addElseIfCommand().end()
                .addText("else-if branch")
                .addTemplateComment().addElseCommand().end()
                .addText("else branch")
                .addTemplateComment().addEndIfCommand().end()
                .build()

            val result = KeywordCommandChainNestingHandler.validateAndHandleNestingStructure(input)

            assertEquals(input, result)
        }

        @Test
        fun `duplicate else throws TemplateParsingException`() {
            val input = ContentPartBuilder.create()
                .addTemplateComment().addIfCommand().end()
                .addText("if branch")
                .addTemplateComment().addElseCommand().end()
                .addText("else branch")
                .addTemplateComment().addElseCommand().end()
                .addText("second else branch")
                .addTemplateComment().addEndIfCommand().end()
                .build()

            val exception = assertThrows(TemplateParsingException::class.java) {
                KeywordCommandChainNestingHandler.validateAndHandleNestingStructure(input)
            }
            assertEquals(TemplateParsingErrorCode.COMMAND_AFTER_ELSE_CLAUSE, exception.errorCode)
        }

        @Test
        fun `else-if after else throws TemplateParsingException`() {
            val input = ContentPartBuilder.create()
                .addTemplateComment().addIfCommand().end()
                .addText("if branch")
                .addTemplateComment().addElseCommand().end()
                .addText("else branch")
                .addTemplateComment().addElseIfCommand().end()
                .addText("else-if branch")
                .addTemplateComment().addEndIfCommand().end()
                .build()

            val exception = assertThrows(TemplateParsingException::class.java) {
                KeywordCommandChainNestingHandler.validateAndHandleNestingStructure(input)
            }
            assertEquals(TemplateParsingErrorCode.COMMAND_AFTER_ELSE_CLAUSE, exception.errorCode)
        }

        @Test
        fun `else of a nested if after the else of the outer if passes validation`() {
            val input = ContentPartBuilder.create()
                .addTemplateComment().addIfCommand().end()
                .addText("outer if branch")
                .addTemplateComment().addElseCommand().end()
                .addTemplateComment().addIfCommand().end()
                .addText("inner if branch")
                .addTemplateComment().addElseCommand().end()
                .addText("inner else branch")
                .addTemplateComment().addEndIfCommand().end()
                .addTemplateComment().addEndIfCommand().end()
                .build()

            val result = KeywordCommandChainNestingHandler.validateAndHandleNestingStructure(input)

            assertEquals(input, result)
        }
    }
}
