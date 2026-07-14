package org.codeblessing.tavnit.example.renderer

import org.codeblessing.tavnit.example.renderer.model.BlackboxDefaultModel

interface RendererWithBlackboxDefaultModel {

    fun renderTemplate(model: BlackboxDefaultModel): String
    fun filePath(model: BlackboxDefaultModel): String
}
