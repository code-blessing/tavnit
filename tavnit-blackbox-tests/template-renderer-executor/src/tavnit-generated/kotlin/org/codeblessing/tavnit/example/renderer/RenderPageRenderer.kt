/*
 * This file is generated using tavnit.
 */
package org.codeblessing.tavnit.example.renderer

import org.codeblessing.tavnit.example.renderer.model.BlackboxDefaultModel
import org.codeblessing.tavnit.example.renderer.RenderItemRenderer

/**
 * Generate the content for the template `RenderPageRenderer`.
 *
 * This template renderer was generated from the template:
 * - file: `render-template.html`
 * - path: `rendertemplate/render-template.html`
 */
object RenderPageRenderer : RendererWithBlackboxDefaultModel {

    override fun renderTemplate(model: BlackboxDefaultModel): String {
        return """
          |    default (indent before the placeholder is removed; embedded lines are not re-indented):
          |    <ul>
          |${RenderItemRenderer.renderTemplate(model = model)}    </ul>
          |
        """.trimMargin(marginPrefix = "|")
    }

    override fun filePath(model: BlackboxDefaultModel): String {
      return "rendertemplate/render-template.html"
    }
}