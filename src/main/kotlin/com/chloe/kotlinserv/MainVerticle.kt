package com.chloe.kotlinserv

import com.google.gson.Gson
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
    val myServer = VertxHttpServer()
    val json = Gson()

    val getRoute = HttpRoute("/countrystats", HttpMethod.GET) {
        val connection = ds.connection

        connection.use {
            val statement =
                connection.prepareStatement("SELECT country, toDate(timestamp) as time, count(country) FROM chloe.events group by country, timestamp")
            val result = statement.executeQuery()

            val list = mutableListOf<CountryStats>()
            while (result.next()) {
                list.add(
                    CountryStats(
                        data = result.getString(1),
                        country = result.getString(2),
                        count = result.getInt(3)
                    )
                )
            }
            HttpResponse(
                code = 200,
                responseBody = json.toJson(list),
                contentType = mapOf("content-type" to "application/json")
            )
        }
    }

    val postRoute = HttpRoute("/geodata", HttpMethod.POST) {
        try {
            val data = json.fromJson(it.body, GeoData::class.java)
            val connection = ds.connection
            val requestHeaders = it.requestHeaders["x-forwarded-for"]
            connection.use {
                val statement =
                    connection.prepareStatement("insert into chloe.events values (${data.timestamp}, '${data.country}', '${requestHeaders?.firstOrNull()}', '${data.userId}')")
                statement.executeQuery()
                HttpResponse(
                    code = 200,
                    responseBody = null,
                    contentType = mapOf("content-type" to "application/json")
                )
            }
        }  catch (e: Exception) {
            HttpResponse(
                code = 500,
                responseBody = "Internal server error",
                contentType = mapOf("content-type" to "application/json")
            )
        }
    }

    myServer.start(port, listOf(getRoute, postRoute))
}
