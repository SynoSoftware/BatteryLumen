package com.synosoftware.battery.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.synosoftware.battery.i18n.asString
import com.synosoftware.battery.i18n.text
import com.synosoftware.battery.ui.components.EvidenceBadge
import com.synosoftware.battery.ui.components.LabelValueRow
import com.synosoftware.battery.ui.components.SectionHeader
import com.synosoftware.battery.ui.model.BatteryUiState

@Composable
fun HowItWorksScreen(
    state: BatteryUiState,
    contentPadding: PaddingValues,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = 16.dp),
    ) {
        item {
            SectionHeader(
                title = text("how_it_works_title").asString(),
                subtitle = text("how_it_works_subtitle").asString(),
            )
        }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(text("how_it_works_measured").asString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(text("how_it_works_measured_desc").asString())
                    Spacer(Modifier.padding(bottom = 8.dp))
                    Text(text("how_it_works_estimated").asString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(text("how_it_works_estimated_desc").asString())
                    Spacer(Modifier.padding(bottom = 8.dp))
                    Text(text("how_it_works_inferred").asString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(text("how_it_works_inferred_desc").asString())
                    Spacer(Modifier.padding(bottom = 8.dp))
                    Text(text("how_it_works_experimental").asString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(text("how_it_works_experimental_desc").asString())
                }
            }
        }
        item {
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(text("capability_matrix_title").asString(), style = MaterialTheme.typography.titleMedium)
                    Text(text("capability_matrix_subtitle").asString())
                }
            }
        }
        items(state.capabilities, key = { it.key }) { capability ->
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(capability.label.asString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    LabelValueRow(text("source_label").asString(), capability.source.asString(), text("evidence_${capability.evidenceGrade.name.lowercase()}").asString())
                    LabelValueRow(text("unit_label").asString(), capability.unit.asString(), capability.availability.asString())
                    Text(capability.reliabilityRule.asString(), style = MaterialTheme.typography.bodyMedium)
                    Text(text("fallback_label", capability.fallback.asString()).asString(), style = MaterialTheme.typography.bodySmall)
                    EvidenceBadge(label = capability.evidenceGrade.name.lowercase().replaceFirstChar { it.uppercase() }, gradeText = text("evidence_${capability.evidenceGrade.name.lowercase()}").asString())
                }
            }
        }
        item { Spacer(Modifier.height(24.dp)) }
    }
}
