package com.chloe.kotlinserv

import com.chloe.kotlinserv.http.HttpMethod
import com.chloe.kotlinserv.http.HttpResponse
import com.chloe.kotlinserv.http.HttpRoute
import com.chloe.kotlinserv.model.CountryStats
import com.chloe.kotlinserv.model.GeoData
import com.chloe.kotlinserv.writer.ClickhouseGeoDataWriterImpl
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.zaxxer.hikari.HikariDataSource

class DiModule(private val geoDataBatchDelay: Long, private val ds: HikariDataSource) {
    private val json = Gson()

    fun getCountryStats(): HttpRoute {
        val getRoute = HttpRoute("/countrystats", HttpMethod.GET) {
            val connection = ds.connection
            val groupLocal = it.queryParameters.getValue("groupLocal").firstOrNull()
            val startDate = it.queryParameters.getValue("startDate").firstOrNull()
            val endDate = it.queryParameters.getValue("endDate").firstOrNull()

            if (groupLocal == "true") {
                connection.use {
                    val statement = connection.prepareStatement(
                        "SELECT toDate(timestamp), country from chloe.events " +
                            "where toDate(timestamp) > ? and toDate(timestamp) < ?"
                    )

                    statement.setString(1, startDate)
                    statement.setString(2, endDate)

                    val result = statement.executeQuery()

                    val list = mutableListOf<CountryStats>()
                    while (result.next()) {
                        list.add(
                            CountryStats(
                                date = result.getString(1),
                                country = result.getString(2),
                                count = 1
                            )
                        )
                    }

                    val groupingResult = list.groupingBy { countryStats ->
                        countryStats.date to countryStats.country
                    }.reduce { _: Pair<String, String>, accumulator: CountryStats, element: CountryStats ->
                        CountryStats(
                            date = accumulator.date,
                            country = accumulator.country,
                            count = accumulator.count + element.count
                        )
                    }.toList()

                    val data = mutableListOf<CountryStats>()

                    groupingResult.forEach { tmp ->
                        data.add(tmp.second)
                    }

                    HttpResponse(
                        code = 200,
                        responseBody = json.toJson(data),
                        contentType = mapOf("content-type" to "application/json")
                    )
                }
            } else {
                connection.use {
                    val statement =
                        connection.prepareStatement(
                            "SELECT country, toDate(timestamp) as time, count(country) " +
                                "FROM chloe.events WHERE toDate(timestamp) > ? AND toDate(timestamp) < ?" +
                                " GROUP BY country, toDate(timestamp)"
                        )

                    statement.setString(1, startDate)
                    statement.setString(2, endDate)

                    val result = statement.executeQuery()

                    val list = mutableListOf<CountryStats>()
                    while (result.next()) {
                        list.add(
                            CountryStats(
                                date = result.getString(2),
                                country = result.getString(1),
                                count = result.getInt(3)
                            )
                        )
                    }

                    if (list.isEmpty()) {
                        HttpResponse(
                            code = 204,
                            responseBody = null,
                            contentType = mapOf("content-type" to "text/plain")
                        )
                    } else {
                        HttpResponse(
                            code = 200,
                            responseBody = json.toJson(list),
                            contentType = mapOf("content-type" to "application/json")
                        )
                    }
                }
            }
        }
        return getRoute
    }

    fun postGeoData(): HttpRoute {
        val batch = ClickhouseGeoDataWriterImpl(ds = ds, geoDataBatchDelay = geoDataBatchDelay)

        val postRoute = HttpRoute("/geodata", HttpMethod.POST) {
            if (it.body == null) {
                HttpResponse(
                    code = 400,
                    responseBody = null,
                    contentType = mapOf("content-type" to "text/plain")
                )
            } else {
                try {
                    val data = json.fromJson(it.body, GeoData::class.java)
                    val requestHeaders = it.requestHeaders["x-forwarded-for"]

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
        return postRoute
    }
}
