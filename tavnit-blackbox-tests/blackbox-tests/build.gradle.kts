plugins {
    alias(libs.plugins.kotlin.jvm)
    `maven-dependency-repository`
}

val blackboxBaseProject: Project = project(":tavnit-blackbox-tests:example-business-project")
tasks.test {
    useJUnitPlatform()
    systemProperty("blackbox.baseProjectPath", blackboxBaseProject.projectDir.absolutePath)
}
dependencies {
    testImplementation(kotlin("test"))
    testImplementation(project(":tavnit-blackbox-tests:example-business-project"))
    testImplementation("org.junit.jupiter:junit-jupiter-params")
}
