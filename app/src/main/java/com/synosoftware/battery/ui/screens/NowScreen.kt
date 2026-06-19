package com.synosoftware.battery.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.synosoftware.battery.i18n.asString
import com.synosoftware.battery.i18n.text
import com.synosoftware.battery.ui.components.EvidenceBadge
import com.synosoftware.battery.ui.components.LabelValueRow
import com.synosoftware.battery.ui.components.SectionHeader
import com.synosoftware.battery.ui.model.BatteryUiState

@Composable
fun NowScreen(
    state: BatteryUiState,
    onTargetSelected: (Int) -> Unit,
    contentPadding: PaddingValues,
) {
    val snapshot = state.currentSnapshot
    val decision = state.decision

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            SectionHeader(
                title = text("now_title").asString(),
                subtitle = text("now_subtitle").asString(),
            )
        }

        item {
            NotificationPermissionCard()
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Battery stress", style = MaterialTheme.typography.labelLarge)
                    Text(
                        text = decision?.stress?.name?.lowercase()?.let { text("stress_$it").asString() } ?: text("waiting_for_battery_data").asString(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = decision?.reason?.asString() ?: text("open_app_while_charging").asString(),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = decision?.action?.asString() ?: text("continue_charging_or_set_target").asString(),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                    )
                    decision?.let {
                        EvidenceBadge(label = text("evidence_inferred").asString(), gradeText = text("evidence_${it.evidenceGrade.name.lowercase()}").asString())
                    }
                    if (decision != null) {
                        Text(text("confidence_label", text("confidence_${decision.confidence.name.lowercase()}").asString()).asString())
                        Text(decision.confidenceReason.asString(), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        item {
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text("live_telemetry").asString(), style = MaterialTheme.typography.titleMedium)
                    if (snapshot == null) {
                        Text(text("no_battery_snapshot_yet").asString())
                    } else {
                        LabelValueRow(text("temperature_label").asString(), snapshot.temperatureC?.let { "${String.format("%.1f", it)}°C" } ?: text("not_available").asString(), text("evidence_measured").asString())
                        LabelValueRow(text("level_label").asString(), "${snapshot.levelPercent}%", text("evidence_measured").asString())
                        LabelValueRow(text("state_label").asString(), text("charging_state_${snapshot.chargingState.name.lowercase()}").asString(), text("evidence_measured").asString())
                        LabelValueRow(text("source_label").asString(), text("charging_source_${snapshot.chargingSource.name.lowercase()}").asString(), text("evidence_measured").asString())
                        if (snapshot.voltageMv != null) {
                            LabelValueRow(text("voltage_label").asString(), "${snapshot.voltageMv} mV", text("evidence_measured").asString())
                        }
                        if (snapshot.currentUa != null) {
                            LabelValueRow(text("current_label").asString(), "${snapshot.currentUa} uA", text("evidence_measured").asString())
                        }
                    }
                }
            }
        }

        item {
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text("target_label").asString(), style = MaterialTheme.typography.titleMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(80, 85, 90, 100).forEach { target ->
                            FilterChip(
                                selected = state.targetChargePercent == target,
                                onClick = { onTargetSelected(target) },
                                label = { Text("$target%") },
                            )
                        }
                    }
                    if (decision != null) {
                        LabelValueRow(
                            text("best_stop_label").asString(),
                            "${decision.bestStopPercent}%",
                            text("recommended_label").asString(),
                        )
                        LabelValueRow(
                            text("time_to_target_label").asString(),
                            decision.timeToTargetMinutes?.let { "~${it} min" } ?: text("not_available").asString(),
                            text("evidence_estimated").asString(),
                        )
                        LabelValueRow(
                            text("full_charge_label").asString(),
                            decision.timeToFullMinutes?.let { "~${it} min" } ?: text("not_available").asString(),
                            text("evidence_estimated").asString(),
                        )
                    } else {
                        Text(text("target_timing_wait").asString())
                    }
                }
            }
        }

        item {
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text("current_session_label").asString(), style = MaterialTheme.typography.titleMedium)
                    val active = state.activeSession
                    if (active == null) {
                        Text(text("no_active_session_yet").asString())
                    } else {
                        LabelValueRow(text("session_label").asString(), active.timeRange.asString(), active.qualityLabel.asString())
                        LabelValueRow(text("change_label").asString(), active.deltaLabel.asString(), text("evidence_${active.qualityEvidence.name.lowercase()}").asString())
                        LabelValueRow(text("temperature_label").asString(), active.temperatureLabel.asString(), text("confidence_${active.confidence.name.lowercase()}").asString())
                        LabelValueRow(text("source_label").asString(), active.sourceLabel.asString(), if (active.usefulForHealth) text("useful_label").asString() else text("stored_only_label").asString())
                        LabelValueRow(text("above_85_label").asString(), active.timeAbove85Label.asString(), text("evidence_measured").asString())
                        LabelValueRow(text("above_90_label").asString(), active.timeAbove90Label.asString(), text("evidence_measured").asString())
                    }
                }
            }
        }

        item {
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text("evidence_summary_label").asString(), style = MaterialTheme.typography.titleMedium)
                    Text(text("measured_values_direct").asString())
                    Text(text("stress_card_inferred").asString())
                    Text(text("no_battery_health_estimate_v0").asString())
                }
            }
        }

        item { Spacer(Modifier.height(20.dp)) }
    }
}

@Composable
private fun NotificationPermissionCard() {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { }
    val requiresPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    val hasPermission = !requiresPermission || ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.POST_NOTIFICATIONS,
    ) == PackageManager.PERMISSION_GRANTED

    if (hasPermission || LocalInspectionMode.current) {
        return
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text("enable_charge_alerts").asString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(text("allow_notifications").asString())
            Button(onClick = { launcher.launch(Manifest.permission.POST_NOTIFICATIONS) }) {
                Text(text("enable_notifications").asString())
            }
        }
    }
}
