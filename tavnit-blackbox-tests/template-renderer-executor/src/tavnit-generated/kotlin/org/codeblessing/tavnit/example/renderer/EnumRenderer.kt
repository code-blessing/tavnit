/*
 * This file is generated using tavnit.
 */
package org.codeblessing.tavnit.example.renderer

import org.codeblessing.tavnit.example.renderer.model.EnumRenderModel

/**
 * Generate the content for the template `EnumRenderer`.
 *
 * This template renderer was generated from the template:
 * - file: `OrderStatusEnum.kt`
 * - path: `my/example/businessproject/domain/OrderStatusEnum.kt`
 */
object EnumRenderer {

    fun renderTemplate(model: EnumRenderModel): String {
        return """
          |package my.example.businessproject.domain
          |enum class ${model.enumName} {
          |${ model.enumValues.joinToString("") { enumValue ->  """    ${enumValue},
              |""" } }}
          |
        """.trimMargin(marginPrefix = "|")
    }

    fun filePath(model: EnumRenderModel): String {
      return "my/example/businessproject/domain/${model.enumName}Enum.kt"
    }
}