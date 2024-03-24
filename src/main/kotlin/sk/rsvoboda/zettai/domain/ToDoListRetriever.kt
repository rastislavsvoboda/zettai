package sk.rsvoboda.zettai.domain

import sk.rsvoboda.zettai.events.ToDoListState

interface ToDoListRetriever {
    fun retrieveByName(user: User, listName: ListName): ToDoListState?
}