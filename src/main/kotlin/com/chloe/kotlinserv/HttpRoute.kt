package com.chloe.kotlinserv

data class HttpRoute(
    var path: String,
    var method: HttpMethod,
    var data: () -> HttpResponse
)
