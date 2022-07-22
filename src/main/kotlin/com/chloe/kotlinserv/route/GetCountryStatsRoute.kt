package com.chloe.kotlinserv.route

import com.chloe.kotlinserv.http.HttpMethod
import com.chloe.kotlinserv.http.HttpRequest
import com.chloe.kotlinserv.http.HttpResponse
import com.chloe.kotlinserv.http.HttpRoute
import com.chloe.kotlinserv.model.CountryStats
import com.google.gson.Gson
import com.google.inject.Inject
import com.zaxxer.hikari.HikariDataSource

class GetCountryStatsRoute @Inject constructor(private val dataSource: HikariDataSource) : HttpRoute {
    override val endpoint = "/countrystats"
    override val method = HttpMethod.GET

    override val processFunction = { request: HttpRequest ->
        val json = Gson()
        val connection = dataSource.connection
        val groupLocal = request.queryParameters.getValue("groupLocal").firstOrNull()
        val startDate = request.queryParameters.getValue("startDate").firstOrNull()
        val endDate = request.queryParameters.getValue("endDate").firstOrNull()

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
}
