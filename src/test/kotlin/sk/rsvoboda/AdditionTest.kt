package sk.rsvoboda

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import kotlin.random.Random

class AdditionTest {
    fun randomNatural() = Random.nextInt(from = 1, until = 100_000_000)

    @Test
    fun `addition testing` () {
        expectThat(1+1).isEqualTo(2)
    }

    @Test
    fun `zero identity` () {
        repeat(100) {
            val x = randomNatural()
            expectThat(x+0).isEqualTo(x)
        }
    }
}