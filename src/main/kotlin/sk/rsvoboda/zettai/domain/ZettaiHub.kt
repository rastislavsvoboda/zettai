package sk.rsvoboda.zettai.domain

import sk.rsvoboda.zettai.commands.ToDoListCommand
import sk.rsvoboda.zettai.fp.Outcome

typealias ZettaiOutcome<T> = Outcome<ZettaiError, T>

interface ZettaiHub {
    fun getList(user: User, listName: ListName): ZettaiOutcome<ToDoList>
    fun getLists(user: User): ZettaiOutcome<List<ListName>>
    fun handle(command: ToDoListCommand): ZettaiOutcome<ToDoListCommand>
}