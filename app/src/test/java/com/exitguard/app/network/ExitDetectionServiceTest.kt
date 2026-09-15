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
    fun `detectExit successfully parses cloudflare trace response`() = runBlocking {
        val traceResponse = """
            fl=123f45
            h=www.cloudflare.com
            ip=104.28.19.4
            ts=1700000000
            visit_scheme=https
            uag=ExitGuard-Android/1.0
            colo=HKG
            sliver=none
            http=http/2
            loc=HK
            tls=TLSv1.3
            sni=plaintext
            warp=off
            gateway=off
            rbi=off
            kex=X25519
        """.trimIndent()

        server.enqueue(MockResponse().setResponseCode(200).setBody(traceResponse))

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
        assertEquals("104.28.19.4", info.ip)
        assertEquals("HK", info.countryCode)
        assertEquals("HK", info.country)
    }

    @Test
    fun `detectExit falls back to secondary API when primary fails`() = runBlocking {
        // Primary returns 500 error
        server.enqueue(MockResponse().setResponseCode(500))

        // Fallback returns 200 with valid data
        val fallbackTrace = """
            fl=456f78
            ip=198.51.100.1
            loc=SG
        """.trimIndent()
        server.enqueue(MockResponse().setResponseCode(200).setBody(fallbackTrace))

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
        assertEquals("SG", info.country)
    }

    @Test
    fun `detectExit fails when both primary and fallback fail`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(500))
        server.enqueue(MockResponse().setResponseCode(503))

        val service = ExitDetectionService(
            client = OkHttpClient.Builder().build(),
            primaryUrl = server.url("/primary").toString(),
            fallbackUrl = server.url("/fallback").toString()
        )

        val result = service.detectExit()
        assertTrue(result.isFailure)
    }

    @Test
    fun `detectExit fails when response lacks required fields`() = runBlocking {
        val incompleteTrace = """
            fl=123f45
            h=www.cloudflare.com
        """.trimIndent()
        server.enqueue(MockResponse().setResponseCode(200).setBody(incompleteTrace))
        server.enqueue(MockResponse().setResponseCode(404))

        val service = ExitDetectionService(
            client = OkHttpClient.Builder().build(),
            primaryUrl = server.url("/primary").toString(),
            fallbackUrl = server.url("/fallback").toString()
        )

        val result = service.detectExit()
        assertTrue(result.isFailure)
    }
}
