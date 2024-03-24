package sk.rsvoboda.zettai.commands

import sk.rsvoboda.zettai.domain.InconsistentStateError
import sk.rsvoboda.zettai.domain.ToDoListCommandError
import sk.rsvoboda.zettai.domain.ToDoListRetriever
import sk.rsvoboda.zettai.domain.ZettaiOutcome
import sk.rsvoboda.zettai.events.*
import sk.rsvoboda.zettai.fp.asFailure
import sk.rsvoboda.zettai.fp.asSuccess

typealias ToDoListCommandOutcome = ZettaiOutcome<List<ToDoListEvent>>

class ToDoListCommandHandler(
    private val entityRetriever: ToDoListRetriever
) :
        (ToDoListCommand) -> ToDoListCommandOutcome {
    override fun invoke(command: ToDoListCommand): ToDoListCommandOutcome =
        when (command) {
            is CreateToDoList -> command.execute()
            is AddToDoItem -> command.execute()
        }

    private fun CreateToDoList.execute(): ToDoListCommandOutcome {
        val listState = entityRetriever.retrieveByName(user, name) ?: InitialState

        return when (listState) {
            InitialState ->
                ListCreated(id, user, name).asCommandSuccess()

            is ActiveToDoList,
            is OnHoldToDoList,
            is ClosedToDoList -> InconsistentStateError(this, listState).asFailure()
        }
    }

    private fun AddToDoItem.execute(): ToDoListCommandOutcome =
        entityRetriever.retrieveByName(user, name)
            ?.let { listState ->
                when (listState) {
                    is ActiveToDoList -> {
                        if (listState.items.any { it.description == item.description })
                            ToDoListCommandError("cannot have 2 items with same name").asFailure()
                        else {
                            ItemAdded(listState.id, item).asCommandSuccess()
                        }
                    }

                    InitialState,
                    is OnHoldToDoList,
                    is ClosedToDoList -> InconsistentStateError(this, listState).asFailure()
                }
            } ?: ToDoListCommandError("list $name not found").asFailure()

    private fun ToDoListEvent.asCommandSuccess(): ZettaiOutcome<List<ToDoListEvent>> =
        listOf(this).asSuccess()
}