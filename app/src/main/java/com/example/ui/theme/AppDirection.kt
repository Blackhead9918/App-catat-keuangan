package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp

/**
 * Arah desain aplikasi.
 *
 * Ini BUKAN mode terang/gelap — aplikasinya light-only. Yang dipilih di sini adalah
 * bahasa visualnya, dan keduanya memakai struktur informasi, alur, serta komponen yang sama.
 */
enum class ThemeDirection(
    val id: String,
    val label: String,
    val description: String
) {
    BUKU_KAS(
        id = "buku_kas",
        label = "Buku Kas",
        description = "Tenang dan presisi. Tanpa bayangan, angka rata kolom, biru dipakai hemat."
    ),
    PLAYFUL_LEDGER(
        id = "playful_ledger",
        label = "Playful Ledger",
        description = "Hangat dan ekspresif. Border tebal, bayangan keras, kuning sorot, tebal dan tegas."
    );

    companion object {
        fun fromId(id: String?): ThemeDirection =
            entries.firstOrNull { it.id == id } ?: BUKU_KAS
    }
}

/**
 * Seluruh keputusan visual satu arah desain, dalam satu tempat.
 *
 * Komponen UI membaca nilai dari sini lewat [AppTheme] dan tidak boleh menulis warna,
 * radius, atau ukuran teks sendiri.
 */
@Immutable
data class AppTokens(
    // ---- Permukaan ----
    val canvas: Color,
    val surface: Color,
    val surfaceSunken: Color,
    val line: Color,
    val lineStrong: Color,

    // ---- Teks ----
    val ink: Color,
    val ink2: Color,
    val ink3: Color,
    /** Hanya untuk elemen non-teks (ikon mati). Di bawah 4.5:1 — jangan dipakai untuk teks. */
    val inkDisabled: Color,

    // ---- Identitas ----
    val brand: Color,
    val brandSoft: Color,
    val onBrand: Color,

    // ---- Semantik uang ----
    val inText: Color,
    val inBg: Color,
    val outText: Color,
    val outBg: Color,
    val dueText: Color,
    val dueBg: Color,
    val receivable: Color,

    // ---- Aksi destruktif (dipisah dari "uang keluar") ----
    val danger: Color,
    val onDanger: Color,

    // ---- Bentuk ----
    val radiusSheet: Dp,
    val radiusCard: Dp,
    val radiusChip: Dp,
    val radiusPill: Dp,
    val borderWidth: Dp,
    val cardBorder: Color,
    /** 0.dp = tanpa bayangan. > 0.dp = bayangan keras tanpa blur sebesar nilai ini. */
    val hardShadow: Dp,
    val shadowColor: Color,

    // ---- Tipografi ----
    val heroStyle: TextStyle,
    val amountLStyle: TextStyle,
    val amountMStyle: TextStyle,
    val titleStyle: TextStyle,
    val bodyStyle: TextStyle,
    val labelStyle: TextStyle,
    val captionStyle: TextStyle,

    /** Judul pada header tiap layar. */
    val appTitleStyle: TextStyle,
    /** Sorotan di belakang judul layar, atau null bila arah ini tidak memakainya. */
    val titleHighlight: Color?
)

val LocalAppTokens = staticCompositionLocalOf { BukuKasTokens }

/** Titik akses tunggal ke token arah yang sedang aktif. */
object AppTheme {
    val tokens: AppTokens
        @Composable
        @ReadOnlyComposable
        get() = LocalAppTokens.current
}
