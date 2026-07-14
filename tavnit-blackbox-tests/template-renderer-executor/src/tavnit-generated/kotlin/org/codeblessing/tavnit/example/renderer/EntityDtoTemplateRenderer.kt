/*
 * This file is generated using tavnit.
 */
package org.codeblessing.tavnit.example.renderer

import org.codeblessing.tavnit.example.renderer.model.DtoEntityRenderModel

/**
 * Generate the content for the template `EntityDtoTemplateRenderer`.
 *
 * This template renderer was generated from the template:
 * - file: `ProductDto.kt`
 * - path: `my/example/businessproject/dto/ProductDto.kt`
 */
object EntityDtoTemplateRenderer {

    fun renderTemplate(model: DtoEntityRenderModel): String {
        return """
          |package my.example.businessproject.dto
          |
          |
          |/**
          | * The ${model.entityName} DTO (Data Transfer Object) class.
          | */
          |data class ${model.kotlinDtoClassName}(${ model.fields.joinToString("") { field ->  """    val ${field.fieldName}: ${field.fieldTypeNameWithNullability},""" } })
          |
        """.trimMargin(marginPrefix = "|")
    }

    fun filePath(model: DtoEntityRenderModel): String {
      return "my/example/businessproject/dto/ProductDto.kt"
    }
}