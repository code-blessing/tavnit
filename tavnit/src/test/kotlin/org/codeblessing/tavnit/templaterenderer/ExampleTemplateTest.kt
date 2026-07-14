package org.codeblessing.tavnit.templaterenderer

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ExampleTemplateTest {

    private class ExampleTemplate {

        data class ExampleModel(
            val entityName: String,
            val entityNameDecapitalized: String,
            val entityFields: List<ExampleModelField>,
            val isAddToStringWithEntityName: Boolean,
        )

        data class ExampleModelField(
            val fieldName: String,
            val fieldNameDecapitalized: String,
            val isNullable: Boolean,
        )

        fun renderTemplate(model: ExampleModel): String {
            return """
                |package org.codeblessing.tavnit.template;
                |
                |class ${model.entityName}Dto(
               ${
                model.entityFields.joinToString("") { myModel -> 
                    """|   val ${myModel.fieldNameDecapitalized}: ${myModel.fieldName}${if (myModel.isNullable) "?" else ""},
                    """
                }
            }|) {
                |    
                |    public fun get${model.entityName}Info(${model.entityNameDecapitalized}: ${model.entityName}): String {
                |        return "Entity ${model.entityName}: $${model.entityNameDecapitalized}"
                |    }${if (model.isAddToStringWithEntityName) """
                    |
                    |    fun toStringWithEntityName(): String {
                    |        return get${model.entityName}Info(this) + toString()
                    |    }""" else ""}
                |}
            """.trimMargin()
        }
    }

    @Test
    fun `test example template for command syntax`() {

        val template = ExampleTemplate()
        val model = ExampleTemplate.ExampleModel(
            entityName = "Author",
            entityNameDecapitalized = "author",
            entityFields = listOf(
                ExampleTemplate.ExampleModelField(
                    fieldName = "FirstName",
                    fieldNameDecapitalized = "firstName",
                    isNullable = true,
                ),
                ExampleTemplate.ExampleModelField(
                    fieldName = "LastName",
                    fieldNameDecapitalized = "lastName",
                    isNullable = false,
                )
            ),
            isAddToStringWithEntityName = false,
        )


        val expectedContent = """
            package org.codeblessing.tavnit.template;
            
            class AuthorDto(
               val firstName: FirstName?,
               val lastName: LastName,
            ) {
                
                public fun getAuthorInfo(author: Author): String {
                    return "Entity Author: ${'$'}author"
                }
            }
            """.trimIndent()

        val actualContent = template.renderTemplate(model)
        print(actualContent)
        assertEquals(expectedContent, actualContent)
    }

    @Test
    fun `multiline string syntax test zero rows`() {
        val multilineStringRow = """"""
        val multilineStringRowTrimMargin = """
        """.trimMargin()
        val multilineStringRowTrimMarginPipe = """
            |
        """.trimMargin()
        val traditionalStringRow = ""

        assertEquals(multilineStringRow, traditionalStringRow)
        assertEquals(multilineStringRowTrimMargin, traditionalStringRow)
        assertEquals(multilineStringRowTrimMarginPipe, traditionalStringRow)
    }

    @Test
    fun `multiline string syntax test one rows`() {
        val multilineStringRow = """abc"""
        val multilineStringRowTrimMargin = """abc
        """.trimMargin()
        val multilineStringRowTrimMarginPipe = """
            |abc
        """.trimMargin()
        val traditionalStringRow = "abc"

        assertEquals(multilineStringRow, traditionalStringRow)
        assertEquals(multilineStringRowTrimMargin, traditionalStringRow)
        assertEquals(multilineStringRowTrimMarginPipe, traditionalStringRow)
    }

    @Test
    fun `multiline string syntax test two row`() {
        val multilineStringRow = """
            |abc
            |def
        """.trimMargin()
        val traditionalStringRow = "abc\ndef"
        assertEquals(multilineStringRow, traditionalStringRow)
    }


}
