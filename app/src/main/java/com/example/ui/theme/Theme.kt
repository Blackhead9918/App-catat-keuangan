package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Light Banking Color Scheme (Tema Putih Bersih & Biru Perbankan Modern)
private val LightBankingColorScheme = lightColorScheme(
    primary = BankingBluePrimary,            // Biru Perbankan #0056D2
    onPrimary = Color.White,
    primaryContainer = BankingBlueContainer, // Ice blue #EFF6FF
    onPrimaryContainer = BankingBlueDark,    // Navy pekat #003E9A
    secondary = BlueReceivable,              // Biru muda aset piutang
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F2FE),
    onSecondaryContainer = Color(0xFF0369A1),
    tertiary = GoldAccent,
    background = LightBackground,            // Slate-100 #F1F5F9 (memberikan kontras bersih terhadap kartu putih)
    onBackground = LightOnBackground,        // Slate-900 #0F172A
    surface = LightSurface,                  // Putih Murni #FFFFFF
    onSurface = LightOnSurface,              // Slate-800 #1E293B
    surfaceVariant = LightSurfaceVariant,    // Slate-200 #E2E8F0
    onSurfaceVariant = LightOnSurfaceVariant,// Slate-600 #475569
    outline = LightOutline,                  // Slate-300 #CBD5E1
    error = ExpenseRed,
    onError = Color.White
)

private val DarkBankingColorScheme = darkColorScheme(
    primary = BankingBlueLight,
    onPrimary = Color.White,
    primaryContainer = BankingBlueDark,
    onPrimaryContainer = Color(0xFFDBEAFE),
    secondary = BlueReceivable,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF1E3A8A),
    onSecondaryContainer = Color(0xFFBFDBFE),
    tertiary = GoldAccent,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    error = ExpenseRed,
    onError = Color.White
)

@Composable
fun PencatatKeuanganTheme(
    darkTheme: Boolean = false, // Default: Putih Bersih & Biru Perbankan
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkBankingColorScheme else LightBankingColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) = PencatatKeuanganTheme(darkTheme, dynamicColor, content)
