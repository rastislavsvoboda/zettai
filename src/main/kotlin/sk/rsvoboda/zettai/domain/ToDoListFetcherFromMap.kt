package sk.rsvoboda.zettai.domain

typealias ToDoListStore = MutableMap<User, MutableMap<ListName, ToDoList>>

data class ToDoListFetcherFromMap(
    private val store: ToDoListStore
) : ToDoListUpdatableFetcher {
    override fun get(user: User, listName: ListName): ToDoList? =
        store[user]?.get(listName)

    override fun getAll(user: User): List<ListName> =
        store[user]?.keys?.toList() ?: emptyList()

    override fun assignListToUser(user: User, list: ToDoList): ToDoList? =
        store.compute(user) { _, value ->
            val listMap = value ?: mutableMapOf()
            listMap.apply { put(list.listName, list) }
        }?.let { list }
}