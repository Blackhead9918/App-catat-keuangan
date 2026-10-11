package com.example.util

import java.text.NumberFormat
import java.util.Locale

object CurrencyUtils {
    private val localeId = Locale("id", "ID")
    private val numberFormat = NumberFormat.getIntegerInstance(localeId)

    fun formatRupiah(amount: Long): String {
        return "Rp " + numberFormat.format(amount)
    }

    fun formatRupiahWithSign(amount: Long): String {
        return when {
            amount > 0 -> "+Rp " + numberFormat.format(amount)
            amount < 0 -> "-Rp " + numberFormat.format(-amount)
            else -> "Rp 0"
        }
    }

    fun parseRupiahInput(raw: String): Long {
        if (raw.isBlank()) return 0L
        val cleaned = raw.trim().lowercase()

        // Handle shorthand notation like "150rb", "150k", "1.5jt", "1.5m"
        return try {
            if (cleaned.endsWith("rb") || cleaned.endsWith("k")) {
                val num = cleaned.replace("rb", "").replace("k", "").replace(",", ".").trim()
                (num.toDouble() * 1_000).toLong()
            } else if (cleaned.endsWith("jt") || cleaned.endsWith("m") || cleaned.endsWith("juta")) {
                val num = cleaned.replace("juta", "").replace("jt", "").replace("m", "").replace(",", ".").trim()
                (num.toDouble() * 1_000_000).toLong()
            } else {
                val digitsOnly = cleaned.filter { it.isDigit() }
                if (digitsOnly.isEmpty()) 0L else digitsOnly.toLong()
            }
        } catch (_: Exception) {
            0L
        }
    }
}
