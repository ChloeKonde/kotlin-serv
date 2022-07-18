package com.chloe.kotlinserv

import com.google.gson.Gson
import com.typesafe.config.ConfigFactory
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

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
                connection.prepareStatement("SELECT country, toDate(timestamp) as time, count(country) FROM chloe.events WHERE country='user' group by country, toDate(timestamp)")
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

            if (list.isEmpty()) {
                HttpResponse(
                    code = 204,
                    responseBody = "No content",
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

    val query = "insert into chloe.events (timestamp, country, ipAddress, userId) values (?, ?, ?, ?)"
    val batch = Batch(ds = ds, query = query)

    val postRoute = HttpRoute("/geodata", HttpMethod.POST) {
        try {
            val data = json.fromJson(it.body, GeoData::class.java)
            val requestHeaders = it.requestHeaders["x-forwarded-for"]

            batch.addToList(data, requestHeaders?.firstOrNull())

            HttpResponse(
                code = 200,
                responseBody = null,
                contentType = mapOf("content-type" to "application/json")
            )
        } catch (e: java.lang.NullPointerException) {
            HttpResponse(
                code = 400,
                responseBody = "Bad request",
                contentType = mapOf("content-type" to "text/plain")
            )
        }
    }
    myServer.start(port, listOf(getRoute, postRoute))

    val executor = Executors.newScheduledThreadPool(1)
    val runnable = Runnable { batch.run() }

    executor.schedule(
        runnable,
        20000,
        TimeUnit.MILLISECONDS
    )
}
