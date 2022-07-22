package com.chloe.kotlinserv

import com.chloe.kotlinserv.http.*
import com.chloe.kotlinserv.route.GetCountryStatsRoute
import com.chloe.kotlinserv.route.PostGeoDataRoute
import com.chloe.kotlinserv.vertx.VertxHttpServer
import com.typesafe.config.Config
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource

class DiModule(private val config: Config) {
    private val geoDataBatchDelay = config.getLong("geoData.write.batch.delay")

    private fun getClickhouseDataSource(): HikariDataSource {
        val conf = HikariConfig()
        val url = config.getString("clickhouse.url")

        conf.jdbcUrl = url
        conf.driverClassName = "com.clickhouse.jdbc.ClickHouseDriver"
        conf.username = config.getString("clickhouse.user")
        conf.password = config.getString("clickhouse.password")

        return HikariDataSource(conf)
    }

    fun getHttpServer(): HttpServer {
        return VertxHttpServer()
    }

    fun getCountryStats(request: HttpRequest): HttpResponse {
        val getRoute = GetCountryStatsRoute(getClickhouseDataSource())
        return getRoute.processFunction.invoke(request)
    }

    fun postGeoData(request: HttpRequest): HttpResponse {
        val postRoute = PostGeoDataRoute(
            dataSource = getClickhouseDataSource(),
            geoDataBatchDelay = geoDataBatchDelay,
        )

        return postRoute.processFunction.invoke(request)
    }

}
