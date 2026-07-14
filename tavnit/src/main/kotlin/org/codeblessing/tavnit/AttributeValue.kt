package org.codeblessing.tavnit

typealias AttributeValue = String  // just for documentation/clarification

interface AllowedValue {
    val value: AttributeValue
}

inline fun <reified T : Enum<T>> AttributeValue.toEnum(): T =
    enumValues<T>().single() { it is AllowedValue && it.value == this }

enum class IsListValue(override val value: AttributeValue): AllowedValue {
    YES("yes"),
    NO("no"),
}
