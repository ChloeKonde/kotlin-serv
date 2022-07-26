package com.chloe.kotlinserv

import com.chloe.kotlinserv.http.HttpRoute
import com.chloe.kotlinserv.http.HttpServer
import com.chloe.kotlinserv.route.GetCountryStatsRoute
import com.chloe.kotlinserv.route.PostGeoDataRoute
import com.chloe.kotlinserv.service.GeoDataService
import com.chloe.kotlinserv.service.GeoDataServiceImpl
import com.chloe.kotlinserv.vertx.VertxHttpServer
import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.Singleton
import com.google.inject.multibindings.Multibinder
import com.google.inject.name.Names
import com.typesafe.config.Config
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource

class DiModule(private val config: Config) : AbstractModule() {
    @Provides
    private fun getClickhouseDataSource(): HikariDataSource {
        val conf = HikariConfig()
        val url = config.getString("clickhouse.url")

        conf.jdbcUrl = url
        conf.driverClassName = "com.clickhouse.jdbc.ClickHouseDriver"
        conf.username = config.getString("clickhouse.user")
        conf.password = config.getString("clickhouse.password")

        return HikariDataSource(conf)
    }

    override fun configure() {
        val httpRoutes = Multibinder.newSetBinder(binder(), HttpRoute::class.java)
        httpRoutes.addBinding().to(GetCountryStatsRoute::class.java).`in`(Singleton::class.java)
        httpRoutes.addBinding().to(PostGeoDataRoute::class.java).`in`(Singleton::class.java)

        bind(HttpServer::class.java).to(VertxHttpServer::class.java).`in`(Singleton::class.java)
        bind(GeoDataService::class.java).to(GeoDataServiceImpl::class.java).`in`(Singleton::class.java)

        bindConstant().annotatedWith(Names.named("geoDataBatchDelay")).to(config.getLong("geoData.write.batch.delay"))
        bindConstant().annotatedWith(Names.named("databaseName")).to(config.getString("clickhouse.database"))
        bindConstant().annotatedWith(Names.named("tableName")).to(config.getString("clickhouse.table"))
    }
}
