package org.codeblessing.tavnit.contentparsing.commandchain

import java.nio.file.Path
import java.nio.file.Paths

data class ClassDescription(
    val className: String,
    val classPackageName: String,
) {
    val fullQualifiedName: String
        get() {
            if(classPackageName.isBlank()) return className
            return "$classPackageName.$className"
        }

    val classPackagePath: Path
        get() = Paths.get(classPackageName.replace(".", "/"))

    val classFilename: String
        get() = "${className}.kt"

    fun classFilePath(basePath: Path): Path {
        return basePath.resolve(classPackagePath).resolve(classFilename)
    }

}
