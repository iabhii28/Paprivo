package com.abhii.paprivo.viewmodel

import android.app.Application
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.abhii.paprivo.data.models.*
import com.abhii.paprivo.data.preferences.PaprivoPreferences
import com.abhii.paprivo.domain.pdf.PdfOperationsManager
import com.abhii.paprivo.update.*
import com.abhii.paprivo.config.AppConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.InputStream

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val preferences = PaprivoPreferences(application)
    val pdfOperations = PdfOperationsManager(application)
    private val updateChecker = UpdateChecker()

    // Preferences Flows
    val themeMode: StateFlow<ThemeMode> = preferences.themeFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, ThemeMode.BLACK)

    val animationsEnabled = preferences.animationsEnabledFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val animationSpeed = preferences.animationSpeedFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, 1.0f)

    val hapticsEnabled = preferences.hapticsEnabledFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val hapticIntensity = preferences.hapticIntensityFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, "medium")

    val compactMode = preferences.compactModeFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val defaultPageSize = preferences.defaultPageSizeFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, PageSizeOption.A4)

    val defaultOrientation = preferences.defaultOrientationFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, OrientationOption.PORTRAIT)

    val defaultMargins = preferences.defaultMarginsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, MarginsOption.NONE)

    val defaultImageQuality = preferences.defaultImageQualityFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, ImageQualityOption.HIGH)

    // Privacy
    val autoClearCache = preferences.autoClearCacheFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val incognitoMode = preferences.incognitoModeFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    // Appearance
    val navigationBarStyle = preferences.navigationBarStyleFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, "default")

    // PDF
    val compressionLevel = preferences.compressionLevelFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, "medium")
    val removeMetadata = preferences.removeMetadataFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)
    val keepScreenAwake = preferences.keepScreenAwakeFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)
    val continuousScroll = preferences.continuousScrollFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)
    val rememberPosition = preferences.rememberPositionFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)
    val showPageNumbers = preferences.showPageNumbersFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)
    val defaultZoom = preferences.defaultZoomFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, 1.0f)

    // Scanner
    val gridLines = preferences.gridLinesFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val cameraSound = preferences.cameraSoundFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    // Storage
    val autoCleanup = preferences.autoCleanupFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val customStoragePath = preferences.customStoragePathFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    // Backup
    val autoBackupEnabled = preferences.autoBackupEnabledFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val backupEncryption = preferences.backupEncryptionFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    // Storage and Recent Files
    private val _recentFiles = MutableStateFlow<List<PdfFileItem>>(emptyList())
    val recentFiles: StateFlow<List<PdfFileItem>> = _recentFiles.asStateFlow()

    // Update state
    private val _updateInfo = MutableStateFlow<UIUpdateInfo?>(null)
    val updateInfo: StateFlow<UIUpdateInfo?> = _updateInfo.asStateFlow()

    private val _isCheckingUpdate = MutableStateFlow(false)
    val isCheckingUpdate: StateFlow<Boolean> = _isCheckingUpdate.asStateFlow()

    // Operation status / message
    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    init {
        loadRecentFiles()
        checkForUpdates()
    }

    fun loadRecentFiles() {
        if (incognitoMode.value) {
            _recentFiles.value = emptyList()
            return
        }
        viewModelScope.launch {
            val pdfDir = File(getApplication<Application>().filesDir, "pdfs")
            if (pdfDir.exists()) {
                val files = pdfDir.listFiles { f -> f.extension.equals("pdf", ignoreCase = true) }
                    ?.map { file ->
                        val uri = FileProvider.getUriForFile(
                            getApplication(),
                            "${getApplication<Application>().packageName}.fileprovider",
                            file
                        )
                        PdfFileItem(
                            id = file.absolutePath,
                            name = file.name,
                            uri = uri,
                            size = file.length(),
                            pageCount = 1,
                            modifiedAt = file.lastModified()
                        )
                    }?.sortedByDescending { it.modifiedAt } ?: emptyList()

                _recentFiles.value = files
            }
        }
    }

    fun checkForUpdates(showNoUpdateToast: Boolean = false) {
        viewModelScope.launch {
            _isCheckingUpdate.value = true
            updateChecker.checkForUpdates()
                .onSuccess { info ->
                    if (info != null) {
                        _updateInfo.value = UIUpdateInfo(
                            isUpdateAvailable = true,
                            latestVersion = info.version,
                            currentVersion = AppConfig.APP_VERSION,
                            changelog = info.changelog.joinToString("\n"),
                            downloadUrl = info.downloadUrl
                        )
                    } else if (showNoUpdateToast) {
                        _updateInfo.value = UIUpdateInfo(
                            isUpdateAvailable = false,
                            latestVersion = AppConfig.APP_VERSION,
                            currentVersion = AppConfig.APP_VERSION,
                            changelog = "You are on the latest version.",
                            downloadUrl = ""
                        )
                    } else {
                        _updateInfo.value = null
                    }
                }
                .onFailure {
                    if (showNoUpdateToast) {
                        _statusMessage.value = "Failed to check for updates"
                    }
                }
            _isCheckingUpdate.value = false
        }
    }

    fun dismissUpdateDialog() {
        _updateInfo.value = null
    }

    fun sharePdfFile(file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                getApplication(),
                "${getApplication<Application>().packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(intent, "Share PDF").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            getApplication<Application>().startActivity(chooser)
        } catch (e: Exception) {
            _statusMessage.value = "Failed to share PDF: ${e.message}"
        }
    }

    fun downloadPdfFile(file: File) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val resolver = getApplication<Application>().contentResolver
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, file.name)
                        put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/Paprivo")
                    }

                    val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                    if (uri != null) {
                        resolver.openOutputStream(uri)?.use { outputStream ->
                            file.inputStream().use { inputStream ->
                                inputStream.copyTo(outputStream)
                            }
                        }
                        _statusMessage.value = "PDF saved to Downloads/Paprivo"
                    } else {
                        _statusMessage.value = "Failed to save PDF"
                    }
                } else {
                    val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    val paprivoDir = File(downloadsDir, "Paprivo")
                    if (!paprivoDir.exists()) paprivoDir.mkdirs()
                    
                    val targetFile = File(paprivoDir, file.name)
                    file.copyTo(targetFile, overwrite = true)
                    
                    // Trigger media scanner
                    val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                    mediaScanIntent.data = Uri.fromFile(targetFile)
                    getApplication<Application>().sendBroadcast(mediaScanIntent)
                    
                    _statusMessage.value = "PDF saved to Downloads/Paprivo"
                }
            } catch (e: Exception) {
                _statusMessage.value = "Download failed: ${e.message}"
            }
        }
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    fun clearCache() {
        viewModelScope.launch {
            val cacheDir = getApplication<Application>().cacheDir
            cacheDir.deleteRecursively()
            cacheDir.mkdirs()
            _statusMessage.value = "Cache cleared successfully"
        }
    }

    fun clearRecentHistory() {
        _recentFiles.value = emptyList()
        _statusMessage.value = "Recent history cleared"
    }

    fun clearAllData() {
        viewModelScope.launch {
            val filesDir = getApplication<Application>().filesDir
            filesDir.deleteRecursively()
            filesDir.mkdirs()
            clearCache()
            preferences.clearAllSettings()
            _recentFiles.value = emptyList()
            _statusMessage.value = "All local data reset"
        }
    }

    fun exportSettings() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val json = preferences.getAllSettingsAsJson()
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val resolver = getApplication<Application>().contentResolver
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, "Paprivo_Settings_Backup.json")
                        put(MediaStore.MediaColumns.MIME_TYPE, "application/json")
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/PAPRIVO_BACKUP")
                    }

                    val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                    if (uri != null) {
                        resolver.openOutputStream(uri)?.use { outputStream ->
                            outputStream.write(json.toByteArray())
                        }
                        _statusMessage.postValue("Settings saved to Downloads/PAPRIVO_BACKUP")
                    } else {
                        _statusMessage.postValue("Failed to create backup file")
                    }
                } else {
                    val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    val backupDir = File(downloadsDir, "PAPRIVO_BACKUP")
                    if (!backupDir.exists()) backupDir.mkdirs()
                    
                    val file = File(backupDir, "Paprivo_Settings_Backup.json")
                    file.writeText(json)
                    
                    _statusMessage.postValue("Settings saved to Downloads/PAPRIVO_BACKUP")
                }
            } catch (e: Exception) {
                _statusMessage.postValue("Export failed: ${e.message}")
            }
        }
    }

    fun importSettings(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val inputStream = getApplication<Application>().contentResolver.openInputStream(uri)
                val json = inputStream?.bufferedReader()?.use { it.readText() }
                if (json != null) {
                    val success = preferences.restoreSettingsFromJson(json)
                    if (success) {
                        _statusMessage.postValue("Settings restored successfully")
                    } else {
                        _statusMessage.postValue("Failed to restore settings: Invalid backup file")
                    }
                }
            } catch (e: Exception) {
                _statusMessage.postValue("Import failed: ${e.message}")
            }
        }
    }

    // Helper for MutableStateFlow as we were using _statusMessage.value in some places
    // but Dispatchers.IO needs value update or post if it was LiveData.
    // Since it's StateFlow, we can just use .value if we are on main thread,
    // or just .value from IO if we don't mind.
    // Actually StateFlow.value is thread-safe.
    private fun MutableStateFlow<String?>.postValue(msg: String?) {
        this.value = msg
    }
}
