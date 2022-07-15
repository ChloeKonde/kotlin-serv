package com.chloe.kotlinserv

import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler

class VertxHttpServer : HttpServer {

    override fun start(port: Int, routes: List<HttpRoute>) {
        val vertx = Vertx.vertx()
        val httpServer = vertx.createHttpServer()
        val router = Router.router(vertx)

        routes.forEach { route -> deployVertxRoute(route, router) }
        httpServer.requestHandler(router).listen(port)
    }

    private fun deployVertxRoute(route: HttpRoute, router: Router) {
        router.route().handler(BodyHandler.create())

        if (route.method == HttpMethod.GET) {
            router.get(route.endpoint).handler { ctx ->
                val queryParams = ctx.queryParams().map { it.key to it.value }.groupBy({it.first},{it.second}).toMap()
                val headers = ctx.request().headers().map { it.key to it.value }.groupBy({it.first},{it.second}).toMap()
                val httpResponse = route.processFunction.invoke(HttpRequest(headers,"", queryParams))
                convertHttpResponse(ctx, httpResponse)
            }
        } else {
            router.post(route.endpoint).handler { ctx ->
                val queryParams = ctx.queryParams().map { it.key to it.value }.groupBy({it.first},{it.second}).toMap()
                val headers = ctx.request().headers().map { it.key to it.value }.groupBy({it.first},{it.second}).toMap()
                val body = ctx.body().asString()
                val httpResponse = route.processFunction.invoke(HttpRequest(headers, body, queryParams))
                convertHttpResponse(ctx, httpResponse)
            }
        }
    }

    private fun convertHttpResponse(ctx: RoutingContext, httpResponse: HttpResponse) {
        if (httpResponse.responseBody == null) {
            ctx.response().setStatusCode(httpResponse.code).end()
        } else {
            ctx.response().setStatusCode(httpResponse.code).end(httpResponse.responseBody)
        }
    }
}
