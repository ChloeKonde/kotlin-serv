package com.chloe.kotlinserv

interface GeoDataWriter {
    fun addToList(geoData: GeoData, ipAddress: String?)
    fun flush()
}
