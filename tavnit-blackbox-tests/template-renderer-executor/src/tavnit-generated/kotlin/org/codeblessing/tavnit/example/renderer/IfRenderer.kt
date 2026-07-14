/*
 * This file is generated using tavnit.
 */
package org.codeblessing.tavnit.example.renderer

import org.codeblessing.tavnit.example.renderer.model.BlackboxDefaultModel

/**
 * Generate the content for the template `IfRenderer`.
 *
 * This template renderer was generated from the template:
 * - file: `if-case.html`
 * - path: `ifelse/if-case.html`
 */
object IfRenderer : RendererWithBlackboxDefaultModel {

    override fun renderTemplate(model: BlackboxDefaultModel): String {
        return """
          |${ if(model.isTrueAttribute) { """        <li class="book">Yes, 'if' is true</li>
              |${ if(model.isTrueAttribute) { """        <li class="badge">Yes, nested 'if' is true</li>
                  |""" } else { """        <li class="badge">Yes, nested 'else' is true</li>
                  |""" } }""" } else if(model.isTrueAttribute) { """        <li class="movie">Yes, 'else-if' is true</li>
              |""" } else { """        <li class="other">Yes, 'else' is true</li>
              |""" } }
        """.trimMargin(marginPrefix = "|")
    }

    override fun filePath(model: BlackboxDefaultModel): String {
      return "ifelse/if-case.html"
    }
}