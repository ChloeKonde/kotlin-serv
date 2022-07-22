package com.chloe.kotlinserv

import com.chloe.kotlinserv.vertx.VertxHttpServer
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

    val port = config.getInt("port")
    val myServer = VertxHttpServer()

    val geoDataBatchDelay = config.getLong("geoData.write.batch.delay")
    val ds = HikariDataSource(conf)
    val diModule = DiModule(geoDataBatchDelay, ds)

    myServer.start(port, listOf(diModule.getCountryStats(), diModule.postGeoData()))
}
