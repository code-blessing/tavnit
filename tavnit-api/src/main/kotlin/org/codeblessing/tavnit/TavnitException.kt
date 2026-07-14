package org.codeblessing.tavnit

/**
 * Thrown when tavnit cannot process the given configuration or template files, e.g. an invalid
 * configuration, a missing search directory, or a template that cannot be parsed or rendered.
 *
 * This is the exception type that callers of [TavnitApi.runTavnit] are expected to catch.
 */
class TavnitException(message: String, cause: Exception? = null) : RuntimeException(message, cause)
