package sk.rsvoboda.zettai.stories

import org.http4k.core.*
import org.http4k.filter.ClientFilters
import org.http4k.client.JettyClient
import org.http4k.server.Jetty
import org.http4k.server.asServer
import org.http4k.routing.bind
import org.http4k.routing.path
import org.http4k.routing.routes
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.opentest4j.AssertionFailedError
import sk.rsvoboda.zettai.domain.*
import sk.rsvoboda.zettai.ui.HtmlPage
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.api.expectThrows

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
//        val hub = ToDoListHub(lists)
        val server = Zettai2(lists).asServer(Jetty(port))
        server.start()

        val client = ClientFilters
            .SetBaseUriFrom(Uri.of("http://localhost:$port/"))
            .then(JettyClient())

        return ApplicationForAT(client, server)
    }

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
                // TODO: fill details
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
}

interface ScenarioActor {
    val name: String
}

private fun createList(listName: String, items: List<String>): ToDoList =
    // TODO: fill details
    ToDoList(ListName(listName), items.map( { ToDoItem(it)}))

interface Actions {
    fun getToDoList(user: String, listName: String): ToDoList?
}

typealias Step = Actions.() -> Unit

//temporary until the hub
data class Zettai2(val lists: Map<User, List<ToDoList>>) : HttpHandler {

    val routes = routes(
        "/todo/{user}/{list}" bind Method.GET to ::getList
    )

    override fun invoke(request: Request): Response =
        routes(request)

    private fun getList(request: Request): Response =
        request.let(::extractListData)
            .let(::fetchListContent)
            .let(::renderHtml)
            .let(::createResponse)


    fun extractListData(request: Request): Pair<User, ListName> {
        val user = request.path("user").orEmpty()
        val list = request.path("list").orEmpty()
        return User(user) to ListName(list)
    }

    fun fetchListContent(listId: Pair<User, ListName>): ToDoList =
        lists[listId.first]
            ?.firstOrNull { it.listName == listId.second }
            ?: error("List unknown")

    fun createResponse(html: HtmlPage): Response = Response(Status.OK).body(html.raw)


    fun renderHtml(todoList: ToDoList): HtmlPage =
        HtmlPage(
            """
    <html>
        <body>
            <h1>Zettai</h1>
            <h2>${todoList.listName.name}</h2>
            <table>
                <tbody>${renderItems(todoList.items)}</tbody>
            </table>
        </body>
    </html>
    """.trimIndent()
        )

    private fun renderItems(items: List<ToDoItem>) =
        items.map {
            """<tr><td>${it.description}</td></tr>""".trimIndent()
        }.joinToString("")
}