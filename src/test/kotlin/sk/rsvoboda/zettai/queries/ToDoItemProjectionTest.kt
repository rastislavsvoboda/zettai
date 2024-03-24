package sk.rsvoboda.zettai.queries

import org.junit.jupiter.api.Test
import sk.rsvoboda.zettai.domain.*
import sk.rsvoboda.zettai.events.*
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.time.LocalDate

internal class ToDoItemProjectionTest {
    @Test
    fun `findAll returns all the lists of a user`() {
        val user = randomUser()
        val listName1 = randomListName()
        val listName2 = randomListName()
        val listId1 = ToDoListId.mint()
        val listId2 = ToDoListId.mint()
        val events = listOf(
            ListCreated(listId1, user, listName1),
            ListCreated(listId2, user, listName2),
            ItemAdded(listId1, ToDoItem("day-after-tomorrow", LocalDate.now().plusDays(2))),
            ItemAdded(listId1, ToDoItem("no-date")),
            ItemAdded(listId1, ToDoItem("tomorrow", LocalDate.now().plusDays(1))),
            ItemAdded(listId2, ToDoItem("today", LocalDate.now()))
        )
        val projection = events.buildItemProjection()

        val nextItems = projection.findWhatsNext(10, listOf(listId1, listId2))

        expectThat(nextItems.map { it.item.description })
            .isEqualTo(listOf("today", "tomorrow", "day-after-tomorrow"))
    }
}

private fun List<ToDoListEvent>.buildItemProjection(): ToDoItemProjection =
    ToDoItemProjection { after ->
        mapIndexed { i, e ->
            StoredEvent(EventSeq(after.progressive + i + 1), e)
        }.asSequence()
    }.also(ToDoItemProjection::update)