package com.chloe.kotlinserv.utils

import com.chloe.kotlinserv.model.CountryStats
import com.chloe.kotlinserv.model.GeoData
import com.google.gson.Gson

val json = Gson()

fun String.fromJson(): GeoData {
    return json.fromJson(this, GeoData::class.java)
}

fun List<CountryStats>.toJson(): String {
    return json.toJson(this)
}
