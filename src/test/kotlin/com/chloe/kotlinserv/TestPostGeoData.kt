package com.chloe.kotlinserv

import com.chloe.kotlinserv.http.HttpRequest
import com.chloe.kotlinserv.http.HttpResponse
import com.chloe.kotlinserv.model.GeoData
import com.chloe.kotlinserv.route.PostGeoDataRoute
import com.chloe.kotlinserv.service.GeoDataServiceImpl
import io.mockk.confirmVerified
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class TestPostGeoData {
    private val serviceImpl = mockk<GeoDataServiceImpl>()
    private val postGeoDataRoute = PostGeoDataRoute(serviceImpl)

    @Test
    fun testProcessFunction_WhenHttpRequestWithEmptyBody_ShouldReturn400CodeWithoutBody() {
        justRun {
            serviceImpl.addToList(any(), any())
        }
        val httpRequest = HttpRequest(
            requestHeaders = mapOf("x-forwarded-for" to listOf("192.168.50.10")),
            body = null,
            queryParameters = mapOf()
        )
        val expectedHttpResponse = HttpResponse(
            code = 400,
            responseBody = null,
            contentType = mapOf()
        )

        val result = postGeoDataRoute.process(httpRequest)

        assertEquals(expectedHttpResponse.code, result.code)
        assertEquals(expectedHttpResponse.responseBody, result.responseBody)
        assertEquals(expectedHttpResponse.contentType, result.contentType)
        confirmVerified()
    }

    @Test
    fun testProcessFunction_WhenHttpRequest_ShouldReturn200WithoutBody() {
        justRun {
            serviceImpl.addToList(any(), any())
        }
        val geoDataObj = GeoData(country = "eng", timestamp = 4549055295, userId = "user0")
        val geoDataString = """{"country":"eng","timestamp":4549055295,"userId":"user0"}"""
        val ipAddress = "192.168.50.10"
        val httpRequest = HttpRequest(
            requestHeaders = mapOf("x-forwarded-for" to listOf(ipAddress)),
            body = geoDataString,
            queryParameters = mapOf()
        )
        val expectedHttpResponse = HttpResponse(
            code = 200,
            responseBody = null,
            contentType = mapOf()
        )

        val result = postGeoDataRoute.process(httpRequest)

        verify {
            serviceImpl.addToList(geoDataObj, ipAddress)
        }
        assertEquals(expectedHttpResponse.code, result.code)
        assertEquals(expectedHttpResponse.responseBody, result.responseBody)
        assertEquals(expectedHttpResponse.contentType, result.contentType)
        confirmVerified()
    }

    @Test
    fun testProcessFunction_WhenHttpRequestWithIncorrectJson_ShouldReturn400WithoutBody() {
        justRun {
            serviceImpl.addToList(any(), any())
        }
        val ipAddress = "192.168.50.10"
        val httpRequest = HttpRequest(
            requestHeaders = mapOf("x-forwarded-for" to listOf(ipAddress)),
            body = """{"country":,"timestamp":4549055295,"userId":"user0"}""",
            queryParameters = mapOf()
        )
        val expectedHttpResponse = HttpResponse(
            code = 400,
            responseBody = null,
            contentType = mapOf()
        )

        val result = postGeoDataRoute.process(httpRequest)

        assertEquals(expectedHttpResponse.code, result.code)
        assertEquals(expectedHttpResponse.responseBody, result.responseBody)
        assertEquals(expectedHttpResponse.contentType, result.contentType)
        confirmVerified()
    }

    @Test
    fun testProcessFunction_WhenHttpRequest_ShouldReturn500CodeWithoutBody() {
        justRun {
            serviceImpl.addToList(any(), any())
        }
        val ipAddress = "192.168.50.10"
        val httpRequest = HttpRequest(
            requestHeaders = mapOf("x-forwarded-for" to listOf(ipAddress)),
            body = "",
            queryParameters = mapOf()
        )
        val expectedHttpResponse = HttpResponse(
            code = 500,
            responseBody = null,
            contentType = mapOf()
        )

        val result = postGeoDataRoute.process(httpRequest)

        assertEquals(expectedHttpResponse.code, result.code)
        assertEquals(expectedHttpResponse.responseBody, result.responseBody)
        assertEquals(expectedHttpResponse.contentType, result.contentType)
        confirmVerified()
    }
}
