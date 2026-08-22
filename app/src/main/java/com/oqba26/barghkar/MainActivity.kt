package com.oqba26.barghkar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.oqba26.barghkar.ui.components.UpdateDialog
import com.oqba26.barghkar.ui.screens.MainScreen
import com.oqba26.barghkar.ui.theme.BarghKarTheme
import com.oqba26.barghkar.utils.UpdateInfo
import com.oqba26.barghkar.utils.UpdateManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val settingsManager = (application as BarghKarApp).settingsManager
        enableEdgeToEdge()
        setContent {
            val selectedFont by settingsManager.selectedFont.collectAsState()
            val scope = rememberCoroutineScope()
            
            BarghKarTheme(appFont = selectedFont) {
                var updateInfo by remember { mutableStateOf<UpdateInfo?>(null) }
                var isDownloading by remember { mutableStateOf(false) }
                var downloadProgress by remember { mutableFloatStateOf(0f) }
                val updateManager = remember { UpdateManager(this) }

                LaunchedEffect(Unit) {
                    delay(2000.milliseconds)
                    updateInfo = updateManager.checkForUpdate()
                }

                MainScreen()

                updateInfo?.let { info ->
                    UpdateDialog(
                        updateInfo = info,
                        isDownloading = isDownloading,
                        progress = downloadProgress,
                        onDismiss = { updateInfo = null },
                    ) {
                        isDownloading = true
                        val downloadId = updateManager.downloadAndInstall(info.url, "BarghKar_Update.apk")
                        if (downloadId != -1L) {
                            scope.launch {
                                updateManager.getDownloadProgress(downloadId).collect { progress ->
                                    downloadProgress = progress
                                    if (progress >= 1f) {
                                        isDownloading = false
                                        updateInfo = null
                                    }
                                }
                            }
                        } else {
                            isDownloading = false
                        }
                    }
                }
            }
        }
    }
}
