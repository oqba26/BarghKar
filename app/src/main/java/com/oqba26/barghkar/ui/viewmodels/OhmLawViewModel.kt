package com.oqba26.barghkar.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.oqba26.barghkar.domain.Calculations
import java.util.Locale

class OhmLawViewModel : ViewModel() {
    var voltage by mutableStateOf("")
    var current by mutableStateOf("")
    var resistance by mutableStateOf("")
    var power by mutableStateOf("")

    fun onVoltageChange(value: String) {
        voltage = value
        calculateOthers(from = "voltage")
    }

    fun onCurrentChange(value: String) {
        current = value
        calculateOthers(from = "current")
    }

    fun onResistanceChange(value: String) {
        resistance = value
        calculateOthers(from = "resistance")
    }

    private fun calculateOthers(from: String) {
        val v = voltage.toDoubleOrNull()
        val i = current.toDoubleOrNull()
        val r = resistance.toDoubleOrNull()

        when {
            (v != null && i != null && (from != "resistance")) -> {
                resistance = String.format(Locale.US, "%.2f", Calculations.calculateResistance(v, i))
                power = String.format(Locale.US, "%.2f", Calculations.calculatePower(v, i))
            }
            (v != null && r != null && (from != "current")) -> {
                current = String.format(Locale.US, "%.2f", Calculations.calculateCurrent(v, r))
                power = String.format(Locale.US, "%.2f", Calculations.calculatePower(v, current.toDoubleOrNull() ?: 0.0))
            }
            (i != null && r != null && (from != "voltage")) -> {
                voltage = String.format(Locale.US, "%.2f", Calculations.calculateVoltage(i, r))
                power = String.format(Locale.US, "%.2f", Calculations.calculatePower(voltage.toDoubleOrNull() ?: 0.0, i))
            }
        }
    }
    
    fun clear() {
        voltage = ""
        current = ""
        resistance = ""
        power = ""
    }
}
