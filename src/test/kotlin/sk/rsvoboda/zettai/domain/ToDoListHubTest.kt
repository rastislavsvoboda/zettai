package sk.rsvoboda.zettai.domain

import org.junit.jupiter.api.Test
import sk.rsvoboda.zettai.commands.AddToDoItem
import sk.rsvoboda.zettai.commands.CreateToDoList
import sk.rsvoboda.zettai.domain.tooling.expectFailure
import sk.rsvoboda.zettai.domain.tooling.expectSuccess
import strikt.api.expect
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import strikt.assertions.isNull

class ToDoListHubTest {
    val hub = prepareToDoListHubForTests()

    @Test
    fun `get list by user and name`() {
        usersGenerator().take(10).forEach { user ->
            val lists = toDoListGenerator().take(100).toList()
            lists.forEach { list ->
                hub.handle(CreateToDoList(user, list.listName)).expectSuccess()
                list.items.forEach {
                    hub.handle(AddToDoItem(user, list.listName, it)).expectSuccess()
                }
            }

            lists.forEach { list ->
                val myList = hub.getList(user, list.listName).expectSuccess()
                expectThat(myList).isEqualTo(list)
            }
        }
    }

    @Test
    fun `don't get list from other users`() {
        repeat(10) {
            val firstList = randomToDoList()
            val secondList = randomToDoList()
            val firstUser = randomUser()
            val secondUser = randomUser()

            expect {
                that(hub.getList(firstUser, secondList.listName).expectFailure()).isA<InvalidRequestError>()
                that(hub.getList(secondUser, firstList.listName).expectFailure()).isA<InvalidRequestError>()
            }
        }
    }
}