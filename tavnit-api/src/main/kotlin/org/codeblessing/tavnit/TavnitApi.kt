package org.codeblessing.tavnit

import java.nio.file.Path
import java.util.ServiceLoader

/**
 * The public entry point for running tavnit programmatically.
 *
 * The implementation lives in a separate module (`org.codeblessing.tavnit:tavnit`) and is located
 * at runtime through Java's [ServiceLoader]. Both `tavnit-api` and `tavnit` must therefore be on
 * the classpath; otherwise [runTavnit] fails fast with a hint to add the implementation dependency.
 */
object TavnitApi {

    /**
     * Search the configured locations for annotated template files, generate a Kotlin renderer
     * class for each, write the renderers to disk, and return the generated files.
     *
     * @param templatingConfigurations one or more independent configurations to process.
     * @return a map from each input [TemplatingConfiguration] to the list of renderer files
     *   generated for it (in processing order).
     * @throws TavnitException if a search directory is missing, a template cannot be parsed or
     *   rendered, or two templates would generate the same renderer file.
     * @throws IllegalArgumentException if no implementation of [TavnitProcessorApi] is found on the
     *   classpath (add the `org.codeblessing.tavnit:tavnit` dependency).
     */
    fun runTavnit(templatingConfigurations: List<TemplatingConfiguration>): Map<TemplatingConfiguration, List<Path>> {
        val tavnitApis: ServiceLoader<TavnitProcessorApi> = ServiceLoader.load(TavnitProcessorApi::class.java)

        val tavnitApi = requireNotNull(tavnitApis.firstOrNull()) {
            "Could not find an implementation of the interface '${TavnitProcessorApi::class}'\n" +
                    "Hint: Beside the 'org.codeblessing.tavnit:tavnit-api' dependency, " +
                    "also add 'org.codeblessing.tavnit:tavnit' to your classpath."
        }
        return tavnitApi.processTavnit(templatingConfigurations)
    }
}
