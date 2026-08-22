package com.abhii.paprivo.update

import com.google.gson.annotations.SerializedName

data class UpdateResponse(
    @SerializedName("update")
    val update: RemoteUpdateInfo?
)

data class RemoteUpdateInfo(
    @SerializedName("enabled")
    val enabled: Boolean = false,

    @SerializedName("version")
    val version: String = "1.0.0",

    @SerializedName("versionCode")
    val versionCode: Int = 1,

    @SerializedName("title")
    val title: String = "New Update Available",

    @SerializedName("message")
    val message: String = "",

    @SerializedName("changelog")
    val changelog: List<String> = emptyList(),

    @SerializedName("downloadUrl")
    val downloadUrl: String = "",

    @SerializedName("mandatory")
    val mandatory: Boolean = false,

    @SerializedName("showNotNow")
    val showNotNow: Boolean = true
)

data class UIUpdateInfo(
    val isUpdateAvailable: Boolean,
    val latestVersion: String,
    val currentVersion: String,
    val changelog: String,
    val downloadUrl: String
)
