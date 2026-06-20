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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text as AppText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.synosoftware.battery.R
import com.synosoftware.battery.data.sessionTemperatureText
import com.synosoftware.battery.domain.EvidenceGrade
import com.synosoftware.battery.i18n.T
import com.synosoftware.battery.i18n.asString
import com.synosoftware.battery.i18n.confidenceText
import com.synosoftware.battery.ui.components.EvidenceBadge
import com.synosoftware.battery.ui.components.IconBadge
import com.synosoftware.battery.ui.components.LabelValueRow
import com.synosoftware.battery.ui.components.SectionHeader
import com.synosoftware.battery.ui.components.PlainBadge
import com.synosoftware.battery.ui.model.BatteryUiState

@Composable
fun LedgerScreen(
    state: BatteryUiState,
    contentPadding: PaddingValues,
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val totalSessions = state.sessions.size
    val query = searchQuery.trim()
    val filteredSessions = if (query.isEmpty()) {
        state.sessions
    } else {
        state.sessions.filter { session ->
            listOf(
                session.timeRange.asString(),
                session.temperatureLabel.asString(),
                session.sourceLabel.asString(),
                session.qualityLabel.asString(),
                session.confidenceReason.asString(),
            ).joinToString(" ").contains(query, ignoreCase = true)
        }
    }
    val sessionsToShow = if (query.isEmpty()) filteredSessions.take(12) else filteredSessions

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            SectionHeader(
                title = T("sessions.title").asString(),
                subtitle = T("sessions.subtitle").asString(),
            )
        }

        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { AppText(T("sessions.search.label").asString()) },
                placeholder = { AppText(T("sessions.search.hint").asString()) },
            )
        }

        if (query.isEmpty() && totalSessions > sessionsToShow.size) {
            item {
                AppText(
                    text = T("sessions.recent.note", sessionsToShow.size, totalSessions).asString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (state.sessions.isEmpty()) {
            item {
                EmptyLedgerCard()
            }
        } else if (sessionsToShow.isEmpty()) {
            item {
                EmptySearchCard()
            }
        } else {
            items(sessionsToShow, key = { it.id }) { session ->
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
                text = T("sessions.empty.title").asString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            AppText(
                text = T("sessions.empty.hint").asString(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun EmptySearchCard() {
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
                text = T("sessions.search.empty.title").asString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            AppText(
                text = T("sessions.search.empty.hint").asString(),
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
        session.usefulForHealth -> T("sessions.useful").asString()
        session.active -> T("sessions.active").asString()
        else -> T("stored.only.label").asString()
    }
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
                            text = session.timeRange.asString(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    PlainBadge(text = session.qualityLabel.asString())
                    EvidenceBadge(
                        grade = session.qualityEvidence,
                        compact = true,
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                LabelValueRow(
                    T("temperature.label").asString(),
                    sessionTemperatureText(session.maxTemperatureC, session.averageTemperatureC, temperatureUnit).asString(),
                    confidenceText(session.confidence).asString(),
                    compactEvidence = true,
                )
                LabelValueRow(
                    T("source.label").asString(),
                    session.sourceLabel.asString(),
                    note,
                    compactEvidence = true,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    LabelValueRow(
                        T("above.85.label").asString(),
                        session.timeAbove85Label.asString(),
                        T("evidence.measured").asString(),
                        evidenceGrade = EvidenceGrade.MEASURED,
                        compactEvidence = true,
                        modifier = Modifier.weight(1f),
                    )
                    LabelValueRow(
                        T("above.90.label").asString(),
                        session.timeAbove90Label.asString(),
                        T("evidence.measured").asString(),
                        evidenceGrade = EvidenceGrade.MEASURED,
                        compactEvidence = true,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            AppText(
                text = session.confidenceReason.asString(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
