package com.chloe.kotlinserv.service

import com.chloe.kotlinserv.model.CountryStats
import com.chloe.kotlinserv.model.GeoData
import com.google.inject.Inject
import com.google.inject.name.Named
import com.zaxxer.hikari.HikariDataSource
import mu.KotlinLogging
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

private data class GeoIpData(val geoData: GeoData, val ipAddress: String?)

private val logger = KotlinLogging.logger { }

class GeoDataServiceImpl @Inject constructor(
    private val ds: HikariDataSource,
    @Named("geoDataBatchDelay") private val geoDataBatchDelay: Long,
    @Named("tableName") private val tableName: String,
    @Named("databaseName") private val dbName: String
) : GeoDataService {
    private val list: MutableList<GeoIpData> = mutableListOf()
    private val lock: Any = Object()
    private val query: String =
        "insert into $dbName.$tableName (timestamp, country, ipAddress, userId) values (?, ?, ?, ?)"

    init {
        logger.debug { "Init scheduled thread pool with executor" }
        val executor = Executors.newScheduledThreadPool(1)

        executor.scheduleAtFixedRate(
            { flush() },
            geoDataBatchDelay,
            geoDataBatchDelay,
            TimeUnit.SECONDS
        )
    }

    override fun addToList(geoData: GeoData, ipAddress: String?) {
        synchronized(lock) {
            logger.debug { "Adding to list $geoData with ip: $ipAddress" }
            list.add(GeoIpData(geoData, ipAddress))
        }
    }

    override fun flush() {
        var tmp: List<GeoIpData>
        synchronized(lock) {
            tmp = list.toList()
            list.clear()
        }
        tmp.takeIf { it.isNotEmpty() }?.let {
            logger.debug { "Start flush to clickhouse" }
            try {
                val connection = ds.connection
                connection.use {
                    val st = connection.prepareStatement(query)

                    for (el in tmp) {
                        st.setLong(1, el.geoData.timestamp)
                        st.setString(2, el.geoData.country)
                        st.setString(3, el.ipAddress)
                        st.setString(4, el.geoData.userId)
                        st.addBatch()
                    }
                    st.executeBatch()
                    logger.debug { "Finish flushing to clickhouse, added ${tmp.count()} elements" }
                }
            } catch (e: Exception) {
                logger.error(e) { "Can't save data to clickhouse" }
            }
        }
    }

    override fun retrieveCountryStats(startDate: String, endDate: String, groupLocal: Boolean): List<CountryStats> {
        return if (groupLocal) {
            groupLocal(startDate, endDate)
        } else {
            groupNonLocal(startDate, endDate)
        }
    }

    private fun groupLocal(startDate: String, endDate: String): List<CountryStats> {
        logger.debug { "Start grouping local" }
        val connection = ds.connection

        val statement = connection.prepareStatement(
            "SELECT toDate(timestamp), country from $dbName.$tableName " +
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
        logger.debug { "Finish grouping local" }
        return data
    }

    private fun groupNonLocal(startDate: String, endDate: String): List<CountryStats> {
        logger.debug { "Start grouping in db" }

        val connection = ds.connection

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
            logger.debug { "Finish grouping in db" }
            return list
        }
    }
}
