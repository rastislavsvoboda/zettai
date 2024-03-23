package sk.rsvoboda.zettai.domain

import sk.rsvoboda.zettai.commands.ToDoListCommandHandler
import sk.rsvoboda.zettai.events.ToDoListEventStore
import sk.rsvoboda.zettai.events.ToDoListEventStreamerInMemory
import sk.rsvoboda.zettai.webserer.Zettai

fun prepareToDoListHubForTests(fetcher: ToDoListFetcherFromMap) : ToDoListHub{
    val streamer = ToDoListEventStreamerInMemory()
    val eventStore = ToDoListEventStore(streamer)
    val cmdHandler = ToDoListCommandHandler(eventStore, fetcher)
    return ToDoListHub(fetcher, cmdHandler, eventStore)
}

fun prepareZettaiForTests() : Zettai {
    return Zettai(
        prepareToDoListHubForTests(
            ToDoListFetcherFromMap(
                mutableMapOf()
            )
        )
    )
}