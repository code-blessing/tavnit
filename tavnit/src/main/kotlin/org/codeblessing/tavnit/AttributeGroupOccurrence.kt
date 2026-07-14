package org.codeblessing.tavnit

enum class AttributeGroupOccurrence(
    val minNumberOfAttributeGroups: Int,
    val maxNumberOfAttributeGroups: Int,
) {
    ONE_ATTRIBUTE_GROUP(minNumberOfAttributeGroups = 1, maxNumberOfAttributeGroups = 1),
    ZERO_OR_ONE_ATTRIBUTE_GROUP(minNumberOfAttributeGroups = 0, maxNumberOfAttributeGroups = 1),
    ZERO_OR_MANY_ATTRIBUTE_GROUP(minNumberOfAttributeGroups = 0, maxNumberOfAttributeGroups = Int.MAX_VALUE),
    MANY_ATTRIBUTE_GROUP(minNumberOfAttributeGroups = 1, maxNumberOfAttributeGroups = Int.MAX_VALUE),
}
