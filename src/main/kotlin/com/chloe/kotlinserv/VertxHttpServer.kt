package com.chloe.kotlinserv

import com.google.gson.Gson
import io.vertx.core.Vertx
import io.vertx.core.http.HttpServerResponse
import io.vertx.ext.web.Router

class VertxHttpServer : HttpServer {

    override fun start(port: Int) {
        val vertx = Vertx.vertx()
        val httpServer = vertx.createHttpServer()
        val router = Router.router(vertx)
        val gson = Gson()

        router.get("/countrystats/").handler { ctx ->
            val response: HttpServerResponse = ctx.response()
            response.putHeader("content-type", "application/json")
            val data = CountryStats("23-12-2000", "RUS", 3)
            response.end(gson.toJson(data))
        }

        router.post("/geodata").handler { ctx ->
            val response: HttpServerResponse = ctx.response()
            response.putHeader("content-type", "application/json")
            val data = GeoData("GB", 3243242, "myTestUser")
            response.end(gson.toJson(data))
        }
        httpServer.listen(8080)
    }
}
