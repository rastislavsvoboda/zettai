package sk.rsvoboda.zettai.domain

import sk.rsvoboda.zettai.commands.ToDoListCommandHandler
import sk.rsvoboda.zettai.events.ToDoListEventStore
import sk.rsvoboda.zettai.events.ToDoListEventStreamerInMemory
import sk.rsvoboda.zettai.queries.ToDoListQueryRunner
import sk.rsvoboda.zettai.webserer.Zettai

fun prepareToDoListHubForTests(): ToDoListHub {
    val streamer = ToDoListEventStreamerInMemory()
    val eventStore = ToDoListEventStore(streamer)
    val cmdHandler = ToDoListCommandHandler(eventStore)
    val queryRunner = ToDoListQueryRunner(streamer::fetchAfter)
    return ToDoListHub(queryRunner, cmdHandler, eventStore)
}

fun prepareZettaiForTests(): Zettai {
    return Zettai(prepareToDoListHubForTests())
}