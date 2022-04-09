package me.ndts.tumark

import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

private fun <T> safeTry(f: () -> T): Result<T> =
    try {
        Result.success(f())
    } catch (e: Exception) {
        Result.failure(e)
    }

fun Connection.safeExecute(): Result<Connection.Response> = safeTry { this.execute() }

fun Connection.safeGet(): Result<Document> = safeTry { this.get() }

fun Connection.Response.safeParse(): Result<Document> = safeTry { this.parse() }

fun Element.safeChild(index: Int): Result<Element> = safeTry { this.child(index) }

fun String.connect(): Connection =
    Jsoup.connect(this)