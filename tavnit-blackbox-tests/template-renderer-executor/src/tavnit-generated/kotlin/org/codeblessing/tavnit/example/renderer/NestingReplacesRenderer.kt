/*
 * This file is generated using tavnit.
 */
package org.codeblessing.tavnit.example.renderer

import org.codeblessing.tavnit.example.renderer.model.BlackboxDefaultModel

/**
 * Generate the content for the template `NestingReplacesRenderer`.
 *
 * This template renderer was generated from the template:
 * - file: `nesting-replaces.html`
 * - path: `nesting/nesting-replaces.html`
 */
object NestingReplacesRenderer : RendererWithBlackboxDefaultModel {

    override fun renderTemplate(model: BlackboxDefaultModel): String {
        return """
          |<html lang="en">
          |<body>
          |    <ul>
          |    <li>within-comment-order: THREE</li>
          |    <li>innermost-first: THREE</li>
          |    <li>expression: ${40 + 2}</li>
          |    <li>after-inner: ZERO</li>
          |    </ul>
          |
        """.trimMargin(marginPrefix = "|")
    }

    override fun filePath(model: BlackboxDefaultModel): String {
      return "nesting/nesting-replaces.html"
    }
}