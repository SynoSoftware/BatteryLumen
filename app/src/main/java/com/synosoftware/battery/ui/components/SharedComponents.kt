package com.synosoftware.battery.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.synosoftware.battery.domain.EvidenceGrade
import com.synosoftware.battery.i18n.asString
import com.synosoftware.battery.i18n.text

@Composable
fun SectionHeader(
    title: String,
    subtitle: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
        Text(subtitle, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun LabelValueRow(
    label: String,
    value: String,
    evidence: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.labelLarge)
            Text(value, style = MaterialTheme.typography.bodyMedium)
        }
        AssistChip(onClick = {}, label = { Text(evidence) })
    }
}

@Composable
fun EvidenceBadge(
    label: String,
    gradeText: String,
) {
    AssistChip(
        onClick = {},
        label = { Text(text("evidence_badge", label, gradeText).asString()) },
    )
}
