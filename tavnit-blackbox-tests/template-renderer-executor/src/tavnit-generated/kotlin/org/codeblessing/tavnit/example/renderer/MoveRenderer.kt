/*
 * This file is generated using tavnit.
 */
package org.codeblessing.tavnit.example.renderer

import org.codeblessing.tavnit.example.renderer.model.BlackboxDefaultModel

/**
 * Generate the content for the template `MoveRenderer`.
 *
 * This template renderer was generated from the template:
 * - file: `move.html`
 * - path: `move/move.html`
 */
object MoveRenderer : RendererWithBlackboxDefaultModel {

    override fun renderTemplate(model: BlackboxDefaultModel): String {
        return """
          |<!DOCTYPE html>
          |<html lang="en">
          |<body>
          |    <ul>
          |        <li lang='en' id="b1">Clean Code</li>
          |        <li id="b2"><span>Refactoring</span></li>
          |        <li>moved-across-normal-comment:<!-- a plain html comment the move below travels across -->VALUE</li>
          |        <li>x,y,z,(last)w,UNREACHABLE</li>
          |    </ul>
          |</body>
          |</html>
          |
        """.trimMargin(marginPrefix = "|")
    }

    override fun filePath(model: BlackboxDefaultModel): String {
      return "move/move.html"
    }
}