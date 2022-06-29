package com.chloe.kotlinserv

import io.vertx.core.Vertx

class VertxHttpServer : HttpServer {
    lateinit var vertx:Vertx

    override fun start(port: Int) {
        vertx = Vertx.vertx()
        val httpServer = vertx.createHttpServer()
        httpServer.listen(8080)
    }
}
