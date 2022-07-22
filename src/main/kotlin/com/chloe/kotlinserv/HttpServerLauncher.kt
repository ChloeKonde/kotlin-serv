package com.chloe.kotlinserv

import com.chloe.kotlinserv.http.HttpServer
import com.google.inject.Guice
import com.typesafe.config.ConfigFactory
import java.io.File

fun main(args: Array<String>) {
    val file = File(args[0])
    val config = ConfigFactory.parseFile(file)

    val injector = Guice.createInjector(DiModule(config))

    val httpServer = injector.getInstance(HttpServer::class.java)

    val port = config.getInt("port")

    httpServer.start(port)
}
