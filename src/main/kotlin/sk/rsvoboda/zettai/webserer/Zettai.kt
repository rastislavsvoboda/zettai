package sk.rsvoboda.zettai.webserer

import org.http4k.core.*
import org.http4k.core.Method.GET
import org.http4k.routing.bind
import org.http4k.routing.path
import org.http4k.routing.routes
import sk.rsvoboda.zettai.domain.*
import sk.rsvoboda.zettai.ui.renderPage

import sk.rsvoboda.zettai.ui.HtmlPage

data class Zettai(val hub: ZettaiHub) : HttpHandler {
    val httpHandler = routes(
        "/ping" bind GET to { Response(Status.OK) },
        "/todo/{user}/{listname}" bind GET to ::getTodoList
    )

    override fun invoke(request: Request): Response = httpHandler(request)

    fun toResponse(htmlPage: HtmlPage): Response =
        Response(Status.OK).body(htmlPage.raw)

    private fun getTodoList(request: Request): Response {
        val user = request.path("user").orEmpty().let(::User)
        val listName = request.path("listname").orEmpty().let(ListName.Companion::fromUntrusted)

        return listName
            ?.let { hub.getList(user, it) }
            ?.let(::renderPage)
            ?.let(::toResponse)
            ?: Response(Status.NOT_FOUND)
    }
}