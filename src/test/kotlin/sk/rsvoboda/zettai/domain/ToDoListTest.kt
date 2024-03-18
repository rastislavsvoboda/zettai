package sk.rsvoboda.zettai.domain

import org.junit.jupiter.api.Test
import sk.rsvoboda.zettai.domain.tooling.*
import strikt.api.expectThat
import strikt.assertions.isEqualTo


class ToDoListTest {
    val validCharset = uppercase + lowercase + digits + "-"
    val invalidCharset = " !@#$%^&*()+={}[]|:;'<>,./?\u2202\u2203\u2204\u2205"

    @Test
    fun `Valid names are alphanum+hiphen between 3 and 40 chars lenght`() {
        stringsGenerator(validCharset, 3, 40)
            .take(100)
            .forEach {
                expectThat(ListName.fromUntrusted(it))
                    .isEqualTo(ListName.fromTrusted(it))
            }
    }

    @Test
    fun `Name cannot be empty`() {
        expectThat(ListName.fromUntrusted("")).isEqualTo(null)
    }

    @Test
    fun `Names longer than 40 chars are not valid`() {
        stringsGenerator(validCharset, 41, 200)
            .take(100)
            .forEach {
                expectThat(ListName.fromUntrusted(it))
                    .isEqualTo(null)
            }
    }

    @Test
    fun `Invalid chars are not allowed in the name`() {
        stringsGenerator(validCharset, 1, 30)
            .map { substituteRandomChar(invalidCharset, it) }
            .take(1000)
            .forEach {
                expectThat(ListName.fromUntrusted(it))
                    .isEqualTo(null)
            }
    }
}