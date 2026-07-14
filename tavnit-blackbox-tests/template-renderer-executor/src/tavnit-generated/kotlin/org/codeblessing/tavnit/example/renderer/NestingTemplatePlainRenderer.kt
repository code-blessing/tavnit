/*
 * This file is generated using tavnit.
 */
package org.codeblessing.tavnit.example.renderer

import org.codeblessing.tavnit.example.renderer.model.BlackboxDefaultModel

/**
 * Generate the content for the template `NestingTemplatePlainRenderer`.
 *
 * This template renderer was generated from the template:
 * - file: `nesting-template-renderer.html`
 * - path: `nesting/nesting-template-renderer.html`
 */
object NestingTemplatePlainRenderer : RendererWithBlackboxDefaultModel {

    override fun renderTemplate(model: BlackboxDefaultModel): String {
        return """
          |        <li class="plain">TOKEN rendered on Day</li>
          |
        """.trimMargin(marginPrefix = "|")
    }

    override fun filePath(model: BlackboxDefaultModel): String {
      return "nesting/nesting-template-renderer-plain-output.html"
    }
}