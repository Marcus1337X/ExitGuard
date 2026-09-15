package com.exitguard.app.network

import com.exitguard.app.model.ExitInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class ExitDetectionService(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(3, TimeUnit.SECONDS)
        .readTimeout(3, TimeUnit.SECONDS)
        .callTimeout(5, TimeUnit.SECONDS)
        .build(),
    private val primaryUrl: String = "https://ipwho.is/",
    private val fallbackUrl: String = "https://api.ip.sb/geoip"
) {

    suspend fun detectExit(): Result<ExitInfo> = withContext(Dispatchers.IO) {
        // Try primary API (ipwho.is)
        val primaryResult = queryPrimary(primaryUrl)
        if (primaryResult.isSuccess) {
            return@withContext primaryResult
        }

        // Try fallback API if primary fails
        val fallbackResult = queryFallback(fallbackUrl)
        if (fallbackResult.isSuccess) {
            return@withContext fallbackResult
        }

        val primaryError = primaryResult.exceptionOrNull()?.message ?: "未知网络错误"
        Result.failure(IOException("公网出口检测失败: $primaryError"))
    }

    private fun queryPrimary(url: String): Result<ExitInfo> {
        return try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "ExitGuard-Android/1.0")
                .header("Accept", "application/json")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return Result.failure(IOException("HTTP 错误: ${response.code}"))
                }
                val body = response.body?.string() ?: return Result.failure(IOException("响应体为空"))
                val json = JSONObject(body)

                // ipwho.is returns boolean "success"
                val success = json.optBoolean("success", false)
                if (!success) {
                    val message = json.optString("message", "API 返回 success=false")
                    return Result.failure(IOException("出口接口返回错误: $message"))
                }

                val ip = json.optString("ip", "").trim()
                val countryCode = json.optString("country_code", "").trim()
                val country = json.optString("country", "").trim()

                if (ip.isEmpty() || countryCode.isEmpty()) {
                    return Result.failure(IOException("出口信息缺少 IP 或国家代码"))
                }

                Result.success(
                    ExitInfo(
                        ip = ip,
                        countryCode = countryCode.uppercase(),
                        country = country
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun queryFallback(url: String): Result<ExitInfo> {
        return try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "ExitGuard-Android/1.0")
                .header("Accept", "application/json")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return Result.failure(IOException("备用接口 HTTP 错误: ${response.code}"))
                }
                val body = response.body?.string() ?: return Result.failure(IOException("备用接口响应为空"))
                val json = JSONObject(body)

                val ip = json.optString("ip", "").trim()
                val countryCode = json.optString("country_code", "").trim()
                val country = json.optString("country", "").trim()

                if (ip.isEmpty() || countryCode.isEmpty()) {
                    return Result.failure(IOException("备用接口返回缺少 IP 或国家代码"))
                }

                Result.success(
                    ExitInfo(
                        ip = ip,
                        countryCode = countryCode.uppercase(),
                        country = country
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
