package org.codeblessing.tavnit

enum class CommandAttributeKey(
    val keyAsString: String,
    val allowedValues: List<AllowedValue>? = null,
    val requireNotEmpty: Boolean = true,
) {
    TEMPLATE_RENDERER_CLASS_NAME("templateRendererClassName"),
    TEMPLATE_RENDERER_PACKAGE_NAME("templateRendererPackageName"),
    TEMPLATE_RENDERER_INTERFACE_NAME("templateRendererInterfaceName"),
    TEMPLATE_RENDERER_INTERFACE_PACKAGE_NAME("templateRendererInterfacePackageName"),
    TEMPLATE_MODEL_CLASS_NAME("modelClassName"),
    TEMPLATE_MODEL_PACKAGE_NAME("modelPackageName"),
    TEMPLATE_MODEL_NAME("modelName"),
    TEMPLATE_MODEL_IS_LIST("isList", IsListValue.entries.toList()),
    REPLACE_BY_EXPRESSION("replaceByExpression"),
    REPLACE_BY_VALUE("replaceByValue"),
    SEARCH_VALUE("searchValue"),
    CONDITION_EXPRESSION("conditionExpression"),
    LOOP_ITERABLE_EXPRESSION("iteratorExpression"),
    LOOP_VARIABLE_NAME("loopVariable"),
    TEXT("text", requireNotEmpty = false), // text may be blank to insert ident
    MODEL_EXPRESSION("modelExpression"),
    BEFORE_FIRST_OCCURRENCE_OF("beforeFirstOccurrenceOf"),
    AFTER_FIRST_OCCURRENCE_OF("afterFirstOccurrenceOf"),
    BEFORE_LAST_OCCURRENCE_OF("beforeLastOccurrenceOf"),
    AFTER_LAST_OCCURRENCE_OF("afterLastOccurrenceOf"),
    IMPORT_CLASS_NAME("importClassName"),
    IMPORT_PACKAGE_NAME("importPackageName", requireNotEmpty = false),
    ;

    companion object {
        fun fromString(key: String): CommandAttributeKey? {
            return entries.firstOrNull { it.keyAsString == key }
        }
    }
}
