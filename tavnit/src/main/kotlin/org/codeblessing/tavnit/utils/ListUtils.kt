package org.codeblessing.tavnit.utils

fun <T> List<T>.subListUntilElement(element: T): List<T> {
    checkElementConstraint(element)
    val index = this.indexOfFirst { it == element }
    return this.subList(0, index)
}

fun <T> List<T>.subListStartingAfterElement(element: T): List<T> {
    checkElementConstraint(element)
    val index = this.indexOfFirst { it == element }
    return this.subList(index + 1, size)
}

private fun <T> List<T>.checkElementConstraint(element: T) {
    val count = this.count { it == element }
    if(count == 0) {
        throw NoSuchElementException()
    }
    if(count > 1) {
        throw IllegalStateException()
    }
}


