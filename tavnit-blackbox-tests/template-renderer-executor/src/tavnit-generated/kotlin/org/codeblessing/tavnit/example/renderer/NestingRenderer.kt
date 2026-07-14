/*
 * This file is generated using tavnit.
 */
package org.codeblessing.tavnit.example.renderer

import org.codeblessing.tavnit.example.renderer.model.BlackboxDefaultModel

/**
 * Generate the content for the template `NestingRenderer`.
 *
 * This template renderer was generated from the template:
 * - file: `nesting.html`
 * - path: `nesting/nesting.html`
 */
object NestingRenderer : RendererWithBlackboxDefaultModel {

    override fun renderTemplate(model: BlackboxDefaultModel): String {
        return """
          |<ul>
          |${ model.allListEntries.joinToString("") { entry ->  """${ if(entry.isNotBlank()) { """  <li>* ${entry}</li>
                  |""" } else { """""" } }""" } }</ul>
          |<p>tail: MARK ENTRY</p>
          |
        """.trimMargin(marginPrefix = "|")
    }

    override fun filePath(model: BlackboxDefaultModel): String {
      return "nesting/nesting.html"
    }
}