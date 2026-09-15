package com.exitguard.app.network

import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

class ExitDetectionServiceTest {

    private lateinit var server: MockWebServer

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `detectExit successfully parses ipwho response`() = runBlocking {
        val jsonResponse = """
            {
              "ip": "203.0.113.195",
              "success": true,
              "country": "Japan",
              "country_code": "JP"
            }
        """.trimIndent()

        server.enqueue(MockResponse().setResponseCode(200).setBody(jsonResponse))

        val service = ExitDetectionService(
            client = OkHttpClient.Builder()
                .connectTimeout(2, TimeUnit.SECONDS)
                .readTimeout(2, TimeUnit.SECONDS)
                .build(),
            primaryUrl = server.url("/").toString(),
            fallbackUrl = server.url("/fallback").toString()
        )

        val result = service.detectExit()
        assertTrue(result.isSuccess)
        val info = result.getOrThrow()
        assertEquals("203.0.113.195", info.ip)
        assertEquals("JP", info.countryCode)
        assertEquals("Japan", info.country)
    }

    @Test
    fun `detectExit falls back to secondary API when primary fails`() = runBlocking {
        // Primary returns 500 error
        server.enqueue(MockResponse().setResponseCode(500))

        // Fallback returns 200 with valid data
        val fallbackJson = """
            {
              "ip": "198.51.100.1",
              "country_code": "SG",
              "country": "Singapore"
            }
        """.trimIndent()
        server.enqueue(MockResponse().setResponseCode(200).setBody(fallbackJson))

        val service = ExitDetectionService(
            client = OkHttpClient.Builder().build(),
            primaryUrl = server.url("/primary").toString(),
            fallbackUrl = server.url("/fallback").toString()
        )

        val result = service.detectExit()
        assertTrue(result.isSuccess)
        val info = result.getOrThrow()
        assertEquals("198.51.100.1", info.ip)
        assertEquals("SG", info.countryCode)
    }

    @Test
    fun `detectExit fails when both primary and fallback fail`() = runBlocking {
        // Primary returns success=false
        val primaryFailJson = """
            {
              "success": false,
              "message": "Rate limit exceeded"
            }
        """.trimIndent()
        server.enqueue(MockResponse().setResponseCode(200).setBody(primaryFailJson))

        // Fallback returns 503
        server.enqueue(MockResponse().setResponseCode(503))

        val service = ExitDetectionService(
            client = OkHttpClient.Builder().build(),
            primaryUrl = server.url("/primary").toString(),
            fallbackUrl = server.url("/fallback").toString()
        )

        val result = service.detectExit()
        assertTrue(result.isFailure)
    }
}
