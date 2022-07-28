package com.chloe.kotlinserv.http

interface HttpRoute {
    val endpoint: String
    val method: HttpMethod
    fun processFunction(request: HttpRequest): HttpResponse
}
