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
                val data = buildHttpResponse(ctx)
                val httpResponse = route.processFunction.invoke(
                    HttpRequest(requestHeaders = data[0], body = null, queryParameters = data[1])
                )
                convertHttpResponse(ctx, httpResponse)
            }
        } else {
            router.post(route.endpoint).handler { ctx ->
                val data = buildHttpResponse(ctx)
                val body = ctx.body().asString()
                val httpResponse = route.processFunction.invoke(
                    HttpRequest(requestHeaders = data[0], body = body, queryParameters = data[1])
                )
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

    private fun buildHttpResponse(ctx: RoutingContext): List<Map<String, List<String>>> {
        val queryParam =
            ctx.queryParams().map { it.key to it.value }.groupBy({ it.first }, { it.second }).toMap()
        val headers =
            ctx.request().headers().map { it.key to it.value }.groupBy({ it.first }, { it.second }).toMap()

        val result = mutableListOf<Map<String, List<String>>>()

        result.add(headers)
        result.add(queryParam)

        return result
    }
}
