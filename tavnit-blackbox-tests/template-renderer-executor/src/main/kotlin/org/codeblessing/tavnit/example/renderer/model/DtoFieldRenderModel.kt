package org.codeblessing.tavnit.example.renderer.model

data class DtoFieldRenderModel(
    val fieldName: String,
    val fieldTypeName: String,
    val isNullable: Boolean,
) {
    val fieldTypeNameWithNullability: String
        get() = "${fieldTypeName}${if (isNullable) "?" else ""}"
}

