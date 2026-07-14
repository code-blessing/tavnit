rootProject.name = "tavnit"

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            val kotlinVersion = version("kotlin", "2.2.0")
            val junitVersion = version("junit", "5.10.1")

            library("kotlin-stdlib", "org.jetbrains.kotlin", "kotlin-stdlib").versionRef(kotlinVersion)
            library("junit-bom", "org.junit", "junit-bom").versionRef(junitVersion)
            plugin("kotlin-jvm", "org.jetbrains.kotlin.jvm").versionRef(kotlinVersion)
        }
    }
}


include("tavnit")
include("tavnit-api")
include("tavnit-blackbox-tests:blackbox-tests")
include("tavnit-blackbox-tests:template-renderer-creator")
include("tavnit-blackbox-tests:template-renderer-executor")
include("tavnit-blackbox-tests:example-business-project")
