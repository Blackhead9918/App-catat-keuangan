package com.example.ui.theme

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.TextStyle

/**
 * Teks memakai font sistem (keputusan proyek: light-only + font sistem, tidak ada file font).
 *
 * [tabular] mengaktifkan `tnum` supaya lebar digit seragam, sehingga kolom nominal
 * bertumpuk rapi secara vertikal. Ini berlaku untuk angka, bukan teks naratif.
 */
fun appTextStyle(
    size: Int,
    weight: FontWeight,
    lineHeight: Int,
    letterSpacing: Double,
    tabular: Boolean = false
): TextStyle = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = letterSpacing.sp,
    fontFeatureSettings = if (tabular) "tnum" else null,
    textAlign = TextAlign.Unspecified
)
