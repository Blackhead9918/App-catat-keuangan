package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color

/**
 * Tema aplikasi.
 *
 * Aplikasi ini **light-only** dan memakai **font sistem** — tidak ada mode gelap dan tidak
 * ada file font. `darkTheme` dan `dynamicColor` dipertahankan hanya supaya pemanggil lama
 * tetap kompilasi; keduanya tidak berpengaruh.
 *
 * `direction` memilih *arah desain* (Buku Kas / Playful Ledger), bukan mode gelap.
 */
@Suppress("UNUSED_PARAMETER")
@Composable
fun PencatatKeuanganTheme(
    direction: ThemeDirection = ThemeDirection.BUKU_KAS,
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val tokens = tokensFor(direction)
    val colorScheme = remember(tokens) { appColorScheme(tokens) }
    val typography = remember(tokens) { appTypography(tokens) }

    CompositionLocalProvider(LocalAppTokens provides tokens) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            content = content
        )
    }
}

/** Alias lama. Dipertahankan agar pemanggil lama tidak perlu diubah serentak. */
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) = PencatatKeuanganTheme(
    direction = ThemeDirection.BUKU_KAS,
    darkTheme = darkTheme,
    dynamicColor = dynamicColor,
    content = content
)

private fun appColorScheme(tokens: AppTokens) = lightColorScheme(
    primary = tokens.brand,
    onPrimary = tokens.onBrand,
    primaryContainer = tokens.brandSoft,
    onPrimaryContainer = tokens.ink,
    inversePrimary = tokens.brand,
    secondary = tokens.receivable,
    onSecondary = tokens.onBrand,
    secondaryContainer = tokens.brandSoft,
    onSecondaryContainer = tokens.ink,
    tertiary = tokens.dueText,
    onTertiary = tokens.onBrand,
    tertiaryContainer = tokens.dueBg,
    onTertiaryContainer = tokens.ink,
    background = tokens.canvas,
    onBackground = tokens.ink,
    surface = tokens.surface,
    onSurface = tokens.ink,
    surfaceVariant = tokens.surfaceSunken,
    onSurfaceVariant = tokens.ink2,
    surfaceTint = tokens.brand,
    inverseSurface = tokens.ink,
    inverseOnSurface = tokens.canvas,
    error = tokens.danger,
    onError = tokens.onDanger,
    errorContainer = tokens.outBg,
    onErrorContainer = tokens.outText,
    outline = tokens.lineStrong,
    outlineVariant = tokens.line,
    scrim = Color(0xFF000000)
)

/**
 * Peran Material 3 dipetakan dari token arah, supaya layar yang belum dimigrasikan penuh
 * ikut berubah saat user berganti tema.
 */
private fun appTypography(tokens: AppTokens) = Typography(
    headlineLarge = tokens.heroStyle,
    headlineMedium = tokens.amountLStyle,
    headlineSmall = tokens.amountLStyle,
    titleLarge = tokens.amountLStyle,
    titleMedium = tokens.titleStyle,
    titleSmall = tokens.amountMStyle,
    bodyLarge = tokens.bodyStyle,
    bodyMedium = tokens.bodyStyle,
    bodySmall = tokens.captionStyle,
    labelLarge = tokens.labelStyle,
    labelMedium = tokens.labelStyle,
    labelSmall = tokens.captionStyle
)
