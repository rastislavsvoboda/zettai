package sk.rsvoboda.zettai.queries

import sk.rsvoboda.zettai.domain.ToDoItem
import sk.rsvoboda.zettai.domain.ToDoStatus
import sk.rsvoboda.zettai.events.*
import sk.rsvoboda.zettai.fp.*

data class ItemProjectionRow(val item: ToDoItem, val listId: EntityId)

class ToDoItemProjection(eventFetcher: FetchStoredEvents<ToDoListEvent>) :
    InMemoryProjection<ItemProjectionRow, ToDoListEvent> by ConcurrentMapProjection(
        eventFetcher,
        ::eventProjector
    ) {

    fun findWhatsNext(maxRows: Int, lists: List<EntityId>): List<ItemProjectionRow> =
        allRows().values
            .filter { it.listId in lists }
            .filter { it.item.dueDate != null && it.item.status == ToDoStatus.Todo }
            .sortedBy { it.item.dueDate }
            .take(maxRows)

    companion object {
        fun eventProjector(e: ToDoListEvent): List<DeltaRow<ItemProjectionRow>> =
            when (e) {
                is ListCreated -> emptyList()
                is ItemAdded -> CreateRow(e.itemRowId(e.item), ItemProjectionRow(e.item, e.id)).toSingle()
                is ItemRemoved -> DeleteRow<ItemProjectionRow>(e.itemRowId(e.item)).toSingle()
                is ItemModified -> listOf(
                    DeleteRow(e.itemRowId(e.prevItem)),
                    CreateRow(e.itemRowId(e.item), ItemProjectionRow(e.item, e.id))
                )

                is ListPutOnHold -> emptyList()
                is ListReleased -> emptyList()
                is ListClosed -> emptyList()
            }
    }
}

private fun ToDoListEvent.itemRowId(item: ToDoItem): RowId =
    RowId("${id}_${item.description}")