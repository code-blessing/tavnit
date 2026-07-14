package org.codeblessing.tavnit.documentation

import org.codeblessing.tavnit.MAIN_FUNCTION_USAGE

object MainFunctionUsageMarkdownCreator {

    fun createMarkdownDocumentation(): String {
        val sb = StringBuilder()
        sb.appendLine("""
# Main function usage

Call the main function ```org.codeblessing.tavnit.TavnitKt``` with the following parameter:

```
$MAIN_FUNCTION_USAGE
```
""".trimIndent())
        return sb.toString()
    }

}
