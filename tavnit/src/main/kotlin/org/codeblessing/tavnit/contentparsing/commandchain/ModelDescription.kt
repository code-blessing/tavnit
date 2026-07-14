package org.codeblessing.tavnit.contentparsing.commandchain

data class ModelDescription(
    val modelClassDescription: ClassDescription,
    val modelName: String,
    val isList: Boolean,
)
