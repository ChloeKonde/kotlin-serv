package com.chloe.kotlinserv.service

import com.chloe.kotlinserv.model.CountryStats
import com.chloe.kotlinserv.model.GeoData

interface GeoDataService {
    fun addToList(geoData: GeoData, ipAddress: String?)

    fun flush()

    fun retrieveCountryStats(startDate: String, endDate: String, groupLocal: Boolean): List<CountryStats>
}
