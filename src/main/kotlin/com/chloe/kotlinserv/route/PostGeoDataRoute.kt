package com.chloe.kotlinserv.route

import com.chloe.kotlinserv.http.HttpMethod
import com.chloe.kotlinserv.http.HttpRequest
import com.chloe.kotlinserv.http.HttpResponse
import com.chloe.kotlinserv.http.HttpRoute
import com.chloe.kotlinserv.model.GeoData
import com.chloe.kotlinserv.writer.ClickhouseGeoDataWriterImpl
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.zaxxer.hikari.HikariDataSource

class PostGeoDataRoute(private val dataSource: HikariDataSource, private val geoDataBatchDelay: Long) : HttpRoute {
    override val endpoint = "/geodata"
    override val method = HttpMethod.POST

    override val processFunction = { request: HttpRequest ->
        val batch = ClickhouseGeoDataWriterImpl(ds = dataSource, geoDataBatchDelay = geoDataBatchDelay)
        val json = Gson()

        if (request.body == null) {
            HttpResponse(
                code = 400,
                responseBody = null,
                contentType = mapOf("content-type" to "text/plain")
            )
        } else {
            try {
                val data = json.fromJson(request.body, GeoData::class.java)
                val requestHeaders = request.requestHeaders["x-forwarded-for"]

                batch.addToList(data, requestHeaders?.firstOrNull())

                HttpResponse(
                    code = 200,
                    responseBody = null,
                    contentType = mapOf("content-type" to "application/json")
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
