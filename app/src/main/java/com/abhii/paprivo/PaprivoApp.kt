package com.abhii.paprivo

import android.app.Application

import com.abhii.paprivo.data.preferences.PaprivoPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class PaprivoApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        val preferences = PaprivoPreferences(this)
        CoroutineScope(Dispatchers.IO).launch {
            if (preferences.autoClearCacheFlow.first()) {
                cacheDir.deleteRecursively()
                cacheDir.mkdirs()
            }
        }
    }
}
