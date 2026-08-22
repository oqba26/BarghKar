package com.oqba26.barghkar.domain

import kotlin.math.pow

object Calculations {
    
    // Ohm's Law
    fun calculateVoltage(current: Double, resistance: Double): Double = current * resistance
    fun calculateCurrent(voltage: Double, resistance: Double): Double = if (resistance != 0.0) voltage / resistance else 0.0
    fun calculateResistance(voltage: Double, current: Double): Double = if (current != 0.0) voltage / current else 0.0
    fun calculatePower(voltage: Double, current: Double): Double = voltage * current

    // Voltage Drop (Single Phase)
    // V_drop = (2 * L * I * R_per_km) / 1000
    fun calculateVoltageDrop(length: Double, current: Double, resistancePerKm: Double): Double {
        return (2 * length * current * resistancePerKm) / 1000
    }

    // AWG to mm2
    fun awgToMm2(awg: Double): Double {
        return 0.012668 * 92.0.pow((36.0 - awg) / 39.0)
    }
}
