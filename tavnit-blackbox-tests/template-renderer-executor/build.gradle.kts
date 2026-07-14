plugins {
    alias(libs.plugins.kotlin.jvm)
    `maven-dependency-repository`
}

val directoryForGeneratedTemplateRenderer = "src/tavnit-generated/kotlin"
val directoryForTemplateRendererGeneratedKotlinSource = "src/generated/kotlin"
val directoryForTemplateRendererGeneratedHtmlSource = "src/generated/resources/html"
val taskNameCreateTavnitRenderers = ":tavnit-blackbox-tests:template-renderer-creator:createTavnitRenderers"

val exampleBusinessProjectPath: File = project(":tavnit-blackbox-tests:example-business-project").projectDir
kotlin {
    sourceSets["main"].kotlin.srcDir(directoryForGeneratedTemplateRenderer)
}

tasks.register<JavaExec>("executeTavnitRenderers") {
    dependsOn(taskNameCreateTavnitRenderers)
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("org.codeblessing.tavnit.example.TavnitRendererExecutorKt")

    val generatedKotlinSourceDir = exampleBusinessProjectPath.resolve(directoryForTemplateRendererGeneratedKotlinSource)
    val generatedHtmlSourceDir = exampleBusinessProjectPath.resolve(directoryForTemplateRendererGeneratedHtmlSource)


    args(
        generatedKotlinSourceDir,
        generatedHtmlSourceDir
    )

    outputs.dir(generatedKotlinSourceDir)
    outputs.dir(generatedHtmlSourceDir)
}

tasks.compileKotlin {
    dependsOn(taskNameCreateTavnitRenderers)
}

tasks.register("example") { // easier to remember and shorter to type
    dependsOn("executeTavnitRenderers")
}

