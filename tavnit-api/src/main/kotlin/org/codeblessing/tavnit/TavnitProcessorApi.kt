package org.codeblessing.tavnit

import java.nio.file.Path

/**
 * Service-provider interface implemented by the tavnit implementation module and located at runtime
 * via [java.util.ServiceLoader].
 *
 * Application code should not call this directly; use [TavnitApi.runTavnit] instead, which resolves
 * the implementation and adds a helpful error when it is missing from the classpath.
 */
interface TavnitProcessorApi {

    /**
     * Process the given [templatingConfigurations] and return the renderer files generated for each.
     * See [TavnitApi.runTavnit] for the full contract.
     */
    fun processTavnit(templatingConfigurations: List<TemplatingConfiguration>): Map<TemplatingConfiguration, List<Path>>
}
