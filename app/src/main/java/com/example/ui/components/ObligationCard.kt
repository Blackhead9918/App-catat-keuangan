package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AppTheme

enum class ObligationState { Overdue, DueToday, DueSoon, Scheduled, Done }

/**
 * SATU kartu untuk semua kewajiban bertanggal: tagihan berulang, gajian terjadwal,
 * piutang, dan hutang. Menggantikan empat bentuk kartu berbeda yang sebelumnya ada.
 *
 * Aturan anti-padat: maksimum satu nominal dan satu label status per kartu.
 */
@Composable
fun ObligationCard(
    title: String,
    amount: Long,
    state: ObligationState,
    statusText: String,
    modifier: Modifier = Modifier,
    metaText: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    signedAmount: Boolean = false,
    amountTone: MoneyTone? = null,
    ready: Boolean = true,
    cardTestTag: String? = null,
    actionTestTag: String? = null
) {
    val tokens = AppTheme.tokens
    val shape = RoundedCornerShape(tokens.radiusCard)

    val accent: Color = when (state) {
        ObligationState.Overdue -> tokens.outText
        ObligationState.DueToday -> tokens.dueText
        ObligationState.DueSoon -> tokens.dueText
        ObligationState.Scheduled -> tokens.line
        ObligationState.Done -> tokens.inText
    }
    val tileBg: Color = when (state) {
        ObligationState.Overdue -> tokens.outBg
        ObligationState.DueToday -> tokens.dueBg
        ObligationState.DueSoon -> tokens.dueBg
        ObligationState.Scheduled -> tokens.surfaceSunken
        ObligationState.Done -> tokens.inBg
    }
    val icon: ImageVector = when (state) {
        ObligationState.Overdue -> Icons.Default.Warning
        ObligationState.DueToday -> Icons.Default.AccessTime
        ObligationState.DueSoon -> Icons.Default.Event
        ObligationState.Scheduled -> Icons.Default.Schedule
        ObligationState.Done -> Icons.Default.CheckCircle
    }
    val resolvedAmountTone = amountTone ?: when (state) {
        ObligationState.Done -> MoneyTone.Secondary
        else -> MoneyTone.Neutral
    }
    val urgent = state == ObligationState.Overdue || state == ObligationState.DueToday

    HardShadowBox(shape = shape, modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(tokens.surface)
                .border(tokens.borderWidth, tokens.cardBorder, shape)
                .drawBehind {
                    drawRect(
                        color = accent,
                        size = Size(4.dp.toPx(), size.height)
                    )
                }
                .then(if (cardTestTag != null) Modifier.testTag(cardTestTag) else Modifier)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 14.dp, top = 14.dp, bottom = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconTile(
                    icon = icon,
                    tint = accent,
                    background = tileBg,
                    contentDescription = null
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = tokens.titleStyle,
                        color = tokens.ink,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = statusText,
                        style = tokens.captionStyle,
                        color = accent,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!metaText.isNullOrBlank()) {
                        Text(
                            text = metaText,
                            style = tokens.captionStyle,
                            color = tokens.ink3,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(horizontalAlignment = Alignment.End) {
                    MoneyText(
                        amount = amount,
                        size = MoneySize.AmountM,
                        tone = resolvedAmountTone,
                        signed = signedAmount,
                        ready = ready
                    )
                    if (actionLabel != null && onAction != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            onClick = onAction,
                            shape = RoundedCornerShape(tokens.radiusChip),
                            color = if (urgent) accent else tokens.surface,
                            contentColor = if (urgent) Color.White else tokens.ink,
                            border = BorderStroke(tokens.borderWidth, tokens.cardBorder),
                            modifier = Modifier
                                .defaultMinSize(minWidth = 48.dp)
                                .then(
                                    if (actionTestTag != null) Modifier.testTag(actionTestTag) else Modifier
                                )
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = actionLabel,
                                    style = tokens.labelStyle,
                                    color = if (urgent) Color.White else tokens.ink,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
