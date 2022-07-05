package com.chloe.kotlinserv

import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext

class VertxHttpServer : HttpServer {

    override fun start(port: Int, routes: List<HttpRoute>) {
        val vertx = Vertx.vertx()
        val httpServer = vertx.createHttpServer()
        val router = Router.router(vertx)

        routes.forEach { route -> deployVertxRoute(route, router) }
        httpServer.listen(port)
    }

    private fun deployVertxRoute(route: HttpRoute, router: Router) {
        if (route.method == HttpMethod.GET) {
            router.get(route.endpoint).handler { ctx ->
                val httpResponse = route.processFunction.invoke()
                convertHttpResponse(ctx, httpResponse)
            }
        } else {
            router.post(route.endpoint).handler { ctx ->
                val httpResponse = route.processFunction.invoke()
                convertHttpResponse(ctx, httpResponse)
            }
        }
    }

    private fun convertHttpResponse(ctx: RoutingContext, httpResponse: HttpResponse) {
        ctx.response().setStatusCode(httpResponse.code).end(httpResponse.responseBody)
    }
}
