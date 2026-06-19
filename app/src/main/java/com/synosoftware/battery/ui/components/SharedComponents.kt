package com.synosoftware.battery.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text as AppText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.util.Locale

@Composable
fun SectionHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        AppText(
            title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )
        AppText(
            subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun LabelValueRow(
    label: String,
    value: String,
    evidence: String,
    compactEvidence: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            AppText(
                label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            AppText(
                value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        EvidenceBadge(
            text = evidence,
            compact = compactEvidence,
        )
    }
}

@Composable
fun EvidenceBadge(
    text: String,
    compact: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val tone = evidenceTone(text)
    val displayText = if (compact) {
        text.firstOrNull()?.uppercaseChar()?.toString().orEmpty()
    } else {
        text
    }

    Surface(
        modifier = modifier,
        color = tone.container,
        contentColor = tone.content,
        shape = RoundedCornerShape(999.dp),
        border = BorderStroke(1.dp, tone.border),
        tonalElevation = 0.dp,
    ) {
        AppText(
            text = displayText,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = if (compact) 8.dp else 12.dp, vertical = 5.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun MetricTile(
    title: String,
    value: String,
    evidence: String,
    iconRes: Int? = null,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
        tonalElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (iconRes != null) {
                IconBadge(
                    resId = iconRes,
                    contentDescription = null,
                )
            }
            AppText(
                title.uppercase(Locale.ROOT),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            AppText(
                value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
            )
            EvidenceBadge(
                text = evidence,
                compact = false,
            )
        }
    }
}

@Composable
fun IconBadge(
    @DrawableRes resId: Int,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.primary,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
        tonalElevation = 0.dp,
    ) {
        LucideIcon(
            resId = resId,
            contentDescription = contentDescription,
            modifier = Modifier.size(18.dp).padding(8.dp),
        )
    }
}

private data class EvidenceTone(
    val container: Color,
    val content: Color,
    val border: Color,
)

@Composable
private fun evidenceTone(text: String): EvidenceTone {
    return when {
        text.contains("Measured", ignoreCase = true) -> EvidenceTone(
            container = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
            content = MaterialTheme.colorScheme.primary,
            border = MaterialTheme.colorScheme.primary.copy(alpha = 0.38f),
        )
        text.contains("Estimated", ignoreCase = true) -> EvidenceTone(
            container = MaterialTheme.colorScheme.surfaceContainerLowest,
            content = MaterialTheme.colorScheme.primary,
            border = MaterialTheme.colorScheme.primary.copy(alpha = 0.75f),
        )
        text.contains("Inferred", ignoreCase = true) -> EvidenceTone(
            container = MaterialTheme.colorScheme.surfaceContainerHigh,
            content = MaterialTheme.colorScheme.onSurfaceVariant,
            border = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.9f),
        )
        text.contains("Experimental", ignoreCase = true) -> EvidenceTone(
            container = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.75f),
            content = MaterialTheme.colorScheme.onTertiaryContainer,
            border = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.95f),
        )
        else -> EvidenceTone(
            container = MaterialTheme.colorScheme.surfaceContainerHighest,
            content = MaterialTheme.colorScheme.onSurfaceVariant,
            border = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.9f),
        )
    }
}
