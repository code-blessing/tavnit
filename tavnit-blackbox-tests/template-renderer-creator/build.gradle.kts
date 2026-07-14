plugins {
    alias(libs.plugins.kotlin.jvm)
    `maven-dependency-repository`
}

dependencies {
    implementation(project(":tavnit-api"))
    // The implementation module is put additionally on the task classpath as a packaged jar (see
    // createTavnitRenderers below), not as a project dependency. Gradle (9.x) resolves
    // project dependencies on the runtime classpath via the loose classes/resources directory
    // variants, whose META-INF/services output can go stale and make the ServiceLoader lookup fail.
    runtimeOnly(project(":tavnit"))
}

tasks.register<JavaExec>("createTavnitRenderers") {
    val exampleBusinessProjectPath = project(":tavnit-blackbox-tests:example-business-project").projectDir
    val templateRendererExecutorPath = project(":tavnit-blackbox-tests:template-renderer-executor").projectDir
    val targetDirectoryForTemplateRenderer = templateRendererExecutorPath.resolve("src/tavnit-generated/kotlin")

    // Consume the packaged jar of the implementation module so its ServiceLoader registration
    // (META-INF/services/...TavnitProcessorApi) always travels atomically with the
    // classes. This avoids the intermittent "Could not find an implementation of
    // TavnitProcessorApi" failure caused by a stale loose resources directory.
    val implementationJar = project(":tavnit").tasks.named<Jar>("jar")
    dependsOn(implementationJar)

    classpath = sourceSets.main.get().runtimeClasspath + files(implementationJar)
    mainClass.set("org.codeblessing.tavnit.TavnitKt")

    val pathToKotlinSourceFiles = exampleBusinessProjectPath.resolve("src/main/kotlin")
    val pathToHtmlSourceFiles = exampleBusinessProjectPath.resolve("src/main/resources/html")
    args(
        "--template-renderer",
        targetDirectoryForTemplateRenderer.absolutePath,  // Path to the base directory where the template renderers are written to
        "--search",
        "${pathToKotlinSourceFiles.absolutePath}:*.kt",
        "--search",
        "${pathToHtmlSourceFiles.absolutePath}:*.html",
    )

    inputs.dir(pathToKotlinSourceFiles)
    inputs.dir(pathToHtmlSourceFiles)
    outputs.dir(targetDirectoryForTemplateRenderer)
}
