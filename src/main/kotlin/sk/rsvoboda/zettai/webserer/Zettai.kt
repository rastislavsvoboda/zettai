package sk.rsvoboda.zettai.webserer

import org.http4k.core.*
import org.http4k.core.body.form
import org.http4k.routing.bind
import org.http4k.routing.path
import org.http4k.routing.routes
import sk.rsvoboda.zettai.domain.*
import sk.rsvoboda.zettai.ui.renderPage

import sk.rsvoboda.zettai.ui.HtmlPage
import sk.rsvoboda.zettai.ui.renderListsPage
import java.time.LocalDate

class Zettai(val hub: ZettaiHub) : HttpHandler {
    val httpHandler = routes(
        "/ping" bind Method.GET to { Response(Status.OK) },
        "/todo/{user}/{listname}" bind Method.GET to ::getTodoList,
        "/todo/{user}/{listname}" bind Method.POST to ::addNewItem,
        "/todo/{user}" bind Method.GET to ::getAllLists
    )

//    private fun Request.extractListNameFromForm(formName: String) =
//        form(formName)
//            ?.let(ListName.Companion::fromUntrusted)

    private fun addNewItem(request: Request): Response {

        val user = request.extractUser()
        val listName = request.extractListName()

        // todo: use extract??

        val item = request.form("itemname")
            ?.let { ToDoItem(it) }
            ?: return Response(Status.BAD_REQUEST)
        return hub.addItemToList(user, listName, item)
            ?.let { Response(Status.SEE_OTHER).header("Location", "/todo/${user.name}/${listName.name}") }
            ?: Response(Status.NOT_FOUND)
    }

    override fun invoke(request: Request): Response = httpHandler(request)

    private fun getTodoList(request: Request): Response {
        val user = request.extractUser()
        val listName = request.path("listname").orEmpty().let(ListName.Companion::fromUntrusted)

        return listName
            ?.let { hub.getList(user, it) }
            ?.let(::renderPage)
            ?.let(::toResponse)
            ?: Response(Status.NOT_FOUND)
    }

    fun toResponse(htmlPage: HtmlPage): Response =
        Response(Status.OK).body(htmlPage.raw)

    private fun getAllLists(req: Request): Response {
        val user = req.extractUser()

        return hub.getLists(user)
            ?.let { renderListsPage(user, it) }
            ?.let(::toResponse)
            ?: Response(Status.BAD_REQUEST)
    }

    private fun Request.extractUser(): User = path("user").orEmpty().let(::User)
    private fun Request.extractListName(): ListName =
        path("listname").orEmpty().let(ListName.Companion::fromUntrustedOrThrow)

//    private fun Request.extractItem(): ToDoItem? {
//        val name = form("itemname") ?: return null
//        val duedate = tryOrNull { LocalDate.parse(form("itemdue")) }
//        return ToDoItem(name, duedate)
//    }
}