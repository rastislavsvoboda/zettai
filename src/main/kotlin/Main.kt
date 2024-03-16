package org.example

import org.example.sk.rsvoboda.zettai.Zettai
import org.example.sk.rsvoboda.zettai.Zettai.ToDoItem
import org.http4k.core.*
import org.http4k.routing.path
import org.http4k.routing.routes
import org.http4k.routing.bind
import org.http4k.server.Jetty
import org.http4k.server.asServer

fun main() {
    val items = listOf("write chapter", "insert code", "draw diagrams")
    val toDoList = Zettai.ToDoList(Zettai.ListName("book"), items.map(::ToDoItem))
    val lists = mapOf(Zettai.User("uberto") to listOf(toDoList))

    val app: HttpHandler = Zettai(lists)
    app.asServer(Jetty(8080)).start() // starting the server

    println("Server started at http://localhost:8080/todo/uberto/book")
}