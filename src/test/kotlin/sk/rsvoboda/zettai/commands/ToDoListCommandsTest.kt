package sk.rsvoboda.zettai.commands

import org.junit.jupiter.api.Test
import sk.rsvoboda.zettai.domain.*
import sk.rsvoboda.zettai.events.*
import sk.rsvoboda.zettai.domain.tooling.expectSuccess
import sk.rsvoboda.zettai.domain.tooling.expectFailure
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.single

internal class ToDoListCommandsTest {

    val streamer = ToDoListEventStreamerInMemory()
    val eventStore = ToDoListEventStore(streamer)

    val handler = ToDoListCommandHandler(eventStore)

    val user = randomUser()
    val name = randomListName()

    @Test
    fun `Add list fails if the user has already a list with same name`() {
        val cmd = CreateToDoList(user, name)
        val res = handler(cmd).expectSuccess()

        expectThat(res).single().isA<ListCreated>()
        eventStore(res)

        val duplicatedRes = handler(cmd).expectFailure()
        expectThat(duplicatedRes).isA<InconsistentStateError>()
    }

    @Test
    fun `Add items fails if the list doesn't exists`() {
        val cmd = AddToDoItem(user, name, randomItem())

        val res = handler(cmd).expectFailure()
        expectThat(res).isA<InconsistentStateError>()
    }
}