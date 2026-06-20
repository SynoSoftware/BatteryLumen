package com.synosoftware.battery.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text as AppText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.synosoftware.battery.R
import com.synosoftware.battery.data.temperatureText
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
import com.synosoftware.battery.ui.components.LucideIcon
import com.synosoftware.battery.ui.components.MetricTile
import com.synosoftware.battery.ui.components.PlainBadge
import com.synosoftware.battery.ui.components.SectionHeader
import com.synosoftware.battery.ui.model.BatteryUiState
import com.synosoftware.battery.ui.model.MIN_USEFUL_SESSION_COUNT

@Composable
fun NowScreen(
    state: BatteryUiState,
    onTargetSelected: (Int) -> Unit,
    contentPadding: PaddingValues,
) {
    val snapshot = state.currentSnapshot
    val decision = state.decision
    val riskTone = when (decision?.stress) {
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
                    title = T("live.telemetry.title").asString(),
                    subtitle = T("live.telemetry.subtitle").asString(),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    MetricTile(
                        modifier = Modifier.weight(1f),
                        iconRes = R.drawable.lucide_thermometer,
                        title = T("temperature.label").asString(),
                        value = temperatureText(snapshot?.temperatureC, state.temperatureUnit).asString(),
                        evidence = T("evidence.measured").asString(),
                        evidenceGrade = EvidenceGrade.MEASURED,
                        showEvidence = false,
                    )
                    MetricTile(
                        modifier = Modifier.weight(1f),
                        iconRes = R.drawable.lucide_battery_full,
                        title = T("level.label").asString(),
                        value = snapshot?.let { T("value.percent", it.levelPercent).asString() } ?: T("value.na").asString(),
                        evidence = T("evidence.measured").asString(),
                        evidenceGrade = EvidenceGrade.MEASURED,
                        showEvidence = false,
                    )
                    MetricTile(
                        modifier = Modifier.weight(1f),
                        iconRes = R.drawable.lucide_zap,
                        title = T("state.label").asString(),
                        value = snapshot?.let { chargingStateText(it.chargingState).asString() } ?: T("value.na").asString(),
                        evidence = T("evidence.measured").asString(),
                        evidenceGrade = EvidenceGrade.MEASURED,
                        showEvidence = false,
                    )
                }
            }
        }

        item {
            HealthSummaryCard(
                estimate = state.healthEstimate,
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
            NotificationPermissionCard()
        }

        item { Spacer(Modifier.height(12.dp)) }
    }
}

@Composable
private fun HeroDecisionCard(
    decision: com.synosoftware.battery.domain.BatteryDecision?,
    riskTone: androidx.compose.ui.graphics.Color,
    containerColor: androidx.compose.ui.graphics.Color,
    borderColor: androidx.compose.ui.graphics.Color,
) {
    val riskLabel = decision?.stress?.let { stressText(it).asString() } ?: T("waiting.for.battery.data").asString()
    val reason = decision?.reason?.asString() ?: T("open.app.while.charging").asString()
    val action = decision?.action?.asString() ?: T("continue.charging.or.set.target").asString()
    val confidenceSummaryText = decision?.confidence?.let { confidenceText(it).asString() } ?: T("waiting.for.battery.data").asString()
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
                    text = T("now.stress.label").asString(),
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
                    PlainBadge(text = confidenceText(decision.confidence).asString())
                    EvidenceBadge(grade = decision.evidenceGrade)
                }
                AppText(
                    text = T("confidence.summary", confidenceSummaryText, decision.confidenceReason.asString()).asString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                AppText(
                    text = T("waiting.for.battery.data").asString(),
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
                text = T("target.label").asString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                listOf(80, 85, 90, 100).forEach { target ->
                    FilterChip(
                        selected = state.targetChargePercent == target,
                        onClick = { onTargetSelected(target) },
                        label = { AppText(T("value.percent", target).asString()) },
                    )
                }
            }

            if (decision != null) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    LabelValueRow(
                        T("best.stop.label").asString(),
                        T("value.percent", decision.bestStopPercent).asString(),
                        T("recommended.label").asString(),
                    )
                    LabelValueRow(
                        T("time.to.target.label").asString(),
                        decision.timeToTargetMinutes?.let { T("value.min.short", it).asString() } ?: T("value.na").asString(),
                        T("evidence.estimated").asString(),
                        evidenceGrade = EvidenceGrade.ESTIMATED,
                    )
                }
            } else {
                AppText(
                    text = T("target.timing.wait").asString(),
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
                AppText(T("set.alarm.target", T("value.percent", state.targetChargePercent)).asString())
            }

            AppText(
                text = T("target.guidance.note").asString(),
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
    estimate: com.synosoftware.battery.ui.model.BatteryHealthEstimateUi,
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
                text = T("health.summary.title").asString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            if (estimate.hasEstimate) {
                val capacityMah = requireNotNull(estimate.estimatedCapacityMah)
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
                            "health.capacity.reference",
                            T("value.mah", capacityMah).asString(),
                        ).asString(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    AppText(
                        text = T("value.mah", capacityMah).asString(),
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
                    text = T("health.based.on.sessions", estimate.usefulSessionCount).asString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                AppText(
                    text = T("health.insufficient.body", MIN_USEFUL_SESSION_COUNT).asString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                AppText(
                    text = T("health.sessions.collected", estimate.usefulSessionCount, MIN_USEFUL_SESSION_COUNT).asString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                AppText(
                    text = T("health.collecting.data").asString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun DailySummaryCard(
    summary: com.synosoftware.battery.ui.model.DailyChargingSummaryUi,
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
                text = T("daily.summary.title").asString(),
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
                    text = T("daily.summary.based.on", summary.sessionCount).asString(),
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
                    text = T("now.details.title").asString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                FilterChip(
                    selected = expanded,
                    onClick = { expanded = !expanded },
                    label = {
                        AppText(
                            text = if (expanded) T("now.details.hide").asString() else T("now.details.show").asString(),
                        )
                    },
                )
            }

            if (!expanded) {
                AppText(
                    text = T("decision.details.hint").asString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else if (decision == null) {
                AppText(
                    text = T("waiting.for.battery.data").asString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    LabelValueRow(
                        T("decision.thermal.label").asString(),
                        stressText(decision.thermalStress).asString(),
                        T("evidence.inferred").asString(),
                        evidenceGrade = EvidenceGrade.INFERRED,
                        compactEvidence = true,
                    )
                    LabelValueRow(
                        T("decision.charge.label").asString(),
                        stressText(decision.chargeLevelStress).asString(),
                        T("evidence.inferred").asString(),
                        evidenceGrade = EvidenceGrade.INFERRED,
                        compactEvidence = true,
                    )
                    if (snapshot != null) {
                        LabelValueRow(
                            T("source.label").asString(),
                            chargingSourceText(snapshot.chargingSource).asString(),
                            T("evidence.measured").asString(),
                            evidenceGrade = EvidenceGrade.MEASURED,
                            compactEvidence = true,
                        )
                    }
                    if (active != null) {
                        LabelValueRow(
                            T("above.85.label").asString(),
                            active.timeAbove85Label.asString(),
                            T("evidence.measured").asString(),
                            evidenceGrade = EvidenceGrade.MEASURED,
                            compactEvidence = true,
                        )
                        LabelValueRow(
                            T("above.90.label").asString(),
                            active.timeAbove90Label.asString(),
                            T("evidence.measured").asString(),
                            evidenceGrade = EvidenceGrade.MEASURED,
                            compactEvidence = true,
                        )
                    }
                    if (decision.timeToFullMinutes != null) {
                        LabelValueRow(
                            T("full.charge.label").asString(),
                            T("value.min.short", decision.timeToFullMinutes).asString(),
                            T("evidence.estimated").asString(),
                            evidenceGrade = EvidenceGrade.ESTIMATED,
                            compactEvidence = true,
                        )
                    }
                    if (snapshot != null) {
                        LabelValueRow(
                            T("voltage.label").asString(),
                            snapshot.voltageMv?.let { T("value.mv", it).asString() } ?: T("value.na").asString(),
                            T("evidence.measured").asString(),
                            evidenceGrade = EvidenceGrade.MEASURED,
                            compactEvidence = true,
                        )
                        LabelValueRow(
                            T("current.label").asString(),
                            snapshot.currentUa?.let { T("value.ua", it).asString() } ?: T("value.na").asString(),
                            T("evidence.measured").asString(),
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
private fun NotificationPermissionCard() {
    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(hasNotificationPermission(context)) }
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
                T("notification.channel.charge.target.title").asString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            AppText(
                T("notification.channel.charge.target.description").asString(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(onClick = { launcher.launch(Manifest.permission.POST_NOTIFICATIONS) }) {
                AppText(T("allow.notifications").asString())
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
