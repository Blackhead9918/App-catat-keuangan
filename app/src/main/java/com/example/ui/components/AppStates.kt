package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AppTheme

/**
 * Tombol aksi utama (pil penuh). Elemen yang bisa ditekan memakai bayangan keras di
 * Arah C, dan tanpa bayangan di Arah A.
 */
@Composable
fun AppPillButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color? = null,
    contentColor: Color? = null,
    testTag: String? = null
) {
    val tokens = AppTheme.tokens
    val shape = RoundedCornerShape(tokens.radiusPill)
    HardShadowBox(shape = shape, modifier = modifier) {
        Surface(
            onClick = onClick,
            shape = shape,
            color = containerColor ?: tokens.brand,
            contentColor = contentColor ?: tokens.onBrand,
            border = BorderStroke(tokens.borderWidth, tokens.cardBorder),
            modifier = Modifier
                .fillMaxWidth()
                .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
        ) {
            Box(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    style = tokens.labelStyle,
                    color = contentColor ?: tokens.onBrand,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * Empty state yang selalu tampil — bagian tidak lagi disembunyikan begitu saja, sehingga
 * user tahu fungsi itu ada dan tahu langkah berikutnya.
 */
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    testTag: String? = null
) {
    val tokens = AppTheme.tokens
    AppCard(
        modifier = modifier.then(if (testTag != null) Modifier.testTag(testTag) else Modifier),
        shape = RoundedCornerShape(tokens.radiusCard),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            IconTile(
                icon = icon,
                tint = tokens.brand,
                background = tokens.brandSoft,
                tileSize = 42.dp,
                iconSize = 22.dp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = tokens.titleStyle,
                color = tokens.ink,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = tokens.captionStyle,
                color = tokens.ink3,
                textAlign = TextAlign.Center
            )
            if (actionLabel != null && onAction != null) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.Center) {
                    Surface(
                        onClick = onAction,
                        shape = RoundedCornerShape(tokens.radiusChip),
                        color = tokens.brandSoft,
                        border = BorderStroke(tokens.borderWidth, tokens.cardBorder)
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = actionLabel,
                                style = tokens.labelStyle,
                                color = tokens.brand,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Placeholder loading. Dirender selama data Room belum siap, menggantikan "Rp 0". */
@Composable
fun SkeletonBlock(
    height: Dp,
    modifier: Modifier = Modifier,
    widthFraction: Float = 1f
) {
    val tokens = AppTheme.tokens
    Surface(
        color = tokens.surfaceSunken,
        shape = RoundedCornerShape(6.dp),
        modifier = modifier
            .fillMaxWidth(widthFraction)
            .height(height)
    ) {}
}

/** Baris nominal ringkas dengan label di kiri dan nilai rata kanan. */
@Composable
fun ValueLine(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color? = null
) {
    val tokens = AppTheme.tokens
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = tokens.captionStyle,
            color = tokens.ink3,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            style = tokens.labelStyle,
            color = valueColor ?: tokens.ink,
            maxLines = 1
        )
    }
}
