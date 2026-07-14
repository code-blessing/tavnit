package org.codeblessing.tavnit.contentparsing.commentparser

import org.codeblessing.tavnit.contentparsing.TemplateParsingErrorCode
import org.codeblessing.tavnit.contentparsing.TemplateParsingException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class TemplateCommentParserTest {

    @Nested
    inner class SingleCommandComment {

        @Test
        fun `parseComment handles basic command without brackets`() {
            val commentText = "@my-keyword"
            val commandStructure = parseCommentExpectingSingeResult(commentText)
            assertEquals("my-keyword", commandStructure.keyword)
            assertTrue(commandStructure.brackets.isEmpty())
        }

        @Test
        fun `parseComment handles command with single bracket containing key-value`() {
            val commentText = """@my-keyword[foo="bar"]"""
            val commandStructure = parseCommentExpectingSingeResult(commentText)

            assertEquals("my-keyword", commandStructure.keyword)
            assertEquals(1, commandStructure.brackets.size)
            assertEquals("bar", commandStructure.brackets[0]["foo"])
        }

        @Test
        fun `parseComment handles command with single bracket containing multiple key-value`() {
            val commentText = """@my-keyword[foo="bar" far="baz"]"""
            val commandStructure = parseCommentExpectingSingeResult(commentText)

            assertEquals("my-keyword", commandStructure.keyword)
            assertEquals(1, commandStructure.brackets.size)
            assertEquals("bar", commandStructure.brackets[0]["foo"])
            assertEquals("baz", commandStructure.brackets[0]["far"])
        }

        @Test
        fun `parseComment handles command with multiple brackets`() {
            val commentText = """@my-keyword[foo="bar"][fox="bar2"]"""
            val commandStructure = parseCommentExpectingSingeResult(commentText)

            assertEquals("my-keyword", commandStructure.keyword)
            assertEquals(2, commandStructure.brackets.size)
            assertEquals("bar", commandStructure.brackets[0]["foo"])
            assertEquals("bar2", commandStructure.brackets[1]["fox"])
        }

        @Test
        fun `parseComment handles command with multiple brackets containing multiple key-value pairs`() {
            val commentText = """@my-keyword[foo="bar" far="baz"][fox="bar2" far="baz"]"""
            val commandStructure = parseCommentExpectingSingeResult(commentText)

            assertEquals("my-keyword", commandStructure.keyword)
            assertEquals(2, commandStructure.brackets.size)
            assertEquals("bar", commandStructure.brackets[0]["foo"])
            assertEquals("baz", commandStructure.brackets[0]["far"])
            assertEquals("bar2", commandStructure.brackets[1]["fox"])
            assertEquals("baz", commandStructure.brackets[1]["far"])
        }

        @Test
        fun `parseComment handles whitespace before at-sign`() {
            val commentText = """   @my-keyword[foo="bar"]"""
            val commandStructure = parseCommentExpectingSingeResult(commentText)

            assertEquals("my-keyword", commandStructure.keyword)
            assertEquals("bar", commandStructure.brackets[0]["foo"])
        }

        @Test
        fun `parseComment handles whitespace after keyword`() {
            val commentText = """@my-keyword  [foo="bar"]"""
            val commandStructure = parseCommentExpectingSingeResult(commentText)

            assertEquals("my-keyword", commandStructure.keyword)
            assertEquals("bar", commandStructure.brackets[0]["foo"])
        }

        @Test
        fun `parseComment handles whitespace after command`() {
            val commentText = """@my-keyword[foo="bar"]   """
            val commandStructure = parseCommentExpectingSingeResult(commentText)

            assertEquals("my-keyword", commandStructure.keyword)
            assertEquals("bar", commandStructure.brackets[0]["foo"])
        }

        @Test
        fun `parseComment handles special characters in value`() {
            val commentText = """@my-keyword[foo="ba,  $#@+^d\*pi&/-vr"]   """
            val commandStructure = parseCommentExpectingSingeResult(commentText)

            assertEquals("my-keyword", commandStructure.keyword)
            assertEquals("""ba,  $#@+^d\*pi&/-vr""", commandStructure.brackets[0]["foo"])
        }

        @Test
        fun `parseComment handles opening bracket in value`() {
            val commentText = """@my-keyword[foo="ba[r"]   """
            val commandStructure = parseCommentExpectingSingeResult(commentText)

            assertEquals("my-keyword", commandStructure.keyword)
            assertEquals("ba[r", commandStructure.brackets[0]["foo"])
        }

        @Test
        fun `parseComment handles closing bracket in value`() {
            val commentText = """@my-keyword[foo="ba]r"]   """
            val commandStructure = parseCommentExpectingSingeResult(commentText)

            assertEquals("my-keyword", commandStructure.keyword)
            assertEquals("ba]r", commandStructure.brackets[0]["foo"])
        }

        @Test
        fun `parseComment handles line breaks`() {
            val commentText = """

             @my-keyword[foo="bar"]

             """.trimIndent()
            val commandStructure = parseCommentExpectingSingeResult(commentText)

            assertEquals("my-keyword", commandStructure.keyword)
            assertEquals("bar", commandStructure.brackets[0]["foo"])
        }

        @Test
        fun `parseComment handles whitespace in brackets`() {
            val commentText = """@my-keyword [ foo="bar" ][ fox="bar2" ]"""
            val commandStructure = parseCommentExpectingSingeResult(commentText)

            assertEquals("my-keyword", commandStructure.keyword)
            assertEquals(2, commandStructure.brackets.size)
            assertEquals("bar", commandStructure.brackets[0]["foo"])
            assertEquals("bar2", commandStructure.brackets[1]["fox"])
        }

        @Test
        fun `parseComment handles whitespace in brackets with multiple key-value pairs`() {
            val commentText = """@my-keyword [ foo="bar"   far="baz"  ][ fox="bar2" ]"""
            val commandStructure = parseCommentExpectingSingeResult(commentText)

            assertEquals("my-keyword", commandStructure.keyword)
            assertEquals(2, commandStructure.brackets.size)
            assertEquals("bar", commandStructure.brackets[0]["foo"])
            assertEquals("bar2", commandStructure.brackets[1]["fox"])
        }

        @Test
        fun `parseComment handles whitespace between brackets`() {
            val commentText = """@my-keyword [foo="bar"] [fox="bar2"]"""
            val commandStructure = parseCommentExpectingSingeResult(commentText)

            assertEquals("my-keyword", commandStructure.keyword)
            assertEquals(2, commandStructure.brackets.size)
            assertEquals("bar", commandStructure.brackets[0]["foo"])
            assertEquals("bar2", commandStructure.brackets[1]["fox"])
        }

        @Test
        fun `parseComment handles complex whitespace scenario`() {
            val commentText = """


              @my-keyword  [  foo="bar"  ]  [  fox="bar2 bar2"  ]

                """.trimIndent()
            val commandStructure = parseCommentExpectingSingeResult(commentText)

            assertEquals("my-keyword", commandStructure.keyword)
            assertEquals(2, commandStructure.brackets.size)
            assertEquals("bar", commandStructure.brackets[0]["foo"])
            assertEquals("bar2 bar2", commandStructure.brackets[1]["fox"])
        }

        @Test
        fun `parseComment handles complex newline scenario`() {
            val commentText = """


              @my-keyword  [
                foo="bar"
                fee="gain"
                fox="trot"
              ]
              [
                flex="grid" fox="fit"
                fix="fox"
              ]

                """.trimIndent()
            val commandStructure = parseCommentExpectingSingeResult(commentText)

            assertEquals("my-keyword", commandStructure.keyword)
            assertEquals(2, commandStructure.brackets.size)
            assertEquals("bar", commandStructure.brackets[0]["foo"])
            assertEquals("gain", commandStructure.brackets[0]["fee"])
            assertEquals("trot", commandStructure.brackets[0]["fox"])
            assertEquals("grid", commandStructure.brackets[1]["flex"])
            assertEquals("fit", commandStructure.brackets[1]["fox"])
            assertEquals("fox", commandStructure.brackets[1]["fix"])
        }

        @Test
        fun `parseComment throws exception for empty string`() {
            val commentText = ""
            val exception = assertThrows<TemplateParsingException> {
                TemplateCommentParser.parseComment(commentText)
            }
            assertEquals(TemplateParsingErrorCode.INVALID_COMMENT_STRUCTURE, exception.errorCode)
        }

        @Test
        fun `parseComment throws exception with empty brackets`() {
            val commentText = """@my-keyword[]"""
            val exception = assertThrows<TemplateParsingException> {
                TemplateCommentParser.parseComment(commentText)
            }
            assertEquals(TemplateParsingErrorCode.INVALID_COMMENT_STRUCTURE, exception.errorCode)
        }

        @Test
        fun `parseComment throws exception with multiple empty brackets`() {
            val commentText = """@my-keyword[][foo="bar"][]"""
            val exception = assertThrows<TemplateParsingException> {
                TemplateCommentParser.parseComment(commentText)
            }
            assertEquals(TemplateParsingErrorCode.INVALID_COMMENT_STRUCTURE, exception.errorCode)
        }

        @Test
        fun `parseComment throws exception for duplicate keys`() {
            val commentText = """@my-keyword[foo="bar" foo="bar2"]"""
            val exception = assertThrows<TemplateParsingException> {
                TemplateCommentParser.parseComment(commentText)
            }
            assertEquals(TemplateParsingErrorCode.DUPLICATE_ATTRIBUTE_KEY, exception.errorCode)
        }

        @Test
        fun `parseComment throws exception for whitespaces between key and equal sign`() {
            val commentText = """@my-keyword[foo  ="bar"][fox="bar2 bar2"]"""
            val exception = assertThrows<TemplateParsingException> {
                TemplateCommentParser.parseComment(commentText)
            }
            assertEquals(TemplateParsingErrorCode.INVALID_COMMENT_STRUCTURE, exception.errorCode)
        }

        @Test
        fun `parseComment throws exception for whitespaces between equal sign and value`() {
            val commentText = """@my-keyword[foo=  "bar"][fox="bar2 bar2"]"""
            val exception = assertThrows<TemplateParsingException> {
                TemplateCommentParser.parseComment(commentText)
            }
            assertEquals(TemplateParsingErrorCode.INVALID_COMMENT_STRUCTURE, exception.errorCode)
        }

        @Test
        fun `parseComment throws exception for whitespaces between key and value`() {
            val commentText = """@my-keyword[foo  =  "bar"][fox="bar2 bar2"]"""
            val exception = assertThrows<TemplateParsingException> {
                TemplateCommentParser.parseComment(commentText)
            }
            assertEquals(TemplateParsingErrorCode.INVALID_COMMENT_STRUCTURE, exception.errorCode)
        }

        @Test
        fun `parseComment throws exception for key-value pairs with comma as separator`() {
            val commentText = """@my-keyword[foo="bar",far="baz"]"""
            val exception = assertThrows<TemplateParsingException> {
                TemplateCommentParser.parseComment(commentText)
            }
            assertEquals(TemplateParsingErrorCode.INVALID_COMMENT_STRUCTURE, exception.errorCode)
        }

        @Test
        fun `parseComment throws exception for key-value pairs missing a space as separator`() {
            val commentText = """@my-keyword[foo="bar"far="baz"]"""
            val exception = assertThrows<TemplateParsingException> {
                TemplateCommentParser.parseComment(commentText)
            }
            assertEquals(TemplateParsingErrorCode.INVALID_COMMENT_STRUCTURE, exception.errorCode)
        }

        @Test
        fun `parseComment throws exception for unquoted values`() {
            val commentText = """@my-keyword[foo=bar]"""
            val exception = assertThrows<TemplateParsingException> {
                TemplateCommentParser.parseComment(commentText)
            }
            assertEquals(TemplateParsingErrorCode.INVALID_COMMENT_STRUCTURE, exception.errorCode)
        }

        @Test
        fun `parseComment throws exception for value with quote character`() {
            val commentText = """@my-keyword[foo="ba"r"]"""
            val exception = assertThrows<TemplateParsingException> {
                TemplateCommentParser.parseComment(commentText)
            }
            assertEquals(TemplateParsingErrorCode.INVALID_COMMENT_STRUCTURE, exception.errorCode)
        }

        @Test
        fun `parseComment throws exception for key-value pairs with missing quote at start`() {
            val commentText = """@my-keyword[foo=bar"][fox="bar2 bar2"]"""
            val exception = assertThrows<TemplateParsingException> {
                TemplateCommentParser.parseComment(commentText)
            }
            assertEquals(TemplateParsingErrorCode.INVALID_COMMENT_STRUCTURE, exception.errorCode)
        }

        @Test
        fun `parseComment throws exception for key-value pairs with missing quote at the end`() {
            val commentText = """@my-keyword[foo="bar][fox="bar2 bar2"]"""
            val exception = assertThrows<TemplateParsingException> {
                TemplateCommentParser.parseComment(commentText)
            }
            assertEquals(TemplateParsingErrorCode.INVALID_COMMENT_STRUCTURE, exception.errorCode)
        }

        @Test
        fun `parseComment throws exception for missing closing bracket`() {
            val commentText = """@my-keyword[foo="bar"[fox="bar2 bar2"]"""
            val exception = assertThrows<TemplateParsingException> {
                TemplateCommentParser.parseComment(commentText)
            }
            assertEquals(TemplateParsingErrorCode.INVALID_COMMENT_STRUCTURE, exception.errorCode)
        }

        @Test
        fun `parseComment throws exception for missing opening bracket`() {
            val commentText = """@my-keyword [foo="bar"]fox="bar2 bar2"]"""
            val exception = assertThrows<TemplateParsingException> {
                TemplateCommentParser.parseComment(commentText)
            }
            assertEquals(TemplateParsingErrorCode.INVALID_COMMENT_STRUCTURE, exception.errorCode)
        }

        @Test
        fun `parseComment throws exception for attribute key with special characters`() {
            val commentText = """@my-keyword [foo*="bar"]"""
            val exception = assertThrows<TemplateParsingException> {
                TemplateCommentParser.parseComment(commentText)
            }
            assertEquals(TemplateParsingErrorCode.INVALID_COMMENT_STRUCTURE, exception.errorCode)
        }

        fun parseCommentExpectingSingeResult(comment: String): CommandStructure {
            val commands = TemplateCommentParser.parseComment(comment)
            assertEquals(1, commands.size, "Expected comment to have exactly one command")
            return commands.single()
        }
    }

    @Nested
    inner class EscapingAndDecoding {

        @Test
        fun `parseComment decodes escaped double quote in value`() {
            val commentText = """@my-keyword[foo="ba\"r"]"""
            val commandStructure = parseCommentExpectingSingleResult(commentText)

            assertEquals("my-keyword", commandStructure.keyword)
            assertEquals("""ba"r""", commandStructure.brackets[0]["foo"])
        }

        @Test
        fun `parseComment decodes escaped double quote as entire value`() {
            val commentText = """@my-keyword[foo="\""]"""
            val commandStructure = parseCommentExpectingSingleResult(commentText)

            assertEquals("my-keyword", commandStructure.keyword)
            assertEquals("\"", commandStructure.brackets[0]["foo"])
        }

        @Test
        fun `parseComment decodes escaped backslash in value`() {
            val commentText = """@my-keyword[foo="ba\\r"]"""
            val commandStructure = parseCommentExpectingSingleResult(commentText)

            assertEquals("my-keyword", commandStructure.keyword)
            assertEquals("""ba\r""", commandStructure.brackets[0]["foo"])
        }

        @Test
        fun `parseComment decodes escaped backslash at end of value`() {
            val commentText = """@my-keyword[foo="bar\\"]"""
            val commandStructure = parseCommentExpectingSingleResult(commentText)

            assertEquals("my-keyword", commandStructure.keyword)
            assertEquals("""bar\""", commandStructure.brackets[0]["foo"])
        }

        @Test
        fun `parseComment decodes multiple escaped characters in value`() {
            val commentText = """@my-keyword[foo="a\"b\\c\"d"]"""
            val commandStructure = parseCommentExpectingSingleResult(commentText)

            assertEquals("my-keyword", commandStructure.keyword)
            assertEquals("""a"b\c"d""", commandStructure.brackets[0]["foo"])
        }

        @Test
        fun `parseComment decodes escaped characters across multiple key-value pairs`() {
            val commentText = """@my-keyword[foo="say \"hello\"" bar="path\\to\\file"]"""
            val commandStructure = parseCommentExpectingSingleResult(commentText)

            assertEquals("my-keyword", commandStructure.keyword)
            assertEquals("""say "hello"""", commandStructure.brackets[0]["foo"])
            assertEquals("""path\to\file""", commandStructure.brackets[0]["bar"])
        }

        @Test
        fun `parseComment decodes escaped characters across multiple brackets`() {
            val commentText = """@my-keyword[foo="\"quoted\""][bar="back\\slash"]"""
            val commandStructure = parseCommentExpectingSingleResult(commentText)

            assertEquals("my-keyword", commandStructure.keyword)
            assertEquals(""""quoted"""", commandStructure.brackets[0]["foo"])
            assertEquals("""back\slash""", commandStructure.brackets[1]["bar"])
        }

        @Test
        fun `parseComment throws exception for unescaped double quote inside value`() {
            val commentText = """@my-keyword[foo="ba"r"]"""
            val exception = assertThrows<TemplateParsingException> {
                TemplateCommentParser.parseComment(commentText)
            }
            assertEquals(TemplateParsingErrorCode.INVALID_COMMENT_STRUCTURE, exception.errorCode)
        }

        @Test
        fun `parseComment throws exception for trailing backslash that is not an escape sequence`() {
            val commentText = """@my-keyword[foo="bar\"]"""
            val exception = assertThrows<TemplateParsingException> {
                TemplateCommentParser.parseComment(commentText)
            }
            assertEquals(TemplateParsingErrorCode.INVALID_COMMENT_STRUCTURE, exception.errorCode)
        }

        private fun parseCommentExpectingSingleResult(comment: String): CommandStructure {
            val commands = TemplateCommentParser.parseComment(comment)
            assertEquals(1, commands.size, "Expected comment to have exactly one command")
            return commands.single()
        }
    }

    @Nested
    inner class MultipleCommandsComment {

        @Test
        fun `a comment with one command having no attribute group is not splitted`() {
            val commentText = "@command-name"
            val commandStructureList = TemplateCommentParser.parseComment(commentText)
            assertEquals(1, commandStructureList.size)
            assertEquals("command-name", commandStructureList.first().keyword)
        }

        @Test
        fun `a comment with one command having one attribute group is not splitted`() {
            val commentText = "@command-name ${commandGroup()}"
            val commandStructureList = TemplateCommentParser.parseComment(commentText)
            assertEquals(1, commandStructureList.size)
            assertEquals("command-name", commandStructureList.first().keyword)
        }

        @Test
        fun `a comment with one command having multiple attribute groups is not splitted`() {
            val commentText = "@command-name ${commandGroup()}${commandGroup()}${commandGroup()}"
            val commandStructureList = TemplateCommentParser.parseComment(commentText)
            assertEquals(1, commandStructureList.size)
            assertEquals("command-name", commandStructureList.first().keyword)
        }

        @Test
        fun `a comment with two command having no attribute group is splitted into two commands`() {
            val commentText = "@command-name-a @command-name-b"
            val commandStructureList = TemplateCommentParser.parseComment(commentText)
            assertEquals(2, commandStructureList.size)
            assertEquals("command-name-a", commandStructureList.first().keyword)
            assertEquals("command-name-b", commandStructureList.last().keyword)
        }

        @Test
        fun `a comment with two commands having each one attribute group is splitted into two commands`() {
            val commentText = "@command-name-a ${commandGroup()} @command-name-b ${commandGroup()}"
            val commandStructureList = TemplateCommentParser.parseComment(commentText)
            assertEquals(2, commandStructureList.size)
            assertEquals("command-name-a", commandStructureList.first().keyword)
            assertEquals("command-name-b", commandStructureList.last().keyword)
        }

        @Test
        fun `a comment with two commands having multiple attribute groups is splitted into two commands`() {
            val commentText = "@command-name-a \t ${commandGroup()} \n\t ${commandGroup()}${commandGroup()} @command-name-b \n\n\t ${commandGroup()}\n ${commandGroup()}\n\t  ${commandGroup()}"
            val commandStructureList = TemplateCommentParser.parseComment(commentText)
            assertEquals(2, commandStructureList.size)
            assertEquals("command-name-a", commandStructureList.first().keyword)
            assertEquals("command-name-b", commandStructureList.last().keyword)
        }

        private fun commandGroup(): String {
            return "[\n" +
                    "attributeA=\"value1\"\n" +
                    "attributeB=\"value2\"\n" +
                    "attributeC=\"value3\"\n" +
                    "]"
        }
    }
}
