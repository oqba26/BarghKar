package com.oqba26.barghkar.domain

import androidx.compose.ui.graphics.Color

data class WireColor(
    val type: String,
    val colorName: String,
    val color: Color,
    val description: String,
)

data class ElectricalSymbol(
    val name: String,
    val description: String,
    val iconName: String // We will map this to a Material Icon
)

object ReferenceData {
    val iecColors = listOf(
        WireColor("فاز 1 (L1)", "قهوه‌ای", Color(0xFF8B4513), "استاندارد جدید IEC"),
        WireColor("فاز 2 (L2)", "سیاه", Color(0xFF000000), "استاندارد جدید IEC"),
        WireColor("فاز 3 (L3)", "خاکستری", Color(0xFF808080), "استاندارد جدید IEC"),
        WireColor("نول (N)", "آبی", Color(0xFF0000FF), "استاندارد جهانی"),
        WireColor("ارت (PE)", "سبز/زرد", Color(0xFFADFF2F), "اتصال زمین ایمنی")
    )

    val commonSymbols = listOf(
        ElectricalSymbol("لامپ", "نقطه روشنایی معمولی", "Lightbulb"),
        ElectricalSymbol("کلید یک‌پل", "قطع و وصل مدار تکی", "ToggleOn"),
        ElectricalSymbol("پریز", "خروجی برق ۲۲۰ ولت", "Outlet"),
        ElectricalSymbol("فیوز", "محافظ مدار در برابر اضافه بار", "Security"),
        ElectricalSymbol("موتور", "مصرف کننده سه‌فاز یا تک‌فاز", "Settings")
    )
}
