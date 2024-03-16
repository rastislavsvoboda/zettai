package sk.rsvoboda.zettai.domain

data class ToDoList(val listName: ListName, val items: List<ToDoItem>)

data class ListName(val name: String)

data class ToDoItem(val description: String)