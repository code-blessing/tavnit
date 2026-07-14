package org.codeblessing.tavnit

object ClasspathResourceLoader {

    fun loadClasspathResource(classpathResourcePath: String, suffixToRemove: String = "\n"): String {
        this.javaClass.classLoader.getResourceAsStream(classpathResourcePath).use { inputStream ->
            return requireNotNull(inputStream){
              "Could not load classpath resource: $classpathResourcePath"
            }.bufferedReader().readText().removeSuffix(suffixToRemove)
        }
    }
}
