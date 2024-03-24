package sk.rsvoboda.zettai.commands

import sk.rsvoboda.zettai.domain.ListName
import sk.rsvoboda.zettai.domain.ToDoItem
import sk.rsvoboda.zettai.domain.User
import sk.rsvoboda.zettai.events.ToDoListId

sealed class ToDoListCommand

data class CreateToDoList(
    val user: User,
    val name: ListName
) : ToDoListCommand() {
    val id: ToDoListId = ToDoListId.mint()
}

data class AddToDoItem(
    val user: User,
    val name: ListName,
    val item: ToDoItem
) : ToDoListCommand()