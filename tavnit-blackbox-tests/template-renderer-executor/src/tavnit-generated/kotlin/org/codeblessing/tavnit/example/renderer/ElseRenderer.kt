/*
 * This file is generated using tavnit.
 */
package org.codeblessing.tavnit.example.renderer

import org.codeblessing.tavnit.example.renderer.model.BlackboxDefaultModel

/**
 * Generate the content for the template `ElseRenderer`.
 *
 * This template renderer was generated from the template:
 * - file: `else-case.html`
 * - path: `ifelse/else-case.html`
 */
object ElseRenderer : RendererWithBlackboxDefaultModel {

    override fun renderTemplate(model: BlackboxDefaultModel): String {
        return """
          |${ if(model.isFalseAttribute) { """        <li class="book">Yes, 'if' is true</li>
              |${ if(model.isTrueAttribute) { """        <li class="badge">Yes, nested 'if' is true</li>
                  |""" } else { """        <li class="badge">Yes, nested 'else' is true</li>
                  |""" } }""" } else if(model.isFalseAttribute) { """        <li class="movie">Yes, 'else-if' is true</li>
              |""" } else { """        <li class="other">Yes, 'else' is true</li>
              |""" } }
        """.trimMargin(marginPrefix = "|")
    }

    override fun filePath(model: BlackboxDefaultModel): String {
      return "ifelse/else-case.html"
    }
}