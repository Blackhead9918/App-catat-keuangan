package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppTheme

/**
 * Blok neraca: satu angka hero (kekayaan bersih) di atas, lalu tiga baris nilainya.
 *
 * Menggantikan kartu gradient bergaya kartu ATM. Keempat angka tetap lengkap —
 * user harus bisa memverifikasi sendiri angka kekayaan bersih dari komponennya
 * (kas + piutang − hutang), jadi tidak ada satu pun baris yang boleh dihilangkan.
 */
@Composable
fun BalanceSheetBlock(
    netWorth: Long,
    cash: Long,
    receivable: Long,
    payable: Long,
    ready: Boolean,
    modifier: Modifier = Modifier,
    onAdjustClick: (() -> Unit)? = null,
    testTag: String? = null
) {
    val tokens = AppTheme.tokens
    AppCard(
        modifier = modifier.then(if (testTag != null) Modifier.testTag(testTag) else Modifier),
        shape = RoundedCornerShape(tokens.radiusSheet),
        contentPadding = PaddingValues(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "KEKAYAAN BERSIH",
                style = tokens.captionStyle,
                color = tokens.ink3,
                letterSpacing = 1.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            if (onAdjustClick != null) {
                Surface(
                    onClick = onAdjustClick,
                    shape = RoundedCornerShape(tokens.radiusChip),
                    color = tokens.brandSoft,
                    border = BorderStroke(tokens.borderWidth, tokens.cardBorder),
                    modifier = Modifier.testTag("btn_adjust_balance_open")
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Atur Saldo",
                            style = tokens.labelStyle,
                            color = tokens.brand,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        MoneyText(
            amount = netWorth,
            size = MoneySize.Hero,
            ready = ready,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))
        AppDivider()
        Spacer(modifier = Modifier.height(8.dp))

        MoneyRow(label = "Kas", amount = cash, ready = ready)
        MoneyRow(label = "Piutang (di orang)", amount = receivable, ready = ready)
        MoneyRow(
            label = "Hutang (tanggungan)",
            amount = -payable,
            tone = MoneyTone.Negative,
            ready = ready
        )
    }
}

/**
 * Versi ringkas untuk Ceklis dan Aset/Hutang: satu angka + beberapa baris tanpa hero.
 */
@Composable
fun SummaryBlock(
    title: String,
    amount: Long,
    ready: Boolean,
    modifier: Modifier = Modifier,
    rows: List<Triple<String, Long, MoneyTone>> = emptyList(),
    amountTone: MoneyTone = MoneyTone.Neutral,
    testTag: String? = null
) {
    val tokens = AppTheme.tokens
    AppCard(
        modifier = modifier.then(if (testTag != null) Modifier.testTag(testTag) else Modifier),
        shape = RoundedCornerShape(tokens.radiusSheet),
        contentPadding = PaddingValues(18.dp)
    ) {
        Text(
            text = title.uppercase(),
            style = tokens.captionStyle,
            color = tokens.ink3,
            letterSpacing = 1.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(8.dp))
        MoneyText(
            amount = amount,
            size = MoneySize.AmountL,
            tone = amountTone,
            ready = ready,
            modifier = Modifier.fillMaxWidth()
        )
        if (rows.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            AppDivider()
            Spacer(modifier = Modifier.height(8.dp))
            Column {
                rows.forEach { (label, value, tone) ->
                    MoneyRow(label = label, amount = value, tone = tone, ready = ready)
                }
            }
        }
    }
}
