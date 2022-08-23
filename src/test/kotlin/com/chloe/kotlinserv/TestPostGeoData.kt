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
    @Test
    fun testProcessFunction_WhenHttpRequestWithEmptyBody_ShouldReturn400CodeWithoutBody() {
        val serviceImpl = mockk<GeoDataServiceImpl>()
        justRun {
            serviceImpl.addToList(any(), any())
        }
        val postGeoDataRoute = PostGeoDataRoute(serviceImpl)

        val httpRequest = HttpRequest(
            requestHeaders = mapOf("x-forwarded-for" to listOf("192.168.50.10")),
            body = null,
            queryParameters = mapOf()
        )
        val result = postGeoDataRoute.process(httpRequest)

        val expectedHttpResponse = HttpResponse(
            code = 400,
            responseBody = null,
            contentType = mapOf()
        )
        assertEquals(expectedHttpResponse.code, result.code)
        assertEquals(expectedHttpResponse.responseBody, result.responseBody)
        assertEquals(expectedHttpResponse.contentType, result.contentType)
        confirmVerified()
    }

    @Test
    fun testProcessFunction_WhenHttpRequest_ShouldReturn200WithoutBody() {
        val serviceImpl = mockk<GeoDataServiceImpl>()
        justRun {
            serviceImpl.addToList(any(), any())
        }
        val postGeoDataRoute = PostGeoDataRoute(serviceImpl)

        val geoDataObj = GeoData(country = "eng", timestamp = 4549055295, userId = "user0")
        val geoDataString = """{"country":"eng","timestamp":4549055295,"userId":"user0"}"""
        val ipAddress = "192.168.50.10"
        val httpRequest = HttpRequest(
            requestHeaders = mapOf("x-forwarded-for" to listOf(ipAddress)),
            body = geoDataString,
            queryParameters = mapOf()
        )
        val result = postGeoDataRoute.process(httpRequest)
        verify {
            serviceImpl.addToList(geoDataObj, ipAddress)
        }

        val expectedHttpResponse = HttpResponse(
            code = 200,
            responseBody = null,
            contentType = mapOf()
        )
        assertEquals(expectedHttpResponse.code, result.code)
        assertEquals(expectedHttpResponse.responseBody, result.responseBody)
        assertEquals(expectedHttpResponse.contentType, result.contentType)
        confirmVerified()
    }

    @Test
    fun testProcessFunction_WhenHttpRequestWithIncorrectJson_ShouldReturn400WithoutBody() {
        val serviceImpl = mockk<GeoDataServiceImpl>()
        justRun {
            serviceImpl.addToList(any(), any())
        }
        val postGeoDataRoute = PostGeoDataRoute(serviceImpl)

        val ipAddress = "192.168.50.10"
        val httpRequest = HttpRequest(
            requestHeaders = mapOf("x-forwarded-for" to listOf(ipAddress)),
            body = """{"country":,"timestamp":4549055295,"userId":"user0"}""",
            queryParameters = mapOf()
        )
        val result = postGeoDataRoute.process(httpRequest)

        val expectedHttpResponse = HttpResponse(
            code = 400,
            responseBody = null,
            contentType = mapOf()
        )
        assertEquals(expectedHttpResponse.code, result.code)
        assertEquals(expectedHttpResponse.responseBody, result.responseBody)
        assertEquals(expectedHttpResponse.contentType, result.contentType)
        confirmVerified()
    }

    @Test
    fun testProcessFunction_WhenHttpRequest_ShouldReturn500CodeWithoutBody() {
        val serviceImpl = mockk<GeoDataServiceImpl>()
        justRun {
            serviceImpl.addToList(any(), any())
        }
        val postGeoDataRoute = PostGeoDataRoute(serviceImpl)

        val ipAddress = "192.168.50.10"
        val httpRequest = HttpRequest(
            requestHeaders = mapOf("x-forwarded-for" to listOf(ipAddress)),
            body = "",
            queryParameters = mapOf()
        )
        val result = postGeoDataRoute.process(httpRequest)

        val expectedHttpResponse = HttpResponse(
            code = 500,
            responseBody = null,
            contentType = mapOf()
        )
        assertEquals(expectedHttpResponse.code, result.code)
        assertEquals(expectedHttpResponse.responseBody, result.responseBody)
        assertEquals(expectedHttpResponse.contentType, result.contentType)
        confirmVerified()
    }
}
