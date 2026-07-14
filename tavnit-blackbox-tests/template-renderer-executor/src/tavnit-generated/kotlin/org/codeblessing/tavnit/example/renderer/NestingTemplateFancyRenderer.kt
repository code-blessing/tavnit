/*
 * This file is generated using tavnit.
 */
package org.codeblessing.tavnit.example.renderer

import org.codeblessing.tavnit.example.renderer.model.BlackboxDefaultModel
import java.time.DayOfWeek

/**
 * Generate the content for the template `NestingTemplateFancyRenderer`.
 *
 * This template renderer was generated from the template:
 * - file: `nesting-template-renderer.html`
 * - path: `nesting/nesting-template-renderer.html`
 */
object NestingTemplateFancyRenderer : RendererWithBlackboxDefaultModel {

    override fun renderTemplate(model: BlackboxDefaultModel): String {
        return """
          |        <li class="fancy">REPLACED-IN-A rendered on ${DayOfWeek.MONDAY}</li>
          |
        """.trimMargin(marginPrefix = "|")
    }

    override fun filePath(model: BlackboxDefaultModel): String {
      return "nesting/nesting-template-renderer-fancy-output.html"
    }
}