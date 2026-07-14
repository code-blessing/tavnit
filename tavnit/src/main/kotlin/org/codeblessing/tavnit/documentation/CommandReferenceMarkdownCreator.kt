package org.codeblessing.tavnit.documentation

import org.codeblessing.tavnit.AttributeGroupOccurrence
import org.codeblessing.tavnit.CommandAttributeKey
import org.codeblessing.tavnit.CommandKey
import org.codeblessing.tavnit.IsListValue

object CommandReferenceMarkdownCreator {

    private const val COMMAND_PREFIX = "@"

    // Shared note appended to every remove-blanks command, explaining the default whitespace
    // handling and how these commands override it per side.
    private const val DEFAULT_WHITESPACE_HANDLING_NOTE =
        "The exact whitespace rules and override commands are described here: [WHITESPACE-HANDLING.md](WHITESPACE-HANDLING.md)"

    // linked map to preserve the order of the keys
    private val commandKeyDocumentation: Map<CommandKey, List<String>> = linkedMapOf(
        CommandKey.TEMPLATE_RENDERER to listOf(
            "Defines the template renderer kotlin class in which the content of the given file is put into. Optionally declares model instances (kotlin function parameters) passed to the renderer. ",
            "The first attribute group specifies the renderer class; subsequent repeating groups each define one model parameter.",
            "Additional ${CommandKey.TEMPLATE_RENDERER.keyword} commands can be nested inside the top-level one; each nested template-renderer produces an independent renderer class and is closed with ${CommandKey.END_TEMPLATE_RENDERER.keyword}. " +
                "A nested template renderer is completely independent (and its content therefore removed from) the parent template renderer. " +
                "Also all other commands defined in the parent template (models, if..else, replacements, etc.) will not affect the child template renderer, as each template renderer resides in its own class.",
        ),
        CommandKey.END_TEMPLATE_RENDERER to emptyList(),
        CommandKey.REPLACE_VALUE_BY_EXPRESSION to listOf(
            "Replaces a value by a kotlin expression in a multiline string. The expression is often accessing properties or functions on a model instance declared with the ${CommandKey.TEMPLATE_RENDERER.keyword} command.",
        ),
        CommandKey.END_REPLACE_VALUE_BY_EXPRESSION to emptyList(),
        CommandKey.REPLACE_VALUE_BY_VALUE to listOf(
            "Replaces a value by another (fixed) value.",
        ),
        CommandKey.END_REPLACE_VALUE_BY_VALUE to emptyList(),
        CommandKey.IF_CONDITION to listOf(
            "Render the enclosed content only if the condition expression evaluates to true.",
        ),
        CommandKey.ELSE_IF_CONDITION to listOf(
            "Render the enclosed content only if the condition expression evaluates to true and all previous conditions of the ${CommandKey.IF_CONDITION.keyword}/${CommandKey.ELSE_IF_CONDITION.keyword} conditions evaluates to false.",
        ),
        CommandKey.ELSE_CLAUSE to listOf(
            "Render the enclosed content only if all previous ${CommandKey.IF_CONDITION.keyword}/${CommandKey.ELSE_IF_CONDITION.keyword} conditions evaluates to false",
        ),
        CommandKey.END_IF_CONDITION to emptyList(),
        CommandKey.FOREACH to listOf(
            "Iterates/Loops over a collection of items (=iterable). In each loop, the current item is hold in a loop variable.",
        ),
        CommandKey.END_FOREACH to emptyList(),
        CommandKey.IGNORE_TEXT to listOf(
            "Ignores the text and does not output it in the template renderer.",
        ),
        CommandKey.END_IGNORE_TEXT to emptyList(),
        CommandKey.PRINT_TEXT to listOf(
            "Print additional text as output of the template renderer.",
        ),
        CommandKey.REMARK to listOf(
            "Adds a remark to the template that is purely meant for internal documentation. " +
                    "The text is completely ignored: it neither appears in the generated template renderer nor in its output.",
            "Use this command to leave notes or explanations inside a template (e.g. why a certain construct is built the way it is) " +
                    "without affecting the result in any way.",
        ),
        CommandKey.MODIFY_PROVIDED_FILENAME_BY_REPLACEMENTS to listOf(
            "Each template renderer provides the path of the source file as string. By using this command, the path can be modified with all replacements " +
                "provided by ```${CommandKey.REPLACE_VALUE_BY_EXPRESSION.keyword}``` and ```${CommandKey.REPLACE_VALUE_BY_VALUE.keyword}``` the " +
                "```${CommandKey.MODIFY_PROVIDED_FILENAME_BY_REPLACEMENTS.keyword}``` command is currently nested in.",
            "The intention of this command is that the filename and path can also take part of the replacements and this has not to be handled " +
                    "separately and outside of the template renderer; the replacements for the filename follow often the same patterns as for the file content. " +
                    "If you change in your template every ```foo``` to ```bar```, it is likely that you also want to change the path of the file " +
                    "e.g. from ```src/foo/foo.txt``` to ```src/bar/bar.txt``` to generate dynamic file paths.",
            "You can use this command multiple times per template renderer. The replacements are done one after another in the order of the command usage.",
            "If you create multiple template renderers from one file (multiple ${CommandKey.TEMPLATE_RENDERER.keyword}), you can (and have to) " +
                    "call ```${CommandKey.MODIFY_PROVIDED_FILENAME_BY_REPLACEMENTS.keyword}``` for each template renderer individually.",
        ),
        CommandKey.RENDER_TEMPLATE to listOf(
            "Calls another template renderer and embeds its output. The first attribute group specifies the renderer class; subsequent groups map model parameters to expressions.",
            "This command's syntax has a lot of similarity to ${CommandKey.TEMPLATE_RENDERER.keyword}, as it calls a " +
                    "template renderer defined by the ${CommandKey.TEMPLATE_RENDERER.keyword} block."
        ),
        CommandKey.ADD_IMPORT_TO_RENDERER to listOf(
            "Adds one or more imports to the generated template renderer class. Each repeating attribute group adds one import.",
            "Once a symbol is imported, it can be referenced by its short name in any expression (e.g. in " +
                    "```${CommandKey.IF_CONDITION.keyword}``` conditions, ```${CommandKey.FOREACH.keyword}``` iterables or " +
                    "```${CommandKey.REPLACE_VALUE_BY_EXPRESSION.keyword}``` expressions). This lets you keep those expressions short and readable " +
                    "instead of repeating the fully qualified name (e.g. write ```DayOfWeek.WEDNESDAY``` instead of ```java.time.DayOfWeek.WEDNESDAY```).",
            "Imports are added to the renderer class header in addition to the imports tavnit derives automatically " +
                    "(model classes, the renderer interface and rendered sub-templates). Duplicate imports are removed.",
        ),
        CommandKey.MOVE_COMMENT_BACKWARD to listOf(
            "Moves the whole comment in which this command is written backward (i.e. before the preceding text). " +
                    "Optionally positions it relative to the first or last occurrence of a given text in the surrounding content. " +
                    "The comment will be moved at most to the previous comment or to the beginning of the file.",
            "This is useful as some file formats do not allow to put a comment as first line of the file.",
            "Example:  XML starts with a preamble like ```<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>``` and this " +
                    "text should be part of the template renderer's output. But it is not possible to write a XML comment before " +
                    "this preamble. To still span the template from the beginning of the file, you can move the comment to the " +
                    "beginning of the file using this command " +
                    "(```${CommandKey.MOVE_COMMENT_BACKWARD.commandPrefix()}${CommandKey.MOVE_COMMENT_BACKWARD.keyword}```)",
        ),
        CommandKey.MOVE_COMMENT_FORWARD to listOf(
            "Moves the whole comment in which this command is written forward (i.e. after the following text). " +
                    "Optionally positions it relative to the first or last occurrence of a given text in the surrounding content. " +
                    "The comment will be moved at most to the next comment or to the end of the file.",
        ),
        CommandKey.REMOVE_BLANKS_BEFORE_COMMENT to listOf(
            "Removes the consecutive blanks (spaces and tabs) directly preceding the comment from the neighboring text part. " +
                    "Stops before the line-ending; the line-ending itself is kept.",
            "This is useful if you don't want to have dangling spaces/indents in your template output if the " +
                    "tavnit comments itself have to follow some indentation rules (e.g. by your linter).",
            DEFAULT_WHITESPACE_HANDLING_NOTE,
        ),
        CommandKey.REMOVE_BLANKS_AFTER_COMMENT to listOf(
            "Removes the consecutive blanks (spaces and tabs) directly following the comment from the neighboring text part. " +
                    "Stops before the line-ending; the line-ending itself is kept.",
            "This is useful if you don't want to have dangling spaces/indents in your template output if the " +
                    "tavnit comments itself have to follow some indentation rules (e.g. by your linter).",
            DEFAULT_WHITESPACE_HANDLING_NOTE,
        ),
        CommandKey.REMOVE_BLANKS_AND_LINEBREAK_BEFORE_COMMENT to listOf(
            "Removes the consecutive blanks (spaces and tabs) directly preceding the comment from the neighboring text part, " +
                    "including the immediately adjacent line-ending.",
            "This is useful if you don't want to have empty lines in your template output due to the tavnits comments.",
            DEFAULT_WHITESPACE_HANDLING_NOTE,
        ),
        CommandKey.REMOVE_BLANKS_AND_LINEBREAK_AFTER_COMMENT to listOf(
            "Removes the consecutive blanks (spaces and tabs) directly following the comment from the neighboring text part, " +
                    "including the immediately adjacent line-ending.",
            "This is useful if you don't want to have empty lines in your template output due to the tavnits comments.",
            DEFAULT_WHITESPACE_HANDLING_NOTE,
        ),
        CommandKey.NO_DEFAULT_WHITESPACE_REMOVE to listOf(
            "Disables the default whitespace handling for the comment it is written in. Only the comment itself is removed; " +
                    "no blanks and no line breaks before or after the comment are touched.",
            "Normally a comment that stands alone on its line has its surrounding whitespace collapsed automatically " +
                    "(see the note below). Use this command when you intentionally want to keep that whitespace " +
                    "(e.g. a blank line or the indentation) around an otherwise stand-alone comment.",
            "Explicit remove-blanks commands still take effect: they override the (now disabled) default decision for their " +
                    "own side. So you can combine this command with e.g. ```${COMMAND_PREFIX}${CommandKey.REMOVE_BLANKS_BEFORE_COMMENT.keyword}``` " +
                    "to keep everything except the blanks directly before the comment.",
            DEFAULT_WHITESPACE_HANDLING_NOTE,
        ),
    )

    // A short, worked example per command, rendered as a fenced code block in the reference.
    // Closing (end-*) commands are demonstrated inside their opening command's example, so they
    // intentionally have no separate entry here.
    private val commandKeyExamples: Map<CommandKey, String> = linkedMapOf(
        CommandKey.TEMPLATE_RENDERER to """
            ```html
            <!-- @tt{{{
              @template-renderer
                 [ templateRendererClassName="GreetingRenderer" templateRendererPackageName="com.example.render" ]
                 [ modelName="user" modelClassName="User" modelPackageName="com.example.model" ]
            }}}@ -->
            <p>Hello</p>
            <!-- @tt{{{ @end-template-renderer }}}@ -->
            ```
        """.trimIndent(),
        CommandKey.REPLACE_VALUE_BY_EXPRESSION to """
            ```html
            <h1><!-- @tt{{{ @replace-value-by-expression [ searchValue="TITLE" replaceByExpression="page.title" ] }}}@ -->TITLE<!-- @tt{{{ @end-replace-value-by-expression }}}@ --></h1>
            ```
        """.trimIndent(),
        CommandKey.REPLACE_VALUE_BY_VALUE to """
            ```html
            <a><!-- @tt{{{ @replace-value-by-value [ searchValue="URL" replaceByValue="/home" ] }}}@ -->URL<!-- @tt{{{ @end-replace-value-by-value }}}@ --></a>
            ```
        """.trimIndent(),
        CommandKey.IF_CONDITION to """
            ```html
            <!-- @tt{{{ @if [ conditionExpression="user.isAdmin" ] }}}@ -->
            <p>Welcome, administrator.</p>
            <!-- @tt{{{ @end-if }}}@ -->
            ```
        """.trimIndent(),
        CommandKey.ELSE_IF_CONDITION to """
            ```html
            <!-- @tt{{{ @if [ conditionExpression="cart.isEmpty()" ] }}}@ -->
            <p>Your cart is empty.</p>
            <!-- @tt{{{ @else-if [ conditionExpression="cart.hasSingleItem()" ] }}}@ -->
            <p>One item in your cart.</p>
            <!-- @tt{{{ @else }}}@ -->
            <p>Several items in your cart.</p>
            <!-- @tt{{{ @end-if }}}@ -->
            ```
        """.trimIndent(),
        CommandKey.ELSE_CLAUSE to """
            ```html
            <!-- @tt{{{ @if [ conditionExpression="user.isLoggedIn" ] }}}@ -->
            <p>Welcome back.</p>
            <!-- @tt{{{ @else }}}@ -->
            <p>Please log in.</p>
            <!-- @tt{{{ @end-if }}}@ -->
            ```
        """.trimIndent(),
        CommandKey.FOREACH to """
            ```html
            <ul><!-- @tt{{{ @foreach [ iteratorExpression="page.items" loopVariable="item" ] }}}@ -->
              <li>an item</li><!-- @tt{{{ @end-foreach }}}@ -->
            </ul>
            ```
        """.trimIndent(),
        CommandKey.IGNORE_TEXT to """
            ```html
            <ul><!-- @tt{{{ @foreach [ iteratorExpression="page.items" loopVariable="item" ] }}}@ -->
              <li>an item</li><!-- @tt{{{ @end-foreach @ignore-text }}}@ -->
              <li>only a sample row to keep the raw file looking complete</li>
              <li>another sample row</li><!-- @tt{{{ @end-ignore-text }}}@ -->
            </ul>
            ```
        """.trimIndent(),
        CommandKey.PRINT_TEXT to """
            ```html
            <p><!-- @tt{{{ @print-text [ text="Generated by tavnit" ] }}}@ --></p>
            ```
        """.trimIndent(),
        CommandKey.REMARK to """
            ```html
            <!-- @tt{{{ @remark [ text="This block is repeated once per article." ] }}}@ -->
            ```
        """.trimIndent(),
        CommandKey.MODIFY_PROVIDED_FILENAME_BY_REPLACEMENTS to """
            ```html
            <!-- @tt{{{
              @template-renderer [ templateRendererClassName="PageRenderer" ]
              @replace-value-by-value [ searchValue="index" replaceByValue="home" ]
              @modify-provided-filepath-by-replacements
            }}}@ -->
            ```

            With a source file `src/index.html` the generated `filePath(...)` returns `src/home.html`.
        """.trimIndent(),
        CommandKey.RENDER_TEMPLATE to """
            ```html
            <tbody><!-- @tt{{{
              @foreach [ iteratorExpression="page.rows" loopVariable="row" ]
              @render-template
                 [ templateRendererClassName="RowRenderer" templateRendererPackageName="com.example.render" ]
                 [ modelName="row" modelExpression="row" ]
              @end-foreach
            }}}@ --></tbody>
            ```
        """.trimIndent(),
        CommandKey.ADD_IMPORT_TO_RENDERER to """
            ```html
            <!-- @tt{{{ @add-import-to-renderer [ importClassName="DayOfWeek.WEDNESDAY" importPackageName="java.time" ] }}}@ -->
            ```
        """.trimIndent(),
        CommandKey.MOVE_COMMENT_BACKWARD to """
            ```xml
            <?xml version="1.0"?><!-- @tt{{{
              @move-comment-backward
              @template-renderer [ templateRendererClassName="FeedRenderer" ]
            }}}@ -->
            <feed>...</feed>
            ```
        """.trimIndent(),
        CommandKey.MOVE_COMMENT_FORWARD to """
            ```html
            <!-- @tt{{{ @move-comment-forward [ afterFirstOccurrenceOf="</head>" ] }}}@ -->
            ```
        """.trimIndent(),
        CommandKey.REMOVE_BLANKS_BEFORE_COMMENT to """
            ```html
            <li>item</li>    <!-- @tt{{{ @remove-blanks-before-comment }}}@ -->
            ```

            The blanks between `</li>` and the comment are removed; the line-ending is kept.
        """.trimIndent(),
        CommandKey.REMOVE_BLANKS_AFTER_COMMENT to """
            ```html
            <!-- @tt{{{ @remove-blanks-after-comment }}}@ -->    <li>item</li>
            ```

            The blanks between the comment and `<li>` are removed; the line-ending is kept.
        """.trimIndent(),
        CommandKey.REMOVE_BLANKS_AND_LINEBREAK_BEFORE_COMMENT to """
            ```html
            <p>text</p>
            <!-- @tt{{{ @remove-blanks-and-linebreak-before-comment }}}@ -->
            ```

            The blanks and the line-ending directly before the comment are removed, so no empty
            line is left in the output.
        """.trimIndent(),
        CommandKey.REMOVE_BLANKS_AND_LINEBREAK_AFTER_COMMENT to """
            ```html
            <!-- @tt{{{ @remove-blanks-and-linebreak-after-comment }}}@ -->
            <p>text</p>
            ```

            The blanks and the line-ending directly after the comment are removed, so no empty
            line is left in the output.
        """.trimIndent(),
        CommandKey.NO_DEFAULT_WHITESPACE_REMOVE to """
            ```html
                <!-- @tt{{{ @no-default-whitespace-remove }}}@ -->
            ```

            Only the comment itself is removed; the indentation and the surrounding line breaks that
            the default handling would otherwise collapse are kept.
        """.trimIndent(),
    )


    // linked map to preserve the order of the keys
    private val commandAttributeKeyDocumentation: Map<CommandAttributeKey, List<String>> = linkedMapOf(
        CommandAttributeKey.TEMPLATE_RENDERER_CLASS_NAME to listOf(
            "The name of the template class that will generate this template.",
        ),
        CommandAttributeKey.TEMPLATE_RENDERER_PACKAGE_NAME to listOf(
            "The name of the package where the class defined with ```${CommandAttributeKey.TEMPLATE_RENDERER_CLASS_NAME.keyAsString}``` resides in.",
        ),
        CommandAttributeKey.TEMPLATE_RENDERER_INTERFACE_NAME to listOf(
            "The name of an optional interface class name that is added to the class defined with the ```${CommandAttributeKey.TEMPLATE_RENDERER_CLASS_NAME.keyAsString}```.",
        ),
        CommandAttributeKey.TEMPLATE_RENDERER_INTERFACE_PACKAGE_NAME to listOf(
            "The name of the package where the interface defined with ```${CommandAttributeKey.TEMPLATE_RENDERER_INTERFACE_NAME.keyAsString}``` resides in.",
        ),
        CommandAttributeKey.TEMPLATE_MODEL_NAME to listOf(
            "The name of the model variable. The variable can later be used to access fields and functions on the model e.g. in conditions or as replacement values.",
        ),
        CommandAttributeKey.TEMPLATE_MODEL_CLASS_NAME to listOf(
            "The name of the model class. This class provides all the fields in the template.",
        ),
        CommandAttributeKey.TEMPLATE_MODEL_PACKAGE_NAME to listOf(
            "The name of the package where the model class defined with ```${CommandAttributeKey.TEMPLATE_MODEL_CLASS_NAME.keyAsString}``` resides in.",
        ),
        CommandAttributeKey.TEMPLATE_MODEL_IS_LIST to listOf(
            "When set to ```${IsListValue.YES.value}```, the model parameter is declared as a list of the model class defined with ```${CommandAttributeKey.TEMPLATE_MODEL_CLASS_NAME.keyAsString}```, i.e. ```List<ModelClass>``` instead of ```ModelClass```. Defaults to ```${IsListValue.NO.value}```.",
        ),
        CommandAttributeKey.SEARCH_VALUE to listOf(
            "The token that has to be searched in the enclosed block of content. The search is case-sensitive.",
        ),
        CommandAttributeKey.REPLACE_BY_EXPRESSION to listOf(
            "The expression accessing the model class with which the token defined with the attribute ```${CommandAttributeKey.SEARCH_VALUE.keyAsString}``` is replaced.",
        ),
        CommandAttributeKey.REPLACE_BY_VALUE to listOf(
            "The plain value the attribute ```${CommandAttributeKey.SEARCH_VALUE.keyAsString}``` is replaced.",
        ),
        CommandAttributeKey.CONDITION_EXPRESSION to listOf(
            "The condition returning a boolean value that is used for the if statement or else-if statement.",
        ),
        CommandAttributeKey.LOOP_ITERABLE_EXPRESSION to listOf(
            "The expression returning the collection/iterable that is looped over.",
        ),
        CommandAttributeKey.LOOP_VARIABLE_NAME to listOf(
            "The name of the loop variable, similar to the model variable from ```${CommandAttributeKey.TEMPLATE_MODEL_NAME.keyAsString}```. The variable holds the current instance of the loop iterable defined with ```${CommandAttributeKey.LOOP_ITERABLE_EXPRESSION.keyAsString}```.",
        ),
        CommandAttributeKey.TEXT to listOf(
            "The text content of the command (e.g. the text printed by ```${CommandKey.PRINT_TEXT.keyword}``` " +
                    "or the remark documented by ```${CommandKey.REMARK.keyword}```).",
        ),
        CommandAttributeKey.MODEL_EXPRESSION to listOf(
            "The expression that provides the value for the model parameter specified by ```${CommandAttributeKey.TEMPLATE_MODEL_NAME.keyAsString}``` when calling the template renderer.",
        ),
        CommandAttributeKey.IMPORT_CLASS_NAME to listOf(
            "The name of the symbol to import (e.g. a class, object or enum entry like ```DayOfWeek.WEDNESDAY```). " +
                    "Combined with ```${CommandAttributeKey.IMPORT_PACKAGE_NAME.keyAsString}``` (when given) to form the fully qualified import.",
        ),
        CommandAttributeKey.IMPORT_PACKAGE_NAME to listOf(
            "The name of the package the symbol defined with ```${CommandAttributeKey.IMPORT_CLASS_NAME.keyAsString}``` resides in (e.g. ```java.time```). " +
                    "When omitted, the value of ```${CommandAttributeKey.IMPORT_CLASS_NAME.keyAsString}``` is imported as-is.",
        ),
        CommandAttributeKey.BEFORE_FIRST_OCCURRENCE_OF to listOf(
            "Positions the comment before the first occurrence of the given text in the surrounding content.",
        ),
        CommandAttributeKey.AFTER_FIRST_OCCURRENCE_OF to listOf(
            "Positions the comment after the first occurrence of the given text in the surrounding content.",
        ),
        CommandAttributeKey.BEFORE_LAST_OCCURRENCE_OF to listOf(
            "Positions the comment before the last occurrence of the given text in the surrounding content.",
        ),
        CommandAttributeKey.AFTER_LAST_OCCURRENCE_OF to listOf(
            "Positions the comment after the last occurrence of the given text in the surrounding content.",
        ),
    )

    private fun CommandKey.createMarkDownChapterLink(): String {
        return "[$keyword](#${keyword.lowercase().replace(' ', '-')})"
    }

    fun createMarkdownDocumentation(): String {
        val sb = StringBuilder()
        sb.appendLine("""
            # Keyword/Command reference
        """.trimIndent())

        sb.appendLine("""

            The following keywords/commands are supported:
        """.trimIndent())
        for ((commandKey, _) in commandKeyDocumentation) {
            val aliases = if(commandKey.aliases.isNotEmpty()) " (${commandKey.aliases.joinToString()})" else ""
            sb.appendLine("* ${commandKey.createMarkDownChapterLink()}${aliases}")
        }
        sb.appendLine("""

            Commands always start with a `$COMMAND_PREFIX`.


        """.trimIndent())

        for ((commandKey, docLines) in commandKeyDocumentation) {
            sb.appendLine("""

                    ## ${commandKey.keyword}
                """.trimIndent()
            )
            sb.appendLine()
            sb.appendLine("Syntax: ")
            sb.appendLine("``````")
            sb.appendLine(commandKey.commandSyntax())
            sb.appendLine("``````")
            sb.appendLine("""

                    ${commandKey.aliasesDescription()}
                """.trimIndent()
            )
            for (line in docLines) {
                sb.appendLine("""

                        $line
                    """.trimIndent()
                )
            }
            commandKeyExamples[commandKey]?.let { example ->
                sb.appendLine()
                sb.appendLine("Example:")
                sb.appendLine()
                sb.appendLine(example)
            }
            sb.appendLine("""

                    Varia:
                    * ${commandKey.openingClosingDescription()}
                    * ${commandKey.autoclosingDescription()}
                    * ${commandKey.groupSupportDescription()}
                    * ${commandKey.nestingDescription()}
                    * ${commandKey.mutualExclusionDescription()}
                """.trimIndent()
            )
            for ((constraintIndex, constraint) in commandKey.attributeGroupConstraints.withIndex()) {
                val sectionLabel = when {
                    commandKey.attributeGroupConstraints.size > 1 && constraintIndex == 0 -> "Primary Attributes"
                    constraint.occurrence == AttributeGroupOccurrence.MANY_ATTRIBUTE_GROUP
                            || constraint.occurrence == AttributeGroupOccurrence.ZERO_OR_MANY_ATTRIBUTE_GROUP -> "Repeatable Group Attributes"
                    else -> "Attributes"
                }
                sb.appendLine("""

                        $sectionLabel:
                    """.trimIndent()
                )
                val attributesDocumentation = commandAttributeKeyDocumentation.filter { it.key in constraint.allowedAttributes }
                createAttributeDocumentation(attributesDocumentation, constraint.requiredAttributes, constraint.mutualExclusiveAttributes, sb)
            }
        }
        return sb.toString()
    }

    private fun createAttributeDocumentation(
        attributesDocumentation: Map<CommandAttributeKey, List<String>>,
        requiredAttributes: Set<CommandAttributeKey>,
        mutualExclusiveAttributes: Set<CommandAttributeKey>,
        sb: StringBuilder
    ) {
        for((attributeKey, attributeDocLines) in attributesDocumentation) {
            val mutualExclusiveWith = calculateMutualExclusiveAttributes(attributeKey, mutualExclusiveAttributes)
            val descriptionText = attributeDocLines.joinToString("\n\n  ")
            sb.appendLine("""
                    * *${attributeKey.keyAsString}*: $descriptionText
                      * Required attribute: _${(attributeKey in requiredAttributes).yesOrNo()}_
                      * Required not empty: _${attributeKey.requireNotEmpty.yesOrNo()}_
                      * Allowed values: ${if(attributeKey.allowedValues == null) "_\\<unrestricted\\>_" else attributeKey.allowedValues.joinToString(",") { "```${it.value}```"}}
                      * Mutually exclusive with: ${if(mutualExclusiveWith.isEmpty()) "none" else mutualExclusiveWith.joinToString(", ") { "```${it.keyAsString}```" }}
                    """.trimIndent()
            )
        }
    }

    private fun calculateMutualExclusiveAttributes(
        attribute: CommandAttributeKey,
        mutualExclusiveAttributes: Set<CommandAttributeKey>
    ): Set<CommandAttributeKey> {
        if(attribute !in mutualExclusiveAttributes) {
            return emptySet()
        }
        return mutualExclusiveAttributes - attribute
    }

    private fun CommandKey.commandSyntax(): String {
        val sb = StringBuilder()
        sb.append("${commandPrefix()}${keyword}")
        if (!isClosingCommand) {
            for (constraint in attributeGroupConstraints) {
                sb.append("\n    [")
                constraint.allowedAttributes.forEach { attribute ->
                    val attributeValue = if (attribute.allowedValues == null) "..." else attribute.allowedValues.joinToString("|") { it.value }
                    sb.append("\n        ${attribute.keyAsString}=\"${attributeValue}\"")
                }
                sb.append("\n    ]")
                if (constraint.occurrence == AttributeGroupOccurrence.MANY_ATTRIBUTE_GROUP
                    || constraint.occurrence == AttributeGroupOccurrence.ZERO_OR_MANY_ATTRIBUTE_GROUP) {
                    sb.append("\n    [ ... ]")
                }
            }
            correspondingClosingCommandKey?.let { closingKey ->
                sb.append("\n    ....")
                sb.append("\n${closingKey.commandPrefix()}${closingKey.keyword}")
            }
        }
        return sb.toString()
    }

    private fun CommandKey.openingClosingDescription(): String {
        return if(!isOpeningCommand && !isClosingCommand) {
            "This command stands for itself and does not need to be closed by another command."
        } else if(isOpeningCommand) {
            "This command must be closed using the ${correspondingClosingCommandKey!!.createMarkDownChapterLink()} command."
        } else {
            "This command is closing the ${correspondingOpeningCommandKey!!.createMarkDownChapterLink()} command."
        }
    }

    private fun CommandKey.autoclosingDescription(): String {
        val correspondingClosingCommand = this.correspondingClosingCommandKey
        return if(isTriggerAutoclose) {
            "This command triggers to close all nested commands that support auto-closing."
        } else if(isAutoclosingSupported) {
            "This command supports to be auto-closed. The corresponding ${correspondingClosingCommand!!.createMarkDownChapterLink()} command can be skipped."
        } else {
            "This command neither triggers an auto-closing of nested commands nor will it be auto-closed."
        }
    }

    private fun CommandKey.groupSupportDescription(): String =
        when {
            attributeGroupConstraints.isEmpty() ->
                "This command/keyword does not support groups and has no attributes."
            attributeGroupConstraints.size == 1 && attributeGroupConstraints[0].occurrence == AttributeGroupOccurrence.ONE_ATTRIBUTE_GROUP ->
                "This command/keyword must have exactly one group of attributes."
            attributeGroupConstraints.size == 1 && attributeGroupConstraints[0].occurrence == AttributeGroupOccurrence.ZERO_OR_ONE_ATTRIBUTE_GROUP ->
                "This command can have zero or one group of attributes."
            attributeGroupConstraints.size == 1 && attributeGroupConstraints[0].occurrence == AttributeGroupOccurrence.MANY_ATTRIBUTE_GROUP ->
                "This command can have many groups of attributes"
            attributeGroupConstraints.size == 1 && attributeGroupConstraints[0].occurrence == AttributeGroupOccurrence.ZERO_OR_MANY_ATTRIBUTE_GROUP ->
                "This command can have zero or many groups of attributes"
            attributeGroupConstraints.size > 1 && attributeGroupConstraints.last().occurrence == AttributeGroupOccurrence.MANY_ATTRIBUTE_GROUP ->
                "This command has a primary group of attributes followed by one or more groups of attributes."
            attributeGroupConstraints.size > 1 && attributeGroupConstraints.last().occurrence == AttributeGroupOccurrence.ZERO_OR_MANY_ATTRIBUTE_GROUP ->
                "This command has a primary group of attributes optionally followed by zero or more groups of attributes."
            attributeGroupConstraints.size > 1 && attributeGroupConstraints.last().occurrence == AttributeGroupOccurrence.ZERO_OR_ONE_ATTRIBUTE_GROUP ->
                "This command has a primary group of attributes optionally followed by one more group of attributes."
            else ->
                "This command/keyword has ${attributeGroupConstraints.size} fixed groups of attributes."
        }

    private fun CommandKey.aliasesDescription(): String {
        if (aliases.isEmpty()) {
            return "Aliases: _none_"
        }
        val aliasesAsMarkdown = aliases.joinToString(", ") { "```${commandPrefix()}$it```" }
        return "Aliases: $aliasesAsMarkdown (can be used in place of ```${commandPrefix()}${keyword}```)"
    }

    private fun CommandKey.mutualExclusionDescription(): String {
        val exclusions = mutuallyExclusiveCommandKeys.sortedBy { it.keyword }
        return if (exclusions.isEmpty()) {
            "This command/keyword can be combined with any other command in the same comment."
        } else {
            "This command/keyword must not be used in the same comment as ${exclusions.joinToString(", ") { it.createMarkDownChapterLink() }} (mutually exclusive)."
        }
    }

    private fun CommandKey.nestingDescription(): String =
        if(directlyNestedInsideCommandKey == null) {
            "This command/keyword is NOT forced to reside as nested element in a certain parent element."
        } else {
            "This command/keyword must reside as directly nested element in the parent element ${directlyNestedInsideCommandKey.createMarkDownChapterLink()}."
        }

    private fun Boolean.yesOrNo(): String {
        return if (this) "Yes" else "No"
    }

    private fun CommandKey.commandPrefix(): String = COMMAND_PREFIX
}
