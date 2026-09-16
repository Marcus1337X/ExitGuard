package com.exitguard.app.network

import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.ServerSocket

class ExitDetectionServiceTest {

    private var server: SimpleHttpServer? = null

    @After
    fun tearDown() {
        server?.close()
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

        server = SimpleHttpServer { path ->
            if (path == "/trace") 200 to traceResponse else 404 to ""
        }

        val port = server!!.port
        val service = ExitDetectionService(
            primaryUrl = "http://127.0.0.1:$port/trace",
            fallbackUrl = "http://127.0.0.1:$port/fallback"
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
        val fallbackTrace = """
            fl=456f78
            ip=198.51.100.1
            loc=SG
        """.trimIndent()

        server = SimpleHttpServer { path ->
            when (path) {
                "/primary" -> 500 to "Error"
                "/fallback" -> 200 to fallbackTrace
                else -> 404 to ""
            }
        }

        val port = server!!.port
        val service = ExitDetectionService(
            primaryUrl = "http://127.0.0.1:$port/primary",
            fallbackUrl = "http://127.0.0.1:$port/fallback"
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
        server = SimpleHttpServer { _ -> 500 to "Internal Error" }
        val port = server!!.port

        val service = ExitDetectionService(
            primaryUrl = "http://127.0.0.1:$port/primary",
            fallbackUrl = "http://127.0.0.1:$port/fallback"
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

        server = SimpleHttpServer { path ->
            if (path == "/primary") 200 to incompleteTrace else 404 to ""
        }
        val port = server!!.port

        val service = ExitDetectionService(
            primaryUrl = "http://127.0.0.1:$port/primary",
            fallbackUrl = "http://127.0.0.1:$port/fallback"
        )

        val result = service.detectExit()
        assertTrue(result.isFailure)
    }

    private class SimpleHttpServer(
        private val handler: (path: String) -> Pair<Int, String>
    ) : AutoCloseable {
        private val serverSocket = ServerSocket(0)
        val port: Int = serverSocket.localPort
        @Volatile private var running = true

        init {
            Thread {
                while (running) {
                    try {
                        val socket = serverSocket.accept()
                        Thread {
                            try {
                                val reader = socket.getInputStream().bufferedReader()
                                val requestLine = reader.readLine() ?: ""
                                val path = requestLine.split(" ").getOrNull(1) ?: "/"
                                val (code, body) = handler(path)
                                val writer = socket.getOutputStream().bufferedWriter()
                                val statusLine = if (code == 200) "200 OK" else "$code Error"
                                val bytes = body.toByteArray(Charsets.UTF_8)
                                writer.write("HTTP/1.1 $statusLine\r\n")
                                writer.write("Content-Type: text/plain; charset=utf-8\r\n")
                                writer.write("Content-Length: ${bytes.size}\r\n")
                                writer.write("Connection: close\r\n\r\n")
                                writer.write(body)
                                writer.flush()
                                socket.close()
                            } catch (e: Exception) {}
                        }.start()
                    } catch (e: Exception) {
                        // socket closed
                    }
                }
            }.start()
        }

        override fun close() {
            running = false
            try {
                serverSocket.close()
            } catch (e: Exception) {}
        }
    }
}

