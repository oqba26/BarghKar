package com.oqba26.barghkar.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.oqba26.barghkar.ui.theme.AppFont
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SettingsManager(context: Context) {
    private val sharedPreferences: SharedPreferences = createEncryptedPrefs(context)

    private val _selectedFont = MutableStateFlow(getSelectedFont())
    val selectedFont: StateFlow<AppFont> = _selectedFont

    private val _useEnglishNumbers = MutableStateFlow(getUseEnglishNumbers())
    val useEnglishNumbers: StateFlow<Boolean> = _useEnglishNumbers

    private fun createEncryptedPrefs(context: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            "app_settings_secure",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun getSelectedFont(): AppFont {
        val fontName = sharedPreferences.getString("selected_font", AppFont.Estedad.name)
        return try {
            AppFont.valueOf(fontName ?: AppFont.Estedad.name)
        } catch (_: Exception) {
            AppFont.Estedad
        }
    }

    fun setSelectedFont(font: AppFont) {
        sharedPreferences.edit {
            putString("selected_font", font.name)
        }
        _selectedFont.value = font
    }

    fun getUseEnglishNumbers(): Boolean {
        return sharedPreferences.getBoolean("use_english_numbers", false)
    }

    fun setUseEnglishNumbers(use: Boolean) {
        sharedPreferences.edit {
            putBoolean("use_english_numbers", use)
        }
        _useEnglishNumbers.value = use
    }
}
