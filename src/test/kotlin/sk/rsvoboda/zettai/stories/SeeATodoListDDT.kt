package sk.rsvoboda.zettai.stories

import com.ubertob.pesticide.core.*
import strikt.api.expectThat
import strikt.api.Assertion
import strikt.assertions.containsExactlyInAnyOrder
import strikt.assertions.isNotNull
import sk.rsvoboda.zettai.domain.*
import sk.rsvoboda.zettai.tooling.ZettaiActions
import sk.rsvoboda.zettai.tooling.ZettaiDDT
import sk.rsvoboda.zettai.tooling.allActions
import strikt.assertions.isNull

class SeeATodoListDDT : ZettaiDDT(allActions()) {
    val frank by NamedActor(::ToDoListOwner)
    val bob by NamedActor(::ToDoListOwner)

    val shoppingListName = "shopping"
    val shoppingItems = listOf("carrots", "apples", "milk")

    val gardenListName = "gardening"
    val gardenItems = listOf("fix the fence", "mowing the lawn")

    @DDT
    fun `List owners can see their lists`() = ddtScenario {
        setUp {
            frank.`starts with a list`(shoppingListName, shoppingItems)
            bob.`starts with a list`(gardenListName, gardenItems)
        }.thenPlay(
            frank.`can see #listname with #itemnames`(shoppingListName, shoppingItems),
            bob.`can see #listname with #itemnames`(gardenListName, gardenItems)
        )
    }

    @DDT
    fun `Only owners can see their lists`() = ddtScenario {
        setUp {
            frank.`starts with a list`(shoppingListName, shoppingItems)
            bob.`starts with a list`(gardenListName, gardenItems)
        }.thenPlay(
            frank.`cannot see #listname`(gardenListName),
            bob.`cannot see #listname`(shoppingListName)
        )
    }

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

        private val Assertion.Builder<ToDoList>.itemNames
            get() = get { items.map { it.description } }
    }
}