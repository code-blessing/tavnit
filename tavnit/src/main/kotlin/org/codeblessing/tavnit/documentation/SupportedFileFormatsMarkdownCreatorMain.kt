package org.codeblessing.tavnit.documentation

fun main() {
    // Write the bytes as UTF-8 explicitly: some comment styles (e.g. the APL lamp ⍝) are non-ASCII
    // and would otherwise be lost depending on the platform default charset of stdout.
    System.out.write(SupportedFileFormatsMarkdownCreator.createMarkdownDocumentation().toByteArray(Charsets.UTF_8))
    System.out.flush()
}
