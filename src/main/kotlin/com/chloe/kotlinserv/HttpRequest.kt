package com.chloe.kotlinserv

data class HttpRequest(
    val requestHeaders: Map<String, List<String>>,
    val body: String?,
    val queryParameters: Map<String, List<String>>
)
