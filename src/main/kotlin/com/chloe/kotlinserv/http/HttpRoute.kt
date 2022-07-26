package com.chloe.kotlinserv.http

interface HttpRoute {
    val endpoint: String
    val method: HttpMethod
    val processFunction: (request: HttpRequest) -> HttpResponse
}
