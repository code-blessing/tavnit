package org.codeblessing.tavnit.application

import org.codeblessing.tavnit.CommentStyle
import org.codeblessing.tavnit.RelativeFile
import org.codeblessing.tavnit.TavnitException
import org.codeblessing.tavnit.TemplatingConfiguration
import org.codeblessing.tavnit.TavnitProcessorApi
import org.codeblessing.tavnit.contentparsing.ContentParser
import org.codeblessing.tavnit.filemapping.ContentMapper
import org.codeblessing.tavnit.filesearch.FileTraversal
import org.codeblessing.tavnit.templaterenderer.TemplateRendererClassContentCreator
import org.codeblessing.tavnit.templaterenderer.TemplateRendererContentCreator
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.createParentDirectories
import kotlin.io.path.readText
import kotlin.io.path.writeText

class TavnitProcessor: TavnitProcessorApi {

    override fun processTavnit(
        templatingConfigurations: List<TemplatingConfiguration>,
    ): Map<TemplatingConfiguration, List<Path>> {
        val templateFileByGeneratedFile = mutableMapOf<Path, RelativeFile>()
        return templatingConfigurations.associateWith { templatingConfiguration ->
            val foundFiles = FileTraversal.searchFiles(templatingConfiguration.fileSearchLocations)
            val targetBasePath = templatingConfiguration.templateRendererConfiguration.templateRendererTargetSourceBasePath

            foundFiles.flatMap { foundFile ->
                val templateRendererClasses = try {
                    parseContentAndCreateTemplateRenderers(
                        filepath = foundFile,
                        targetBasePath = targetBasePath,
                        contentToParse = foundFile.filePath.readText(),
                        supportedCommentStyles = ContentMapper.mapContent(foundFile.filePath)
                    )
                } catch (e: TavnitException) {
                    // the inner message already carries the line number and reason;
                    // fold the file into a single message instead of nesting exceptions
                    throw TavnitException(
                        "Error in template file '${foundFile.filePath.absolutePathString()}': ${e.message}", e
                    )
                } catch (e: Exception) {
                    throw TavnitException(
                        "Error processing template file '${foundFile.filePath.absolutePathString()}': ${e.message}", e
                    )
                }
                templateRendererClasses
                    .onEach { templateRendererClass ->
                        val templateRendererClassFilePath = templateRendererClass.templateRendererClassFilePath
                        failOnDuplicateGeneratedFile(templateFileByGeneratedFile, templateRendererClassFilePath, foundFile)
                        templateRendererClassFilePath.createParentDirectories()
                        templateRendererClassFilePath.writeText(templateRendererClass.templateRendererClassContent)
                    }
                    .map { templateRendererClass -> templateRendererClass.templateRendererClassFilePath }
            }
        }
    }

    private fun failOnDuplicateGeneratedFile(
        templateFileByGeneratedFile: MutableMap<Path, RelativeFile>,
        templateRendererClassFilePath: Path,
        foundFile: RelativeFile,
    ) {
        val generatedFileKey = templateRendererClassFilePath.toAbsolutePath().normalize()
        val alreadyGeneratedFrom = templateFileByGeneratedFile.putIfAbsent(generatedFileKey, foundFile)
        if (alreadyGeneratedFrom != null) {
            throw TavnitException(
                "The template renderer class file '${templateRendererClassFilePath.absolutePathString()}' " +
                "generated from the template file '${foundFile.filePath.absolutePathString()}' " +
                "was already generated from the template file '${alreadyGeneratedFrom.filePath.absolutePathString()}'. " +
                "The combination of template renderer class name and package name must be unique."
            )
        }
    }

    private fun parseContentAndCreateTemplateRenderers(
        filepath: RelativeFile,
        contentToParse: String,
        supportedCommentStyles: List<CommentStyle>,
        targetBasePath: Path
    ): List<TemplateRendererClass> {
        val templates = ContentParser.parseContent(contentToParse, supportedCommentStyles)
        return templates.map { templateRendererDescription ->
            val kotlinTemplateContent = TemplateRendererContentCreator.createMultilineStringTemplateContent(filepath, templateRendererDescription)
            val kotlinTemplateRendererClassContent = TemplateRendererClassContentCreator.wrapInKotlinClassContent(filepath, templateRendererDescription, kotlinTemplateContent)
            val kotlinFilePath = templateRendererDescription.templateRendererClass.classFilePath(targetBasePath)

            TemplateRendererClass(
                templateRendererDescription = templateRendererDescription,
                templateRendererClassContent = kotlinTemplateRendererClassContent,
                templateRendererClassFilePath = kotlinFilePath,
            )
        }
    }
}
