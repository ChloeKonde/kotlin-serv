package com.chloe.kotlinserv

import com.zaxxer.hikari.HikariDataSource

class Batch(private val ds: HikariDataSource, private val query: String) : Runnable {
    private val list = (mutableListOf <FullData>())

    fun addToList(geoData: GeoData, ipAddress: String?) {
        list.add(FullData(geoData, ipAddress))
    }

    override fun run() {
        val connection = ds.connection
        connection.use {
            val st = connection.prepareStatement(query)

            for (el in list) {
                st.setLong(1, el.geoData.timestamp)
                st.setString(2, el.geoData.country)
                st.setString(3, el.ipAddress)
                st.setString(4, el.geoData.userId)
                st.addBatch()
            }
            st.executeBatch()
        }
    }
}
