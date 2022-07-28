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
import mu.KotlinLogging

private val logger = KotlinLogging.logger { }

class VertxHttpServer @Inject constructor(
    private val routes: Set<HttpRoute>
) : HttpServer {

    override fun start(port: Int) {
        logger.debug { "Start vertx server" }
        val vertx = Vertx.vertx()
        val httpServer = vertx.createHttpServer()
        val router = Router.router(vertx)

        routes.forEach { route: HttpRoute -> deployVertxRoute(route, router) }

        httpServer.requestHandler(router).listen(port)
        logger.debug { "Vertx server working on $port port" }
    }

    private fun deployVertxRoute(route: HttpRoute, router: Router) {
        logger.debug { "Start deploying vertx route $route" }
        router.route().handler(BodyHandler.create())

        if (route.method == HttpMethod.GET) {
            router.get(route.endpoint).handler { ctx ->
                val headers = getHeaders(ctx)
                val queryParams = getQueryParams(ctx)
                val httpResponse = route.processFunction(
                    HttpRequest(requestHeaders = headers, body = null, queryParameters = queryParams)
                )
                convertHttpResponse(ctx, httpResponse)
                logger.debug { "Finish deploying country stats route" }
            }
        } else {
            router.post(route.endpoint).handler { ctx ->
                val headers = getHeaders(ctx)
                val queryParams = getQueryParams(ctx)
                val body = ctx.body().asString()
                val httpResponse = route.processFunction(
                    HttpRequest(requestHeaders = headers, body = body, queryParameters = queryParams)
                )
                convertHttpResponse(ctx, httpResponse)
                logger.debug { "Finish deploying geo data route" }
            }
        }
    }

    private fun convertHttpResponse(ctx: RoutingContext, httpResponse: HttpResponse) {
        logger.debug { "Start converting http response" }
        if (httpResponse.responseBody == null) {
            ctx.response().setStatusCode(httpResponse.code).end()
        } else {
            ctx.response().setStatusCode(httpResponse.code).end(httpResponse.responseBody)
        }
        logger.debug { "Finish converting http response" }
    }

    private fun getHeaders(ctx: RoutingContext): Map<String, List<String>> {
        logger.debug { "Getting headers from routing context" }
        return ctx.request().headers().map { it.key to it.value }.groupBy({ it.first }, { it.second }).toMap()
    }

    private fun getQueryParams(ctx: RoutingContext): Map<String, List<String>> {
        logger.debug { "Getting query parameters from routing context" }
        return ctx.queryParams().map { it.key to it.value }.groupBy({ it.first }, { it.second }).toMap()
    }
}
