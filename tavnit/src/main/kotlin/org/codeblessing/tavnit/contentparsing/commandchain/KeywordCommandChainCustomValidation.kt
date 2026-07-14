package org.codeblessing.tavnit.contentparsing.commandchain

import org.codeblessing.tavnit.CommandAttributeKey
import org.codeblessing.tavnit.contentparsing.KeywordCommand
import org.codeblessing.tavnit.contentparsing.TemplateParsingErrorCode
import org.codeblessing.tavnit.contentparsing.TemplateParsingException
import org.codeblessing.tavnit.contentparsing.resolver.TemplateCommentContentPart
import org.codeblessing.tavnit.contentparsing.resolver.TemplateContentPart
import javax.lang.model.SourceVersion

object KeywordCommandChainCustomValidation {

    fun validate(templateContentParts: List<TemplateContentPart>): List<TemplateContentPart> {
        for (part in templateContentParts) {
            if (part is TemplateCommentContentPart) {
                for (keywordCommand in part.keywordCommands) {
                    validateCommand(part, keywordCommand)
                }
            }
        }
        return templateContentParts
    }

    private fun validateCommand(part: TemplateCommentContentPart, keywordCommand: KeywordCommand) {
        for (groupIndex in keywordCommand.attributeGroupIndices()) {
            validateGroup(part, keywordCommand, groupIndex)
        }
        validateUniqueModelNames(part, keywordCommand)
    }

    private fun validateGroup(
        part: TemplateCommentContentPart,
        keywordCommand: KeywordCommand,
        groupIndex: Int,
    ) {
        validateClassName(part, keywordCommand, groupIndex, CommandAttributeKey.TEMPLATE_RENDERER_CLASS_NAME)
        validateClassName(part, keywordCommand, groupIndex, CommandAttributeKey.TEMPLATE_RENDERER_INTERFACE_NAME)
        validatePackageName(part, keywordCommand, groupIndex, CommandAttributeKey.TEMPLATE_RENDERER_PACKAGE_NAME)
        validatePackageName(part, keywordCommand, groupIndex, CommandAttributeKey.TEMPLATE_RENDERER_INTERFACE_PACKAGE_NAME)
        validateClassName(part, keywordCommand, groupIndex, CommandAttributeKey.TEMPLATE_MODEL_CLASS_NAME)
        validatePackageName(part, keywordCommand, groupIndex, CommandAttributeKey.TEMPLATE_MODEL_PACKAGE_NAME)
        validateParameterName(part, keywordCommand, groupIndex, CommandAttributeKey.TEMPLATE_MODEL_NAME)
        validateParameterName(part, keywordCommand, groupIndex, CommandAttributeKey.LOOP_VARIABLE_NAME)
        validateImportName(part, keywordCommand, groupIndex, CommandAttributeKey.IMPORT_CLASS_NAME)
        validateImportName(part, keywordCommand, groupIndex, CommandAttributeKey.IMPORT_PACKAGE_NAME)
    }

    private fun validateClassName(
        part: TemplateCommentContentPart,
        keywordCommand: KeywordCommand,
        groupIndex: Int,
        attributeKey: CommandAttributeKey,
    ) {
        val value = keywordCommand.attributeOptional(groupIndex, attributeKey) ?: return
        if (!isValidJavaClassName(value)) {
            throw TemplateParsingException(
                lineNumbers = part.lineNumbers,
                errorCode = TemplateParsingErrorCode.INVALID_JAVA_CLASS_NAME,
                msg = TemplateParsingErrorCode.INVALID_JAVA_CLASS_NAME.resolve(
                    "value" to value,
                    "attributeKey" to attributeKey.keyAsString,
                    "command" to keywordCommand.commandKey.keyword,
                ),
            )
        }
    }

    private fun validatePackageName(
        part: TemplateCommentContentPart,
        keywordCommand: KeywordCommand,
        groupIndex: Int,
        attributeKey: CommandAttributeKey,
    ) {
        val value = keywordCommand.attributeOptional(groupIndex, attributeKey) ?: return
        if (!isValidJavaPackageName(value)) {
            throw TemplateParsingException(
                lineNumbers = part.lineNumbers,
                errorCode = TemplateParsingErrorCode.INVALID_JAVA_PACKAGE_NAME,
                msg = TemplateParsingErrorCode.INVALID_JAVA_PACKAGE_NAME.resolve(
                    "value" to value,
                    "attributeKey" to attributeKey.keyAsString,
                    "command" to keywordCommand.commandKey.keyword,
                ),
            )
        }
    }

    private fun validateParameterName(
        part: TemplateCommentContentPart,
        keywordCommand: KeywordCommand,
        groupIndex: Int,
        attributeKey: CommandAttributeKey,
    ) {
        val value = keywordCommand.attributeOptional(groupIndex, attributeKey) ?: return
        if (!isValidJavaParameterName(value)) {
            throw TemplateParsingException(
                lineNumbers = part.lineNumbers,
                errorCode = TemplateParsingErrorCode.INVALID_JAVA_PARAMETER_NAME,
                msg = TemplateParsingErrorCode.INVALID_JAVA_PARAMETER_NAME.resolve(
                    "value" to value,
                    "attributeKey" to attributeKey.keyAsString,
                    "command" to keywordCommand.commandKey.keyword,
                ),
            )
        }
    }

    private fun validateImportName(
        part: TemplateCommentContentPart,
        keywordCommand: KeywordCommand,
        groupIndex: Int,
        attributeKey: CommandAttributeKey,
    ) {
        val value = keywordCommand.attributeOptional(groupIndex, attributeKey) ?: return
        // an empty import package name is allowed and means the class name is already qualified
        if (value.isEmpty()) return
        if (!isValidImportName(value)) {
            throw TemplateParsingException(
                lineNumbers = part.lineNumbers,
                errorCode = TemplateParsingErrorCode.INVALID_IMPORT_NAME,
                msg = TemplateParsingErrorCode.INVALID_IMPORT_NAME.resolve(
                    "value" to value,
                    "attributeKey" to attributeKey.keyAsString,
                    "command" to keywordCommand.commandKey.keyword,
                ),
            )
        }
    }

    private fun validateUniqueModelNames(
        part: TemplateCommentContentPart,
        keywordCommand: KeywordCommand,
    ) {
        val seen = mutableSetOf<String>()
        for (groupIndex in keywordCommand.attributeGroupIndices()) {
            val modelName = keywordCommand.attributeOptional(groupIndex, CommandAttributeKey.TEMPLATE_MODEL_NAME) ?: continue
            if (!seen.add(modelName)) {
                throw TemplateParsingException(
                    lineNumbers = part.lineNumbers,
                    errorCode = TemplateParsingErrorCode.DUPLICATE_MODEL_NAME,
                    msg = TemplateParsingErrorCode.DUPLICATE_MODEL_NAME.resolve(
                        "modelName" to modelName,
                        "command" to keywordCommand.commandKey.keyword,
                    ),
                )
            }
        }
    }

    private fun isValidJavaClassName(name: String): Boolean =
        SourceVersion.isIdentifier(name) && !SourceVersion.isKeyword(name)

    private fun isValidJavaPackageName(name: String): Boolean =
        name.isNotEmpty() && name.split('.').all { part ->
            SourceVersion.isIdentifier(part) && !SourceVersion.isKeyword(part)
        }

    private fun isValidJavaParameterName(name: String): Boolean =
        SourceVersion.isIdentifier(name) && !SourceVersion.isKeyword(name)

    private fun isValidImportName(name: String): Boolean =
        name.split('.').all { part ->
            SourceVersion.isIdentifier(part) && !SourceVersion.isKeyword(part)
        }
}
