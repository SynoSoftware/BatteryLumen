package com.synosoftware.battery.ui.screens

import android.content.Context
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text as AppText
import com.synosoftware.battery.R
import com.synosoftware.battery.data.sessionTemperatureText
import com.synosoftware.battery.data.preferences.TemperatureUnit
import com.synosoftware.battery.domain.EvidenceGrade
import com.synosoftware.battery.i18n.T
import com.synosoftware.battery.i18n.asString
import com.synosoftware.battery.i18n.confidenceText
import com.synosoftware.battery.i18n.resolveText
import com.synosoftware.battery.ui.components.EvidenceBadge
import com.synosoftware.battery.ui.components.IconBadge
import com.synosoftware.battery.ui.components.LabelValueRow
import com.synosoftware.battery.ui.components.PlainBadge
import com.synosoftware.battery.ui.components.SectionHeader
import com.synosoftware.battery.ui.model.BatterySessionUi
import com.synosoftware.battery.ui.model.BatteryUiState

@Composable
fun LedgerScreen(
    state: BatteryUiState,
    contentPadding: PaddingValues,
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val totalSessions = state.sessions.size
    val query = searchQuery.trim()
    val context = LocalContext.current
    val filteredSessions = if (query.isEmpty()) {
        state.sessions
    } else {
        state.sessions.filter { session -> session.matchesQuery(context, query) }
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
                title = T(R.string.sessions_title),
                subtitle = T(R.string.sessions_subtitle),
            )
        }

        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { AppText(T(R.string.sessions_search_label)) },
                placeholder = { AppText(T(R.string.sessions_search_hint)) },
            )
        }

        if (query.isEmpty() && totalSessions > sessionsToShow.size) {
            item {
                AppText(
                    text = T(R.string.sessions_recent_note, sessionsToShow.size, totalSessions),
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

private fun BatterySessionUi.matchesQuery(context: Context, query: String): Boolean {
    return context.resolveText(timeRange).contains(query, ignoreCase = true) ||
        context.resolveText(temperatureLabel).contains(query, ignoreCase = true) ||
        context.resolveText(sourceLabel).contains(query, ignoreCase = true) ||
        context.resolveText(qualityLabel).contains(query, ignoreCase = true) ||
        context.resolveText(confidenceReason).contains(query, ignoreCase = true)
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
                text = T(R.string.sessions_empty_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            AppText(
                text = T(R.string.sessions_empty_hint),
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
                text = T(R.string.sessions_search_empty_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            AppText(
                text = T(R.string.sessions_search_empty_hint),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SessionCard(
    session: BatterySessionUi,
    temperatureUnit: TemperatureUnit,
) {
    val accent = when {
        session.usefulForHealth -> MaterialTheme.colorScheme.primary
        session.active -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.outline
    }
    val note = when {
        session.usefulForHealth -> T(R.string.sessions_useful)
        session.active -> T(R.string.sessions_active)
        else -> T(R.string.stored_only_label)
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
                    T(R.string.temperature_label),
                    sessionTemperatureText(session.maxTemperatureC, session.averageTemperatureC, temperatureUnit).asString(),
                    confidenceText(session.confidence).asString(),
                    compactEvidence = true,
                )
                LabelValueRow(
                    T(R.string.source_label),
                    session.sourceLabel.asString(),
                    note,
                    compactEvidence = true,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    LabelValueRow(
                        T(R.string.above_85_label),
                        session.timeAbove85Label.asString(),
                        T(R.string.evidence_measured),
                        evidenceGrade = EvidenceGrade.MEASURED,
                        compactEvidence = true,
                        modifier = Modifier.weight(1f),
                    )
                    LabelValueRow(
                        T(R.string.above_90_label),
                        session.timeAbove90Label.asString(),
                        T(R.string.evidence_measured),
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
