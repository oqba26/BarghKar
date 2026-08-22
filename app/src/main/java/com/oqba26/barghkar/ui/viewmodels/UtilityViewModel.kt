package com.oqba26.barghkar.ui.viewmodels

import android.app.Application
import android.content.Context
import android.hardware.camera2.CameraManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.oqba26.barghkar.domain.Calculations
import java.util.Locale

class UtilityViewModel(application: Application) : AndroidViewModel(application) {
    
    // Unit Converter State
    var awgValue by mutableStateOf("")
    var mm2Value by mutableStateOf("")

    fun onAwgChange(value: String) {
        awgValue = value
        val awg = value.toDoubleOrNull()
        mm2Value = if (awg != null) {
            String.format(Locale.US, "%.2f", Calculations.awgToMm2(awg))
        } else {
            ""
        }
    }

    // Flashlight State
    var isFlashlightOn by mutableStateOf(value = false)
        private set

    private val cameraManager = application.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private var cameraId: String? = null

    init {
        try {
            cameraId = cameraManager.cameraIdList.firstOrNull()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun toggleFlashlight() {
        val id = cameraId ?: return
        try {
            isFlashlightOn = !isFlashlightOn
            cameraManager.setTorchMode(id, isFlashlightOn)
        } catch (e: Exception) {
            isFlashlightOn = false
            e.printStackTrace()
        }
    }
}
