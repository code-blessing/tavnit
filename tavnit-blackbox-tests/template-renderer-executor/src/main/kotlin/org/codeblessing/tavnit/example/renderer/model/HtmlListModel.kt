package org.codeblessing.tavnit.example.renderer.model

data class HtmlListModel(
    val filenameWithoutPrefix: String,
    val pageTitle: String,
    val allListEntries: List<String>
)
