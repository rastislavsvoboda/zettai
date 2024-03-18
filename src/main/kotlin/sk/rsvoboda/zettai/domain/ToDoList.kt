package sk.rsvoboda.zettai.domain

import java.time.LocalDate
import java.util.*

fun String.capitalize() = replaceFirstChar {
    if (it.isLowerCase()) it.titlecase(
        Locale.getDefault()
    ) else it.toString()
}

val pathElementPattern = Regex(pattern = "[A-Za-z0-9-]+")

data class ListName internal constructor(val name: String) {
    companion object {
        fun fromTrusted(name: String): ListName = ListName(name)
        fun fromUntrustedOrThrow(name: String): ListName =
            fromUntrusted(name) ?: throw IllegalArgumentException("Invalid list name $name")

        fun fromUntrusted(name: String): ListName? =
            if (name.matches(pathElementPattern) && name.length in 1..40)
                fromTrusted(name)
            else
                null
    }
}

data class ToDoList(val listName: ListName, val items: List<ToDoItem>) {
    companion object {
        fun build(
            listName: String, items: List<String>
        ): ToDoList = ToDoList(ListName.fromUntrustedOrThrow(listName), items.map { ToDoItem(it) })
    }
}

data class ToDoItem(
    val description: String,
    val dueDate: LocalDate? = null,
    val status: ToDoStatus = ToDoStatus.Todo
)

enum class ToDoStatus { Todo, InProgress, Done, Blocked }