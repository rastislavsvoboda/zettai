package sk.rsvoboda.zettai.webserer

import org.http4k.core.*
import org.http4k.core.body.form
import org.http4k.routing.bind
import org.http4k.routing.path
import org.http4k.routing.routes
import sk.rsvoboda.zettai.commands.AddToDoItem
import sk.rsvoboda.zettai.commands.CreateToDoList
import sk.rsvoboda.zettai.domain.*
import sk.rsvoboda.zettai.fp.failIfNull
import sk.rsvoboda.zettai.fp.tryOrNull
import sk.rsvoboda.zettai.fp.onFailure
import sk.rsvoboda.zettai.fp.recover
import sk.rsvoboda.zettai.ui.HtmlPage
import sk.rsvoboda.zettai.ui.renderListPage
import sk.rsvoboda.zettai.ui.renderListsPage
import sk.rsvoboda.zettai.ui.renderWhatsNextPage
import java.time.LocalDate

class Zettai(val hub: ZettaiHub) : HttpHandler {
    override fun invoke(request: Request): Response = httpHandler(request)

    val httpHandler = routes(
        "/ping" bind Method.GET to { Response(Status.OK) },
        "/todo/{user}/{listname}" bind Method.GET to ::getTodoList,
        "/todo/{user}/{listname}" bind Method.POST to ::addNewItem,
        "/todo/{user}" bind Method.GET to ::getAllLists,
        "/todo/{user}" bind Method.POST to ::createNewList,
        "/whatsnext/{user}" bind Method.GET to ::whatsNext
    )

    private fun createNewList(request: Request): Response {
        val user = request.extractUser()
            .recover { User("anonymous") }
        val listName = request.form("listname")
            ?.let(ListName.Companion::fromUntrusted)
            ?: return Response(Status.BAD_REQUEST).body("missing listname in form")

        return hub.handle(CreateToDoList(user, listName))
            .transform { Response(Status.SEE_OTHER).header("Location", "/todo/${user.name}") }
            .recover { Response(Status.UNPROCESSABLE_ENTITY).body(it.msg) }
    }

    private fun addNewItem(request: Request): Response {
        val user = request.extractUser()
            .recover { User("anonumous") }
        val listName = request.extractListName()
            .onFailure { return Response(Status.BAD_REQUEST).body(it.msg) }
        val item = request.extractItem()
            .onFailure { return Response(Status.BAD_REQUEST).body(it.msg) }
        return hub.handle(AddToDoItem(user, listName, item))
            .transform { Response(Status.SEE_OTHER).header("Location", "/todo/${user.name}/${listName.name}") }
            .recover { Response(Status.UNPROCESSABLE_ENTITY) }
    }

    private fun getTodoList(request: Request): Response {
        val user = request.extractUser()
            .onFailure { return Response(Status.BAD_REQUEST).body(it.msg) }
        val listName = request.extractListName()
            .onFailure { return Response(Status.BAD_REQUEST).body(it.msg) }
        return hub.getList(user, listName)
            .transform { renderListPage(user, it) }
            .transform(::toResponse)
            .recover { Response(Status.NOT_FOUND).body(it.msg) }
    }

    fun toResponse(htmlPage: HtmlPage): Response =
        Response(Status.OK).body(htmlPage.raw)

    private fun getAllLists(request: Request): Response {
        val user = request.extractUser()
            .onFailure { return Response(Status.BAD_REQUEST).body(it.msg) }
        return hub.getLists(user)
            .transform { renderListsPage(user, it) }
            .transform(::toResponse)
            .recover { Response(Status.NOT_FOUND).body(it.msg) }
    }

    private fun whatsNext(request: Request): Response {
        val user = request.extractUser()
            .onFailure { return Response(Status.BAD_REQUEST).body(it.msg) }
        return hub.whatsNext(user)
            .transform { renderWhatsNextPage(user, it) }
            .transform(::toResponse)
            .recover { Response(Status.NOT_FOUND).body(it.msg) }
    }

    private fun Request.extractUser(): ZettaiOutcome<User> =
        path("user")
            .failIfNull(InvalidRequestError("User not present"))
            .transform(::User)

    private fun Request.extractListName(): ZettaiOutcome<ListName> =
        path("listname")
            .orEmpty().let(ListName.Companion::fromUntrustedOrThrow)
            .failIfNull(InvalidRequestError("Invalid list name in path: $this"))

    private fun Request.extractItem(): ZettaiOutcome<ToDoItem> {
        val duedate = tryOrNull { LocalDate.parse(form("itemdue")) }
        return form("itemname")
            .failIfNull(InvalidRequestError("User not present"))
            .transform { ToDoItem(it, duedate) }
    }
}