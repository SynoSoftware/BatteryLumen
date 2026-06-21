package com.synosoftware.battery.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text as AppText
import com.synosoftware.battery.R
import com.synosoftware.battery.domain.EvidenceGrade
import com.synosoftware.battery.domain.DeviceCapability
import com.synosoftware.battery.i18n.T
import com.synosoftware.battery.i18n.asString
import com.synosoftware.battery.ui.components.EvidenceBadge
import com.synosoftware.battery.ui.components.IconBadge
import com.synosoftware.battery.ui.components.LabelValueRow
import com.synosoftware.battery.ui.components.SectionHeader
import com.synosoftware.battery.ui.model.BatteryUiState

@Composable
fun HowItWorksScreen(
    state: BatteryUiState,
    contentPadding: PaddingValues,
) {
    var capabilitiesExpanded by rememberSaveable { mutableStateOf(false) }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            SectionHeader(
                title = T(R.string.info_title),
                subtitle = T(R.string.info_subtitle),
            )
        }

        item {
            RegistryCard(
                iconRes = R.drawable.lucide_info,
                title = T(R.string.info_grades_title),
            ) {
                GradeRow(
                    grade = EvidenceGrade.MEASURED,
                    body = T(R.string.info_grades_measured_body),
                )
                GradeRow(
                    grade = EvidenceGrade.ESTIMATED,
                    body = T(R.string.info_grades_estimated_body),
                )
                GradeRow(
                    grade = EvidenceGrade.INFERRED,
                    body = T(R.string.info_grades_inferred_body),
                )
                GradeRow(
                    grade = EvidenceGrade.EXPERIMENTAL,
                    body = T(R.string.info_grades_experimental_body),
                )
            }
        }

        item {
            RegistryCard(
                iconRes = R.drawable.lucide_history,
                title = T(R.string.info_models_title),
            ) {
                RuleRow(
                    title = T(R.string.info_models_thermal_title),
                    body = T(R.string.info_models_thermal_body),
                    grade = EvidenceGrade.INFERRED,
                )
                RuleRow(
                    title = T(R.string.info_models_charge_title),
                    body = T(R.string.info_models_charge_body),
                    grade = EvidenceGrade.ESTIMATED,
                )
                RuleRow(
                    title = T(R.string.info_models_session_title),
                    body = T(R.string.info_models_session_body),
                    grade = EvidenceGrade.INFERRED,
                )
            }
        }

        item {
            CapabilityCard(
                capabilities = state.capabilities,
                expanded = capabilitiesExpanded,
                onToggleExpanded = { capabilitiesExpanded = !capabilitiesExpanded },
            )
        }

        item {
            if (state.experimentalMetricsEnabled) {
                NoteCard(
                    title = T(R.string.info_experimental_title),
                    body = T(R.string.info_experimental_desc),
                )
            } else {
                NoteCard(
                    title = T(R.string.info_experimental_title),
                    body = T(R.string.info_experimental_disabled),
                )
            }
        }

        item { Spacer(Modifier.height(12.dp)) }
    }
}

@Composable
private fun CapabilityCard(
    capabilities: List<DeviceCapability>,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
) {
    RegistryCard(
        iconRes = R.drawable.lucide_battery_full,
        title = T(R.string.info_capability_title),
    ) {
        AppText(
            text = T(R.string.info_capability_subtitle),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(4.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppText(
                text = T(R.string.info_capability_summary, capabilities.size),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            FilterChip(
                selected = expanded,
                onClick = onToggleExpanded,
                label = {
                    AppText(
                        text = if (expanded) {
                            T(R.string.info_capability_hide)
                        } else {
                            T(R.string.info_capability_show)
                        },
                    )
                },
            )
        }
        if (expanded) {
            Spacer(Modifier.height(4.dp))
            capabilities.forEach { capability ->
                CapabilityRow(capability = capability)
            }
        }
    }
}

@Composable
private fun RegistryCard(
    iconRes: Int,
    title: String,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)),
        tonalElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                IconBadge(resId = iconRes, contentDescription = null)
                AppText(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            content()
        }
    }
}

@Composable
private fun GradeRow(
    grade: EvidenceGrade,
    body: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        EvidenceBadge(grade = grade)
        AppText(
            text = body,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun RuleRow(
    title: String,
    body: String,
    grade: EvidenceGrade,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            EvidenceBadge(grade = grade, compact = true)
            AppText(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
        }
        AppText(
            text = body,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun CapabilityRow(capability: DeviceCapability) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        LabelValueRow(
            label = capability.label.asString(),
            value = capability.source.asString(),
            evidence = capability.availability.asString(),
            compactEvidence = true,
        )
        AppText(
            text = capability.reliabilityRule.asString(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        AppText(
            text = T(R.string.fallback_label, capability.fallback.asString()),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun NoteCard(
    title: String,
    body: String,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
        tonalElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            AppText(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            AppText(
                text = body,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
