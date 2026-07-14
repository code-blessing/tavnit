plugins {
    alias(libs.plugins.kotlin.jvm)
    `tavnit-publishing`
    `maven-dependency-repository`
}

tasks.test {
    useJUnitPlatform()
}
dependencies {
    implementation(project(":tavnit-api"))

    testImplementation(kotlin("test"))
    testImplementation(platform(libs.junit.bom))
    testImplementation("org.junit.jupiter:junit-jupiter-params")
}

tasks.named("build") {
    dependsOn("generateDocumentation")
}

tasks.register("generateDocumentation") {
    dependsOn("generateCommandReferenceDocumentation")
    dependsOn("generateMainFunctionUsageDocumentation")
    dependsOn("generateSupportedFileFormatsDocumentation")
}

tasks.register<JavaExec>("generateMainFunctionUsageDocumentation") {
    dependsOn("classes")
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("org.codeblessing.tavnit.documentation.MainFunctionMarkdownCreatorMainKt")

    val mainFunctionUsageMdFile: File = rootProject.file("documentation/MAIN-FUNCTION-USAGE.md")
    outputs.file(mainFunctionUsageMdFile)

    doFirst {
        standardOutput = mainFunctionUsageMdFile.outputStream()
    }
}

tasks.register<JavaExec>("generateCommandReferenceDocumentation") {
    dependsOn("classes")
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("org.codeblessing.tavnit.documentation.CommandReferenceMarkdownCreatorMainKt")

    val commandReferenceMdFile: File = rootProject.file("documentation/COMMAND-REFERENCE.md")
    outputs.file(commandReferenceMdFile)

    doFirst {
        standardOutput = commandReferenceMdFile.outputStream()
    }
}

tasks.register<JavaExec>("generateSupportedFileFormatsDocumentation") {
    dependsOn("classes")
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("org.codeblessing.tavnit.documentation.SupportedFileFormatsMarkdownCreatorMainKt")

    val supportedFileFormatsMdFile: File = rootProject.file("documentation/SUPPORTED-FILE-FORMATS.md")
    outputs.file(supportedFileFormatsMdFile)

    doFirst {
        standardOutput = supportedFileFormatsMdFile.outputStream()
    }
}

//
// Publishing
//
extensions.getByType<PublishingExtension>().publications {
    getByName<MavenPublication>("mavenTavnit") {
        artifactId = "tavnit"
        pom {
            name.set("Tavnit Implementation")
            description.set("The implementation of the tavnit-api.")
        }
    }
}

