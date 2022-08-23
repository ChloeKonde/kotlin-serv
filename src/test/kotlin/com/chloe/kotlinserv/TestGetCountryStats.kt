package com.chloe.kotlinserv

import com.chloe.kotlinserv.http.HttpRequest
import com.chloe.kotlinserv.http.HttpResponse
import com.chloe.kotlinserv.model.CountryStats
import com.chloe.kotlinserv.route.GetCountryStatsRoute
import com.chloe.kotlinserv.service.GeoDataServiceImpl
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import kotlin.test.assertEquals

class TestGetCountryStats {
    private val serviceImpl = mockk<GeoDataServiceImpl>()
    private val getCountryStatsRoute = GetCountryStatsRoute(serviceImpl)

    @Test
    fun testProcessFunction_WhenServiceRespondWithGroupedLocalData_ShouldReturn200CodeWithBody() {
        every {
            serviceImpl.retrieveCountryStats(
                startDate = "1972-01-01",
                endDate = "1990-07-15",
                groupLocal = true
            )
        } returns listOf(
            CountryStats("1990-04-12", "ENG", 2),
            CountryStats("1990-04-12", "ZXC", 6),
            CountryStats("1972-01-10", "ENG", 4),
            CountryStats("1972-11-12", "enq", 2),
            CountryStats("1972-11-12", "end", 4),
            CountryStats("1990-04-12", "WAS", 3),
            CountryStats("1990-04-12", "QWE", 7)
        )
        val httpRequest = HttpRequest(
            requestHeaders = mapOf("x-forwarded-for" to listOf("192.168.50.10")),
            body = null,
            queryParameters = mapOf(
                "startDate" to listOf("1972-01-01"),
                "endDate" to listOf("1990-07-15"),
                "groupLocal" to listOf("true")
            )
        )
        val expectedHttpResponse = HttpResponse(
            code = 200,
            responseBody = """
               [{"date":"1990-04-12","country":"QWE","count":7},
               {"date":"1972-01-10","country":"ENG","count":4},
               {"date":"1972-11-12","country":"end","count":4},
               {"date":"1990-04-12","country":"WAS","count":3},
               {"date":"1990-04-12","country":"ZXC","count":6},
               {"date":"1972-11-12","country":"enq","count":2},
               {"date":"1990-04-12","country":"ENG","count":2}] 
            """,
            contentType = mapOf("content-type" to "application/json")
        )

        val result = getCountryStatsRoute.process(httpRequest)

        verify {
            serviceImpl.retrieveCountryStats(
                startDate = "1972-01-01",
                endDate = "1990-07-15",
                groupLocal = true
            )
        }
        assertEquals(expectedHttpResponse.code, result.code)
        assertEquals(expectedHttpResponse.contentType, result.contentType)
        JSONAssert.assertEquals(expectedHttpResponse.responseBody, result.responseBody, JSONCompareMode.LENIENT)
        confirmVerified()
    }

    @Test
    fun testProcessFunction_WhenServiceRespondWithGroupedLocalData_ShouldReturn204CodeWithoutBody() {
        every {
            serviceImpl.retrieveCountryStats(
                startDate = "2022-01-01",
                endDate = "2023-07-15",
                groupLocal = true
            )
        } returns listOf()
        val httpRequest = HttpRequest(
            requestHeaders = mapOf("x-forwarded-for" to listOf("192.168.50.10")),
            body = null,
            queryParameters = mapOf(
                "startDate" to listOf("2022-01-01"),
                "endDate" to listOf("2023-07-15"),
                "groupLocal" to listOf("true")
            )
        )
        val expectedHttpResponse = HttpResponse(
            code = 204,
            responseBody = null,
            contentType = mapOf()
        )

        val result = getCountryStatsRoute.process(httpRequest)

        verify {
            serviceImpl.retrieveCountryStats(
                startDate = "2022-01-01",
                endDate = "2023-07-15",
                groupLocal = true
            )
        }
        assertEquals(expectedHttpResponse.code, result.code)
        assertEquals(expectedHttpResponse.contentType, expectedHttpResponse.contentType)
        assertEquals(expectedHttpResponse.responseBody, expectedHttpResponse.responseBody)
        confirmVerified()
    }

    @Test
    fun testProcessFunction_WhenServiceRespondWithoutGroupedLocalData_ShouldReturn204CodeWithoutBody() {
        every {
            serviceImpl.retrieveCountryStats(
                startDate = "2022-01-01",
                endDate = "2023-07-15",
                groupLocal = false
            )
        } returns listOf()
        val httpRequest = HttpRequest(
            requestHeaders = mapOf("x-forwarded-for" to listOf("192.168.50.10")),
            body = null,
            queryParameters = mapOf(
                "startDate" to listOf("2022-01-01"),
                "endDate" to listOf("2023-07-15"),
                "groupLocal" to listOf("false")
            )
        )
        val expectedHttpResponse = HttpResponse(
            code = 204,
            responseBody = null,
            contentType = mapOf()
        )

        val result = getCountryStatsRoute.process(httpRequest)

        verify {
            serviceImpl.retrieveCountryStats(
                startDate = "2022-01-01",
                endDate = "2023-07-15",
                groupLocal = false
            )
        }
        assertEquals(expectedHttpResponse.code, result.code)
        assertEquals(expectedHttpResponse.contentType, expectedHttpResponse.contentType)
        assertEquals(expectedHttpResponse.responseBody, expectedHttpResponse.responseBody)
        confirmVerified()
    }

    @Test
    fun testProcessFunction_WhenServiceRespondWithoutGroupedLocalData_ShouldReturn200CodeWithBody() {
        every {
            serviceImpl.retrieveCountryStats(
                startDate = "1972-01-01",
                endDate = "1990-07-15",
                groupLocal = false
            )
        } returns listOf(
            CountryStats("1990-04-12", "ENG", 2),
            CountryStats("1990-04-12", "ZXC", 6),
            CountryStats("1972-01-10", "ENG", 4),
            CountryStats("1972-11-12", "enq", 2),
            CountryStats("1972-11-12", "end", 4),
            CountryStats("1990-04-12", "WAS", 3),
            CountryStats("1990-04-12", "QWE", 7)
        )
        val httpRequest = HttpRequest(
            requestHeaders = mapOf("x-forwarded-for" to listOf("192.168.50.10")),
            body = null,
            queryParameters = mapOf(
                "startDate" to listOf("1972-01-01"),
                "endDate" to listOf("1990-07-15"),
                "groupLocal" to listOf("false")
            )
        )
        val expectedHttpResponse = HttpResponse(
            code = 200,
            responseBody = """
               [{"date":"1990-04-12","country":"QWE","count":7},
               {"date":"1972-01-10","country":"ENG","count":4},
               {"date":"1972-11-12","country":"end","count":4},
               {"date":"1990-04-12","country":"WAS","count":3},
               {"date":"1990-04-12","country":"ZXC","count":6},
               {"date":"1972-11-12","country":"enq","count":2},
               {"date":"1990-04-12","country":"ENG","count":2}] 
            """,
            contentType = mapOf("content-type" to "application/json")
        )

        val result = getCountryStatsRoute.process(httpRequest)

        verify {
            serviceImpl.retrieveCountryStats(
                startDate = "1972-01-01",
                endDate = "1990-07-15",
                groupLocal = false
            )
        }
        assertEquals(expectedHttpResponse.code, result.code)
        assertEquals(expectedHttpResponse.contentType, result.contentType)
        JSONAssert.assertEquals(expectedHttpResponse.responseBody, result.responseBody, JSONCompareMode.LENIENT)
        confirmVerified()
    }

    @Test
    fun testProcessFunction_WhenServiceRequestsWithWrongQueryParameters_ShouldReturn400CodeWithoutBody() {
        every {
            serviceImpl.retrieveCountryStats(
                startDate = "2022-01-01",
                endDate = "2023-07-15",
                groupLocal = any()
            )
        } throws IllegalArgumentException()
        val httpRequest = HttpRequest(
            requestHeaders = mapOf("x-forwarded-for" to listOf("192.168.50.10")),
            body = null,
            queryParameters = mapOf(
                "startDate" to listOf("2022-01-01"),
                "endDate" to listOf("2023-07-15"),
                "g" to listOf()
            )
        )
        val expectedHttpResponse = HttpResponse(
            code = 400,
            responseBody = null,
            contentType = mapOf()
        )

        val result = getCountryStatsRoute.process(httpRequest)

        assertEquals(expectedHttpResponse.code, result.code)
        assertEquals(expectedHttpResponse.responseBody, result.responseBody)
        assertEquals(expectedHttpResponse.contentType, result.contentType)
        confirmVerified()
    }

    @Test
    fun testProcessFunction_WhenServiceThrowsException_ShouldReturn500CodeWithoutBody() {
        every {
            serviceImpl.retrieveCountryStats(
                startDate = any(),
                endDate = any(),
                groupLocal = any()
            )
        } throws Exception()
        val httpRequest = HttpRequest(
            requestHeaders = mapOf("x-forwarded-for" to listOf("192.168.50.10")),
            body = null,
            queryParameters = mapOf(
                "startDate" to listOf("2022-01-01"),
                "endDate" to listOf("2023-07-15"),
                "groupLocal" to listOf("true")
            )
        )
        val expectedHttpResponse = HttpResponse(
            code = 500,
            responseBody = null,
            contentType = mapOf()
        )

        val result = getCountryStatsRoute.process(httpRequest)

        verify {
            serviceImpl.retrieveCountryStats(
                startDate = "2022-01-01",
                endDate = "2023-07-15",
                groupLocal = true
            )
        }
        assertEquals(expectedHttpResponse.code, result.code)
        assertEquals(expectedHttpResponse.responseBody, result.responseBody)
        assertEquals(expectedHttpResponse.contentType, result.contentType)
        confirmVerified()
    }
}
