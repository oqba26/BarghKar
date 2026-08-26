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
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.oqba26.barghkar.data.sync.SyncWorker
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.milliseconds

import io.github.jan.supabase.auth.handleDeeplinks
import com.oqba26.barghkar.data.remote.SupabaseClient

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val settingsManager = (application as BarghKarApp).settingsManager

        // هندل کردن دیپ‌لینک برای تایید ایمیل
        intent?.let {
            SupabaseClient.client.handleDeeplinks(it)
        }
        
        // پاکسازی فایل‌های APK قدیمی
        val updateManager = UpdateManager(this)
        updateManager.cleanupOldApks()

        // تنظیم همگام‌سازی خودکار
        setupSync()

        enableEdgeToEdge()
        setContent {
            val selectedFont by settingsManager.selectedFont.collectAsState()
            val scope = rememberCoroutineScope()
            
            BarghKarTheme(appFont = selectedFont) {
                var updateInfo by remember { mutableStateOf<UpdateInfo?>(null) }
                var isDownloading by remember { mutableStateOf(value = false) }
                var downloadProgress by remember { mutableFloatStateOf(0f) }
                // val updateManager = remember { UpdateManager(this) } // حذف شد چون در بالا تعریف شده

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

    private fun setupSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(1, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "SupabaseSync",
            androidx.work.ExistingPeriodicWorkPolicy.KEEP,
            syncRequest,
        )
    }
}
