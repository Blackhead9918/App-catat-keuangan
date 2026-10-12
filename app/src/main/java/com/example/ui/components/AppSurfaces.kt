package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppTheme

/**
 * Wadah untuk elemen yang bisa ditekan. Bayangan keras (Arah C) hanya digambar bila
 * arah tersebut memintanya; Arah A merender tanpa bayangan sama sekali.
 *
 * Ruang untuk bayangan direservasi lewat padding, sehingga bayangan tidak pernah
 * menutupi kartu tetangga.
 */
@Composable
fun HardShadowBox(
    shape: Shape,
    modifier: Modifier = Modifier,
    shadow: Dp = AppTheme.tokens.hardShadow,
    shadowColor: Color = AppTheme.tokens.shadowColor,
    content: @Composable BoxScope.() -> Unit
) {
    Box(modifier = modifier.padding(end = shadow, bottom = shadow)) {
        if (shadow > 0.dp) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(x = shadow, y = shadow)
                    .background(color = shadowColor, shape = shape)
            )
        }
        content()
    }
}

/**
 * Kartu dasar aplikasi. Selalu `surface` + border dari token, bukan warna literal.
 */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    shape: Shape? = null,
    containerColor: Color? = null,
    borderColor: Color? = null,
    elevated: Boolean = false,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val tokens = AppTheme.tokens
    val resolvedShape = shape ?: RoundedCornerShape(tokens.radiusCard)

    HardShadowBox(
        shape = resolvedShape,
        modifier = modifier,
        shadow = if (elevated) tokens.hardShadow else 0.dp
    ) {
        val base = Modifier
            .fillMaxWidth()
            .clip(resolvedShape)
            .background(containerColor ?: tokens.surface)
            .border(tokens.borderWidth, borderColor ?: tokens.cardBorder, resolvedShape)

        Box(modifier = if (onClick != null) base.clickable(onClick = onClick) else base) {
            Column(modifier = Modifier.padding(contentPadding), content = content)
        }
    }
}

@Composable
fun AppDivider(
    modifier: Modifier = Modifier,
    color: Color? = null,
    thickness: Dp = 1.dp
) {
    val tokens = AppTheme.tokens
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(thickness)
            .background(color ?: tokens.line)
    )
}

/** Kotak ikon berwarna. Di Arah C ikut memakai border tebal. */
@Composable
fun IconTile(
    icon: ImageVector,
    tint: Color,
    background: Color,
    contentDescription: String? = null,
    modifier: Modifier = Modifier,
    tileSize: Dp = 36.dp,
    iconSize: Dp = 18.dp
) {
    val tokens = AppTheme.tokens
    val shape = RoundedCornerShape(tokens.radiusChip)
    Box(
        modifier = modifier
            .size(tileSize)
            .clip(shape)
            .background(background)
            .border(tokens.borderWidth, tokens.cardBorder, shape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(iconSize)
        )
    }
}

/** Header layar. Menyatu dengan canvas, bukan bilah berwarna penuh. */
@Composable
fun AppHeader(
    title: String,
    subtitle: String? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val tokens = AppTheme.tokens
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(tokens.canvas)
            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = tokens.appTitleStyle,
                color = tokens.ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = tokens.titleHighlight
                    ?.let { highlight -> Modifier.background(highlight, RoundedCornerShape(3.dp)) }
                    ?: Modifier
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = tokens.captionStyle,
                    color = tokens.ink3,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            content = actions
        )
    }
}

/** Tombol ikon bulat/kotak dengan area ketuk minimum 48dp. */
@Composable
fun HeaderIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String? = null
) {
    val tokens = AppTheme.tokens
    val shape = RoundedCornerShape(tokens.radiusChip)
    HardShadowBox(shape = shape, modifier = modifier, shadow = tokens.hardShadow) {
        Surface(
            onClick = onClick,
            shape = shape,
            color = tokens.surface,
            border = BorderStroke(tokens.borderWidth, tokens.cardBorder),
            modifier = Modifier
                .size(40.dp)
                .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    tint = tokens.ink,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    trailingText: String? = null,
    trailingAction: (() -> Unit)? = null,
    trailingColor: Color? = null
) {
    val tokens = AppTheme.tokens
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title.uppercase(),
            style = tokens.captionStyle,
            fontWeight = FontWeight.SemiBold,
            color = tokens.ink3,
            letterSpacing = 1.2.sp
        )
        if (trailingText != null) {
            Text(
                text = trailingText,
                style = tokens.captionStyle,
                color = trailingColor ?: tokens.ink3,
                modifier = if (trailingAction != null) {
                    Modifier.clickable(onClick = trailingAction)
                } else {
                    Modifier
                }
            )
        }
    }
}
