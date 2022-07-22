package com.chloe.kotlinserv.writer

import com.chloe.kotlinserv.model.GeoData

interface GeoDataWriter {
    fun addToList(geoData: GeoData, ipAddress: String?)
    fun flush()
}