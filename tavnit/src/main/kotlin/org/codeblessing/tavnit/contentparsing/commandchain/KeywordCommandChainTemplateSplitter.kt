package org.codeblessing.tavnit.contentparsing.commandchain

import org.codeblessing.tavnit.CommandAttributeKey
import org.codeblessing.tavnit.CommandAttributeKey.TEMPLATE_MODEL_CLASS_NAME
import org.codeblessing.tavnit.CommandAttributeKey.TEMPLATE_MODEL_IS_LIST
import org.codeblessing.tavnit.CommandAttributeKey.TEMPLATE_MODEL_NAME
import org.codeblessing.tavnit.CommandAttributeKey.TEMPLATE_MODEL_PACKAGE_NAME
import org.codeblessing.tavnit.CommandAttributeKey.TEMPLATE_RENDERER_CLASS_NAME
import org.codeblessing.tavnit.CommandAttributeKey.TEMPLATE_RENDERER_INTERFACE_NAME
import org.codeblessing.tavnit.CommandAttributeKey.TEMPLATE_RENDERER_INTERFACE_PACKAGE_NAME
import org.codeblessing.tavnit.CommandAttributeKey.TEMPLATE_RENDERER_PACKAGE_NAME
import org.codeblessing.tavnit.CommandKey
import org.codeblessing.tavnit.IsListValue
import org.codeblessing.tavnit.contentparsing.KeywordCommand
import org.codeblessing.tavnit.contentparsing.TemplateParsingErrorCode
import org.codeblessing.tavnit.contentparsing.TemplateParsingException
import org.codeblessing.tavnit.contentparsing.resolver.TemplateCommentContentPart
import org.codeblessing.tavnit.contentparsing.resolver.TemplateContentPart
import org.codeblessing.tavnit.contentparsing.resolver.TextContentPart
import org.codeblessing.tavnit.toEnum

/**
 * Splits a flat list of [TemplateContentPart]s into one [TemplateRendererDescription] per
 * `@template-renderer`…`@end-template-renderer` block found at any nesting depth.
 *
 * - Content before the first `@template-renderer` and after the last `@end-template-renderer`
 *   is silently discarded.
 * - Nested blocks are removed from their parent's template chain and returned as their own
 *   [TemplateRendererDescription] (outer first, then inner, depth-first).
 * - An unclosed `@template-renderer` block throws [TemplateParsingException].
 */
object KeywordCommandChainTemplateSplitter {

    private const val DEFAULT_PACKAGE_NAME = ""

    fun splitIntoTemplateRendererDescriptions(templateContentParts: List<TemplateContentPart>): List<TemplateRendererDescription> {
        return try {
            val result = mutableListOf<TemplateRendererDescription>()
            var i = 0
            while (i < templateContentParts.size) {
                val part = templateContentParts[i]
                if (part.isTemplateRendererCommand()) {
                    val extracted = extractBlock(templateContentParts, i)
                    result.add(interpretSingleTemplate(extracted.outerParts))
                    for (nestedSection in extracted.nestedSections) {
                        result.addAll(splitIntoTemplateRendererDescriptions(nestedSection))
                    }
                    i = extracted.nextIndex
                } else {
                    i++
                }
            }
            result
        } catch (e: TemplateParsingException) {
            throw e
        } catch (e: Exception) {
            throw TemplateParsingException(
                errorCode = TemplateParsingErrorCode.ERROR_SPLITTING_TEMPLATE,
                msg = TemplateParsingErrorCode.ERROR_SPLITTING_TEMPLATE.resolve("message" to (e.message ?: "")),
                cause = e,
            )
        }
    }

    private data class ExtractedBlock(
        val outerParts: List<TemplateContentPart>,
        val nestedSections: List<List<TemplateContentPart>>,
        val nextIndex: Int,
    )

    private fun extractBlock(parts: List<TemplateContentPart>, startIndex: Int): ExtractedBlock {
        val outerParts = mutableListOf<TemplateContentPart>()
        val nestedSections = mutableListOf<List<TemplateContentPart>>()
        outerParts.add(parts[startIndex])
        var depth = 0
        var currentNestedSection: MutableList<TemplateContentPart>? = null
        var i = startIndex + 1

        while (i < parts.size) {
            val part = parts[i]
            if (currentNestedSection != null) {
                when {
                    part.isTemplateRendererCommand() -> {
                        depth++
                        currentNestedSection.add(part)
                    }
                    part.isEndTemplateRendererCommand() -> {
                        if (depth > 0) {
                            depth--
                            currentNestedSection.add(part)
                        } else {
                            currentNestedSection.add(part)
                            nestedSections.add(currentNestedSection)
                            currentNestedSection = null
                        }
                    }
                    else -> currentNestedSection.add(part)
                }
            } else {
                when {
                    part.isTemplateRendererCommand() -> {
                        currentNestedSection = mutableListOf(part)
                        depth = 0
                    }
                    part.isEndTemplateRendererCommand() -> {
                        outerParts.add(part)
                        return ExtractedBlock(outerParts, nestedSections, i + 1)
                    }
                    else -> outerParts.add(part)
                }
            }
            i++
        }

        throw TemplateParsingException(
            lineNumbers = parts[startIndex].lineNumbers,
            errorCode = TemplateParsingErrorCode.TEMPLATE_RENDERER_BLOCK_NOT_CLOSED,
            msg = TemplateParsingErrorCode.TEMPLATE_RENDERER_BLOCK_NOT_CLOSED.resolve(),
        )
    }

    private fun interpretSingleTemplate(templateContentParts: List<TemplateContentPart>): TemplateRendererDescription {
        val rendererCommand = findTemplateRendererCommand(templateContentParts)
        return TemplateRendererDescription(
            templateRendererClass = rendererCommand.toClassDescription(
                classNameAttribute = TEMPLATE_RENDERER_CLASS_NAME,
                packageNameAttribute = TEMPLATE_RENDERER_PACKAGE_NAME,
            ),
            templateRendererInterface = rendererCommand.toOptionalClassDescription(
                classNameAttribute = TEMPLATE_RENDERER_INTERFACE_NAME,
                packageNameAttribute = TEMPLATE_RENDERER_INTERFACE_PACKAGE_NAME,
            ),
            modelClasses = rendererCommand.toModelDescriptions(),
            templateChain = buildTemplateChain(templateContentParts),
        )
    }

    private fun buildTemplateChain(contentParts: List<TemplateContentPart>): List<TemplateContentPart> {
        val result = mutableListOf<TemplateContentPart>()
        for (part in contentParts) {
            when (part) {
                is TemplateCommentContentPart -> {
                    val filtered = part.keywordCommands.filter { cmd ->
                        cmd.commandKey != CommandKey.TEMPLATE_RENDERER &&
                        cmd.commandKey != CommandKey.END_TEMPLATE_RENDERER
                    }
                    if (filtered.isNotEmpty()) {
                        result.add(part.copy(keywordCommands = filtered))
                    }
                }
                is TextContentPart -> result.add(part)
            }
        }
        return result
    }

    private fun findTemplateRendererCommand(parts: List<TemplateContentPart>): KeywordCommand {
        return parts
            .filterIsInstance<TemplateCommentContentPart>()
            .first { it.isTemplateRendererCommand() }
            .keywordCommands
            .first { it.commandKey == CommandKey.TEMPLATE_RENDERER }
    }

    private fun KeywordCommand.toModelDescriptions(): List<ModelDescription> {
        return attributeGroupIndices().drop(1).map { groupIndex ->
            ModelDescription(
                modelClassDescription = toClassDescription(
                    groupId = groupIndex,
                    classNameAttribute = TEMPLATE_MODEL_CLASS_NAME,
                    packageNameAttribute = TEMPLATE_MODEL_PACKAGE_NAME,
                ),
                modelName = attribute(groupId = groupIndex, key = TEMPLATE_MODEL_NAME),
                isList = attributeOptional(
                    groupId = groupIndex,
                    key = TEMPLATE_MODEL_IS_LIST
                )?.let { it.toEnum<IsListValue>() == IsListValue.YES } ?: false,
            )
        }
    }

    private fun KeywordCommand.toClassDescription(
        groupId: Int = 0,
        classNameAttribute: CommandAttributeKey,
        packageNameAttribute: CommandAttributeKey,
    ): ClassDescription = ClassDescription(
        className = attribute(groupId, classNameAttribute),
        classPackageName = attributeOptional(groupId, packageNameAttribute) ?: DEFAULT_PACKAGE_NAME,
    )

    private fun KeywordCommand.toOptionalClassDescription(
        groupId: Int = 0,
        classNameAttribute: CommandAttributeKey,
        packageNameAttribute: CommandAttributeKey,
    ): ClassDescription? = attributeOptional(groupId, classNameAttribute)?.let { className ->
        ClassDescription(
            className = className,
            classPackageName = attributeOptional(groupId, packageNameAttribute) ?: DEFAULT_PACKAGE_NAME,
        )
    }

    private fun TemplateContentPart.isTemplateRendererCommand(): Boolean =
        this is TemplateCommentContentPart && keywordCommands.any { it.commandKey == CommandKey.TEMPLATE_RENDERER }

    private fun TemplateContentPart.isEndTemplateRendererCommand(): Boolean =
        this is TemplateCommentContentPart && keywordCommands.any { it.commandKey == CommandKey.END_TEMPLATE_RENDERER }
}
