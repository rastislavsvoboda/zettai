package sk.rsvoboda.zettai.tooling

import com.ubertob.pesticide.core.DdtActions
import com.ubertob.pesticide.core.DdtProtocol
import com.ubertob.pesticide.core.DomainDrivenTest
import sk.rsvoboda.zettai.domain.ListName
import sk.rsvoboda.zettai.domain.ToDoList
import sk.rsvoboda.zettai.domain.User
import sk.rsvoboda.zettai.stories.SeeATodoListDDT

interface ZettaiActions : DdtActions<DdtProtocol> {
    fun SeeATodoListDDT.ToDoListOwner.`starts with a list`(listName: String, items: List<String>)
    fun getToDoList(user: User, listName: ListName): ToDoList?

}

typealias ZettaiDDT = DomainDrivenTest<ZettaiActions>

fun allActions() = setOf(
    DomainOnlyActions(),
    HttpActions()
)