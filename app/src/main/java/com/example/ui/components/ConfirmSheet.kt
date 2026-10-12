package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AppTheme

/**
 * Satu-satunya dialog konfirmasi untuk aksi berdampak: menghapus data dan memotong saldo.
 *
 * Sebelumnya aksi hapus dieksekusi langsung tanpa konfirmasi apa pun di sembilan tombol,
 * dan pembayaran tagihan dari Kalender memotong saldo tanpa bertanya.
 */
@Composable
fun ConfirmSheet(
    title: String,
    message: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    destructive: Boolean = false,
    amountPreview: Long? = null,
    hint: String? = null,
    confirmTestTag: String? = null
) {
    val tokens = AppTheme.tokens
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        shape = RoundedCornerShape(tokens.radiusSheet),
        containerColor = tokens.surface,
        title = {
            Text(
                text = title,
                style = tokens.titleStyle,
                fontWeight = FontWeight.Bold,
                color = tokens.ink
            )
        },
        text = {
            Column {
                Text(
                    text = message,
                    style = tokens.bodyStyle,
                    color = tokens.ink2
                )
                if (amountPreview != null) {
                    Spacer(modifier = Modifier.height(14.dp))
                    AppCard(
                        containerColor = if (destructive) tokens.outBg else tokens.surfaceSunken,
                        shape = RoundedCornerShape(tokens.radiusChip)
                    ) {
                        MoneyText(
                            amount = amountPreview,
                            size = MoneySize.AmountL,
                            tone = if (destructive) MoneyTone.Negative else MoneyTone.Neutral
                        )
                        if (hint != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = hint,
                                style = tokens.captionStyle,
                                color = tokens.ink3
                            )
                        }
                    }
                } else if (hint != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = hint,
                        style = tokens.captionStyle,
                        color = tokens.ink3
                    )
                }
            }
        },
        confirmButton = {
            Surface(
                onClick = {
                    onConfirm()
                },
                shape = RoundedCornerShape(tokens.radiusPill),
                color = if (destructive) tokens.danger else tokens.brand,
                contentColor = if (destructive) tokens.onDanger else tokens.onBrand,
                border = BorderStroke(tokens.borderWidth, tokens.cardBorder),
                modifier = if (confirmTestTag != null) {
                    Modifier.testTag(confirmTestTag)
                } else {
                    Modifier
                }
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = confirmLabel,
                        style = tokens.labelStyle,
                        color = if (destructive) tokens.onDanger else tokens.onBrand,
                        maxLines = 1
                    )
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Batal",
                    style = tokens.labelStyle,
                    color = tokens.ink2
                )
            }
        }
    )
}
