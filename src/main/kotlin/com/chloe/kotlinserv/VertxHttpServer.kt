package com.chloe.kotlinserv

import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext

class VertxHttpServer : HttpServer {
    lateinit var router: Router
    override fun start(port: Int, routes: List<HttpRoute>) {
        val vertx = Vertx.vertx()
        val httpServer = vertx.createHttpServer()
        router = Router.router(vertx)

        routes.forEach { route -> deployVertxRoute(route) }
        httpServer.listen(8080)
    }

    fun deployVertxRoute(route: HttpRoute) {
        if (route.method == HttpMethod.GET) {
            router.get(route.path).handler { ctx ->
                val httpResponse = route.data.invoke()
                convertHttpResponse(ctx, httpResponse)
            }
        }
    }

    fun convertHttpResponse(ctx: RoutingContext, httpResponse: HttpResponse) {
        ctx.response().end(httpResponse.responseBody, httpResponse.code)
    }
}
