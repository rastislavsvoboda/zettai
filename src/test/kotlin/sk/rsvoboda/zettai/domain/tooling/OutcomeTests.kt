package sk.rsvoboda.zettai.domain.tooling

import org.junit.jupiter.api.fail
import sk.rsvoboda.zettai.fp.Outcome
import sk.rsvoboda.zettai.fp.OutcomeError
import sk.rsvoboda.zettai.fp.onFailure

fun <E : OutcomeError, T> Outcome<E, T>.expectSuccess(): T =
    onFailure { error -> fail { "$this expected success but was $error" } }

fun <E : OutcomeError, T> Outcome<E, T>.expectFailure(): E =
    onFailure { error -> return error }
        .let { fail { "Expected failure but was $it" } }