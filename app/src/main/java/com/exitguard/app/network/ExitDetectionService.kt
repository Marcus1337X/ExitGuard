package com.exitguard.app.network

import com.exitguard.app.model.ExitInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit

@Serializable
private data class IpWhoResponse(
    val ip: String? = null,
    val success: Boolean = false,
    val message: String? = null,
    val country: String? = null,
    val country_code: String? = null
)

@Serializable
private data class IpSbResponse(
    val ip: String? = null,
    val country: String? = null,
    val country_code: String? = null
)

class ExitDetectionService(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(3, TimeUnit.SECONDS)
        .readTimeout(3, TimeUnit.SECONDS)
        .callTimeout(5, TimeUnit.SECONDS)
        .build(),
    private val primaryUrl: String = "https://ipwho.is/",
    private val fallbackUrl: String = "https://api.ip.sb/geoip"
) {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

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
                val parsed = json.decodeFromString<IpWhoResponse>(body)

                if (!parsed.success) {
                    val message = parsed.message ?: "API 返回 success=false"
                    return Result.failure(IOException("出口接口返回错误: $message"))
                }

                val ip = parsed.ip?.trim().orEmpty()
                val countryCode = parsed.country_code?.trim().orEmpty()
                val country = parsed.country?.trim().orEmpty()

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
                val parsed = json.decodeFromString<IpSbResponse>(body)

                val ip = parsed.ip?.trim().orEmpty()
                val countryCode = parsed.country_code?.trim().orEmpty()
                val country = parsed.country?.trim().orEmpty()

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
