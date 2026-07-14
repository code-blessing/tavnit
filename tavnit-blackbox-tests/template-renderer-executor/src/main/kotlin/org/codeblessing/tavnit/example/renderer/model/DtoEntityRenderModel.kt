package org.codeblessing.tavnit.example.renderer.model

data class DtoEntityRenderModel(
    val entityName: String,
    val fields: List<DtoFieldRenderModel>,
) {
    val dtoPackageName: String = "my.example.businessproject.dto"
    val kotlinDtoClassName: String = "${entityName}Dto"
    val kotlinDtoFileName: String = "${kotlinDtoClassName}.kt"
    val dtoNestedPackageDirectory: String = dtoPackageName.replace(".", "/")
}
