plugins {
    alias(libs.plugins.kotlin.jvm)
    `tavnit-publishing`
    `maven-dependency-repository`
}

tasks.jar {
    val tavnitVersion = project.property("tavnit.version") as String
    manifest {
        attributes("Main-Class" to "org.codeblessing.tavnit.TavnitKt")
        attributes("Implementation-Version" to tavnitVersion)
    }
}

tasks.test {
    useJUnitPlatform()
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation(platform(libs.junit.bom))
    testImplementation("org.junit.jupiter:junit-jupiter-params")
}

//
// Publishing
//
extensions.getByType<PublishingExtension>().publications {
    getByName<MavenPublication>("mavenTavnit") {
        artifactId = "tavnit-api"
        pom {
            name.set("Tavnit API")
            description.set("The API for the tavnit project.")
        }
    }
}
