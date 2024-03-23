package sk.rsvoboda.zettai.commands

import sk.rsvoboda.zettai.domain.ListName
import sk.rsvoboda.zettai.domain.User
import sk.rsvoboda.zettai.events.InitialState
import sk.rsvoboda.zettai.events.ListCreated
import sk.rsvoboda.zettai.events.ToDoListEvent
import sk.rsvoboda.zettai.events.ToDoListState

// returns null in case of error
typealias CommandHandler<CMD, EVENT> = (CMD) -> List<EVENT>?

//// to avoid coupling to EventStore
//typealias ToDoListRetriever = (user: User, listName: ListName) -> ToDoListState?

interface ToDoListRetriever {
    fun retrieveByName(user: User, listName: ListName): ToDoListState?
}

class ToDoListCommandHandler(private val entityRetriever: ToDoListRetriever) :
        (ToDoListCommand) -> List<ToDoListEvent>? {
    override fun invoke(command: ToDoListCommand): List<ToDoListEvent>? =
        when (command) {
            is CreateToDoList -> command.execute()
            else -> null // ignore for the moment
        }

    private fun CreateToDoList.execute(): List<ToDoListEvent>? =
        entityRetriever.retrieveByName(user, name)
            ?.let { listState ->
                when (listState) {
                    InitialState -> {
                        listOf(ListCreated(id, user, name))
                    }

                    else -> null // command failed
                }
            }
}