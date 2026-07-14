package org.codeblessing.tavnit.templaterenderer

import org.codeblessing.tavnit.ClasspathResourceLoader
import org.codeblessing.tavnit.RelativeFile
import org.codeblessing.tavnit.contentparsing.commandchain.ClassDescription
import org.codeblessing.tavnit.contentparsing.commandchain.ModelDescription
import org.codeblessing.tavnit.contentparsing.commandchain.TemplateRendererDescription
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TemplateRendererClassContentCreatorTest {

    @Test
    fun `wrap template content into kotlin template object class content`() {
        val templateRendererDescription = TemplateRendererDescription(
            templateRendererClass = ClassDescription(
                className = "TemplateTest",
                classPackageName = "org.codeblessing.tavnit.template",
            ),
            templateRendererInterface = null,
            modelClasses = listOf(
                ModelDescription(
                    modelClassDescription = ClassDescription(
                        className = "TemplateModel",
                        classPackageName = "org.codeblessing.tavnit.template.model",
                    ),
                    modelName = "model",
                    isList = false,
                ),
                ModelDescription(
                    modelClassDescription = ClassDescription(
                        className = "TemplateModel",
                        classPackageName = "org.codeblessing.tavnit.template.model",
                    ),
                    modelName = "models",
                    isList = true,
                )
            ),
            templateChain = emptyList()
        )
        val kotlinTemplateRendererMethodContent = KotlinTemplateRendererMethodContent(
            rendererCode = "| hello world",
            filepath = "dum-dir/dum-sub-dir/dummy.txt",
        )
        val kotlinClassContent = TemplateRendererClassContentCreator.wrapInKotlinClassContent(
            RelativeFile.fromRelativeString("dum-dir/dum-sub-dir/dummy.txt"),
            templateRendererDescription,
            kotlinTemplateRendererMethodContent,
        )

        val expectedContent = ClasspathResourceLoader.loadClasspathResource(
            classpathResourcePath = "org/codeblessing/tavnit/templaterenderer/TemplateRendererClassContentCreatorTest-expected-content.txt"
        )
        assertEquals(expectedContent, kotlinClassContent)
    }

}
