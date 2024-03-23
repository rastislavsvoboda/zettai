package sk.rsvoboda.zettai.domain

import sk.rsvoboda.zettai.commands.ToDoListCommand
import sk.rsvoboda.zettai.commands.ToDoListCommandHandler
import sk.rsvoboda.zettai.events.ToDoListEvent
import sk.rsvoboda.zettai.fp.EventPersister

class ToDoListHub(
    private val fetcher: ToDoListUpdatableFetcher,
    val commandHandler: ToDoListCommandHandler,
    val persistEvents: EventPersister<ToDoListEvent>
) : ZettaiHub {
    override fun handle(command: ToDoListCommand): ToDoListCommand? =
        commandHandler(command)
            ?.let(persistEvents)
            ?.let { command } // returning the command (in case of success)

    override fun getList(user: User, listName: ListName): ToDoList? =
        fetcher.get(user, listName)

    override fun getLists(user: User): List<ListName>? =
        fetcher.getAll(user)
}