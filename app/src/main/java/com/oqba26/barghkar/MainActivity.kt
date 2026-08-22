package com.oqba26.barghkar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.oqba26.barghkar.ui.screens.MainScreen
import com.oqba26.barghkar.ui.theme.BarghKarTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val settingsManager = (application as BarghKarApp).settingsManager
        enableEdgeToEdge()
        setContent {
            val selectedFont by settingsManager.selectedFont.collectAsState()
            BarghKarTheme(appFont = selectedFont) {
                MainScreen()
            }
        }
    }
}
