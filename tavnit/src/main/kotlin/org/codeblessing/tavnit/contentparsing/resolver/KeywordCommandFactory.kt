package org.codeblessing.tavnit.contentparsing.resolver

import org.codeblessing.tavnit.AttributeGroup
import org.codeblessing.tavnit.CommandAttributeKey
import org.codeblessing.tavnit.CommandKey
import org.codeblessing.tavnit.contentparsing.KeywordCommand
import org.codeblessing.tavnit.contentparsing.TemplateParsingErrorCode
import org.codeblessing.tavnit.contentparsing.TemplateParsingException
import org.codeblessing.tavnit.contentparsing.commentparser.CommandStructure
import org.codeblessing.tavnit.contentparsing.linenumbers.LineNumbers

/**
 * Translates a raw [CommandStructure] (parsed comment text) into a fully validated [KeywordCommand],
 * rejecting invalid input with a [TemplateParsingException].
 * It does so by:
 * - Resolving the keyword string (or any of its [CommandKey.aliases]) to a known [CommandKey], failing fast on unknown keywords
 * - Checking that the number of bracket groups falls within the min/max bounds derived from the command's [CommandKey.attributeGroupConstraints]
 * - Converting each raw attribute key string to a typed [CommandAttributeKey], rejecting strings not present in the enum
 * - Verifying each attribute key is permitted for its specific group index (groups beyond the constraint list reuse the last constraint)
 * - Validating each attribute value against the key's allowed values list when one is defined
 * - Rejecting blank attribute values for keys that require a non-empty value
 * - Ensuring all required attributes for each group are present
 * - Ensuring no attributes outside the allowed set for each group have snuck through
 */
object KeywordCommandFactory {

    fun createKeywordCommand(
        commandStructure: CommandStructure,
        lineNumbers: LineNumbers
    ): KeywordCommand {
        val keyword = commandStructure.keyword
        val allowedKeyword = CommandKey.allKeywords().joinToString(",") { "\'$it\'"}
        val commandKey = CommandKey.fromKeywordOrAlias(keyword) ?: throw TemplateParsingException(
            lineNumbers = lineNumbers,
            errorCode = TemplateParsingErrorCode.UNKNOWN_KEYWORD,
            msg = TemplateParsingErrorCode.UNKNOWN_KEYWORD.resolve(
                "keyword" to keyword,
                "allowedKeywords" to allowedKeyword,
            ),
        )

        val numberOfAttributeGroups = commandStructure.brackets.size
        if (numberOfAttributeGroups < commandKey.minNumberOfAttributeGroups) {
            throw TemplateParsingException(
                lineNumbers = lineNumbers,
                errorCode = TemplateParsingErrorCode.TOO_FEW_ATTRIBUTE_GROUPS,
                msg = TemplateParsingErrorCode.TOO_FEW_ATTRIBUTE_GROUPS.resolve(
                    "min" to commandKey.minNumberOfAttributeGroups.toString(),
                    "actual" to numberOfAttributeGroups.toString(),
                ),
            )
        }

        if (numberOfAttributeGroups > commandKey.maxNumberOfAttributeGroups) {
            throw TemplateParsingException(
                lineNumbers = lineNumbers,
                errorCode = TemplateParsingErrorCode.TOO_MANY_ATTRIBUTE_GROUPS,
                msg = TemplateParsingErrorCode.TOO_MANY_ATTRIBUTE_GROUPS.resolve(
                    "max" to commandKey.maxNumberOfAttributeGroups.toString(),
                    "actual" to numberOfAttributeGroups.toString(),
                ),
            )
        }

        val attributeGroups = commandStructure.brackets.mapIndexed { groupIndex, attributeMap ->
            AttributeGroup(
                attributes = attributeMap
                    .mapKeys { (keyString) ->
                        CommandAttributeKey.fromString(keyString)
                            ?: throw TemplateParsingException(
                                lineNumbers = lineNumbers,
                                errorCode = TemplateParsingErrorCode.UNKNOWN_ATTRIBUTE_KEY,
                                msg = TemplateParsingErrorCode.UNKNOWN_ATTRIBUTE_KEY.resolve(
                                    "key" to keyString,
                                    "groupIndex" to (groupIndex + 1).toString(),
                                    "allowedAttributes" to commandKey.allowedAttributesForGroup(groupIndex).map { it.keyAsString }.toString(),
                                ),
                            )
                    }
                    .onEach { (attributeKey, attributeValue) ->
                        if (attributeKey !in commandKey.allowedAttributesForGroup(groupIndex)) {
                            throw TemplateParsingException(
                                lineNumbers = lineNumbers,
                                errorCode = TemplateParsingErrorCode.ATTRIBUTE_KEY_NOT_ALLOWED,
                                msg = TemplateParsingErrorCode.ATTRIBUTE_KEY_NOT_ALLOWED.resolve(
                                    "key" to attributeKey.keyAsString,
                                    "groupIndex" to (groupIndex + 1).toString(),
                                    "allowedAttributes" to commandKey.allowedAttributesForGroup(groupIndex).map { it.keyAsString }.toString(),
                                ),
                            )
                        }
                        if (attributeKey.allowedValues != null && attributeValue !in attributeKey.allowedValues.map { it.value }) {
                            throw TemplateParsingException(
                                lineNumbers = lineNumbers,
                                errorCode = TemplateParsingErrorCode.ATTRIBUTE_VALUE_NOT_ALLOWED,
                                msg = TemplateParsingErrorCode.ATTRIBUTE_VALUE_NOT_ALLOWED.resolve(
                                    "value" to attributeValue,
                                    "key" to attributeKey.keyAsString,
                                    "groupIndex" to (groupIndex + 1).toString(),
                                    "allowedValues" to attributeKey.allowedValues.map { it.value }.toString(),
                                ),
                            )
                        }
                        if (attributeKey.requireNotEmpty && attributeValue.isBlank()) {
                            throw TemplateParsingException(
                                lineNumbers = lineNumbers,
                                errorCode = TemplateParsingErrorCode.BLANK_ATTRIBUTE_VALUE,
                                msg = TemplateParsingErrorCode.BLANK_ATTRIBUTE_VALUE.resolve(
                                    "key" to attributeKey.keyAsString,
                                    "groupIndex" to (groupIndex + 1).toString(),
                                ),
                            )
                        }

                    }
            ).also { attributeGroup ->
                    val missingAttributes = commandKey.missingRequiredAttributesForGroup(groupIndex, attributeGroup.attributes.keys)
                    if(missingAttributes.isNotEmpty()) {
                        throw TemplateParsingException(
                            lineNumbers = lineNumbers,
                            errorCode = TemplateParsingErrorCode.MISSING_REQUIRED_ATTRIBUTES,
                            msg = TemplateParsingErrorCode.MISSING_REQUIRED_ATTRIBUTES.resolve(
                                "command" to commandKey.keyword,
                                "groupIndex" to (groupIndex + 1).toString(),
                                "missingAttributes" to missingAttributes.joinToString { it.keyAsString },
                            ),
                        )
                    }
                    val unallowedAttributes = commandKey.unallowedAttributesForGroup(groupIndex, attributeGroup.attributes.keys)
                    if(unallowedAttributes.isNotEmpty()) {
                        throw TemplateParsingException(
                            lineNumbers = lineNumbers,
                            errorCode = TemplateParsingErrorCode.UNALLOWED_ATTRIBUTES,
                            msg = TemplateParsingErrorCode.UNALLOWED_ATTRIBUTES.resolve(
                                "command" to commandKey.keyword,
                                "groupIndex" to (groupIndex + 1).toString(),
                                "unallowedAttributes" to unallowedAttributes.joinToString { it.keyAsString },
                            ),
                        )
                    }
                    val mutualExclusiveAttributes = commandKey.mutualExclusiveAttributesForGroup(groupIndex)
                    val presentMutualExclusiveAttributes = attributeGroup.attributes.keys.intersect(mutualExclusiveAttributes)
                    if (presentMutualExclusiveAttributes.size > 1) {
                        throw TemplateParsingException(
                            lineNumbers = lineNumbers,
                            errorCode = TemplateParsingErrorCode.MUTUALLY_EXCLUSIVE_ATTRIBUTES,
                            msg = TemplateParsingErrorCode.MUTUALLY_EXCLUSIVE_ATTRIBUTES.resolve(
                                "command" to commandKey.keyword,
                                "groupIndex" to (groupIndex + 1).toString(),
                                "mutualExclusiveAttributes" to mutualExclusiveAttributes.joinToString { it.keyAsString },
                                "foundAttributes" to presentMutualExclusiveAttributes.joinToString { it.keyAsString },
                            ),
                        )
                    }
                }
            }

        return KeywordCommand(commandKey, attributeGroups)
    }
}
