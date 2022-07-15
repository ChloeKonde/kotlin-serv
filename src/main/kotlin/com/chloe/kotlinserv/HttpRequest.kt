package com.chloe.kotlinserv

import io.vertx.core.MultiMap

data class HttpRequest(
    val requestHeaders: Map<String, List<String>>,
    val body: String?,
    val queryParameters: Map<String, List<String>>
)