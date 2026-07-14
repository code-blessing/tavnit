package org.codeblessing.tavnit

import org.codeblessing.tavnit.AttributeGroupOccurrence.MANY_ATTRIBUTE_GROUP
import org.codeblessing.tavnit.AttributeGroupOccurrence.ONE_ATTRIBUTE_GROUP
import org.codeblessing.tavnit.AttributeGroupOccurrence.ZERO_OR_MANY_ATTRIBUTE_GROUP
import org.codeblessing.tavnit.AttributeGroupOccurrence.ZERO_OR_ONE_ATTRIBUTE_GROUP
import org.codeblessing.tavnit.CommandAttributeKey.*

enum class CommandKey(
    val keyword: String,
    val aliases: Set<String> = emptySet(),
    val attributeGroupConstraints: List<AttributeGroupConstraint> = emptyList(),
    val correspondingOpeningCommandKey: CommandKey? = null,
    val directlyNestedInsideCommandKey: CommandKey? = null,
    val isAutoclosingSupported: Boolean = false,
) {
    TEMPLATE_RENDERER(
        keyword = "template-renderer",
        attributeGroupConstraints = listOf(
            AttributeGroupConstraint(
                occurrence = ONE_ATTRIBUTE_GROUP,
                requiredAttributes = setOf(TEMPLATE_RENDERER_CLASS_NAME),
                optionalAttributes = setOf(TEMPLATE_RENDERER_PACKAGE_NAME, TEMPLATE_RENDERER_INTERFACE_NAME, TEMPLATE_RENDERER_INTERFACE_PACKAGE_NAME),
            ),
            AttributeGroupConstraint(
                occurrence = ZERO_OR_MANY_ATTRIBUTE_GROUP,
                requiredAttributes = setOf(TEMPLATE_MODEL_CLASS_NAME, TEMPLATE_MODEL_NAME),
                optionalAttributes = setOf(TEMPLATE_MODEL_PACKAGE_NAME, TEMPLATE_MODEL_IS_LIST),
            ),
        ),
        isAutoclosingSupported = true,
    ),
    END_TEMPLATE_RENDERER(
        keyword = "end-template-renderer",
        correspondingOpeningCommandKey = TEMPLATE_RENDERER,
    ),
    REPLACE_VALUE_BY_EXPRESSION(
        keyword = "replace-value-by-expression",
        aliases = setOf("re"),
        attributeGroupConstraints = listOf(
            AttributeGroupConstraint(
                occurrence = MANY_ATTRIBUTE_GROUP,
                requiredAttributes = setOf(SEARCH_VALUE, REPLACE_BY_EXPRESSION),
            )
        ),
        isAutoclosingSupported = true,
    ),
    END_REPLACE_VALUE_BY_EXPRESSION(
        keyword = "end-replace-value-by-expression",
        aliases = setOf("end-re"),
        correspondingOpeningCommandKey = REPLACE_VALUE_BY_EXPRESSION,
    ),
    REPLACE_VALUE_BY_VALUE(
        keyword = "replace-value-by-value",
        aliases = setOf("rv"),
        attributeGroupConstraints = listOf(
            AttributeGroupConstraint(
                occurrence = MANY_ATTRIBUTE_GROUP,
                requiredAttributes = setOf(SEARCH_VALUE, REPLACE_BY_VALUE),
            )
        ),
        isAutoclosingSupported = true,
    ),
    END_REPLACE_VALUE_BY_VALUE(
        keyword = "end-replace-value-by-value",
        aliases = setOf("end-rv"),
        correspondingOpeningCommandKey = REPLACE_VALUE_BY_VALUE,
    ),
    IF_CONDITION(
        keyword = "if",
        attributeGroupConstraints = listOf(
            AttributeGroupConstraint(
                occurrence = ONE_ATTRIBUTE_GROUP,
                requiredAttributes = setOf(CONDITION_EXPRESSION),
            )
        ),
    ),
    ELSE_IF_CONDITION(
        keyword = "else-if",
        attributeGroupConstraints = listOf(
            AttributeGroupConstraint(
                occurrence = ONE_ATTRIBUTE_GROUP,
                requiredAttributes = setOf(CONDITION_EXPRESSION),
            )
        ),
        directlyNestedInsideCommandKey = IF_CONDITION,
    ),
    ELSE_CLAUSE(
        keyword = "else",
        directlyNestedInsideCommandKey = IF_CONDITION,
    ),
    END_IF_CONDITION(
        keyword = "end-if",
        correspondingOpeningCommandKey = IF_CONDITION,
    ),
    FOREACH(
        keyword = "foreach",
        attributeGroupConstraints = listOf(
            AttributeGroupConstraint(
                occurrence = ONE_ATTRIBUTE_GROUP,
                requiredAttributes = setOf(LOOP_ITERABLE_EXPRESSION, LOOP_VARIABLE_NAME),
            )
        ),
    ),
    END_FOREACH(
        keyword = "end-foreach",
        correspondingOpeningCommandKey = FOREACH,
    ),
    IGNORE_TEXT(
        keyword = "ignore-text",
        isAutoclosingSupported = true,
    ),
    END_IGNORE_TEXT(
        keyword = "end-ignore-text",
        correspondingOpeningCommandKey = IGNORE_TEXT,
    ),
    PRINT_TEXT(
        keyword = "print-text",
        attributeGroupConstraints = listOf(
            AttributeGroupConstraint(
                occurrence = ONE_ATTRIBUTE_GROUP,
                requiredAttributes = setOf(TEXT),
            )
        ),
    ),
    REMARK(
        keyword = "remark",
        attributeGroupConstraints = listOf(
            AttributeGroupConstraint(
                occurrence = ONE_ATTRIBUTE_GROUP,
                requiredAttributes = setOf(TEXT),
            )
        ),
    ),
    MODIFY_PROVIDED_FILENAME_BY_REPLACEMENTS(
        keyword = "modify-provided-filepath-by-replacements",
    ),
    RENDER_TEMPLATE(
        keyword = "render-template",
        attributeGroupConstraints = listOf(
            AttributeGroupConstraint(
                occurrence = ONE_ATTRIBUTE_GROUP,
                requiredAttributes = setOf(TEMPLATE_RENDERER_CLASS_NAME),
                optionalAttributes = setOf(TEMPLATE_RENDERER_PACKAGE_NAME),
            ),
            AttributeGroupConstraint(
                occurrence = ZERO_OR_MANY_ATTRIBUTE_GROUP,
                requiredAttributes = setOf(TEMPLATE_MODEL_NAME, MODEL_EXPRESSION),
            ),
        ),
    ),
    ADD_IMPORT_TO_RENDERER(
        keyword = "add-import-to-renderer",
        attributeGroupConstraints = listOf(
            AttributeGroupConstraint(
                occurrence = MANY_ATTRIBUTE_GROUP,
                requiredAttributes = setOf(IMPORT_CLASS_NAME),
                optionalAttributes = setOf(IMPORT_PACKAGE_NAME),
            )
        ),
    ),
    MOVE_COMMENT_BACKWARD(
        keyword = "move-comment-backward",
        aliases = setOf("mvb"),
        attributeGroupConstraints = listOf(
            AttributeGroupConstraint(
                occurrence = ZERO_OR_ONE_ATTRIBUTE_GROUP,
                optionalAttributes = setOf(BEFORE_FIRST_OCCURRENCE_OF, AFTER_FIRST_OCCURRENCE_OF, BEFORE_LAST_OCCURRENCE_OF, AFTER_LAST_OCCURRENCE_OF),
                mutualExclusiveAttributes = setOf(BEFORE_FIRST_OCCURRENCE_OF, AFTER_FIRST_OCCURRENCE_OF, BEFORE_LAST_OCCURRENCE_OF, AFTER_LAST_OCCURRENCE_OF),
            )
        ),
    ),
    MOVE_COMMENT_FORWARD(
        keyword = "move-comment-forward",
        aliases = setOf("mvf"),
        attributeGroupConstraints = listOf(
            AttributeGroupConstraint(
                occurrence = ZERO_OR_ONE_ATTRIBUTE_GROUP,
                optionalAttributes = setOf(BEFORE_FIRST_OCCURRENCE_OF, AFTER_FIRST_OCCURRENCE_OF, BEFORE_LAST_OCCURRENCE_OF, AFTER_LAST_OCCURRENCE_OF),
                mutualExclusiveAttributes = setOf(BEFORE_FIRST_OCCURRENCE_OF, AFTER_FIRST_OCCURRENCE_OF, BEFORE_LAST_OCCURRENCE_OF, AFTER_LAST_OCCURRENCE_OF),
            )
        ),
    ),
    REMOVE_BLANKS_BEFORE_COMMENT(
        keyword = "remove-blanks-before-comment",
        aliases = setOf("rbb"),
    ),
    REMOVE_BLANKS_AFTER_COMMENT(
        keyword = "remove-blanks-after-comment",
        aliases = setOf("rba"),
    ),
    REMOVE_BLANKS_AND_LINEBREAK_BEFORE_COMMENT(
        keyword = "remove-blanks-and-linebreak-before-comment",
        aliases = setOf("rlb"),
    ),
    REMOVE_BLANKS_AND_LINEBREAK_AFTER_COMMENT(
        keyword = "remove-blanks-and-linebreak-after-comment",
        aliases = setOf("rla"),
    ),
    NO_DEFAULT_WHITESPACE_REMOVE(
        keyword = "no-default-whitespace-remove",
        aliases = setOf("ndr"),
    ),
    ;

    companion object {
        /**
         * Groups of command keys that must not be used together in the same template comment because
         * they contradict each other. Within each group every command key excludes all other command
         * keys of the same group (e.g. moving a comment backward and forward at the same time, or
         * removing only the blanks and removing the blanks together with the line break on the same
         * side of a comment).
         */
        private val MUTUALLY_EXCLUSIVE_COMMAND_KEY_GROUPS: List<Set<CommandKey>> = listOf(
            setOf(MOVE_COMMENT_BACKWARD, MOVE_COMMENT_FORWARD),
            setOf(REMOVE_BLANKS_BEFORE_COMMENT, REMOVE_BLANKS_AND_LINEBREAK_BEFORE_COMMENT),
            setOf(REMOVE_BLANKS_AFTER_COMMENT, REMOVE_BLANKS_AND_LINEBREAK_AFTER_COMMENT),
        )

        init {
            val allNames = entries.flatMap { listOf(it.keyword) + it.aliases }
            val duplicates = allNames.groupingBy { it }.eachCount().filter { it.value > 1 }.keys
            require(duplicates.isEmpty()) {
                "CommandKey keywords and aliases must be unique, but found duplicates: $duplicates"
            }

            require(MUTUALLY_EXCLUSIVE_COMMAND_KEY_GROUPS.all { it.size >= 2 }) {
                "Each mutual exclusion group must contain at least two command keys."
            }
            val keysInGroups = MUTUALLY_EXCLUSIVE_COMMAND_KEY_GROUPS.flatten()
            val keysInMultipleGroups = keysInGroups.groupingBy { it }.eachCount().filter { it.value > 1 }.keys
            require(keysInMultipleGroups.isEmpty()) {
                "A command key must not appear in more than one mutual exclusion group, but found: $keysInMultipleGroups"
            }
        }

        fun fromKeywordOrAlias(keywordOrAlias: String): CommandKey? {
            return entries.firstOrNull { it.keyword == keywordOrAlias || keywordOrAlias in it.aliases }
        }

        fun allKeywords(): List<String> {
            return entries.flatMap { it.aliases + it.keyword }.sorted()
        }
    }

    val minNumberOfAttributeGroups: Int
        get() = attributeGroupConstraints.sumOf { it.occurrence.minNumberOfAttributeGroups }

    val maxNumberOfAttributeGroups: Int
        get() = attributeGroupConstraints.fold(0) { acc, c ->
            if (acc == Int.MAX_VALUE || c.occurrence.maxNumberOfAttributeGroups == Int.MAX_VALUE) Int.MAX_VALUE
            else acc + c.occurrence.maxNumberOfAttributeGroups
        }

    val allowedAttributes: Set<CommandAttributeKey>
        get() = attributeGroupConstraints.flatMap { it.allowedAttributes }.toSet()

    /**
     * The other command keys that must not appear together with this command key in the same template comment.
     */
    val mutuallyExclusiveCommandKeys: Set<CommandKey>
        get() = MUTUALLY_EXCLUSIVE_COMMAND_KEY_GROUPS
            .filter { this in it }
            .flatMapTo(mutableSetOf()) { it }
            .minus(this)

    private fun constraintForGroup(groupIndex: Int): AttributeGroupConstraint {
        require(attributeGroupConstraints.isNotEmpty()) {
            "constraintForGroup called on CommandKey '$keyword' which has no attribute group constraints"
        }
        return if (groupIndex < attributeGroupConstraints.size) attributeGroupConstraints[groupIndex]
        else attributeGroupConstraints.last()
    }

    fun requiredAttributesForGroup(groupIndex: Int): Set<CommandAttributeKey> =
        constraintForGroup(groupIndex).requiredAttributes

    fun allowedAttributesForGroup(groupIndex: Int): Set<CommandAttributeKey> =
        constraintForGroup(groupIndex).allowedAttributes

    fun mutualExclusiveAttributesForGroup(groupIndex: Int): Set<CommandAttributeKey> =
        constraintForGroup(groupIndex).mutualExclusiveAttributes

    fun missingRequiredAttributesForGroup(groupIndex: Int, presentAttributes: Set<CommandAttributeKey>): Set<CommandAttributeKey> {
        val required = requiredAttributesForGroup(groupIndex)
        return required.toMutableSet().apply { removeAll(presentAttributes) }
    }

    fun unallowedAttributesForGroup(groupIndex: Int, presentAttributes: Set<CommandAttributeKey>): Set<CommandAttributeKey> {
        val allowed = allowedAttributesForGroup(groupIndex)
        return presentAttributes.toMutableSet().apply { removeAll(allowed) }
    }

    val correspondingClosingCommandKey: CommandKey?
        get() = entries.singleOrNull { it.correspondingOpeningCommandKey == this }

    val isOpeningCommand: Boolean
        get() = correspondingClosingCommandKey != null
    val isClosingCommand: Boolean
        get() = this.correspondingOpeningCommandKey != null
    val isRequiredDirectlyNestedInOtherCommand: Boolean
        get() = this.directlyNestedInsideCommandKey != null

    val correspondingOpeningCommandKeyForAutoclose: CommandKey?
        get() = correspondingOpeningCommandKey ?: directlyNestedInsideCommandKey

    val isTriggerAutoclose: Boolean
        get() = this.correspondingOpeningCommandKeyForAutoclose != null
}
