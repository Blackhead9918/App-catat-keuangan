package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Tema Putih Bersih & Biru Perbankan (Clean White & Banking Blue Theme)
// Terinspirasi dari aplikasi perbankan modern terkemuka (BCA, Livin' Mandiri, BRImo, Blu BCA)
val BankingBluePrimary = Color(0xFF0056D2)       // Royal Blue perbankan modern
val BankingBlueDark = Color(0xFF003E9A)          // Deep Navy Blue
val BankingBlueNavy = Color(0xFF0A192F)          // Midnight Navy untuk teks pekat
val BankingBlueLight = Color(0xFF1E88E5)         // Sky Blue accent
val BankingBlueContainer = Color(0xFFEFF6FF)     // Ice Blue container lembut
val BankingBlueBorder = Color(0xFFBFDBFE)        // Border biru muda

// Gradient Khusus Kartu ATM / Rekening Bank
val CardGradientStart = Color(0xFF003E9A)        // Deep Navy
val CardGradientMid = Color(0xFF0056D2)          // Royal Blue
val CardGradientEnd = Color(0xFF1E88E5)          // Vibrant Bank Blue

// Aset, Pemasukan & Pengeluaran
val IncomeGreen = Color(0xFF10B981)              // Hijau uang masuk / gajian
val IncomeGreenContainer = Color(0xFFECFDF5)
val ExpenseRed = Color(0xFFEF4444)               // Merah tagihan / pengeluaran
val ExpenseRedContainer = Color(0xFFFEF2F2)
val GoldAccent = Color(0xFFF59E0B)               // Emas aksen EMV chip kartu
val BlueReceivable = Color(0xFF0284C7)           // Biru cyan untuk aset piutang

// Light Banking Theme (Putih Bersih Utama)
val LightBackground = Color(0xFFF1F5F9)          // Slate-100: memberikan kontras sempurna untuk kartu putih murni
val LightSurface = Color(0xFFFFFFFF)             // Putih murni untuk kartu & panel
val LightSurfaceVariant = Color(0xFFE2E8F0)      // Slate-200
val LightOutline = Color(0xFFCBD5E1)             // Slate-300: border kartu rapi & presisi
val LightOnBackground = Color(0xFF0F172A)        // Slate-900: teks gelap tegas
val LightOnSurface = Color(0xFF1E293B)           // Slate-800
val LightOnSurfaceVariant = Color(0xFF475569)    // Slate-600: teks sekunder

// Dark Banking Theme
val DarkBackground = Color(0xFF0B1320)           // Dark Navy Night
val DarkSurface = Color(0xFF131F33)              // Surface Navy
val DarkSurfaceVariant = Color(0xFF1A2B47)
val DarkOutline = Color(0xFF2E446B)
val DarkOnBackground = Color(0xFFF8FAFC)
val DarkOnSurface = Color(0xFFE2E8F0)
val DarkOnSurfaceVariant = Color(0xFF94A3B8)

// Backward-compatible alias for existing references
val EmeraldPrimary = BankingBluePrimary
val MintAccent = IncomeGreen
