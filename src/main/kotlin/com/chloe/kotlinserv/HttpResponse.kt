package com.chloe.kotlinserv

typealias Headers = Map<String, String>

data class HttpResponse(
    val code: Int,
    val responseBody: String,
    val contentType: Headers
)
