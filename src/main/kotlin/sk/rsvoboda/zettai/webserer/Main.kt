package sk.rsvoboda.zettai.webserer

import org.http4k.server.Jetty
import org.http4k.server.asServer
import sk.rsvoboda.zettai.commands.ToDoListCommandHandler
import sk.rsvoboda.zettai.domain.*
import sk.rsvoboda.zettai.events.ToDoListEventStore
import sk.rsvoboda.zettai.events.ToDoListEventStreamerInMemory
import sk.rsvoboda.zettai.queries.ToDoListQueryRunner

fun main() {
    val streamer = ToDoListEventStreamerInMemory()
    val eventStore = ToDoListEventStore(streamer)

    val commandHandler = ToDoListCommandHandler(eventStore)
    val queryHandler = ToDoListQueryRunner(streamer::fetchAfter)

    val hub = ToDoListHub(queryHandler, commandHandler, eventStore)

    Zettai(hub).asServer(Jetty(8080)).start()

    println("Server started at http://localhost:8080/todo/uberto")
}
