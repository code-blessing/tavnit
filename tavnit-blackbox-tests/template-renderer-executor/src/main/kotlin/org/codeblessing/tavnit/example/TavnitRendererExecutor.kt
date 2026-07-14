package org.codeblessing.tavnit.example

import org.codeblessing.tavnit.example.SourceType.HTML
import org.codeblessing.tavnit.example.SourceType.KOTLIN
import org.codeblessing.tavnit.example.renderer.ElseIfRenderer
import org.codeblessing.tavnit.example.renderer.ElseRenderer
import org.codeblessing.tavnit.example.renderer.EntityDtoTemplateRenderer
import org.codeblessing.tavnit.example.renderer.ForeachRenderer
import org.codeblessing.tavnit.example.renderer.MoveDefaultRenderer
import org.codeblessing.tavnit.example.renderer.MoveRenderer
import org.codeblessing.tavnit.example.renderer.NestingRenderer
import org.codeblessing.tavnit.example.renderer.NestingReplacesRenderer
import org.codeblessing.tavnit.example.renderer.RenderItemRenderer
import org.codeblessing.tavnit.example.renderer.RenderPageRenderer
import org.codeblessing.tavnit.example.renderer.RendererWithBlackboxDefaultModel
import org.codeblessing.tavnit.example.renderer.EnumRenderer
import org.codeblessing.tavnit.example.renderer.HtmlListPageRenderer
import org.codeblessing.tavnit.example.renderer.IfRenderer
import org.codeblessing.tavnit.example.renderer.NestedElseRenderer
import org.codeblessing.tavnit.example.renderer.NestingTemplateFancyRenderer
import org.codeblessing.tavnit.example.renderer.NestingTemplatePlainRenderer
import org.codeblessing.tavnit.example.renderer.PrintTextRenderer
import org.codeblessing.tavnit.example.renderer.WhitespaceHtmlRenderer
import org.codeblessing.tavnit.example.renderer.model.BlackboxDefaultModel
import org.codeblessing.tavnit.example.renderer.model.DtoEntityRenderModel
import org.codeblessing.tavnit.example.renderer.model.DtoFieldRenderModel
import org.codeblessing.tavnit.example.renderer.model.EnumRenderModel
import org.codeblessing.tavnit.example.renderer.model.HtmlListModel
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText
import kotlin.system.exitProcess

private enum class SourceType {
    KOTLIN,
    HTML,
    ;
}

private data class RendererDeclaration(
    val sourceType: SourceType,
    val renderer: RendererWithBlackboxDefaultModel,
)

private const val PRINT_GENERATED_CONTENT = false
private val ALL_DEFAULT_RENDERERS = listOf(
    RendererDeclaration(sourceType = HTML, renderer = NestingTemplateFancyRenderer),
    RendererDeclaration(sourceType = HTML, renderer = ForeachRenderer),
    RendererDeclaration(sourceType = HTML, renderer = IfRenderer),
    RendererDeclaration(sourceType = HTML, renderer = NestedElseRenderer),
    RendererDeclaration(sourceType = HTML, renderer = ElseIfRenderer),
    RendererDeclaration(sourceType = HTML, renderer = ElseRenderer),
    RendererDeclaration(sourceType = HTML, renderer = MoveDefaultRenderer),
    RendererDeclaration(sourceType = HTML, renderer = MoveRenderer),
    RendererDeclaration(sourceType = HTML, renderer = NestingRenderer),
    RendererDeclaration(sourceType = HTML, renderer = NestingReplacesRenderer),
    RendererDeclaration(sourceType = HTML, renderer = NestingTemplatePlainRenderer),
    RendererDeclaration(sourceType = HTML, renderer = RenderItemRenderer),
    RendererDeclaration(sourceType = HTML, renderer = RenderPageRenderer),
    RendererDeclaration(sourceType = HTML, renderer = WhitespaceHtmlRenderer),
    RendererDeclaration(sourceType = HTML, renderer = PrintTextRenderer),
)

fun main(args: Array<String>) {
    if(args.size != 2) {
        println("Wrong arguments!")
        println("Use <path to generated kotlin files> <path to generated HTML files>")
        exitProcess(1)
    }

    val pathToGeneratedKotlinFiles = Paths.get(args[0])
    val pathToGeneratedHtmlFiles = Paths.get(args[1])

    println("Use template renderer with tavnit")

    fun SourceType.basePathToGeneratedFiles(): Path {
        return when(this) {
            KOTLIN -> pathToGeneratedKotlinFiles
            HTML -> pathToGeneratedHtmlFiles
        }

    }

    // documentation renderer

    val listPageModel = HtmlListModel(
        filenameWithoutPrefix = "today-news",
        pageTitle = "news of today",
        allListEntries = listOf(
            "Tech Startup Raises $200M in Latest Funding Round",
            "Markets Rally as Inflation Cools to Two-Year Low",
            "New AI Tool Promises to Cut Software Development Time in Half",
            "Underdogs Stun Champions in Last-Minute Comeback",
            "Veteran Player Announces Retirement After Record-Breaking Career",
        ),
    )
    val sourceType = HTML
    writeGeneratedContentToFile(
        sourceType = sourceType,
        model = listPageModel,
        content = HtmlListPageRenderer.renderTemplate(listPageModel),
        filepath = sourceType.basePathToGeneratedFiles().resolve("documentation/today-news.html")
    )


    // default renderers

    ALL_DEFAULT_RENDERERS.forEach { defaultRenderer ->
        val blackboxDefaultModel = BlackboxDefaultModel()
        val content = defaultRenderer.renderer.renderTemplate(blackboxDefaultModel)
        val filepath = defaultRenderer.renderer.filePath(blackboxDefaultModel)

        writeGeneratedContentToFile(
            sourceType = defaultRenderer.sourceType,
            model = blackboxDefaultModel,
            content = content,
            filepath = defaultRenderer.sourceType.basePathToGeneratedFiles().resolve(filepath)
        )
    }

    // enum type example renderers

    enumTypes().forEach { enumType ->
        val blackboxDefaultModel = BlackboxDefaultModel()
        val content = EnumRenderer.renderTemplate(enumType)
        val filepath = EnumRenderer.filePath(enumType)

        val sourceType = KOTLIN
        writeGeneratedContentToFile(
            sourceType = sourceType,
            model = blackboxDefaultModel,
            content = content,
            filepath = sourceType.basePathToGeneratedFiles().resolve(filepath)
        )
    }

    // DTO example renderers

    val dtoRenderModels = listOf(
        createDtoEntity("Category"),
        createDtoEntity("Cart"),
        createDtoEntity("User"),
        createDtoEntity("RelatedProduct"),
    )

    dtoRenderModels.forEach { dtoRenderModel ->
        val sourceType = KOTLIN
        val content = EntityDtoTemplateRenderer.renderTemplate(model = dtoRenderModel)
        val filepath = sourceType.basePathToGeneratedFiles()
            .resolve(dtoRenderModel.dtoNestedPackageDirectory)
            .resolve(dtoRenderModel.kotlinDtoFileName)

        writeGeneratedContentToFile(
            sourceType = sourceType,
            model = dtoRenderModel,
            content = content,
            filepath = sourceType.basePathToGeneratedFiles().resolve(filepath)
        )
    }
}

private fun enumTypes(): List<EnumRenderModel> {
    return listOf(
        EnumRenderModel("WorkStatus", listOf("new", "inProgress", "finished")),
        EnumRenderModel("Season", listOf("winter", "spring", "summer", "fall")),
        EnumRenderModel("PaymentStatus", listOf("PENDING", "PAID", "REFUNDED")),
    )
}

private fun <M> writeGeneratedContentToFile(
    sourceType: SourceType,
    model: M,
    content: String,
    filepath: Path
) {
    filepath.parent.createDirectories()
    filepath.writeText(content)

    if(PRINT_GENERATED_CONTENT) {
        println(" ------------------------------------------------------------------------------------------------------------ ")
        println(" $sourceType (Model: ${model}) ")
        println(" $filepath ")
        println(" ------------------------------------------------------------------------------------------------------------ ")
        println(content)
    }
}

private fun createDtoEntity(entityName: String): DtoEntityRenderModel {
    val entityNameDecapitalized = entityName.replaceFirstChar { it.lowercase() }
    return DtoEntityRenderModel(
        entityName = entityName,
        fields = listOf(
            createDtoField("${entityNameDecapitalized}Key"),
            createDtoField("${entityNameDecapitalized}Ean", type = "Int"),
            createDtoField("${entityNameDecapitalized}Text"),
            createDtoField("${entityNameDecapitalized}Description", isNullable = true),
        )
    )
}

private fun createDtoField(fieldName: String, type: String = "String", isNullable: Boolean = false): DtoFieldRenderModel {
    return DtoFieldRenderModel(
        fieldName = fieldName,
        fieldTypeName = type,
        isNullable = isNullable
    )
}
