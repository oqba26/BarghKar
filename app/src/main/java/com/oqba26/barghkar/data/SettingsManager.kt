package com.oqba26.barghkar.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.oqba26.barghkar.ui.theme.AppFont
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SettingsManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    private val _selectedFont = MutableStateFlow(getSelectedFont())
    val selectedFont: StateFlow<AppFont> = _selectedFont

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
}
