package com.chloe.kotlinserv

import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
import io.vertx.core.http.HttpServer
import io.vertx.core.http.HttpServerResponse
import io.vertx.ext.web.Router


class MainVerticle : AbstractVerticle() {

    override fun start(startPromise: Promise<Void>) {
        val server: HttpServer = vertx.createHttpServer()
        val router: Router = Router.router(vertx)

        router.get("/countrystats/").handler { ctx ->
            val response: HttpServerResponse = ctx.response()
            response.putHeader("content-type", "text/plain")
            response.end("lol gets")
        }
        router.post("/geodata/").handler { ctx ->
            val response: HttpServerResponse = ctx.response()
            response.putHeader("content-type", "text/plain")
            response.end("kek gets")
        }

        server.requestHandler(router).listen(8080)
    }
}
