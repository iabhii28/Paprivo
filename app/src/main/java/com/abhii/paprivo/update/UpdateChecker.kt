package com.abhii.paprivo.update

import com.abhii.paprivo.config.AppConfig
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class UpdateChecker(private val currentVersionCode: Int = AppConfig.APP_VERSION_CODE) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    suspend fun checkForUpdates(url: String = AppConfig.UPDATE_JSON_URL): Result<RemoteUpdateInfo?> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(url)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(Exception("HTTP error: ${response.code}"))
                }

                val body = response.body?.string() ?: return@withContext Result.success(null)
                val updateResponse = gson.fromJson(body, UpdateResponse::class.java)
                val updateInfo = updateResponse.update

                if (updateInfo != null && updateInfo.enabled && updateInfo.versionCode > currentVersionCode) {
                    Result.success(updateInfo)
                } else {
                    Result.success(null)
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
