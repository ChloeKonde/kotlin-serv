package com.chloe.kotlinserv

import com.google.gson.Gson
import io.vertx.core.http.HttpServerResponse
import io.vertx.ext.web.Router

fun main(args: Array<String>) {
    val myServer = VertxHttpServer()
    val json = Gson()

    val getRoute = HttpRoute("/countrystats", HttpMethod.GET) {
        val data = CountryStats("23-12-2000", "RUS", 3)
        HttpResponse(200, responseBody=json.toJson(data), mapOf("content-type" to "application/json"))
    }

    val postRoute = HttpRoute("/geodata", HttpMethod.POST) {
        val data = GeoData("RUS", 3405454052, "user1")
        val jsonContent = json.toJson(data)
        HttpResponse(200, jsonContent, mapOf("content-type" to "application/json"))
    }

    myServer.start(8000 , listOf(getRoute, postRoute))
}
