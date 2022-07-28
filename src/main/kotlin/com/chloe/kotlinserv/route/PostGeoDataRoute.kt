package com.chloe.kotlinserv.route

import com.chloe.kotlinserv.http.HttpMethod
import com.chloe.kotlinserv.http.HttpRequest
import com.chloe.kotlinserv.http.HttpResponse
import com.chloe.kotlinserv.http.HttpRoute
import com.chloe.kotlinserv.service.GeoDataServiceImpl
import com.chloe.kotlinserv.utils.fromJson
import com.google.gson.JsonSyntaxException
import com.google.inject.Inject
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class PostGeoDataRoute @Inject constructor(
    private val geoDataServiceImpl: GeoDataServiceImpl,
) : HttpRoute {
    override val endpoint: String = "/geodata"
    override val method: HttpMethod = HttpMethod.POST

    override fun processFunction(request: HttpRequest): HttpResponse {
        if (request.body == null) {
            return HttpResponse(
                code = 400,
                responseBody = null,
                contentType = mapOf("content-type" to "text/plain")
            )
        } else {
            try {
                val data = request.body.fromJson()
                val requestHeaders = request.requestHeaders["x-forwarded-for"]

                geoDataServiceImpl.addToList(data, requestHeaders?.firstOrNull())

                return HttpResponse(
                    code = 200,
                    responseBody = null,
                    contentType = mapOf("content-type" to "application/json")
                )
            } catch (e: JsonSyntaxException) {
                logger.error(e) { "Geo data couldn't be parsed" }
                return HttpResponse(
                    code = 400,
                    responseBody = null,
                    contentType = mapOf("content-type" to "text/plain")
                )
            }
        }
    }
}
