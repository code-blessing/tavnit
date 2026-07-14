/*
 * This file is generated using tavnit.
 */
package org.codeblessing.tavnit.example.renderer

import org.codeblessing.tavnit.example.renderer.model.BlackboxDefaultModel

/**
 * Generate the content for the template `PrintTextRenderer`.
 *
 * This template renderer was generated from the template:
 * - file: `print-text.html`
 * - path: `printtext/print-text.html`
 */
object PrintTextRenderer : RendererWithBlackboxDefaultModel {

    override fun renderTemplate(model: BlackboxDefaultModel): String {
        return """
          |<html lang="en">
          |<body>
          |    <ul>
          |        <li>normal text STARTinserted-textEND</li>
          |        <li>blanks only START   END</li>
          |        <li>special characters STARTa ${"$"}dollar and ${"\"\"\""} quotesEND</li>
          |        <li>raw kotlin string val s = ${"\"\"\""}text${"\"\"\""} and a ${"$"}variable</li>
          |    </ul>
          |</body>
          |</html>
          |
        """.trimMargin(marginPrefix = "|")
    }

    override fun filePath(model: BlackboxDefaultModel): String {
      return "printtext/print-text.html"
    }
}