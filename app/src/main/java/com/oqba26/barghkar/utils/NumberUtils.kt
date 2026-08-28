package com.oqba26.barghkar.utils

import java.text.NumberFormat
import java.util.Locale
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

object NumberUtils {
    fun formatNumber(number: Any, useEnglish: Boolean = false): String {
        val locale = if (useEnglish) Locale.ENGLISH else Locale("fa", "IR")
        val formatter = NumberFormat.getInstance(locale)

        return when (number) {
            is Number -> formatter.format(number)
            is String -> {
                val clean = number.trim()
                val parsed = clean.toDoubleOrNull()
                // اگر عدد بود و شماره تلفن نبود (با 0 شروع نمی‌شد)، با فرمت‌کننده استاندارد نمایش بده (مثلاً برای جداکننده هزارگان)
                if (parsed != null && !clean.startsWith("0")) {
                    formatter.format(parsed)
                } else {
                    // برای شماره تلفن یا موارد مشابه، فقط اعداد رو فارسی کن
                    if (useEnglish) clean else localizeDigits(clean)
                }
            }
            else -> number.toString()
        }
    }

    fun formatPrice(price: Long, useEnglish: Boolean = false): String {
        return formatNumber(price, useEnglish)
    }

    fun localizeDigits(input: String): String {
        val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
        return input.map { char ->
            if (char in '0'..'9') persianDigits[char - '0'] else char
        }.joinToString("")
    }

    fun englishizeDigits(input: String): String {
        val englishDigits = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
        return input.map { char ->
            if (char in '۰'..'۹') englishDigits[char - '۰'] else char
        }.joinToString("")
    }

    fun getPersianNumberTransformation() = VisualTransformation { text ->
        TransformedText(
            AnnotatedString(localizeDigits(text.text)),
            OffsetMapping.Identity
        )
    }
}
