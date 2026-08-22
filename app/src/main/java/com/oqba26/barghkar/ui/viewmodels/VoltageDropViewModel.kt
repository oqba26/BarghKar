package com.oqba26.barghkar.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.oqba26.barghkar.domain.Calculations
import java.util.Locale

class VoltageDropViewModel : ViewModel() {
    var length by mutableStateOf("")
    var current by mutableStateOf("")
    var resistancePerKm by mutableStateOf("")
    var voltageDrop by mutableStateOf("0.00")
    var percentageDrop by mutableStateOf("0.00")
    var sourceVoltage by mutableStateOf("220")

    fun onLengthChange(value: String) {
        length = value
        calculate()
    }

    fun onCurrentChange(value: String) {
        current = value
        calculate()
    }

    fun onResistanceChange(value: String) {
        resistancePerKm = value
        calculate()
    }
    
    fun onSourceVoltageChange(value: String) {
        sourceVoltage = value
        calculate()
    }

    private fun calculate() {
        val l = length.toDoubleOrNull() ?: 0.0
        val i = current.toDoubleOrNull() ?: 0.0
        val r = resistancePerKm.toDoubleOrNull() ?: 0.0
        val v = sourceVoltage.toDoubleOrNull() ?: 220.0

        val drop = Calculations.calculateVoltageDrop(l, i, r)
        voltageDrop = String.format(Locale.US, "%.2f", drop)
        percentageDrop = if (v != 0.0) String.format(Locale.US, "%.2f", (drop / v) * 100) else "0.00"
    }
}
