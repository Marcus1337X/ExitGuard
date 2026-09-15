package com.exitguard.app.network

import com.exitguard.app.model.ExitInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit

class ExitDetectionService(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(3, TimeUnit.SECONDS)
        .readTimeout(3, TimeUnit.SECONDS)
        .callTimeout(5, TimeUnit.SECONDS)
        .build(),
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

    private fun queryTrace(url: String): Result<ExitInfo> {
        return try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "ExitGuard-Android/1.0")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return Result.failure(IOException("HTTP 错误: ${response.code}"))
                }
                val body = response.body?.string() ?: return Result.failure(IOException("响应体为空"))
                parseTraceResponse(body)
            }
        } catch (e: Exception) {
            Result.failure(e)
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
