package com.synosoftware.battery.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text as AppText
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.synosoftware.battery.R
import com.synosoftware.battery.data.temperatureText
import com.synosoftware.battery.domain.BatteryDecision
import com.synosoftware.battery.domain.EvidenceGrade
import com.synosoftware.battery.domain.StressLevel
import com.synosoftware.battery.i18n.T
import com.synosoftware.battery.i18n.asString
import com.synosoftware.battery.i18n.chargingStateText
import com.synosoftware.battery.i18n.chargingSourceText
import com.synosoftware.battery.i18n.confidenceText
import com.synosoftware.battery.i18n.healthApproxPercentText
import com.synosoftware.battery.i18n.healthCapacityRangeText
import com.synosoftware.battery.i18n.healthPercentRangeText
import com.synosoftware.battery.i18n.healthTrendText
import com.synosoftware.battery.i18n.stressText
import com.synosoftware.battery.ui.components.EvidenceBadge
import com.synosoftware.battery.ui.components.IconBadge
import com.synosoftware.battery.ui.components.LabelValueRow
import com.synosoftware.battery.ui.components.MetricTile
import com.synosoftware.battery.ui.components.PlainBadge
import com.synosoftware.battery.ui.components.SectionHeader
import com.synosoftware.battery.ui.model.BatteryHealthEstimateUi
import com.synosoftware.battery.ui.model.BatteryUiState
import com.synosoftware.battery.ui.model.DailyChargingSummaryUi
import com.synosoftware.battery.ui.model.MIN_USEFUL_SESSION_COUNT

@Composable
fun NowScreen(
    state: BatteryUiState,
    onTargetSelected: (Int) -> Unit,
    contentPadding: PaddingValues,
) {
    val context = LocalContext.current
    val snapshot = state.currentSnapshot
    val decision = state.decision
    val stress = decision?.stress
    val riskTone = when (stress) {
        StressLevel.EXCELLENT, StressLevel.GOOD, StressLevel.NORMAL, null -> MaterialTheme.colorScheme.primary
        StressLevel.HIGH_STRESS -> MaterialTheme.colorScheme.tertiary
        StressLevel.SEVERE_STRESS -> MaterialTheme.colorScheme.error
    }
    val heroContainer = when (stress) {
        StressLevel.HIGH_STRESS, StressLevel.SEVERE_STRESS -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.32f)
        else -> MaterialTheme.colorScheme.surfaceContainer
    }
    val heroBorder = when (stress) {
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
                riskTone = riskTone,
                containerColor = heroContainer,
                borderColor = heroBorder,
            )
        }

        item {
            TargetCard(
                state = state,
                decision = decision,
                onTargetSelected = onTargetSelected,
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionHeader(
                    title = T(R.string.live_telemetry_title),
                    subtitle = T(R.string.live_telemetry_subtitle),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    MetricTile(
                        modifier = Modifier.weight(1f),
                        iconRes = R.drawable.lucide_thermometer,
                        title = T(R.string.temperature_label),
                        value = temperatureText(snapshot?.temperatureC, state.temperatureUnit).asString(),
                        evidence = T(R.string.evidence_measured),
                        evidenceGrade = EvidenceGrade.MEASURED,
                        showEvidence = false,
                    )
                    MetricTile(
                        modifier = Modifier.weight(1f),
                        iconRes = R.drawable.lucide_battery_full,
                        title = T(R.string.level_label),
                        value = snapshot?.let { T(R.string.value_percent, it.levelPercent) } ?: T(R.string.value_na),
                        evidence = T(R.string.evidence_measured),
                        evidenceGrade = EvidenceGrade.MEASURED,
                        showEvidence = false,
                    )
                    MetricTile(
                        modifier = Modifier.weight(1f),
                        iconRes = R.drawable.lucide_zap,
                        title = T(R.string.state_label),
                        value = snapshot?.let { chargingStateText(it.chargingState).asString() } ?: T(R.string.value_na),
                        evidence = T(R.string.evidence_measured),
                        evidenceGrade = EvidenceGrade.MEASURED,
                        showEvidence = false,
                    )
                }
            }
        }

        item {
            HealthSummaryCard(
                estimate = state.healthEstimate,
                designCapacityMah = state.designCapacityMah,
            )
        }

        item {
            DailySummaryCard(
                summary = state.dailySummary,
            )
        }

        item {
            DecisionDetailsCard(
                state = state,
            )
        }

        item {
            NotificationPermissionCard(
                permissionChecker = { hasNotificationPermission(context) },
            )
        }

        item { Spacer(Modifier.height(12.dp)) }
    }
}

@Composable
private fun HeroDecisionCard(
    decision: BatteryDecision?,
    riskTone: androidx.compose.ui.graphics.Color,
    containerColor: androidx.compose.ui.graphics.Color,
    borderColor: androidx.compose.ui.graphics.Color,
) {
    val stress = decision?.stress
    val riskLabel = stress?.let { stressText(it).asString() } ?: T(R.string.waiting_for_battery_data)
    val reason = decision?.reason?.asString() ?: T(R.string.open_app_while_charging)
    val action = decision?.action?.asString() ?: T(R.string.continue_charging_or_set_target)
    val confidenceLabel = decision?.confidence?.let { confidenceText(it).asString() } ?: T(R.string.waiting_for_battery_data)
    val confidenceReason = decision?.confidenceReason?.asString() ?: T(R.string.waiting_for_battery_data)
    val icon = when (stress) {
        StressLevel.HIGH_STRESS, StressLevel.SEVERE_STRESS -> R.drawable.lucide_triangle_alert
        else -> R.drawable.lucide_battery_charging
    }
    val actionTone = when (stress) {
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
                    text = T(R.string.now_stress_label),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                EvidenceBadge(grade = EvidenceGrade.INFERRED)
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
                        text = riskLabel,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = riskTone,
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
                    PlainBadge(text = confidenceLabel)
                    EvidenceBadge(grade = decision.evidenceGrade)
                }
                AppText(
                    text = T(R.string.confidence_summary, confidenceLabel, confidenceReason),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                AppText(
                    text = T(R.string.waiting_for_battery_data),
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
    decision: BatteryDecision?,
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
                text = T(R.string.target_label),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                listOf(80, 85, 90, 100).forEach { target ->
                    FilterChip(
                        selected = state.targetChargePercent == target,
                        onClick = { onTargetSelected(target) },
                        label = { AppText(T(R.string.value_percent, target)) },
                    )
                }
            }

            if (decision != null) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    LabelValueRow(
                        T(R.string.best_stop_label),
                        T(R.string.value_percent, decision.bestStopPercent),
                        T(R.string.recommended_label),
                    )
                    LabelValueRow(
                        T(R.string.time_to_target_label),
                        decision.timeToTargetMinutes?.let { T(R.string.value_min_short, it) } ?: T(R.string.value_na),
                        T(R.string.evidence_estimated),
                        evidenceGrade = EvidenceGrade.ESTIMATED,
                    )
                }
            } else {
                AppText(
                    text = T(R.string.target_timing_wait),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            AppText(
                text = T(R.string.target_guidance_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun HealthSummaryCard(
    estimate: BatteryHealthEstimateUi,
    designCapacityMah: Int?,
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
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AppText(
                text = T(R.string.health_summary_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            if (estimate.hasEstimate) {
                val estimatedCapacityMah = requireNotNull(estimate.estimatedCapacityMah)
                if (estimate.hasHealthPercent) {
                    val healthPercent = requireNotNull(estimate.healthPercent)
                    AppText(
                        text = healthApproxPercentText(healthPercent).asString(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    estimate.healthRangePercent?.let { range ->
                        AppText(
                            text = healthPercentRangeText(range).asString(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    AppText(
                        text = T(
                            R.string.health_capacity_reference,
                            designCapacityMah?.takeIf { it > 0 }?.let { T(R.string.value_mah, it) } ?: T(R.string.value_na),
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    AppText(
                        text = T(R.string.value_mah, estimatedCapacityMah),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    estimate.likelyRangeMah?.let { range ->
                        AppText(
                            text = healthCapacityRangeText(range).asString(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PlainBadge(text = confidenceText(estimate.confidence).asString())
                    PlainBadge(text = healthTrendText(estimate.trend).asString())
                    EvidenceBadge(grade = EvidenceGrade.ESTIMATED)
                }
                AppText(
                    text = T(R.string.health_based_on_sessions, estimate.usefulSessionCount),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                AppText(
                    text = T(R.string.health_insufficient_body, MIN_USEFUL_SESSION_COUNT),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                AppText(
                    text = T(R.string.health_sessions_collected, estimate.usefulSessionCount, MIN_USEFUL_SESSION_COUNT),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                AppText(
                    text = T(R.string.health_collecting_data),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun DailySummaryCard(
    summary: DailyChargingSummaryUi,
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
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AppText(
                text = T(R.string.daily_summary_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            AppText(
                text = summary.headline.asString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            AppText(
                text = summary.detail.asString(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PlainBadge(text = confidenceText(summary.confidence).asString())
                EvidenceBadge(grade = summary.evidenceGrade)
            }
            if (summary.hasData) {
                AppText(
                    text = T(R.string.daily_summary_based_on, summary.sessionCount),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun DecisionDetailsCard(
    state: BatteryUiState,
) {
    val decision = state.decision
    val snapshot = state.currentSnapshot
    val active = state.activeSession
    var expanded by rememberSaveable { mutableStateOf(false) }

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AppText(
                    text = T(R.string.now_details_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                FilterChip(
                    selected = expanded,
                    onClick = { expanded = !expanded },
                    label = {
                        AppText(
                            text = if (expanded) T(R.string.now_details_hide) else T(R.string.now_details_show),
                        )
                    },
                )
            }

            if (!expanded) {
                AppText(
                    text = T(R.string.decision_details_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else if (decision == null) {
                AppText(
                    text = T(R.string.waiting_for_battery_data),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    LabelValueRow(
                        T(R.string.decision_thermal_label),
                        stressText(decision.thermalStress).asString(),
                        T(R.string.evidence_inferred),
                        evidenceGrade = EvidenceGrade.INFERRED,
                        compactEvidence = true,
                    )
                    LabelValueRow(
                        T(R.string.decision_reason_charge_label),
                        stressText(decision.chargeLevelStress).asString(),
                        T(R.string.evidence_inferred),
                        evidenceGrade = EvidenceGrade.INFERRED,
                        compactEvidence = true,
                    )
                    if (snapshot != null) {
                        LabelValueRow(
                            T(R.string.source_label),
                            chargingSourceText(snapshot.chargingSource).asString(),
                            T(R.string.evidence_measured),
                            evidenceGrade = EvidenceGrade.MEASURED,
                            compactEvidence = true,
                        )
                    }
                    if (active != null) {
                        LabelValueRow(
                            T(R.string.above_85_label),
                            active.timeAbove85Label.asString(),
                            T(R.string.evidence_measured),
                            evidenceGrade = EvidenceGrade.MEASURED,
                            compactEvidence = true,
                        )
                        LabelValueRow(
                            T(R.string.above_90_label),
                            active.timeAbove90Label.asString(),
                            T(R.string.evidence_measured),
                            evidenceGrade = EvidenceGrade.MEASURED,
                            compactEvidence = true,
                        )
                    }
                    if (decision.timeToFullMinutes != null) {
                        LabelValueRow(
                            T(R.string.full_charge_label),
                            T(R.string.value_min_short, decision.timeToFullMinutes),
                            T(R.string.evidence_estimated),
                            evidenceGrade = EvidenceGrade.ESTIMATED,
                            compactEvidence = true,
                        )
                    }
                    if (snapshot != null) {
                        LabelValueRow(
                            T(R.string.voltage_label),
                            snapshot.voltageMv?.let { T(R.string.value_mv, it) } ?: T(R.string.value_na),
                            T(R.string.evidence_measured),
                            evidenceGrade = EvidenceGrade.MEASURED,
                            compactEvidence = true,
                        )
                        LabelValueRow(
                            T(R.string.current_label),
                            snapshot.currentUa?.let { T(R.string.value_ua, it) } ?: T(R.string.value_na),
                            T(R.string.evidence_measured),
                            evidenceGrade = EvidenceGrade.MEASURED,
                            compactEvidence = true,
                        )
                    }
                    AppText(
                        text = decision.confidenceReason.asString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
internal fun NotificationPermissionCard(
    permissionChecker: () -> Boolean,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentPermissionChecker by rememberUpdatedState(permissionChecker)
    var hasPermission by remember { mutableStateOf(currentPermissionChecker()) }
    DisposableEffect(lifecycleOwner) {
        hasPermission = currentPermissionChecker()
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasPermission = currentPermissionChecker()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        hasPermission = granted
    }

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
                T(R.string.notification_channel_charge_target_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            AppText(
                T(R.string.notification_channel_charge_target_description),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(onClick = { launcher.launch(Manifest.permission.POST_NOTIFICATIONS) }) {
                AppText(T(R.string.allow_notifications))
            }
        }
    }
}

private fun hasNotificationPermission(context: android.content.Context): Boolean {
    return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.POST_NOTIFICATIONS,
    ) == PackageManager.PERMISSION_GRANTED
}
