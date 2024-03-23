package sk.rsvoboda.zettai.domain

import sk.rsvoboda.zettai.commands.ToDoListCommand

interface ZettaiHub {
    fun getList(user: User, listName: ListName): ToDoList?
    fun getLists(user: User): List<ListName>?
    fun handle(command: ToDoListCommand): ToDoListCommand?
}