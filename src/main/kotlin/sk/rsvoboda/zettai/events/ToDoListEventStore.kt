package sk.rsvoboda.zettai.events

import sk.rsvoboda.zettai.domain.ToDoListRetriever
import sk.rsvoboda.zettai.domain.ListName
import sk.rsvoboda.zettai.domain.User
import sk.rsvoboda.zettai.fp.EventPersister

class ToDoListEventStore(private val eventStreamer: ToDoListEventStreamer) : ToDoListRetriever,
    EventPersister<ToDoListEvent> {
    private fun retrieveById(id: ToDoListId): ToDoListState? =
        eventStreamer(id)
            ?.fold()

    override fun retrieveByName(user: User, listName: ListName): ToDoListState? =
        eventStreamer.retrieveIdFromName(user, listName)
            ?.let(::retrieveById)

    override fun invoke(events: Iterable<ToDoListEvent>): List<ToDoListEvent> =
        eventStreamer.store(events)
}