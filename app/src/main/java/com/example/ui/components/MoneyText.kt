package com.example.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AppTheme
import com.example.util.CurrencyUtils

enum class MoneySize { Hero, AmountL, AmountM, Inline }

enum class MoneyTone { Neutral, Secondary, Positive, Negative, Due, Receivable }

/** Placeholder saat data Room belum siap — sengaja BUKAN "Rp 0". */
const val MONEY_PLACEHOLDER = "\u2014"

/**
 * Satu-satunya tempat nominal dirender.
 *
 * Menangani: format locale id-ID, tanda eksplisit, warna peran, angka tabular (`tnum`),
 * dan state belum siap. Komponen lain tidak boleh memformat rupiah sendiri.
 */
@Composable
fun MoneyText(
    amount: Long,
    modifier: Modifier = Modifier,
    size: MoneySize = MoneySize.AmountM,
    tone: MoneyTone = MoneyTone.Neutral,
    signed: Boolean = false,
    ready: Boolean = true,
    textAlign: TextAlign? = null,
    maxLines: Int = 1
) {
    val tokens = AppTheme.tokens
    val style = when (size) {
        MoneySize.Hero -> tokens.heroStyle
        MoneySize.AmountL -> tokens.amountLStyle
        MoneySize.AmountM -> tokens.amountMStyle
        MoneySize.Inline -> tokens.labelStyle
    }
    val color = when (tone) {
        MoneyTone.Neutral -> tokens.ink
        MoneyTone.Secondary -> tokens.ink2
        MoneyTone.Positive -> tokens.inText
        MoneyTone.Negative -> tokens.outText
        MoneyTone.Due -> tokens.dueText
        MoneyTone.Receivable -> tokens.receivable
    }

    Text(
        text = if (!ready) {
            MONEY_PLACEHOLDER
        } else if (signed) {
            CurrencyUtils.formatRupiahWithSign(amount)
        } else if (amount < 0) {
            // NumberFormat menghasilkan "Rp -350.000"; tampilkan sebagai "−Rp 350.000".
            "\u2212" + CurrencyUtils.formatRupiah(-amount)
        } else {
            CurrencyUtils.formatRupiah(amount)
        },
        style = style,
        color = color,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
    )
}

/**
 * Baris label + nominal dengan nominal rata kanan dan rata kolom.
 * Dipakai oleh Balance Sheet Block dan ringkasan lain.
 */
@Composable
fun MoneyRow(
    label: String,
    amount: Long,
    modifier: Modifier = Modifier,
    tone: MoneyTone = MoneyTone.Neutral,
    signed: Boolean = false,
    ready: Boolean = true
) {
    val tokens = AppTheme.tokens
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = tokens.labelStyle,
            color = tokens.ink2,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        MoneyText(
            amount = amount,
            size = MoneySize.AmountM,
            tone = tone,
            signed = signed,
            ready = ready,
            textAlign = TextAlign.End
        )
    }
}
