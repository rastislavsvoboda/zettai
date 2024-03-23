package sk.rsvoboda.zettai.tooling

import com.ubertob.pesticide.core.DdtActor
import sk.rsvoboda.zettai.domain.ListName
import sk.rsvoboda.zettai.domain.ToDoItem
import sk.rsvoboda.zettai.domain.ToDoList
import sk.rsvoboda.zettai.domain.User
import strikt.api.Assertion
import strikt.api.expectThat
import strikt.assertions.*

data class ToDoListOwner(override val name: String) : DdtActor<ZettaiActions>() {
    val user = User(name)

    fun `can see #listname with #itemnames`(
        listName: String,
        expectedItems: List<String>
    ) =
        step(listName, expectedItems) {
            val list = getToDoList(user, ListName(listName))

            expectThat(list)
                .isNotNull()
                .itemNames.containsExactlyInAnyOrder(expectedItems)
        }

    fun `cannot see #listname`(listName: String) =
        step(listName) {
            val list = getToDoList(user, ListName.fromUntrustedOrThrow(listName))
            expectThat(list).isNull()
        }

    fun `can add #item to #listname`(itemName: String, listName: String) =
        step(itemName, listName) {
            val item = ToDoItem(itemName)
            addListItem(user, ListName(listName), item)
        }

    fun `cannot see any list`() =
        step {
            val lists = allUserList(user)
            expectThat(lists).isEmpty()
        }

    fun `can see the lists #listNames`(expectedLists: Set<String>) =
        step(expectedLists) {
            val lists = allUserList(user)
            expectThat(lists)
                .map(ListName::name)
                .containsExactly(expectedLists)
        }

    fun `can create a new list called #listname`(listName: String) =
        step(listName) {
            createList(user, ListName.fromUntrustedOrThrow(listName))
        }

    private val Assertion.Builder<ToDoList>.itemNames
        get() = get { items.map { it.description } }
}