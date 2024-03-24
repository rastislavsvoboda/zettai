package sk.rsvoboda.zettai.queries

import sk.rsvoboda.zettai.events.ToDoListEvent
import sk.rsvoboda.zettai.fp.FetchStoredEvents
import sk.rsvoboda.zettai.fp.ProjectionQuery
import sk.rsvoboda.zettai.fp.QueryRunner

class ToDoListQueryRunner(eventFetcher: FetchStoredEvents<ToDoListEvent>) : QueryRunner<ToDoListQueryRunner> {
    internal val listProjection = ToDoListProjection(eventFetcher)
    internal val itemProjection = ToDoItemProjection(eventFetcher)

    override fun <R> invoke(f: ToDoListQueryRunner.() -> R): ProjectionQuery<R> =
        ProjectionQuery(setOf(listProjection, itemProjection)) { f(this) }
}