package com.oqba26.barghkar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import com.oqba26.barghkar.ui.components.UpdateDialog
import com.oqba26.barghkar.ui.screens.MainScreen
import com.oqba26.barghkar.ui.theme.BarghKarTheme
import com.oqba26.barghkar.utils.UpdateInfo
import com.oqba26.barghkar.utils.UpdateManager
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val settingsManager = (application as BarghKarApp).settingsManager
        enableEdgeToEdge()
        setContent {
            val selectedFont by settingsManager.selectedFont.collectAsState()
            BarghKarTheme(appFont = selectedFont) {
                var updateInfo by remember { mutableStateOf<UpdateInfo?>(null) }
                val updateManager = remember { UpdateManager(this) }

                LaunchedEffect(Unit) {
                    delay(2000.milliseconds)
                    updateInfo = updateManager.checkForUpdate()
                }

                MainScreen()

                updateInfo?.let { info ->
                    UpdateDialog(
                        updateInfo = info,
                        onDismiss = { updateInfo = null },
                        onConfirm = {
                            updateManager.downloadAndInstall(info.url, "BarghKar_Update.apk")
                            updateInfo = null
                        }
                    )
                }
            }
        }
    }
}
