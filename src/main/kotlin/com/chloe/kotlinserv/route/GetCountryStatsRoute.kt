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
        val groupLocal = request.queryParameters.getValue("groupLocal").firstOrNull()
        val startDate = request.queryParameters.getValue("startDate").firstOrNull()
        val endDate = request.queryParameters.getValue("endDate").firstOrNull()

        if (groupLocal == "true") {
            val data = geoDataServiceImpl.retrieveCountryStatsLocal(startDate, endDate)

            HttpResponse(
                code = 200,
                responseBody = data.toJson(),
                contentType = mapOf("content-type" to "application/com.chloe.kotlinserv.utils.getJson")
            )
        } else {
            val list = geoDataServiceImpl.retrieveCountryStatsNonLocal(startDate, endDate)
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
                    contentType = mapOf("content-type" to "application/com.chloe.kotlinserv.utils.getJson")
                )
            }
        }
    }
}
