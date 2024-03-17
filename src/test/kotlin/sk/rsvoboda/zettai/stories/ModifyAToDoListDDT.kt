package sk.rsvoboda.zettai.stories

import com.ubertob.pesticide.core.*
import sk.rsvoboda.zettai.tooling.ToDoListOwner
import sk.rsvoboda.zettai.tooling.ZettaiDDT
import sk.rsvoboda.zettai.tooling.allActions

class ModifyAToDoListDDT : ZettaiDDT(allActions()) {
    val ann by NamedActor(::ToDoListOwner)

    @DDT
    fun `The list owner can add new items`() = ddtScenario {
        setUp {
            ann.`starts with a list`("dyi", emptyList())
        }.thenPlay(
            ann.`can add #item to #listname`("paint the shelf", "dyi"),
            ann.`can add #item to #listname`("fix the gate", "dyi"),
            ann.`can add #item to #listname`("change the lock", "dyi"),
            ann.`can see #listname with #itemnames`(
                "dyi", listOf(
                    "fix the gate", "paint the shelf", "change the lock"
                )
            ),
        )
    }
}