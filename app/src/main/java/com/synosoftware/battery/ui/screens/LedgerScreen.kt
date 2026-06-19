package com.synosoftware.battery.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text as AppText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.synosoftware.battery.R
import com.synosoftware.battery.data.sessionTemperatureText
import com.synosoftware.battery.i18n.T
import com.synosoftware.battery.i18n.asString
import com.synosoftware.battery.ui.components.EvidenceBadge
import com.synosoftware.battery.ui.components.IconBadge
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
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            SectionHeader(
                title = T("ledger_page_title").asString(),
                subtitle = T("ledger_subtitle").asString(),
            )
        }

        if (state.sessions.isEmpty()) {
            item {
                EmptyLedgerCard()
            }
        } else {
            items(state.sessions, key = { it.id }) { session ->
                SessionCard(
                    session = session,
                    temperatureUnit = state.temperatureUnit,
                )
            }
        }

        item { Spacer(Modifier.height(12.dp)) }
    }
}

@Composable
private fun EmptyLedgerCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
        tonalElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            IconBadge(
                resId = R.drawable.lucide_history,
                contentDescription = null,
            )
            AppText(
                text = T("ledger_no_sessions").asString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            AppText(
                text = T("ledger_no_sessions_hint").asString(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SessionCard(
    session: com.synosoftware.battery.ui.model.BatterySessionUi,
    temperatureUnit: com.synosoftware.battery.data.preferences.TemperatureUnit,
) {
    val accent = when {
        session.usefulForHealth -> MaterialTheme.colorScheme.primary
        session.active -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.outline
    }
    val note = when {
        session.usefulForHealth -> T("useful_label").asString()
        session.active -> T("session_quality_active").asString()
        else -> T("stored_only_label").asString()
    }
    val evidence = T("evidence_${session.qualityEvidence.name.lowercase()}").asString()

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.25.dp, accent.copy(alpha = 0.55f)),
        tonalElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconBadge(
                            resId = R.drawable.lucide_battery_charging,
                            contentDescription = null,
                        )
                        AppText(
                            text = "${session.headline.asString()}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    AppText(
                        text = "${session.timeRange.asString()}  ${session.deltaLabel.asString()}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    EvidenceBadge(text = session.qualityLabel.asString())
                    EvidenceBadge(text = evidence, compact = true)
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                LabelValueRow(
                    T("temperature_label").asString(),
                    sessionTemperatureText(session.maxTemperatureC, session.averageTemperatureC, temperatureUnit).asString(),
                    T("confidence_${session.confidence.name.lowercase()}").asString(),
                    compactEvidence = true,
                )
                LabelValueRow(
                    T("source_label").asString(),
                    session.sourceLabel.asString(),
                    note,
                    compactEvidence = true,
                )
                LabelValueRow(
                    T("above_85_label").asString(),
                    session.timeAbove85Label.asString(),
                    T("evidence_measured").asString(),
                    compactEvidence = true,
                )
                LabelValueRow(
                    T("above_90_label").asString(),
                    session.timeAbove90Label.asString(),
                    T("evidence_measured").asString(),
                    compactEvidence = true,
                )
            }

            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                tonalElevation = 0.dp,
            ) {
                AppText(
                    text = session.confidenceReason.asString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(14.dp),
                )
            }
        }
    }
}
