package com.chloe.kotlinserv

import com.zaxxer.hikari.HikariDataSource
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

private data class FullData(val geoData: GeoData, val ipAddress: String?)

class ClickhouseGeoDataWriterImpl(private val ds: HikariDataSource, delay: Long) : GeoDataWriter {
    private val list = (mutableListOf<FullData>())
    private val query = "insert into chloe.events (timestamp, country, ipAddress, userId) values (?, ?, ?, ?)"

    init {
        val executor = Executors.newScheduledThreadPool(1)

        executor.scheduleAtFixedRate(
            { flush() },
            delay,
            delay,
            TimeUnit.SECONDS
        )
    }

    override fun addToList(geoData: GeoData, ipAddress: String?) {
        list.add(FullData(geoData, ipAddress))
    }

    override fun flush() {
        try {
            val connection = ds.connection
            connection.use {
                val st = connection.prepareStatement(query)

                for (el in list) {
                    println(el)
                    st.setLong(1, el.geoData.timestamp)
                    st.setString(2, el.geoData.country)
                    st.setString(3, el.ipAddress)
                    st.setString(4, el.geoData.userId)
                    st.addBatch()
                }
                st.executeBatch()
            }
        } catch (e: Exception) {
            println(e)
        }
        list.clear()
    }
}
