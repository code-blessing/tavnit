package my.example.businessproject.domain

/* @tt{{{
  @move-comment-backward
  @template-renderer [
      templateRendererClassName="EnumRenderer"
      templateRendererPackageName="org.codeblessing.tavnit.example.renderer"
  ][
      modelClassName="EnumRenderModel"
      modelPackageName="org.codeblessing.tavnit.example.renderer.model"
      modelName="model"
  ]
  @replace-value-by-expression
    [ searchValue="OrderStatus" replaceByExpression="model.enumName" ]
  @modify-provided-filepath-by-replacements
  @rlb
}}}@ */
enum class OrderStatus {
/* @tt{{{
    @foreach [ iteratorExpression="model.enumValues" loopVariable="enumValue" ]
    @replace-value-by-expression [ searchValue="PENDING" replaceByExpression="enumValue" ]
    }}}@
*/
    PENDING,
/* @tt{{{
    @end-foreach
    @ignore-text
}}}@ */
    COMPLETED,
    CANCELLED,
/* @tt{{{ @end-ignore-text  @end-replace-value-by-expression }}}@ */
}
