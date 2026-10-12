package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * ARAH A — "BUKU KAS"
 *
 * Prinsip: kartu didefinisikan oleh garis 1px, bukan bayangan. Biru dipakai hemat
 * (hanya CTA, tab aktif, aksi teks). Semua nominal tabular dan rata kanan.
 *
 * Rasio kontras teks (dihitung dengan formula luminance relatif WCAG 2.1):
 *   ink   / surface  17.75:1   ink2 / surface  5.98:1   ink3 / surface  4.97:1
 *   brand / surface   6.44:1   inText / inBg   4.84:1   outText / outBg  5.45:1
 *   dueText / dueBg   6.25:1   receivable / surface 5.93:1
 */
val BukuKasTokens = AppTokens(
    canvas = Color(0xFFF6F7F9),
    surface = Color(0xFFFFFFFF),
    surfaceSunken = Color(0xFFF1F3F6),
    line = Color(0xFFE6E9EF),
    lineStrong = Color(0xFFD0D5DD),

    ink = Color(0xFF101828),
    ink2 = Color(0xFF5B6472),
    ink3 = Color(0xFF667085),
    inkDisabled = Color(0xFF98A2B3),

    brand = Color(0xFF0056D2),
    brandSoft = Color(0xFFEFF6FF),
    onBrand = Color(0xFFFFFFFF),

    inText = Color(0xFF047857),
    inBg = Color(0xFFD1FAE5),
    outText = Color(0xFFB42318),
    outBg = Color(0xFFFEE4E2),
    dueText = Color(0xFF92400E),
    dueBg = Color(0xFFFEF0C7),
    receivable = Color(0xFF0369A1),

    danger = Color(0xFFB42318),
    onDanger = Color(0xFFFFFFFF),

    radiusSheet = 20.dp,
    radiusCard = 14.dp,
    radiusChip = 10.dp,
    radiusPill = 999.dp,
    borderWidth = 1.dp,
    cardBorder = Color(0xFFE6E9EF),
    hardShadow = 0.dp,
    shadowColor = Color(0xFF101828),

    heroStyle = appTextStyle(32, FontWeight.Bold, 38, -0.6, tabular = true),
    amountLStyle = appTextStyle(24, FontWeight.SemiBold, 30, -0.3, tabular = true),
    amountMStyle = appTextStyle(16, FontWeight.SemiBold, 22, 0.0, tabular = true),
    titleStyle = appTextStyle(17, FontWeight.SemiBold, 24, -0.1),
    bodyStyle = appTextStyle(15, FontWeight.Normal, 22, 0.0),
    labelStyle = appTextStyle(13, FontWeight.Medium, 18, 0.1),
    captionStyle = appTextStyle(12, FontWeight.Medium, 16, 0.2),

    appTitleStyle = appTextStyle(24, FontWeight.Bold, 30, -0.5),
    titleHighlight = null
)

/**
 * ARAH C — "PLAYFUL LEDGER"
 *
 * Prinsip: border 2dp + bayangan keras tanpa blur, hanya pada elemen yang bisa ditekan.
 * Elemen statis (label, pemisah) tidak memakai border dan bayangan.
 *
 * Rasio kontras teks:
 *   ink / canvas 16.88:1   ink2 / surface 7.56:1   ink3 / canvas 4.60:1
 *   brand / surface 5.17:1   inText / inBg 4.57:1   outText / outBg 5.38:1
 *   dueText / dueBg 6.09:1
 * Catatan: dueText di atas kuning sorot (#FDE047) hanya 3.81:1 — pasangan itu
 * hanya untuk ikon, dan teks di atas sorotan selalu memakai ink (13.46:1).
 */
val PlayfulLedgerTokens = AppTokens(
    canvas = Color(0xFFFDF9F0),
    surface = Color(0xFFFFFFFF),
    surfaceSunken = Color(0xFFFDF9F0),
    line = Color(0xFF111827),
    lineStrong = Color(0xFF111827),

    ink = Color(0xFF111827),
    ink2 = Color(0xFF4B5563),
    ink3 = Color(0xFF6B7280),
    inkDisabled = Color(0xFF9CA3AF),

    brand = Color(0xFF2563EB),
    brandSoft = Color(0xFFDBEAFE),
    onBrand = Color(0xFFFFFFFF),

    inText = Color(0xFF15803D),
    inBg = Color(0xFFDCFCE7),
    outText = Color(0xFFB42318),
    outBg = Color(0xFFFEE2E2),
    dueText = Color(0xFF92400E),
    dueBg = Color(0xFFFEF08A),
    receivable = Color(0xFF0369A1),

    danger = Color(0xFFB42318),
    onDanger = Color(0xFFFFFFFF),

    radiusSheet = 18.dp,
    radiusCard = 18.dp,
    radiusChip = 12.dp,
    radiusPill = 999.dp,
    borderWidth = 2.dp,
    cardBorder = Color(0xFF111827),
    hardShadow = 4.dp,
    shadowColor = Color(0xFF111827),

    heroStyle = appTextStyle(34, FontWeight.ExtraBold, 40, -0.8, tabular = true),
    amountLStyle = appTextStyle(26, FontWeight.Bold, 32, -0.4, tabular = true),
    amountMStyle = appTextStyle(18, FontWeight.Bold, 24, -0.2, tabular = true),
    titleStyle = appTextStyle(17, FontWeight.Bold, 23, 0.0),
    bodyStyle = appTextStyle(15, FontWeight.Normal, 22, 0.0),
    labelStyle = appTextStyle(13, FontWeight.SemiBold, 18, 0.1),
    captionStyle = appTextStyle(12, FontWeight.SemiBold, 16, 0.3),

    appTitleStyle = appTextStyle(26, FontWeight.ExtraBold, 32, -0.8),
    titleHighlight = Color(0xFFFDE047)
)

fun tokensFor(direction: ThemeDirection): AppTokens = when (direction) {
    ThemeDirection.BUKU_KAS -> BukuKasTokens
    ThemeDirection.PLAYFUL_LEDGER -> PlayfulLedgerTokens
}
