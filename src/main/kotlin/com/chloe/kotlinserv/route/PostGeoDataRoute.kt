package com.chloe.kotlinserv.route

import com.chloe.kotlinserv.http.HttpMethod
import com.chloe.kotlinserv.http.HttpRequest
import com.chloe.kotlinserv.http.HttpResponse
import com.chloe.kotlinserv.http.HttpRoute
import com.chloe.kotlinserv.service.GeoDataServiceImpl
import com.chloe.kotlinserv.utils.fromJson
import com.google.gson.JsonSyntaxException
import com.google.inject.Inject

class PostGeoDataRoute @Inject constructor(
    private val geoDataServiceImpl: GeoDataServiceImpl,
) : HttpRoute {
    override val endpoint = "/geodata"
    override val method = HttpMethod.POST

    override val processFunction = { request: HttpRequest ->
        if (request.body == null) {
            HttpResponse(
                code = 400,
                responseBody = null,
                contentType = mapOf("content-type" to "text/plain")
            )
        } else {
            try {
                val data = request.body.fromJson()
                val requestHeaders = request.requestHeaders["x-forwarded-for"]

                geoDataServiceImpl.addToList(data, requestHeaders?.firstOrNull())

                HttpResponse(
                    code = 200,
                    responseBody = null,
                    contentType = mapOf("content-type" to "application/com.chloe.kotlinserv.utils.getJson")
                )
            } catch (e: JsonSyntaxException) {
                HttpResponse(
                    code = 400,
                    responseBody = null,
                    contentType = mapOf("content-type" to "text/plain")
                )
            }
        }
    }
}
