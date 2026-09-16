package com.exitguard.app.network

import com.exitguard.app.model.ExitInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class ExitDetectionService(
    private val connectTimeoutMs: Int = 3000,
    private val readTimeoutMs: Int = 3000,
    private val primaryUrl: String = "https://www.cloudflare.com/cdn-cgi/trace",
    private val fallbackUrl: String = "https://1.1.1.1/cdn-cgi/trace"
) {

    suspend fun detectExit(): Result<ExitInfo> = withContext(Dispatchers.IO) {
        // Try primary Cloudflare trace endpoint
        val primaryResult = queryTrace(primaryUrl)
        if (primaryResult.isSuccess) {
            return@withContext primaryResult
        }

        // Try fallback Cloudflare trace endpoint
        val fallbackResult = queryTrace(fallbackUrl)
        if (fallbackResult.isSuccess) {
            return@withContext fallbackResult
        }

        val primaryError = primaryResult.exceptionOrNull()?.message ?: "未知网络错误"
        Result.failure(IOException("公网出口检测失败: $primaryError"))
    }

    fun queryTrace(urlString: String): Result<ExitInfo> {
        var connection: HttpURLConnection? = null
        return try {
            val url = URL(urlString)
            connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = connectTimeoutMs
                readTimeout = readTimeoutMs
                instanceFollowRedirects = true
                setRequestProperty("User-Agent", "ExitGuard-Android/1.0")
                setRequestProperty("Accept", "text/plain")
            }

            val responseCode = connection.responseCode
            if (responseCode !in 200..299) {
                return Result.failure(IOException("HTTP 错误: $responseCode"))
            }

            val body = connection.inputStream.bufferedReader().use(BufferedReader::readText)
            if (body.isBlank()) {
                return Result.failure(IOException("响应体为空"))
            }

            parseTraceResponse(body)
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            connection?.disconnect()
        }
    }

    fun parseTraceResponse(body: String): Result<ExitInfo> {
        return try {
            var ip: String? = null
            var loc: String? = null

            body.lineSequence().forEach { line ->
                val trimmed = line.trim()
                val separatorIndex = trimmed.indexOf('=')
                if (separatorIndex > 0 && separatorIndex < trimmed.length - 1) {
                    val key = trimmed.substring(0, separatorIndex).trim()
                    val value = trimmed.substring(separatorIndex + 1).trim()
                    when (key) {
                        "ip" -> ip = value
                        "loc" -> loc = value
                    }
                }
            }

            if (ip.isNullOrBlank() || loc.isNullOrBlank()) {
                return Result.failure(IOException("Cloudflare trace 响应缺少 ip 或 loc 字段"))
            }

            Result.success(
                ExitInfo(
                    ip = ip!!,
                    countryCode = loc!!.uppercase(),
                    country = loc!!.uppercase()
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

