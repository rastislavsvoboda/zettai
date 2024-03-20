package sk.rsvoboda.zettai.domain

class ToDoListHub(private val fetcher: ToDoListUpdatableFetcher) : ZettaiHub {
    override fun getList(user: User, listName: ListName): ToDoList? =
        fetcher(user, listName)

    override fun addItemToList(user: User, listName: ListName, item: ToDoItem): ToDoList? =
        fetcher(user, listName)?.run {
            val newList = copy(items = items.filterNot { it.description == item.description } + item)
            fetcher.assignListToUser(user, newList)
        }

    override fun getLists(user: User): List<ListName>? =
        fetcher.getAll(user)
}