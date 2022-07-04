package com.chloe.kotlinserv

import com.google.gson.Gson
import io.vertx.core.http.HttpServerResponse
import io.vertx.ext.web.Router

fun main(args: Array<String>) {
    val myServer = VertxHttpServer()

    val getRoute = HttpRoute("/countrystats", HttpMethod.GET) {
        val data = CountryStats("23-12-2000", "RUS", 3)
        val json = Gson().toJson(data)
        HttpResponse("200", json, mapOf("content/type" to "application/json"))
    }

    val postRoute = HttpRoute("/geodata", HttpMethod.POST) {
        val data = GeoData("RUS", 3405454052, "user1")
        val json = Gson().toJson(data)
        HttpResponse("200", json, mapOf("content/type" to "application/json"))
    }

    myServer.start(8080, listOf(getRoute, postRoute))
}
