package sk.rsvoboda.zettai.commands

import sk.rsvoboda.zettai.domain.ListName
import sk.rsvoboda.zettai.domain.ToDoList
import sk.rsvoboda.zettai.domain.ToDoListUpdatableFetcher
import sk.rsvoboda.zettai.domain.User
import sk.rsvoboda.zettai.events.*

// returns null in case of error
typealias CommandHandler<CMD, EVENT> = (CMD) -> List<EVENT>?

interface ToDoListRetriever {
    fun retrieveByName(user: User, listName: ListName): ToDoListState?
}

class ToDoListCommandHandler(
    private val entityRetriever: ToDoListRetriever,
    var readModel: ToDoListUpdatableFetcher // temporary !
) :
        (ToDoListCommand) -> List<ToDoListEvent>? {
    override fun invoke(command: ToDoListCommand): List<ToDoListEvent>? =
        when (command) {
            is CreateToDoList -> command.execute()
            is AddToDoItem -> command.execute()
        }

    private fun CreateToDoList.execute(): List<ToDoListEvent>? =
        entityRetriever.retrieveByName(user, name)
            ?.let { listState ->
                when (listState) {
                    InitialState -> {
                        readModel.assignListToUser(
                            user,
                            ToDoList(name, emptyList())
                        )
                        // TODO: where is toList() ?
                        //ListCreated(id,user,name).toList()
                        listOf(ListCreated(id, user, name))
                    }

                    else -> null // command failed
                }
            }

    private fun AddToDoItem.execute(): List<ToDoListEvent>? =
        entityRetriever.retrieveByName(user, name)
            ?.let { listState ->
                when (listState) {
                    is ActiveToDoList -> {
                        if (listState.items.any { it.description == item.description })
                            null // cannot have 2 items with same name
                        else {
                            readModel.addItemToList(user, listState.name, item)
                            listOf(ItemAdded(listState.id, item))
                        }
                    }

                    InitialState,
                    is OnHoldToDoList,
                    is ClosedToDoList -> null // command fail
                }
            }
}