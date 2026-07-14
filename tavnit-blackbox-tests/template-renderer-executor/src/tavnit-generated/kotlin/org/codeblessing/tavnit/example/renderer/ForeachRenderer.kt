/*
 * This file is generated using tavnit.
 */
package org.codeblessing.tavnit.example.renderer

import org.codeblessing.tavnit.example.renderer.model.BlackboxDefaultModel

/**
 * Generate the content for the template `ForeachRenderer`.
 *
 * This template renderer was generated from the template:
 * - file: `foreach.html`
 * - path: `foreach/foreach.html`
 */
object ForeachRenderer : RendererWithBlackboxDefaultModel {

    override fun renderTemplate(model: BlackboxDefaultModel): String {
        return """
          |<html lang="en">
          |
          |
          |<head><title>${model.articleTitle}</title></head>
          |<body>
          |<p>Here are the ${model.articleTitle.lowercase()}:</p>
          |<ul>${ model.listOfArticleNames.joinToString("") { articleName ->  """
              |    <li>${articleName}</li>""" } }
          |</ul>
          |
          |</body>
          |</html>
          |
        """.trimMargin(marginPrefix = "|")
    }

    override fun filePath(model: BlackboxDefaultModel): String {
      return "foreach/foreach.html"
    }
}