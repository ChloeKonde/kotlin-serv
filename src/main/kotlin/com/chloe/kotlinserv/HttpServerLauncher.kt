package com.chloe.kotlinserv

import com.chloe.kotlinserv.http.HttpRequest
import com.typesafe.config.ConfigFactory
import java.io.File

fun main(args: Array<String>) {
    val file = File(args[0])
    val config = ConfigFactory.parseFile(file)

    val diModule = DiModule(config)
    val myServer = diModule.getHttpServer()

    val port = config.getInt("port")

  //  myServer.start(port, listOf(diModule.getCountryStats(), diModule.postGeoData()))
}
