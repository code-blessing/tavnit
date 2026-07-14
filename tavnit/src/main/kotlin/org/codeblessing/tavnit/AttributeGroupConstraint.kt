package org.codeblessing.tavnit

data class AttributeGroupConstraint(
    val occurrence: AttributeGroupOccurrence,
    val requiredAttributes: Set<CommandAttributeKey> = emptySet(),
    val optionalAttributes: Set<CommandAttributeKey> = emptySet(),
    val mutualExclusiveAttributes: Set<CommandAttributeKey> = emptySet(),
) {
    val allowedAttributes: Set<CommandAttributeKey> = requiredAttributes + optionalAttributes
}
