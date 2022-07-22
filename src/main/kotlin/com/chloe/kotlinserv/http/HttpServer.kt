package com.chloe.kotlinserv.http

interface HttpServer {
    fun start(port: Int, routes: List<HttpRoute>)
}
