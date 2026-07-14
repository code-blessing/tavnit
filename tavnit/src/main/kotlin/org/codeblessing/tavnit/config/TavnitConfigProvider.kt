package org.codeblessing.tavnit.config

object TavnitConfigProvider {

    private val loadedConfiguration: TavnitConfig by lazy { TavnitConfigReader.readConfiguration() }

    fun getConfiguration(): TavnitConfig = loadedConfiguration
}
