package org.codeblessing.tavnit.blackboxtests

import org.junit.jupiter.api.Test

class KotlinDtoBlackboxTest : AbstractBlackboxTest() {

    @Test
    fun `test output of kotlin enum template commands`() {
        assertSameContent(
            kotlinSourcePath().resolve("my/example/businessproject/dto/ProductDto.kt"),
            kotlinGeneratedPath().resolve("my/example/businessproject/dto/CategoryDto.kt"),
            "CategoryDto.expectation.kt",
        )
    }
}
