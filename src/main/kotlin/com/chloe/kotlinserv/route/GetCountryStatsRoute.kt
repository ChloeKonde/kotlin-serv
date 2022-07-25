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
    override val endpoint = "/countrystats"
    override val method = HttpMethod.GET

    override val processFunction = { request: HttpRequest ->
        val groupLocal =
            request.queryParameters
                .getValue("groupLocal")
                .firstOrNull() ?: throw IllegalArgumentException("groupLocal can't be null")

        val startDate = request.queryParameters
            .getValue("startDate")
            .firstOrNull() ?: throw IllegalArgumentException("startDate can't be null")

        val endDate = request.queryParameters
            .getValue("endDate")
            .firstOrNull() ?: throw IllegalArgumentException("endDate can't be null")

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
    }
}
