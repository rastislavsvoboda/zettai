package sk.rsvoboda.zettai.stories

import org.http4k.core.*
import org.http4k.filter.ClientFilters
import org.http4k.client.JettyClient
import org.http4k.server.Jetty
import org.http4k.server.asServer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.opentest4j.AssertionFailedError
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.api.expectThrows

import sk.rsvoboda.zettai.webservice.Zettai
import sk.rsvoboda.zettai.domain.ListName
import sk.rsvoboda.zettai.domain.ToDoItem
import sk.rsvoboda.zettai.domain.ToDoList
import sk.rsvoboda.zettai.domain.User

class SeeATodoListAT {
    val frank = ToDoListOwner("Frank")
    val shoppingItems = listOf("carrots", "apples", "milk")
    val frankList = createList("shopping", shoppingItems)

    val bob = ToDoListOwner("Bob")
    val gardenItems = listOf("fix the fence", "mowing the lawn")
    val bobList = createList("gardening", gardenItems)

    val lists = mapOf(
        frank.asUser() to listOf(frankList),
        bob.asUser() to listOf(bobList)
    )

    fun ToDoListOwner.asUser(): User = User(name)

    @Test
    fun `List owners can see their lists`() {
        val app = startTheApplication(lists)
        app.runScenario(
            frank.canSeeTheList("shopping", shoppingItems),
            bob.canSeeTheList("gardening", gardenItems)
        )
    }

    @Test
    fun `Only owners can see their lists`() {
        val app = startTheApplication(lists)
        app.runScenario(
            frank.cannotSeeTheList("gardening"),
            bob.cannotSeeTheList("shopping")
        )
    }

    fun startTheApplication(lists: Map<User, List<ToDoList>>): ApplicationForAT {
        val port = 8081 // different from main
        val server = Zettai(lists).asServer(Jetty(port))
        server.start()

        val client = ClientFilters
            .SetBaseUriFrom(Uri.of("http://localhost:$port/"))
            .then(JettyClient())

        return ApplicationForAT(client, server)
    }
}

interface ScenarioActor {
    val name: String
}

private fun createList(listName: String, items: List<String>): ToDoList =
    ToDoList(ListName(listName), items.map(::ToDoItem))

interface Actions {
    fun getToDoList(user: String, listName: String): ToDoList?
}

typealias Step = Actions.() -> Unit

class ToDoListOwner(override val name: String) : ScenarioActor {
    fun canSeeTheList(listName: String, items: List<String>): Step = {
        val expectedList = createList(listName, items)
        val list = getToDoList(name, listName)
        expectThat(list).isEqualTo(expectedList)
    }

    fun cannotSeeTheList(listName: String): Step = {
        expectThrows<AssertionFailedError> {
            getToDoList("bob", listName)
        }
    }
}

class ApplicationForAT(val client: HttpHandler, val server: AutoCloseable) : Actions {
    override fun getToDoList(user: String, listName: String): ToDoList {
        val response = client(Request(Method.GET, "/todo/$user/$listName"))

        return if (response.status == Status.OK)
            parseResponse(response.bodyString())
        else
            fail(response.toMessage())
    }

    private fun parseResponse(html: String): ToDoList {
        val nameRegex = "<h2>.*<".toRegex()
        val listName = ListName(extractListName(nameRegex, html))
        val itemsRegex = "<td>.*?<".toRegex()
        val items = itemsRegex.findAll(html)
            .map { ToDoItem(extractItemDesc(it)) }.toList()

        return ToDoList(listName, items)
    }

    private fun extractListName(nameRegex: Regex, html: String): String =
        nameRegex.find(html)?.value
            ?.substringAfter("<h2>")
            ?.dropLast(1)
            .orEmpty()

    private fun extractItemDesc(matchResult: MatchResult): String =
        matchResult.value
            .substringAfter("<td>")
            .dropLast(1)

    fun runScenario(vararg steps: Step) {
        server.use {
            steps.onEach { step -> step(this) }
        }
    }
}