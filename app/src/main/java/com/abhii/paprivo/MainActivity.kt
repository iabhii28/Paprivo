package com.abhii.paprivo

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.abhii.paprivo.ui.navigation.MainNavigation
import com.abhii.paprivo.ui.theme.PaprivoTheme
import com.abhii.paprivo.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            val updateInfo by viewModel.updateInfo.collectAsState()
            val statusMessage by viewModel.statusMessage.collectAsState()
            
            val navController = rememberNavController()

            // Status Message Toast
            LaunchedEffect(statusMessage) {
                statusMessage?.let {
                    Toast.makeText(this@MainActivity, it, Toast.LENGTH_SHORT).show()
                    viewModel.clearStatusMessage()
                }
            }

            PaprivoTheme(themeMode = themeMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainNavigation(
                        navController = navController,
                        viewModel = viewModel
                    )

                    // Update Dialog
                    updateInfo?.let { info ->
                        AlertDialog(
                            onDismissRequest = { viewModel.dismissUpdateDialog() },
                            title = {
                                Text(
                                    text = if (info.isUpdateAvailable) "Update Available" else "Latest Version Installed",
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            },
                            text = {
                                Text(
                                    text = if (info.isUpdateAvailable) {
                                        "Paprivo v${info.latestVersion} is available.\n\nChangelog:\n${info.changelog}"
                                    } else {
                                        "You are using the latest version of Paprivo (v${info.currentVersion})."
                                    },
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            confirmButton = {
                                if (info.isUpdateAvailable && info.downloadUrl.isNotEmpty()) {
                                    TextButton(onClick = {
                                        viewModel.dismissUpdateDialog()
                                        try {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(info.downloadUrl)).apply {
                                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            }
                                            startActivity(intent)
                                        } catch (e: Exception) {
                                            // ignore
                                        }
                                    }) {
                                        Text("Download Update")
                                    }
                                } else {
                                    TextButton(onClick = { viewModel.dismissUpdateDialog() }) {
                                        Text("OK")
                                    }
                                }
                            },
                            dismissButton = {
                                if (info.isUpdateAvailable) {
                                    TextButton(onClick = { viewModel.dismissUpdateDialog() }) {
                                        Text("Later")
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
