package sk.rsvoboda.zettai.commands

import org.junit.jupiter.api.Test
import sk.rsvoboda.zettai.domain.*
import sk.rsvoboda.zettai.events.*
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import strikt.assertions.isNull

internal class ToDoListCommandsTest {

    val noopFetcher = object : ToDoListUpdatableFetcher {
        override fun assignListToUser(user: User, list: ToDoList): ToDoList? = null
        override fun get(user: User, listName: ListName): ToDoList? = TODO("Not yet implemented")
        override fun getAll(user: User): List<ListName>? = TODO("Not yet implemented")
    }

    val streamer = ToDoListEventStreamerInMemory()
    val eventStore = ToDoListEventStore(streamer)

    val handler = ToDoListCommandHandler(eventStore, noopFetcher)
    fun handle(cmd: ToDoListCommand): List<ToDoListEvent>? =
        handler(cmd)?.let(eventStore)

    val user = randomUser()
    val name = randomListName()

    @Test
    fun `CreateToDoList generate the correct event`() {
        val cmd = CreateToDoList(randomUser(), randomListName())
        val entityRetriever: ToDoListRetriever = object : ToDoListRetriever {
            override fun retrieveByName(user: User, listName: ListName) = InitialState
        }

        val handler = ToDoListCommandHandler(entityRetriever, noopFetcher)
        val res = handler(cmd)?.single()

        expectThat(res).isEqualTo(
            ListCreated(cmd.id, cmd.user, cmd.name)
        )
    }

    @Test
    fun `Add list fails if the user has already a list with same name`() {
        val cmd = CreateToDoList(user, name)
        val res = handle(cmd)?.single()

        expectThat(res).isA<ListCreated>()

        val duplicatedRes = handle(cmd)
        expectThat(duplicatedRes).isNull()
    }
}