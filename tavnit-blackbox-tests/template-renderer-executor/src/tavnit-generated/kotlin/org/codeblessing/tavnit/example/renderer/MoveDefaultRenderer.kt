/*
 * This file is generated using tavnit.
 */
package org.codeblessing.tavnit.example.renderer

import org.codeblessing.tavnit.example.renderer.model.BlackboxDefaultModel

/**
 * Generate the content for the template `MoveDefaultRenderer`.
 *
 * This template renderer was generated from the template:
 * - file: `move-default.html`
 * - path: `move/move-default.html`
 */
object MoveDefaultRenderer : RendererWithBlackboxDefaultModel {

    override fun renderTemplate(model: BlackboxDefaultModel): String {
        return """
          |<body>
          |    <ul>
          |        <li id="b1">Clean Code</li>
          |    </ul>
          |</body>
          |</html>
          |
        """.trimMargin(marginPrefix = "|")
    }

    override fun filePath(model: BlackboxDefaultModel): String {
      return "move/move-default.html"
    }
}