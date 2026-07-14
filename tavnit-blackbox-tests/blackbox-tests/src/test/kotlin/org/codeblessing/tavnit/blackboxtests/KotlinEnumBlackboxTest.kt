package org.codeblessing.tavnit.blackboxtests

import org.junit.jupiter.api.Test

class KotlinEnumBlackboxTest : AbstractBlackboxTest() {

    @Test
    fun `test output of kotlin enum template commands`() {
        assertSameContent(
            kotlinSourcePath().resolve("my/example/businessproject/domain/OrderStatusEnum.kt"),
            kotlinGeneratedPath().resolve("my/example/businessproject/domain/PaymentStatusEnum.kt"),
            "paymentstatus-enum.expectation.kt",
        )
    }
}
