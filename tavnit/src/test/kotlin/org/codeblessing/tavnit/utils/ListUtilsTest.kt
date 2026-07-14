package org.codeblessing.tavnit.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ListUtilsTest {

    private val listToSearchIn = listOf(
        "one",
        "two",
        "three",
    )

    @Nested
    inner class SubListUntilElement {

        @Test
        fun `create sublist with subListUntilBeforeElement with element found in the middle of the list`() {
            assertEquals(listOf("one"), listToSearchIn.subListUntilElement("two"))
        }

        @Test
        fun `create sublist with subListUntilBeforeElement with element found as first element of the list`() {
            assertEquals(emptyList<String>(), listToSearchIn.subListUntilElement("one"))
        }

        @Test
        fun `create sublist with subListUntilBeforeElement with element found as last element of the list`() {
            assertEquals(listOf("one", "two"), listToSearchIn.subListUntilElement("three"))
        }

        @Test
        fun `create sublist with subListUntilBeforeElement with element found as first element of a one-element list`() {
            assertEquals(emptyList<String>(), listOf("one").subListUntilElement("one"))
        }

        @Test
        fun `create sublist with subListUntilBeforeElement with element not found in list`() {
            assertThrows<NoSuchElementException> {
                listToSearchIn.subListUntilElement("not-found")
            }
        }

        @Test
        fun `create sublist with subListUntilBeforeElement with empty list throws exception`() {
            val list = emptyList<String>()
            assertThrows<NoSuchElementException> {
                list.subListUntilElement("not-found")
            }
        }

        @Test
        fun `create sublist with subListUntilBeforeElement with duplicate element throws exception`() {
            assertThrows<IllegalStateException> {
                listOf("one", "two", "one").subListUntilElement("one")
            }
        }
    }

    @Nested
    inner class SubListStartingAfterElement {

        @Test
        fun `create sublist with subListStartingAfterElement with element found in the middle of the list`() {
            assertEquals(listOf("three"), listToSearchIn.subListStartingAfterElement("two"))
        }

        @Test
        fun `create sublist with subListStartingAfterElement with element found as first element of the list`() {
            assertEquals(listOf("two", "three"), listToSearchIn.subListStartingAfterElement("one"))
        }

        @Test
        fun `create sublist with subListStartingAfterElement with element found as last element of the list`() {
            assertEquals(emptyList<String>(), listToSearchIn.subListStartingAfterElement("three"))
        }

        @Test
        fun `create sublist with subListStartingAfterElement with element found as first element of a one-element list`() {
            assertEquals(emptyList<String>(), listOf("one").subListStartingAfterElement("one"))
        }

        @Test
        fun `create sublist with subListStartingAfterElement with element not found in list`() {
            assertThrows<NoSuchElementException> {
                listToSearchIn.subListStartingAfterElement("not-found")
            }
        }

        @Test
        fun `create sublist with subListStartingAfterElement with empty list throws exception`() {
            val list = emptyList<String>()
            assertThrows<NoSuchElementException> {
                list.subListStartingAfterElement("not-found")
            }
        }

        @Test
        fun `create sublist with subListStartingAfterElement with duplicate element throws exception`() {
            assertThrows<IllegalStateException> {
                listOf("one", "two", "one").subListStartingAfterElement("one")
            }
        }
    }

}
