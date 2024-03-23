package sk.rsvoboda.zettai.domain

interface ToDoListFetcher {

    fun get(user: User, listName: ListName): ToDoList?

    fun getAll(user: User): List<ListName>?

}

interface ToDoListUpdatableFetcher : ToDoListFetcher {
    fun assignListToUser(user: User, list: ToDoList): ToDoList?

    fun addItemToList(user: User, listName: ListName, item: ToDoItem){
        get(user, listName)?.run {
            val newList = copy(items = items.filterNot { it.description == item.description } + item)
            assignListToUser(user, newList)
        }
    }
}