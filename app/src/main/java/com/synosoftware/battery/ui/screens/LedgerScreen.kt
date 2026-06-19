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
fun LedgerScreen(
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
                title = text("ledger_title").asString(),
                subtitle = text("ledger_subtitle").asString(),
            )
        }
        if (state.sessions.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            text = text("ledger_no_sessions").asString(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(text("ledger_no_sessions_hint").asString())
                    }
                }
            }
        } else {
            items(state.sessions, key = { it.id }) { session ->
                OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            text = "${session.headline.asString()}  ${session.timeRange.asString()}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(session.deltaLabel.asString())
                        Spacer(Modifier.padding(bottom = 8.dp))
                        LabelValueRow(text("temperature_label").asString(), session.temperatureLabel.asString(), text("confidence_${session.confidence.name.lowercase()}").asString())
                        LabelValueRow(text("source_label").asString(), session.sourceLabel.asString(), session.qualityLabel.asString())
                        LabelValueRow(text("above_85_label").asString(), session.timeAbove85Label.asString(), text("evidence_measured").asString())
                        LabelValueRow(text("above_90_label").asString(), session.timeAbove90Label.asString(), text("evidence_measured").asString())
                        EvidenceBadge(label = session.qualityLabel.asString(), gradeText = text("evidence_${session.qualityEvidence.name.lowercase()}").asString())
                    }
                }
            }
        }
        item { Spacer(Modifier.height(24.dp)) }
    }
}
