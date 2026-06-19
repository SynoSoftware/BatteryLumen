package com.synosoftware.battery.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text as AppText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.synosoftware.battery.R
import com.synosoftware.battery.data.sessionTemperatureText
import com.synosoftware.battery.data.temperatureText
import com.synosoftware.battery.domain.StressLevel
import com.synosoftware.battery.i18n.T
import com.synosoftware.battery.i18n.asString
import com.synosoftware.battery.ui.components.EvidenceBadge
import com.synosoftware.battery.ui.components.IconBadge
import com.synosoftware.battery.ui.components.LabelValueRow
import com.synosoftware.battery.ui.components.MetricTile
import com.synosoftware.battery.ui.components.LucideIcon
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
    val stressTone = when (decision?.stress) {
        StressLevel.EXCELLENT, StressLevel.GOOD, StressLevel.NORMAL, null -> MaterialTheme.colorScheme.primary
        StressLevel.HIGH_STRESS -> MaterialTheme.colorScheme.tertiary
        StressLevel.SEVERE_STRESS -> MaterialTheme.colorScheme.error
    }
    val heroContainer = when (decision?.stress) {
        StressLevel.HIGH_STRESS, StressLevel.SEVERE_STRESS -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.32f)
        else -> MaterialTheme.colorScheme.surfaceContainer
    }
    val heroBorder = when (decision?.stress) {
        StressLevel.HIGH_STRESS -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.7f)
        StressLevel.SEVERE_STRESS -> MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
        else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            HeroDecisionCard(
                decision = decision,
                stressTone = stressTone,
                containerColor = heroContainer,
                borderColor = heroBorder,
            )
        }

        item {
            NotificationPermissionCard()
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionHeader(
                    title = T("live_telemetry").asString(),
                    subtitle = T("live_telemetry_subtitle").asString(),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    MetricTile(
                        modifier = Modifier.weight(1f),
                        iconRes = R.drawable.lucide_thermometer,
                        title = T("temperature_label").asString(),
                        value = temperatureText(snapshot?.temperatureC, state.temperatureUnit).asString(),
                        evidence = T("evidence_measured").asString(),
                    )
                    MetricTile(
                        modifier = Modifier.weight(1f),
                        iconRes = R.drawable.lucide_battery_full,
                        title = T("level_label").asString(),
                        value = snapshot?.let { T("value_percent", it.levelPercent).asString() } ?: T("value_na").asString(),
                        evidence = T("evidence_measured").asString(),
                    )
                    MetricTile(
                        modifier = Modifier.weight(1f),
                        iconRes = R.drawable.lucide_zap,
                        title = T("state_label").asString(),
                        value = snapshot?.let { T("charging_state_${it.chargingState.name.lowercase()}").asString() } ?: T("value_na").asString(),
                        evidence = T("evidence_measured").asString(),
                    )
                }
            }
        }

        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surfaceContainer,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f)),
                tonalElevation = 0.dp,
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    AppText(
                        text = T("source_label").asString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    if (snapshot == null) {
                        AppText(T("no_battery_snapshot_yet").asString())
                    } else {
                        LabelValueRow(
                            T("charging_source_label").asString(),
                            T("charging_source_${snapshot.chargingSource.name.lowercase()}").asString(),
                            T("evidence_measured").asString(),
                        )
                        LabelValueRow(
                            T("voltage_label").asString(),
                            snapshot.voltageMv?.let { T("value_mv", it).asString() } ?: T("value_na").asString(),
                            T("evidence_measured").asString(),
                        )
                        LabelValueRow(
                            T("current_label").asString(),
                            snapshot.currentUa?.let { T("value_ua", it).asString() } ?: T("value_na").asString(),
                            T("evidence_measured").asString(),
                        )
                    }
                }
            }
        }

        item {
            TargetCard(
                state = state,
                decision = decision,
                onTargetSelected = onTargetSelected,
            )
        }

        item {
            SessionCard(
                state = state,
            )
        }

        item { Spacer(Modifier.height(12.dp)) }
    }
}

@Composable
private fun HeroDecisionCard(
    decision: com.synosoftware.battery.domain.BatteryDecision?,
    stressTone: androidx.compose.ui.graphics.Color,
    containerColor: androidx.compose.ui.graphics.Color,
    borderColor: androidx.compose.ui.graphics.Color,
) {
    val stressLabel = decision?.stress?.name?.lowercase()?.let { T("stress_$it").asString() } ?: T("waiting_for_battery_data").asString()
    val reason = decision?.reason?.asString() ?: T("open_app_while_charging").asString()
    val action = decision?.action?.asString() ?: T("continue_charging_or_set_target").asString()
    val confidenceText = decision?.confidence?.name?.lowercase()?.let { T("confidence_$it").asString() } ?: T("waiting_for_battery_data").asString()
    val icon = when (decision?.stress) {
        StressLevel.HIGH_STRESS, StressLevel.SEVERE_STRESS -> R.drawable.lucide_triangle_alert
        else -> R.drawable.lucide_battery_charging
    }
    val actionTone = when (decision?.stress) {
        StressLevel.SEVERE_STRESS -> MaterialTheme.colorScheme.error
        StressLevel.HIGH_STRESS -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = containerColor,
        border = BorderStroke(1.5.dp, borderColor),
        tonalElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AppText(
                    text = T("battery_stress_label").asString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                EvidenceBadge(text = T("evidence_inferred").asString())
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                IconBadge(
                    resId = icon,
                    contentDescription = null,
                )
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    AppText(
                        text = stressLabel,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = stressTone,
                    )
                    AppText(
                        text = reason,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Surface(
                shape = MaterialTheme.shapes.large,
                color = actionTone.copy(alpha = 0.16f),
                border = BorderStroke(1.dp, actionTone.copy(alpha = 0.38f)),
                tonalElevation = 0.dp,
            ) {
                AppText(
                    text = action,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(14.dp),
                )
            }

            if (decision != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EvidenceBadge(text = T("confidence_${decision.confidence.name.lowercase()}").asString())
                    EvidenceBadge(text = T("evidence_${decision.evidenceGrade.name.lowercase()}").asString())
                }
                AppText(
                    text = T("confidence_summary", confidenceText, decision.confidenceReason.asString()).asString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                AppText(
                    text = T("waiting_for_battery_data").asString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun TargetCard(
    state: BatteryUiState,
    decision: com.synosoftware.battery.domain.BatteryDecision?,
    onTargetSelected: (Int) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f)),
        tonalElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            AppText(
                text = T("target_label").asString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                listOf(80, 85, 90, 100).forEach { target ->
                    FilterChip(
                        selected = state.targetChargePercent == target,
                        onClick = { onTargetSelected(target) },
                        label = { AppText(T("value_percent", target).asString()) },
                    )
                }
            }

            if (decision != null) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    LabelValueRow(
                        T("best_stop_label").asString(),
                        T("value_percent", decision.bestStopPercent).asString(),
                        T("recommended_label").asString(),
                    )
                    LabelValueRow(
                        T("time_to_target_label").asString(),
                        decision.timeToTargetMinutes?.let { T("value_min_short", it).asString() } ?: T("value_na").asString(),
                        T("evidence_estimated").asString(),
                    )
                    LabelValueRow(
                        T("full_charge_label").asString(),
                        decision.timeToFullMinutes?.let { T("value_min_short", it).asString() } ?: T("value_na").asString(),
                        T("evidence_estimated").asString(),
                    )
                }
            } else {
                AppText(
                    text = T("target_timing_wait").asString(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Button(
                onClick = { onTargetSelected(state.targetChargePercent) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                LucideIcon(
                    resId = R.drawable.lucide_alarm_clock,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp).size(18.dp),
                )
                AppText(T("set_alarm_target", T("value_percent", state.targetChargePercent)).asString())
            }

            AppText(
                text = T("target_guidance_note").asString(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun SessionCard(
    state: BatteryUiState,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f)),
        tonalElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            AppText(
                text = T("current_session_label").asString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            val active = state.activeSession
            if (active == null) {
                AppText(
                    text = T("no_active_session_yet").asString(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                LabelValueRow(
                    T("temperature_label").asString(),
                    sessionTemperatureText(active.maxTemperatureC, active.averageTemperatureC, state.temperatureUnit).asString(),
                    T("confidence_${active.confidence.name.lowercase()}").asString(),
                )
                LabelValueRow(T("source_label").asString(), active.sourceLabel.asString(), if (active.usefulForHealth) T("useful_label").asString() else T("stored_only_label").asString())
                LabelValueRow(T("above_85_label").asString(), active.timeAbove85Label.asString(), T("evidence_measured").asString())
                LabelValueRow(T("above_90_label").asString(), active.timeAbove90Label.asString(), T("evidence_measured").asString())
            }
        }
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

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
        tonalElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            AppText(
                T("notification_channel_charge_target_title").asString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            AppText(
                T("notification_channel_charge_target_description").asString(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(onClick = { launcher.launch(Manifest.permission.POST_NOTIFICATIONS) }) {
                AppText(T("allow_notifications").asString())
            }
        }
    }
}
