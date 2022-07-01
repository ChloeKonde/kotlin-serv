package com.chloe.kotlinserv

import com.google.gson.Gson
import io.vertx.core.http.HttpServerResponse
import io.vertx.ext.web.Router

fun main(args: Array<String>) {
    val myServer = VertxHttpServer()
    myServer.start(8080)
}
