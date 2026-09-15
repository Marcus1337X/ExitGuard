package com.exitguard.app.network

import com.exitguard.app.data.SettingsRepository
import com.exitguard.app.model.ExitInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
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
    private val primaryUrl: String = "https://ipwho.is/",
    private val fallbackUrl: String = "https://api.ip.sb/geoip",
    private val settingsRepository: SettingsRepository? = null
) {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    suspend fun detectExit(): Result<ExitInfo> = withContext(Dispatchers.IO) {
        val activePrimaryUrl = settingsRepository?.getPrimaryApi() ?: primaryUrl

        // Try active primary API
        val primaryResult = queryApi(activePrimaryUrl)
        if (primaryResult.isSuccess) {
            return@withContext primaryResult
        }

        // Try fallback API if different from primary
        if (activePrimaryUrl != fallbackUrl) {
            val fallbackResult = queryApi(fallbackUrl)
            if (fallbackResult.isSuccess) {
                return@withContext fallbackResult
            }
        }

        val primaryError = primaryResult.exceptionOrNull()?.message ?: "未知网络错误"
        Result.failure(IOException("公网出口检测失败: $primaryError"))
    }

    suspend fun testApiUrl(url: String): Result<ExitInfo> = withContext(Dispatchers.IO) {
        queryApi(url)
    }

    private fun queryApi(url: String): Result<ExitInfo> {
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
                parseResponse(body)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun parseResponse(body: String): Result<ExitInfo> {
        return try {
            val jsonElement = json.parseToJsonElement(body)
            if (jsonElement !is JsonObject) {
                return Result.failure(IOException("响应格式非 JSON 对象"))
            }

            // Check if there is an explicit "success" field
            val successPrimitive = jsonElement["success"]?.jsonPrimitive
            if (successPrimitive != null && successPrimitive.booleanOrNull == false) {
                val message = jsonElement["message"]?.jsonPrimitive?.contentOrNull ?: "API 返回 success=false"
                return Result.failure(IOException("接口返回错误: $message"))
            }

            // Check if there is an explicit "status" field (e.g. ip-api.com returns "status":"fail")
            val statusPrimitive = jsonElement["status"]?.jsonPrimitive
            if (statusPrimitive != null && statusPrimitive.contentOrNull?.equals("fail", ignoreCase = true) == true) {
                val message = jsonElement["message"]?.jsonPrimitive?.contentOrNull ?: "API 返回 status=fail"
                return Result.failure(IOException("接口返回错误: $message"))
            }

            // Check for explicit "error" field
            val errorPrimitive = jsonElement["error"]?.jsonPrimitive
            if (errorPrimitive != null && errorPrimitive.booleanOrNull == true) {
                val reason = jsonElement["reason"]?.jsonPrimitive?.contentOrNull ?: "API 报告错误"
                return Result.failure(IOException("接口返回错误: $reason"))
            }

            // Extract IP (check "ip", "query")
            val ip = (jsonElement["ip"]?.jsonPrimitive?.contentOrNull
                ?: jsonElement["query"]?.jsonPrimitive?.contentOrNull
                ?: "").trim()

            // Extract Country Code (check "country_code", "countryCode", "country_code_iso3", "country")
            var countryCode = (jsonElement["country_code"]?.jsonPrimitive?.contentOrNull
                ?: jsonElement["countryCode"]?.jsonPrimitive?.contentOrNull
                ?: "").trim()

            // Extract Country Name (check "country", "country_name", "countryName")
            var countryName = (jsonElement["country_name"]?.jsonPrimitive?.contentOrNull
                ?: jsonElement["countryName"]?.jsonPrimitive?.contentOrNull
                ?: "").trim()

            if (countryCode.isEmpty()) {
                val rawCountry = jsonElement["country"]?.jsonPrimitive?.contentOrNull.orEmpty().trim()
                if (rawCountry.length == 2 && rawCountry.all { it.isLetter() }) {
                    countryCode = rawCountry
                } else {
                    countryName = rawCountry
                }
            } else if (countryName.isEmpty()) {
                countryName = jsonElement["country"]?.jsonPrimitive?.contentOrNull.orEmpty().trim()
            }

            if (ip.isEmpty()) {
                return Result.failure(IOException("接口响应未包含 IP 地址"))
            }

            Result.success(
                ExitInfo(
                    ip = ip,
                    countryCode = countryCode.uppercase(),
                    country = countryName
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
