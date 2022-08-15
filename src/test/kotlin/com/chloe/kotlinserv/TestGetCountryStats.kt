package com.chloe.kotlinserv

import com.chloe.kotlinserv.http.HttpRequest
import com.chloe.kotlinserv.http.HttpResponse
import com.chloe.kotlinserv.model.CountryStats
import com.chloe.kotlinserv.route.GetCountryStatsRoute
import com.chloe.kotlinserv.service.GeoDataServiceImpl
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import kotlin.test.assertEquals

class TestGetCountryStats {
    @Test
    fun `get grouping local info`() {
        val serviceImpl = mockk<GeoDataServiceImpl>()

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

        val getCountryStatsRoute = GetCountryStatsRoute(serviceImpl)

        val httpRequest = HttpRequest(
            requestHeaders = mapOf("x-forwarded-for" to listOf("192.168.50.10")),
            body = null,
            queryParameters = mapOf(
                "startDate" to listOf("1972-01-01"),
                "endDate" to listOf("1990-07-15"),
                "groupLocal" to listOf("true")
            )
        )
        val result = getCountryStatsRoute.processFunction(httpRequest)
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

        assertEquals(expectedHttpResponse.code, result.code)
        assertEquals(expectedHttpResponse.contentType, result.contentType)
        JSONAssert.assertEquals(expectedHttpResponse.responseBody, result.responseBody, JSONCompareMode.LENIENT)
    }
}
