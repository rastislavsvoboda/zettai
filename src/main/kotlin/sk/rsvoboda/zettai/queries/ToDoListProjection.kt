package sk.rsvoboda.zettai.queries

import sk.rsvoboda.zettai.domain.ListName
import sk.rsvoboda.zettai.domain.ToDoItem
import sk.rsvoboda.zettai.domain.ToDoList
import sk.rsvoboda.zettai.domain.User
import sk.rsvoboda.zettai.events.*
import sk.rsvoboda.zettai.fp.*

data class ToDoListProjectionRow(
    val user: User, val active: Boolean, val list: ToDoList
) {
    fun addItem(item: ToDoItem): ToDoListProjectionRow =
        copy(list = list.copy(items = list.items + item))

    fun removeItem(item: ToDoItem): ToDoListProjectionRow =
        copy(list = list.copy(items = list.items - item))

    fun replaceItem(prevItem: ToDoItem, item: ToDoItem): ToDoListProjectionRow =
        copy(list = list.copy(items = list.items - prevItem + item))

    fun putOnHold(): ToDoListProjectionRow =
        copy(active = false)

    fun release(): ToDoListProjectionRow =
        copy(active = true)
}

class ToDoListProjection(eventFetcher: FetchStoredEvents<ToDoListEvent>) :
    InMemoryProjection<ToDoListProjectionRow, ToDoListEvent> by ConcurrentMapProjection(
        eventFetcher,
        ::eventProjector
    ) {
    //    fun findAll(user: User): Sequence<ListName> =
    fun findAll(user: User): List<ListName>? =
        allRows().values
//            .asSequence()
            .filter { it.user == user }
            .map { it.list.listName }

    fun findList(user: User, listName: ListName): ToDoList? =
        allRows().values
            .firstOrNull { it.user == user && it.list.listName == listName }
            ?.list

    companion object {
        fun eventProjector(e: ToDoListEvent): List<DeltaRow<ToDoListProjectionRow>> =
            when (e) {
                is ListCreated -> CreateRow(
                    e.rowId(),
                    ToDoListProjectionRow(e.owner, true, ToDoList(e.name, emptyList()))
                )

                is ItemAdded -> UpdateRow(e.rowId()) { addItem(e.item) }
                is ItemRemoved -> UpdateRow(e.rowId()) { removeItem(e.item) }
                is ItemModified -> UpdateRow(e.rowId()) { replaceItem(e.prevItem, e.item) }
                is ListPutOnHold -> UpdateRow(e.rowId()) { putOnHold() }
                is ListReleased -> UpdateRow(e.rowId()) { release() }
                is ListClosed -> DeleteRow(e.rowId())
            }.toSingle()
    }
}

private fun ToDoListEvent.rowId(): RowId = RowId(id.raw.toString())

