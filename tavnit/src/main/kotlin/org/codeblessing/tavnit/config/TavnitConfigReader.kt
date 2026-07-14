package org.codeblessing.tavnit.config

import org.codeblessing.tavnit.CommentStyle
import org.codeblessing.tavnit.CommentType
import org.codeblessing.tavnit.TavnitException
import java.util.Properties

object TavnitConfigReader {

    private val classLoader = TavnitConfig::class.java.classLoader
    private const val CONFIG_RESOURCE = "tavnit-config.properties"
    private const val CONFIG_OVERWRITE_RESOURCE = "tavnit-config-overwrite.properties"
    private const val COMMENT_STYLE_PREFIX = "commentStyle."
    private const val EXTENSION_PREFIX = "extension."
    private const val START_SUFFIX = ".startOfComment"
    private const val END_SUFFIX = ".endOfComment"
    private const val TYPE_SUFFIX = ".commentType"

    fun readConfiguration(): TavnitConfig = readConfiguration(loadAllProperties())

    internal fun readConfiguration(properties: Properties): TavnitConfig {
        val namedCommentStyles = parseCommentStyles(properties)
        val fileExtensionCommentStyles = parseFileExtensionMappings(properties, namedCommentStyles)
        return TavnitConfig(
            namedCommentStyles = namedCommentStyles,
            fileExtensionCommentStyles = fileExtensionCommentStyles,
        )
    }

    private fun loadAllProperties(): Properties {
        val defaultProperties = loadProperties(CONFIG_RESOURCE, failOnError = true)
        val overwriteProperties = loadProperties(CONFIG_OVERWRITE_RESOURCE, failOnError = false)
        val properties = Properties()
        properties.putAll(defaultProperties)
        properties.putAll(overwriteProperties)
        return properties
    }

    private fun loadProperties(classpathResource: String, failOnError: Boolean): Properties {
        val properties = Properties()
        val stream = classLoader.getResourceAsStream(classpathResource)
            ?: if (failOnError) {
                throw TavnitException("Resource not found on classpath: $classpathResource")
            } else {
                return properties
            }

        stream.use { properties.load(it) }
        return properties
    }

    private fun parseCommentStyles(properties: Properties): Map<String, CommentStyle> {
        val identifiers = properties.stringPropertyNames()
            .filter { it.startsWith(COMMENT_STYLE_PREFIX) }
            .mapNotNull { key ->
                val withoutPrefix = key.removePrefix(COMMENT_STYLE_PREFIX)
                val dotIndex = withoutPrefix.indexOf('.')
                if (dotIndex < 0) null else withoutPrefix.substring(0, dotIndex)
            }
            .toSet()

        return identifiers.associateWith { id ->
            val start = properties.getProperty("$COMMENT_STYLE_PREFIX$id$START_SUFFIX")
                ?: throw TavnitException("Missing '$START_SUFFIX' for comment style '$id' in the tavnit configuration.")
            val end = properties.getProperty("$COMMENT_STYLE_PREFIX$id$END_SUFFIX")
                ?.takeIf { it.isNotEmpty() }
            val commentTypeValue = properties.getProperty("$COMMENT_STYLE_PREFIX$id$TYPE_SUFFIX")
                ?: throw TavnitException("Missing '$TYPE_SUFFIX' for comment style '$id' in the tavnit configuration.")
            val commentType = try {
                CommentType.valueOf(commentTypeValue)
            } catch (e: IllegalArgumentException) {
                throw TavnitException(
                    "Invalid '$TYPE_SUFFIX' value '$commentTypeValue' for comment style '$id' in the tavnit " +
                    "configuration. Allowed values are: ${CommentType.entries.joinToString(", ") { it.name }}.",
                    e,
                )
            }
            if (commentType == CommentType.BLOCK_COMMENT && end == null) {
                throw TavnitException(
                    "The comment style '$id' is a ${CommentType.BLOCK_COMMENT} but has no non-empty " +
                    "'$END_SUFFIX' in the tavnit configuration. A block comment requires an end delimiter."
                )
            }
            CommentStyle(startOfComment = start, endOfComment = end, commentType = commentType)
        }
    }

    private fun parseFileExtensionMappings(
        properties: Properties,
        namedCommentStyles: Map<String, CommentStyle>,
    ): List<FileExtensionCommentStyles> {
        return properties.stringPropertyNames()
            .filter { it.startsWith(EXTENSION_PREFIX) }
            .map { key ->
                val extension = key.removePrefix(EXTENSION_PREFIX)
                val identifiers = properties.getProperty(key).split(",").map { it.trim() }
                val commentStyles = identifiers.map { id ->
                    namedCommentStyles[id]
                        ?: throw TavnitException(
                            "Unknown comment style identifier '$id' for extension '$extension' in the tavnit configuration."
                        )
                }
                FileExtensionCommentStyles(extension = extension, commentStyles = commentStyles)
            }
    }
}
