package com.abhii.paprivo.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.abhii.paprivo.data.models.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "paprivo_preferences")

class PaprivoPreferences(private val context: Context) {

    companion object {
        val KEY_THEME = stringPreferencesKey("app_theme")
        val KEY_ANIMATIONS_ENABLED = booleanPreferencesKey("animations_enabled")
        val KEY_ANIMATION_SPEED = floatPreferencesKey("animation_speed")
        val KEY_HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val KEY_HAPTIC_INTENSITY = stringPreferencesKey("haptic_intensity")
        val KEY_COMPACT_MODE = booleanPreferencesKey("compact_mode")

        val KEY_DEFAULT_PAGE_SIZE = stringPreferencesKey("default_page_size")
        val KEY_DEFAULT_ORIENTATION = stringPreferencesKey("default_orientation")
        val KEY_DEFAULT_MARGINS = stringPreferencesKey("default_margins")
        val KEY_DEFAULT_IMAGE_QUALITY = stringPreferencesKey("default_image_quality")
        val KEY_CONFIRM_OVERWRITE = booleanPreferencesKey("confirm_overwrite")

        val KEY_KEEP_SCREEN_AWAKE = booleanPreferencesKey("keep_screen_awake")
        val KEY_CONTINUOUS_SCROLL = booleanPreferencesKey("continuous_scroll")
        val KEY_REMEMBER_POSITION = booleanPreferencesKey("remember_position")
        val KEY_SHOW_PAGE_NUMBERS = booleanPreferencesKey("show_page_numbers")
        val KEY_DEFAULT_ZOOM = floatPreferencesKey("default_zoom")

        val KEY_SCAN_AUTO_CROP = booleanPreferencesKey("scan_auto_crop")
        val KEY_SCAN_PERSPECTIVE = booleanPreferencesKey("scan_perspective")
        val KEY_SCAN_AUTO_ENHANCE = booleanPreferencesKey("scan_auto_enhance")
        val KEY_SCAN_DEFAULT_MODE = stringPreferencesKey("scan_default_mode")
        val KEY_SCAN_QUALITY = stringPreferencesKey("scan_quality")
        val KEY_SCAN_AUTO_SAVE = booleanPreferencesKey("scan_auto_save")

        val KEY_PRIVACY_RECENT_FILES = booleanPreferencesKey("privacy_recent_files")
        val KEY_PRIVACY_SEARCH_HISTORY = booleanPreferencesKey("privacy_search_history")
        val KEY_PRIVACY_USAGE_DATA = booleanPreferencesKey("privacy_usage_data")
        val KEY_PRIVACY_THUMBNAILS = booleanPreferencesKey("privacy_thumbnails")

        // Privacy
        val KEY_AUTO_CLEAR_CACHE = booleanPreferencesKey("auto_clear_cache")
        val KEY_INCOGNITO_MODE = booleanPreferencesKey("incognito_mode")

        // Appearance
        val KEY_NAVIGATION_BAR_STYLE = stringPreferencesKey("navigation_bar_style")

        // PDF
        val KEY_COMPRESSION_LEVEL = stringPreferencesKey("compression_level")
        val KEY_REMOVE_METADATA = booleanPreferencesKey("remove_metadata")

        // Scanner
        val KEY_GRID_LINES = booleanPreferencesKey("grid_lines")
        val KEY_CAMERA_SOUND = booleanPreferencesKey("camera_sound")

        // Storage
        val KEY_AUTO_CLEANUP = booleanPreferencesKey("auto_cleanup")
        val KEY_CUSTOM_STORAGE_PATH = stringPreferencesKey("custom_storage_path")

        // Backup
        val KEY_AUTO_BACKUP_ENABLED = booleanPreferencesKey("auto_backup_enabled")
        val KEY_BACKUP_ENCRYPTION = booleanPreferencesKey("backup_encryption")
    }

    val themeFlow: Flow<ThemeMode> = context.dataStore.data.map { pref ->
        when (pref[KEY_THEME]) {
            "WHITE" -> ThemeMode.WHITE
            else -> ThemeMode.BLACK
        }
    }

    val animationsEnabledFlow: Flow<Boolean> = context.dataStore.data.map { it[KEY_ANIMATIONS_ENABLED] ?: true }
    val animationSpeedFlow: Flow<Float> = context.dataStore.data.map { it[KEY_ANIMATION_SPEED] ?: 1.0f }
    val hapticsEnabledFlow: Flow<Boolean> = context.dataStore.data.map { it[KEY_HAPTICS_ENABLED] ?: true }
    val hapticIntensityFlow: Flow<String> = context.dataStore.data.map { it[KEY_HAPTIC_INTENSITY] ?: "medium" }
    val compactModeFlow: Flow<Boolean> = context.dataStore.data.map { it[KEY_COMPACT_MODE] ?: false }

    val defaultPageSizeFlow: Flow<PageSizeOption> = context.dataStore.data.map {
        try {
            PageSizeOption.valueOf(it[KEY_DEFAULT_PAGE_SIZE] ?: "A4")
        } catch (e: Exception) {
            PageSizeOption.A4
        }
    }

    val defaultOrientationFlow: Flow<OrientationOption> = context.dataStore.data.map {
        try {
            OrientationOption.valueOf(it[KEY_DEFAULT_ORIENTATION] ?: "PORTRAIT")
        } catch (e: Exception) {
            OrientationOption.PORTRAIT
        }
    }

    val defaultMarginsFlow: Flow<MarginsOption> = context.dataStore.data.map {
        try {
            MarginsOption.valueOf(it[KEY_DEFAULT_MARGINS] ?: "NONE")
        } catch (e: Exception) {
            MarginsOption.NONE
        }
    }

    val defaultImageQualityFlow: Flow<ImageQualityOption> = context.dataStore.data.map {
        try {
            ImageQualityOption.valueOf(it[KEY_DEFAULT_IMAGE_QUALITY] ?: "HIGH")
        } catch (e: Exception) {
            ImageQualityOption.HIGH
        }
    }

    val confirmOverwriteFlow: Flow<Boolean> = context.dataStore.data.map { it[KEY_CONFIRM_OVERWRITE] ?: true }

    val keepScreenAwakeFlow: Flow<Boolean> = context.dataStore.data.map { it[KEY_KEEP_SCREEN_AWAKE] ?: true }
    val continuousScrollFlow: Flow<Boolean> = context.dataStore.data.map { it[KEY_CONTINUOUS_SCROLL] ?: true }
    val rememberPositionFlow: Flow<Boolean> = context.dataStore.data.map { it[KEY_REMEMBER_POSITION] ?: true }
    val showPageNumbersFlow: Flow<Boolean> = context.dataStore.data.map { it[KEY_SHOW_PAGE_NUMBERS] ?: true }
    val defaultZoomFlow: Flow<Float> = context.dataStore.data.map { it[KEY_DEFAULT_ZOOM] ?: 1.0f }

    val scanAutoCropFlow: Flow<Boolean> = context.dataStore.data.map { it[KEY_SCAN_AUTO_CROP] ?: true }
    val scanPerspectiveFlow: Flow<Boolean> = context.dataStore.data.map { it[KEY_SCAN_PERSPECTIVE] ?: true }
    val scanAutoEnhanceFlow: Flow<Boolean> = context.dataStore.data.map { it[KEY_SCAN_AUTO_ENHANCE] ?: true }
    val scanDefaultModeFlow: Flow<ScanEnhancement> = context.dataStore.data.map {
        try {
            ScanEnhancement.valueOf(it[KEY_SCAN_DEFAULT_MODE] ?: "COLOR")
        } catch (e: Exception) {
            ScanEnhancement.COLOR
        }
    }
    val scanQualityFlow: Flow<String> = context.dataStore.data.map { it[KEY_SCAN_QUALITY] ?: "high" }
    val scanAutoSaveFlow: Flow<Boolean> = context.dataStore.data.map { it[KEY_SCAN_AUTO_SAVE] ?: false }

    val privacyRecentFilesFlow: Flow<Boolean> = context.dataStore.data.map { it[KEY_PRIVACY_RECENT_FILES] ?: true }
    val privacySearchHistoryFlow: Flow<Boolean> = context.dataStore.data.map { it[KEY_PRIVACY_SEARCH_HISTORY] ?: true }
    val privacyUsageDataFlow: Flow<Boolean> = context.dataStore.data.map { it[KEY_PRIVACY_USAGE_DATA] ?: false }
    val privacyThumbnailsFlow: Flow<Boolean> = context.dataStore.data.map { it[KEY_PRIVACY_THUMBNAILS] ?: true }

    val autoClearCacheFlow: Flow<Boolean> = context.dataStore.data.map { it[KEY_AUTO_CLEAR_CACHE] ?: false }
    val incognitoModeFlow: Flow<Boolean> = context.dataStore.data.map { it[KEY_INCOGNITO_MODE] ?: false }

    val navigationBarStyleFlow: Flow<String> = context.dataStore.data.map { it[KEY_NAVIGATION_BAR_STYLE] ?: "default" }

    val compressionLevelFlow: Flow<String> = context.dataStore.data.map { it[KEY_COMPRESSION_LEVEL] ?: "medium" }
    val removeMetadataFlow: Flow<Boolean> = context.dataStore.data.map { it[KEY_REMOVE_METADATA] ?: true }

    val gridLinesFlow: Flow<Boolean> = context.dataStore.data.map { it[KEY_GRID_LINES] ?: false }
    val cameraSoundFlow: Flow<Boolean> = context.dataStore.data.map { it[KEY_CAMERA_SOUND] ?: true }

    val autoCleanupFlow: Flow<Boolean> = context.dataStore.data.map { it[KEY_AUTO_CLEANUP] ?: false }
    val customStoragePathFlow: Flow<String> = context.dataStore.data.map { it[KEY_CUSTOM_STORAGE_PATH] ?: "" }

    val autoBackupEnabledFlow: Flow<Boolean> = context.dataStore.data.map { it[KEY_AUTO_BACKUP_ENABLED] ?: false }
    val backupEncryptionFlow: Flow<Boolean> = context.dataStore.data.map { it[KEY_BACKUP_ENCRYPTION] ?: true }

    suspend fun setTheme(theme: ThemeMode) {
        context.dataStore.edit { it[KEY_THEME] = theme.name }
    }

    suspend fun setAnimationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_ANIMATIONS_ENABLED] = enabled }
    }

    suspend fun setAnimationSpeed(speed: Float) {
        context.dataStore.edit { it[KEY_ANIMATION_SPEED] = speed }
    }

    suspend fun setHapticsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_HAPTICS_ENABLED] = enabled }
    }

    suspend fun setHapticIntensity(intensity: String) {
        context.dataStore.edit { it[KEY_HAPTIC_INTENSITY] = intensity }
    }

    suspend fun setCompactMode(compact: Boolean) {
        context.dataStore.edit { it[KEY_COMPACT_MODE] = compact }
    }

    suspend fun setDefaultPageSize(size: PageSizeOption) {
        context.dataStore.edit { it[KEY_DEFAULT_PAGE_SIZE] = size.name }
    }

    suspend fun setDefaultOrientation(orientation: OrientationOption) {
        context.dataStore.edit { it[KEY_DEFAULT_ORIENTATION] = orientation.name }
    }

    suspend fun setDefaultMargins(margins: MarginsOption) {
        context.dataStore.edit { it[KEY_DEFAULT_MARGINS] = margins.name }
    }

    suspend fun setDefaultImageQuality(quality: ImageQualityOption) {
        context.dataStore.edit { it[KEY_DEFAULT_IMAGE_QUALITY] = quality.name }
    }

    suspend fun setConfirmOverwrite(confirm: Boolean) {
        context.dataStore.edit { it[KEY_CONFIRM_OVERWRITE] = confirm }
    }

    suspend fun setKeepScreenAwake(awake: Boolean) {
        context.dataStore.edit { it[KEY_KEEP_SCREEN_AWAKE] = awake }
    }

    suspend fun setContinuousScroll(continuous: Boolean) {
        context.dataStore.edit { it[KEY_CONTINUOUS_SCROLL] = continuous }
    }

    suspend fun setRememberPosition(remember: Boolean) {
        context.dataStore.edit { it[KEY_REMEMBER_POSITION] = remember }
    }

    suspend fun setShowPageNumbers(show: Boolean) {
        context.dataStore.edit { it[KEY_SHOW_PAGE_NUMBERS] = show }
    }

    suspend fun setDefaultZoom(zoom: Float) {
        context.dataStore.edit { it[KEY_DEFAULT_ZOOM] = zoom }
    }

    suspend fun setScanAutoCrop(autoCrop: Boolean) {
        context.dataStore.edit { it[KEY_SCAN_AUTO_CROP] = autoCrop }
    }

    suspend fun setScanPerspective(perspective: Boolean) {
        context.dataStore.edit { it[KEY_SCAN_PERSPECTIVE] = perspective }
    }

    suspend fun setScanAutoEnhance(enhance: Boolean) {
        context.dataStore.edit { it[KEY_SCAN_AUTO_ENHANCE] = enhance }
    }

    suspend fun setScanDefaultMode(mode: ScanEnhancement) {
        context.dataStore.edit { it[KEY_SCAN_DEFAULT_MODE] = mode.name }
    }

    suspend fun setScanQuality(quality: String) {
        context.dataStore.edit { it[KEY_SCAN_QUALITY] = quality }
    }

    suspend fun setScanAutoSave(autoSave: Boolean) {
        context.dataStore.edit { it[KEY_SCAN_AUTO_SAVE] = autoSave }
    }

    suspend fun setPrivacyRecentFiles(save: Boolean) {
        context.dataStore.edit { it[KEY_PRIVACY_RECENT_FILES] = save }
    }

    suspend fun setPrivacySearchHistory(save: Boolean) {
        context.dataStore.edit { it[KEY_PRIVACY_SEARCH_HISTORY] = save }
    }

    suspend fun setPrivacyUsageData(share: Boolean) {
        context.dataStore.edit { it[KEY_PRIVACY_USAGE_DATA] = share }
    }

    suspend fun setPrivacyThumbnails(save: Boolean) {
        context.dataStore.edit { it[KEY_PRIVACY_THUMBNAILS] = save }
    }

    suspend fun setAutoClearCache(enabled: Boolean) {
        context.dataStore.edit { it[KEY_AUTO_CLEAR_CACHE] = enabled }
    }

    suspend fun setIncognitoMode(enabled: Boolean) {
        context.dataStore.edit { it[KEY_INCOGNITO_MODE] = enabled }
    }

    suspend fun setNavigationBarStyle(style: String) {
        context.dataStore.edit { it[KEY_NAVIGATION_BAR_STYLE] = style }
    }

    suspend fun setCompressionLevel(level: String) {
        context.dataStore.edit { it[KEY_COMPRESSION_LEVEL] = level }
    }

    suspend fun setRemoveMetadata(remove: Boolean) {
        context.dataStore.edit { it[KEY_REMOVE_METADATA] = remove }
    }

    suspend fun setGridLines(enabled: Boolean) {
        context.dataStore.edit { it[KEY_GRID_LINES] = enabled }
    }

    suspend fun setCameraSound(enabled: Boolean) {
        context.dataStore.edit { it[KEY_CAMERA_SOUND] = enabled }
    }

    suspend fun setAutoCleanup(enabled: Boolean) {
        context.dataStore.edit { it[KEY_AUTO_CLEANUP] = enabled }
    }

    suspend fun setCustomStoragePath(path: String) {
        context.dataStore.edit { it[KEY_CUSTOM_STORAGE_PATH] = path }
    }

    suspend fun setAutoBackupEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_AUTO_BACKUP_ENABLED] = enabled }
    }

    suspend fun setBackupEncryption(enabled: Boolean) {
        context.dataStore.edit { it[KEY_BACKUP_ENCRYPTION] = enabled }
    }

    suspend fun getAllSettingsAsJson(): String {
        val prefs = context.dataStore.data.first()
        val map = prefs.asMap().mapKeys { it.key.name }
        return Gson().toJson(map)
    }

    suspend fun restoreSettingsFromJson(json: String): Boolean {
        return try {
            val type = object : TypeToken<Map<String, Any>>() {}.type
            val map = Gson().fromJson<Map<String, Any>>(json, type)
            context.dataStore.edit { prefs ->
                map.forEach { (name, value) ->
                    when (name) {
                        "animation_speed", "default_zoom" -> {
                            prefs[floatPreferencesKey(name)] = (value as Number).toFloat()
                        }
                        "animations_enabled", "haptics_enabled", "compact_mode", "confirm_overwrite",
                        "keep_screen_awake", "continuous_scroll", "remember_position", "show_page_numbers",
                        "scan_auto_crop", "scan_perspective", "scan_auto_enhance", "scan_auto_save",
                        "privacy_recent_files", "privacy_search_history", "privacy_usage_data", "privacy_thumbnails",
                        "auto_clear_cache", "incognito_mode", "remove_metadata",
                        "grid_lines", "camera_sound", "auto_cleanup", "auto_backup_enabled", "backup_encryption" -> {
                            prefs[booleanPreferencesKey(name)] = value as Boolean
                        }
                        else -> {
                            prefs[stringPreferencesKey(name)] = value.toString()
                        }
                    }
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun clearAllSettings() {
        context.dataStore.edit { it.clear() }
    }
}
