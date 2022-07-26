package com.chloe.kotlinserv.route

import com.chloe.kotlinserv.http.HttpMethod
import com.chloe.kotlinserv.http.HttpRequest
import com.chloe.kotlinserv.http.HttpResponse
import com.chloe.kotlinserv.http.HttpRoute
import com.chloe.kotlinserv.service.GeoDataServiceImpl
import com.chloe.kotlinserv.utils.toJson
import com.google.inject.Inject

class GetCountryStatsRoute @Inject constructor(
    private val geoDataServiceImpl: GeoDataServiceImpl
) : HttpRoute {
    override val endpoint: String = "/countrystats"
    override val method: HttpMethod = HttpMethod.GET

    private fun HttpRequest.getQueryParameter(key: String): String {
        return this.queryParameters[key]?.firstOrNull() ?: throw IllegalArgumentException("$key can't be null")
    }

    override val processFunction = { request: HttpRequest ->
        try {
            val groupLocal = request.getQueryParameter("groupLocal")

            val startDate = request.getQueryParameter("startDate")

            val endDate = request.getQueryParameter("endDate")

            if (groupLocal == "true") {
                val data = geoDataServiceImpl.retrieveCountryStats(startDate, endDate, groupLocal.toBoolean())

                HttpResponse(
                    code = 200,
                    responseBody = data.toJson(),
                    contentType = mapOf("content-type" to "application/json")
                )
            } else {
                val list = geoDataServiceImpl.retrieveCountryStats(startDate, endDate, groupLocal.toBoolean())
                if (list.isEmpty()) {
                    HttpResponse(
                        code = 204,
                        responseBody = null,
                        contentType = mapOf("content-type" to "text/plain")
                    )
                } else {
                    HttpResponse(
                        code = 200,
                        responseBody = list.toJson(),
                        contentType = mapOf("content-type" to "application/json")
                    )
                }
            }
        } catch (e: IllegalArgumentException) {
            println(e)
            HttpResponse(302, null, mapOf("content-type" to "application/json"))
        }
    }
}
