package com.chloe.kotlinserv.http

interface HttpRoute {
    val endpoint: String
    val method: HttpMethod
    fun process(request: HttpRequest): HttpResponse
}
