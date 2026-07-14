package my.example.businessproject.dto

/* @tt{{{
  @move-comment-backward
  @template-renderer [
      templateRendererClassName="EntityDtoTemplateRenderer"
      templateRendererPackageName="org.codeblessing.tavnit.example.renderer"
  ][
      modelClassName="DtoEntityRenderModel"
      modelPackageName="org.codeblessing.tavnit.example.renderer.model"
      modelName="model"
  ]
  @replace-value-by-expression
    [ searchValue="ProductDto" replaceByExpression="model.kotlinDtoClassName" ]
    [ searchValue="Product" replaceByExpression="model.entityName" ]
    [ searchValue="productCode" replaceByExpression="field.fieldName" ]
    [ searchValue="String" replaceByExpression="field.fieldTypeNameWithNullability" ]
  }}}@ */

/**
 * The Product DTO (Data Transfer Object) class.
 */
data class ProductDto(
/* @tt{{{
    @remove-blanks-and-linebreak-before-comment
    @foreach [ iteratorExpression="model.fields" loopVariable="field" ]
}}}@  */
    val productCode: String,
/* @tt{{{
    @remove-blanks-and-linebreak-before-comment
    @end-foreach
    @ignore-text
}}}@ */
    val productName: String,
/* @tt{{{
    @rlb
    @end-ignore-text
    @end-replace-value-by-expression
}}}@ */
)
