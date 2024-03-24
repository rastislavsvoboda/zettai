package sk.rsvoboda.zettai.queries

import org.junit.jupiter.api.Test
import sk.rsvoboda.zettai.domain.ToDoList
import sk.rsvoboda.zettai.domain.randomItem
import sk.rsvoboda.zettai.domain.randomListName
import sk.rsvoboda.zettai.domain.randomUser
import sk.rsvoboda.zettai.events.*
import strikt.api.expectThat
import strikt.assertions.isEqualTo

internal class ToDoListProjectionTest {
    @Test
    fun `findAll returns all the lists of a user`() {
        val user = randomUser()
        val listName1 = randomListName()
        val listName2 = randomListName()
        val events = listOf(
            ListCreated(ToDoListId.mint(), user, listName1),
            ListCreated(ToDoListId.mint(), user, listName2),
            ListCreated(ToDoListId.mint(), randomUser(), randomListName()),
        )

        val projection = events.buildListProjection()

        expectThat(projection.findAll(user))
            .isEqualTo(listOf(listName1, listName2))
    }

    @Test
    fun `findList get list with correct items`(){
        val user = randomUser()
        val listName = randomListName()
        val id = ToDoListId.mint()
        val item1 = randomItem()
        val item2 = randomItem()
        val item3 = randomItem()
        val events = listOf(
            ListCreated(id, user, listName),
            ItemAdded(id, item1),
            ItemAdded(id, item2),
            ItemModified(id, item2, item3),
            ItemRemoved(id, item1)
        )

        val projection = events.buildListProjection()

        expectThat(projection.findList(user, listName))
            .isEqualTo(ToDoList(listName, listOf(item3)))
    }
}

private fun List<ToDoListEvent>.buildListProjection(): ToDoListProjection =
    ToDoListProjection { after ->
        mapIndexed { i, e ->
            StoredEvent(EventSeq(after.progressive + i + 1), e)
        }.asSequence()
    }.also(ToDoListProjection::update)
