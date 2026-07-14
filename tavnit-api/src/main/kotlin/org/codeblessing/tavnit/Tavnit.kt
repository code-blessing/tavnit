package org.codeblessing.tavnit

import java.nio.file.Paths
import kotlin.system.exitProcess

val MAIN_FUNCTION_USAGE = """
Usage: <tavnit> --template-renderer <path> --search <path>:<pattern> [--search <path>:<pattern> ...]

Options:
  --template-renderer <path>   Target base directory for generated renderer classes (required)
  --search <path>:<pattern>    Source directory and filename glob to search, e.g. ./src:*.kt (required, repeatable)
  --help                       Show this help message

Note:
  On the command line the <pattern> is a glob (e.g. *.kt, *.html), not a regular expression.
  The programmatic API (FileSearchLocation.filenameMatchingPattern) instead takes a Regex.

Examples:
  <tavnit> --template-renderer ./src/generated --search ./src/main/kotlin:*.kt
  <tavnit> --template-renderer ./src/generated --search ./src/main/kotlin:*.kt --search ./src/main/resources:*.html

Where <tavnit> is:
    java -cp ./tavnit-api.jar:./tavnit.jar:${'$'}KOTLIN_HOME/lib/kotlin-stdlib.jar org.codeblessing.tavnit.TavnitKt
or
    kotlin -classpath ./tavnit-api.jar:./tavnit.jar org.codeblessing.tavnit.TavnitKt
""".trimIndent()

/**
 * Command-line entry point for tavnit (main class `org.codeblessing.tavnit.TavnitKt`).
 *
 * Expects `--template-renderer <path>` and one or more repeatable `--search <path>:<pattern>`
 * arguments (the pattern is a filename glob such as `*.kt`), runs tavnit, and prints each generated
 * renderer file path to standard out. Prints usage for `--help` or when called without arguments.
 * Exits with status 1 on invalid arguments or a processing failure ([TavnitException]).
 *
 * See `documentation/MAIN-FUNCTION-USAGE.md` for the full usage description.
 */
fun main(args: Array<String>) {
    if (args.isEmpty() || args.contains("--help")) {
        println(MAIN_FUNCTION_USAGE)
        return
    }

    val configuration = try {
        parseConfiguration(args)
    } catch (e: IllegalArgumentException) {
        System.err.println("tavnit: ${e.message}")
        System.err.println("Run with --help for usage.")
        exitProcess(1)
    }

    try {
        val results = TavnitApi.runTavnit(listOf(configuration))
        results.values.flatten().forEach { println(it) }
    } catch (e: TavnitException) {
        System.err.println("tavnit: ${e.message}")
        exitProcess(1)
    }
}

internal fun parseConfiguration(args: Array<String>): TemplatingConfiguration {
    val templateRenderPath = parseFlag(args, "--template-renderer")
        ?: throw IllegalArgumentException("Missing required argument: --template-renderer <path>")

    val searchValues = parseRepeatingFlag(args, "--search")
    require(searchValues.isNotEmpty()) { "Missing required argument: --search <path>:<pattern>" }

    val fileSearchLocations = searchValues.map { value ->
        val colonIndex = value.lastIndexOf(':')
        require(colonIndex > 0) { "Invalid --search value '$value': expected <path>:<pattern>" }
        val searchPath = value.substring(0, colonIndex)
        val globPattern = value.substring(colonIndex + 1)
        FileSearchLocation(
            rootDirectoryToSearch = Paths.get(searchPath),
            filenameMatchingPattern = globToRegex(globPattern),
        )
    }

    return TemplatingConfiguration(
        fileSearchLocations = fileSearchLocations,
        templateRendererConfiguration = TemplateRendererConfiguration(
            templateRendererTargetSourceBasePath = Paths.get(templateRenderPath),
        ),
    )
}

private fun parseFlag(args: Array<String>, flag: String): String? {
    val index = args.indexOf(flag)
    return if (index >= 0 && index + 1 < args.size) args[index + 1] else null
}

private fun parseRepeatingFlag(args: Array<String>, flag: String): List<String> {
    val result = mutableListOf<String>()
    var i = 0
    while (i < args.size) {
        if (args[i] == flag && i + 1 < args.size) {
            result += args[i + 1]
            i += 2
        } else {
            i++
        }
    }
    return result
}

private fun globToRegex(glob: String): Regex {
    val regex = buildString {
        for (ch in glob) {
            when (ch) {
                '*' -> append(".*")
                '.' -> append("\\.")
                '?' -> append(".")
                else -> append(Regex.escape(ch.toString()))
            }
        }
        append("$")
    }
    return Regex(regex)
}
