plugins {
    alias(libs.plugins.kotlin.jvm)
    `maven-dependency-repository`
}

val directoryForTemplateRendererGeneratedKotlinSource = "src/generated/kotlin"
val directoryForTemplateRendererGeneratedHtmlSource = "src/generated/resources/html"

kotlin {
    sourceSets["main"].kotlin.srcDir(directoryForTemplateRendererGeneratedKotlinSource)
    sourceSets["main"].resources.srcDir(directoryForTemplateRendererGeneratedHtmlSource)
}

tasks.register<Delete>("cleanGeneratedSource") {
    delete(file(directoryForTemplateRendererGeneratedKotlinSource))
    delete(file(directoryForTemplateRendererGeneratedHtmlSource))
}

tasks.clean {
    dependsOn("cleanGeneratedSource")
}

val taskNameExecuteTavnitRenderers = ":tavnit-blackbox-tests:template-renderer-executor:executeTavnitRenderers"
tasks.compileKotlin {
    dependsOn(taskNameExecuteTavnitRenderers)
}

tasks.processResources {
    dependsOn(taskNameExecuteTavnitRenderers)
}
