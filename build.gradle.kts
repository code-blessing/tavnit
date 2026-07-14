tasks.register("build") {
    dependsOn("generateDocumentation")
}

tasks.register("generateDocumentation") {
    dependsOn("updateVersionString")
}

tasks.register("updateVersionString") {
    // Documentation files that embed the dependency coordinates and must track the current version.
    val versionedDocFiles: List<File> = listOf(
        rootProject.file("README.md"),
        rootProject.file("QUICKSTART.md"),
    )
    val newVersion = project.property("tavnit.version") as String

    doLast {
        versionedDocFiles.forEach { replaceVersionString(it, newVersion) }
    }

    inputs.property("version", newVersion)
    inputs.files(versionedDocFiles)
    outputs.files(versionedDocFiles)
}

fun replaceVersionString(file: File, newVersion: String) {
    val content = file.readText()
    val newContent = content
        .replace(Regex("tavnit-api:\\d+\\.\\d+\\.\\d+"), "tavnit-api:$newVersion")
        .replace(Regex("tavnit:\\d+\\.\\d+\\.\\d+"), "tavnit:$newVersion")
        // Maven coordinates: <artifactId>tavnit(-api)</artifactId> ... <version>x.y.z</version>
        .replace(Regex("(<artifactId>tavnit(?:-api)?</artifactId>\\s*<version>)\\d+\\.\\d+\\.\\d+")) {
            "${it.groupValues[1]}$newVersion"
        }

    file.writeText(newContent)
}
