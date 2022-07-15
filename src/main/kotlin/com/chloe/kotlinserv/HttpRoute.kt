package com.chloe.kotlinserv

data class HttpRoute(
    val endpoint: String,
    val method: HttpMethod,
    val processFunction: (request: HttpRequest) -> HttpResponse
)
