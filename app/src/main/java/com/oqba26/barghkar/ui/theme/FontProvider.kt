package com.oqba26.barghkar.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.oqba26.barghkar.R

enum class AppFont(val displayName: String, val fontFamily: FontFamily) {
    Estedad("استعداد", FontFamily(
        Font(R.font.estedad_regular, FontWeight.Normal),
        Font(R.font.estedad_light, FontWeight.Light),
        Font(R.font.estedad_medium, FontWeight.Medium),
        Font(R.font.estedad_bold, FontWeight.Bold),
        Font(R.font.estedad_black, FontWeight.Black)
    )),
    Vazirmatn("وزیر‌متن", FontFamily(
        Font(R.font.vazirmatn_regular, FontWeight.Normal),
        Font(R.font.vazirmatn_light, FontWeight.Light),
        Font(R.font.vazirmatn_medium, FontWeight.Medium),
        Font(R.font.vazirmatn_bold, FontWeight.Bold),
        Font(R.font.vazirmatn_black, FontWeight.Black),
        Font(R.font.vazirmatn_thin, FontWeight.Thin)
    )),
    Byekan("یکان", FontFamily(
        Font(R.font.byekan, FontWeight.Normal),
        Font(R.font.byekan_bold, FontWeight.Bold)
    )),
    Sahel("ساحل", FontFamily(
        Font(R.font.sahel_bold, FontWeight.Normal),
        Font(R.font.sahel_black, FontWeight.Bold)
    )),
    IranianSans("ایرانیان سنس", FontFamily(
        Font(R.font.iraniansans, FontWeight.Normal)
    ))
}
