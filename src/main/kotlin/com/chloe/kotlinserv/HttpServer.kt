package com.chloe.kotlinserv

interface HttpServer {
    fun start(port: Int, routes: List<HttpRoute>)
}
