package com.chloe.kotlinserv.http

data class HttpRoute(
    val endpoint: String,
    val method: HttpMethod,
    val processFunction: (request: HttpRequest) -> HttpResponse
)
