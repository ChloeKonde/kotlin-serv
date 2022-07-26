package com.chloe.kotlinserv.http

data class HttpRequest(
    val requestHeaders: Map<String, List<String>>,
    val body: String?,
    val queryParameters: Map<String, List<String>>
)
