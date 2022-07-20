package com.chloe.kotlinserv

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.typesafe.config.ConfigFactory
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.io.File

fun main(args: Array<String>) {
    val file = File(args[0])
    val config = ConfigFactory.parseFile(file)

    val url = config.getString("clickhouse.url")

    val conf = HikariConfig()
    conf.jdbcUrl = url
    conf.driverClassName = "com.clickhouse.jdbc.ClickHouseDriver"
    conf.username = config.getString("clickhouse.user")
    conf.password = config.getString("clickhouse.password")

    val ds = HikariDataSource(conf)

    val port = config.getInt("port")
    val geoDataBatchDelay = config.getLong("geoData.write.batch.delay")
    val myServer = VertxHttpServer()
    val json = Gson()

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

                val list = mutableListOf<RawResponseFromClickhouse>()
                while (result.next()) {
                    list.add(
                        RawResponseFromClickhouse(
                            timestamp = result.getString(1),
                            country = result.getString(2),
                        )
                    )
                }

                val groupingResult = list.groupingBy { it.timestamp to it.country }.eachCount().toList()
                val data = mutableListOf<CountryStats>()

                groupingResult.forEach {
                    data.add(CountryStats(it.first.first, it.first.second, it.second))
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
                            data = result.getString(2),
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

    myServer.start(port, listOf(getRoute, postRoute))
}
