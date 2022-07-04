package com.chloe.kotlinserv

typealias Header = Map<String, String>

data class HttpResponse(
    val code: String,
    val responseBody: String,
    val contentType: Header
)
