/*
 * This file is generated using tavnit.
 */
package org.codeblessing.tavnit.example.renderer

import org.codeblessing.tavnit.example.renderer.model.BlackboxDefaultModel

/**
 * Generate the content for the template `RenderItemRenderer`.
 *
 * This template renderer was generated from the template:
 * - file: `render-template.html`
 * - path: `rendertemplate/render-template.html`
 */
object RenderItemRenderer : RendererWithBlackboxDefaultModel {

    override fun renderTemplate(model: BlackboxDefaultModel): String {
        return """
          |<li>line-1</li>
          |<li>line-2</li>
          |
        """.trimMargin(marginPrefix = "|")
    }

    override fun filePath(model: BlackboxDefaultModel): String {
      return "rendertemplate/render-template.html"
    }
}