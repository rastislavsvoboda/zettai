package sk.rsvoboda.zettai.domain

import sk.rsvoboda.zettai.commands.ToDoListCommand
import sk.rsvoboda.zettai.commands.ToDoListCommandHandler
import sk.rsvoboda.zettai.events.ToDoListEvent
import sk.rsvoboda.zettai.events.ToDoListState
import sk.rsvoboda.zettai.fp.EventPersister
import sk.rsvoboda.zettai.fp.OutcomeError

sealed class ZettaiError : OutcomeError
data class InvalidRequestError(override val msg: String) : ZettaiError()
data class ToDoListCommandError(override val msg: String) : ZettaiError()
data class InconsistentStateError(val command: ToDoListCommand, val state: ToDoListState) : ZettaiError() {
    override val msg = "Command $command cannot be applied tp state $state"
}

class ToDoListHub(
    private val fetcher: ToDoListUpdatableFetcher,
    val commandHandler: ToDoListCommandHandler,
    val persistEvents: EventPersister<ToDoListEvent>
) : ZettaiHub {
    override fun handle(command: ToDoListCommand) =
        commandHandler(command)
            .transform(persistEvents)
            .transform { command } // returning the command (in case of success)

    override fun getList(user: User, listName: ListName): ZettaiOutcome<ToDoList> =
        fetcher.get(user, listName)

    override fun getLists(user: User): ZettaiOutcome<List<ListName>> =
        fetcher.getAll(user)
}