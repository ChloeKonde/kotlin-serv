package com.chloe.kotlinserv.vertx

import com.chloe.kotlinserv.http.HttpMethod
import com.chloe.kotlinserv.http.HttpRequest
import com.chloe.kotlinserv.http.HttpResponse
import com.chloe.kotlinserv.http.HttpRoute
import com.chloe.kotlinserv.http.HttpServer
import com.google.inject.Inject
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler

class VertxHttpServer @Inject constructor(
    private val routes: Set<HttpRoute>
) : HttpServer {

    override fun start(port: Int) {
        val vertx = Vertx.vertx()
        val httpServer = vertx.createHttpServer()
        val router = Router.router(vertx)

        routes.forEach { route: HttpRoute -> deployVertxRoute(route, router) }

        httpServer.requestHandler(router).listen(port)
    }

    private fun deployVertxRoute(route: HttpRoute, router: Router) {
        router.route().handler(BodyHandler.create())

        if (route.method == HttpMethod.GET) {
            router.get(route.endpoint).handler { ctx ->
                val headers = getHeaders(ctx)
                val queryParams = getQueryParams(ctx)
                val httpResponse = route.processFunction.invoke(
                    HttpRequest(requestHeaders = headers, body = null, queryParameters = queryParams)
                )
                convertHttpResponse(ctx, httpResponse)
            }
        } else {
            router.post(route.endpoint).handler { ctx ->
                val headers = getHeaders(ctx)
                val queryParams = getQueryParams(ctx)
                val body = ctx.body().asString()
                val httpResponse = route.processFunction.invoke(
                    HttpRequest(requestHeaders = headers, body = body, queryParameters = queryParams)
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

    private fun getHeaders(ctx: RoutingContext): Map<String, List<String>> {
        return ctx.request().headers().map { it.key to it.value }.groupBy({ it.first }, { it.second }).toMap()
    }

    private fun getQueryParams(ctx: RoutingContext): Map<String, List<String>> {
        return ctx.queryParams().map { it.key to it.value }.groupBy({ it.first }, { it.second }).toMap()
    }
}
