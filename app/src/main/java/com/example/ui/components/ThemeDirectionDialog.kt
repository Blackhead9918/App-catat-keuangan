package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AppTheme
import com.example.ui.theme.ThemeDirection
import com.example.ui.theme.tokensFor

/**
 * Pemilih arah desain. Menampilkan pratinjau palet tiap arah supaya user bisa melihat
 * perbedaannya sebelum memilih, tanpa harus keluar dari dialog.
 */
@Composable
fun ThemeDirectionDialog(
    current: ThemeDirection,
    onSelect: (ThemeDirection) -> Unit,
    onDismiss: () -> Unit
) {
    val tokens = AppTheme.tokens
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(tokens.radiusSheet),
        containerColor = tokens.surface,
        title = {
            Text(
                text = "Tema Tampilan",
                style = tokens.titleStyle,
                fontWeight = FontWeight.Bold,
                color = tokens.ink
            )
        },
        text = {
            Column {
                Text(
                    text = "Pilih tampilan aplikasi. Struktur, alur, dan datanya tetap sama — hanya warna, bentuk, dan tipografinya yang berubah.",
                    style = tokens.bodyStyle,
                    color = tokens.ink2
                )
                Spacer(modifier = Modifier.height(14.dp))
                ThemeDirection.entries.forEach { direction ->
                    DirectionOption(
                        direction = direction,
                        selected = direction == current,
                        onSelect = { onSelect(direction) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Tutup",
                    style = tokens.labelStyle,
                    color = tokens.ink2
                )
            }
        }
    )
}

@Composable
private fun DirectionOption(
    direction: ThemeDirection,
    selected: Boolean,
    onSelect: () -> Unit
) {
    val tokens = AppTheme.tokens
    val preview = tokensFor(direction)
    val shape = RoundedCornerShape(tokens.radiusCard)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (selected) tokens.brandSoft else tokens.surface)
            .border(
                width = if (selected) 2.dp else tokens.borderWidth,
                color = if (selected) tokens.brand else tokens.cardBorder,
                shape = shape
            )
            .clickable(onClick = onSelect)
            .padding(14.dp)
            .testTag("theme_option_${direction.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = direction.label,
                style = tokens.titleStyle,
                color = tokens.ink
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = direction.description,
                style = tokens.captionStyle,
                color = tokens.ink2
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            listOf(preview.canvas, preview.brand, preview.inText, preview.dueBg).forEach { swatch ->
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(swatch)
                        .border(1.dp, tokens.cardBorder, CircleShape)
                )
            }
        }
        if (selected) {
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Sedang dipakai",
                tint = tokens.brand,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
