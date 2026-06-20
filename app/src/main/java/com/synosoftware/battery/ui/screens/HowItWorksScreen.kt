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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text as AppText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
                title = T("info.title").asString(),
                subtitle = T("info.subtitle").asString(),
            )
        }

        item {
            RegistryCard(
                iconRes = R.drawable.lucide_info,
                title = T("info.grades.title").asString(),
            ) {
                GradeRow(
                    grade = EvidenceGrade.MEASURED,
                    body = T("info.grades.measured.body").asString(),
                )
                GradeRow(
                    grade = EvidenceGrade.ESTIMATED,
                    body = T("info.grades.estimated.body").asString(),
                )
                GradeRow(
                    grade = EvidenceGrade.INFERRED,
                    body = T("info.grades.inferred.body").asString(),
                )
                GradeRow(
                    grade = EvidenceGrade.EXPERIMENTAL,
                    body = T("info.grades.experimental.body").asString(),
                )
            }
        }

        item {
            RegistryCard(
                iconRes = R.drawable.lucide_history,
                title = T("info.models.title").asString(),
            ) {
                RuleRow(
                    title = T("info.models.thermal.title").asString(),
                    body = T("info.models.thermal.body").asString(),
                    grade = EvidenceGrade.INFERRED,
                )
                RuleRow(
                    title = T("info.models.charge.title").asString(),
                    body = T("info.models.charge.body").asString(),
                    grade = EvidenceGrade.ESTIMATED,
                )
                RuleRow(
                    title = T("info.models.session.title").asString(),
                    body = T("info.models.session.body").asString(),
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
                    title = T("info.experimental.title").asString(),
                    body = T("info.experimental.desc").asString(),
                )
            } else {
                NoteCard(
                    title = T("info.experimental.title").asString(),
                    body = T("info.experimental.disabled").asString(),
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
        title = T("info.capability.title").asString(),
    ) {
        AppText(
            text = T("info.capability.subtitle").asString(),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(4.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppText(
                text = T("info.capability.summary", capabilities.size).asString(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            FilterChip(
                selected = expanded,
                onClick = onToggleExpanded,
                label = {
                    AppText(
                        text = if (expanded) {
                            T("info.capability.hide").asString()
                        } else {
                            T("info.capability.show").asString()
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
            text = T("fallback.label", capability.fallback.asString()).asString(),
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
