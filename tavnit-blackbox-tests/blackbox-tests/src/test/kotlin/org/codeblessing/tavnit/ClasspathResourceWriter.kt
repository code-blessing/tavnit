package org.codeblessing.tavnit

import java.nio.file.Paths
import kotlin.io.path.absolutePathString
import kotlin.io.path.createFile
import kotlin.io.path.exists
import kotlin.io.path.writeText

object ClasspathResourceWriter {
    private const val RESOURCE_DIRECTORY = "src/test/resources/"

    fun writeClasspathResource(classpathResourcePath: String, text: String, createFileIfNotExists: Boolean = false) {
        val classpathResourceFile = Paths.get(RESOURCE_DIRECTORY + classpathResourcePath)
        if(!classpathResourceFile.exists()) {
            if(createFileIfNotExists) {
                classpathResourceFile.createFile()
            } else {
                throw RuntimeException("Classpath resource file does not exist. Create an empty file. \n${classpathResourceFile.absolutePathString()}")
            }
        }
        classpathResourceFile.writeText(text)
    }
}
