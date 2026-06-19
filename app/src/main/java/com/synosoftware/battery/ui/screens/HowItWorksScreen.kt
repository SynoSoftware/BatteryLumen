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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text as AppText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.synosoftware.battery.R
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
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            SectionHeader(
                title = T("how_it_works_page_title").asString(),
                subtitle = T("how_it_works_subtitle").asString(),
            )
        }

        item {
            RegistryCard(
                iconRes = R.drawable.lucide_info,
                title = T("how_it_works_grades_title").asString(),
            ) {
                GradeRow(
                    badge = T("evidence_measured").asString(),
                    body = T("how_it_works_measured_desc").asString(),
                )
                GradeRow(
                    badge = T("evidence_estimated").asString(),
                    body = T("how_it_works_estimated_desc").asString(),
                )
                GradeRow(
                    badge = T("evidence_inferred").asString(),
                    body = T("how_it_works_inferred_desc").asString(),
                )
                GradeRow(
                    badge = T("evidence_experimental").asString(),
                    body = T("how_it_works_experimental_desc").asString(),
                )
            }
        }

        item {
            RegistryCard(
                iconRes = R.drawable.lucide_history,
                title = T("how_it_works_models_title").asString(),
            ) {
                RuleRow(
                    title = T("how_it_works_thermal_title").asString(),
                    body = T("how_it_works_thermal_body").asString(),
                    badge = T("evidence_inferred").asString(),
                )
                RuleRow(
                    title = T("how_it_works_charge_title").asString(),
                    body = T("how_it_works_charge_body").asString(),
                    badge = T("evidence_estimated").asString(),
                )
                RuleRow(
                    title = T("how_it_works_session_title").asString(),
                    body = T("how_it_works_session_body").asString(),
                    badge = T("evidence_inferred").asString(),
                )
            }
        }

        item {
            RegistryCard(
                iconRes = R.drawable.lucide_battery_full,
                title = T("capability_matrix_title").asString(),
            ) {
                AppText(
                    text = T("capability_matrix_subtitle").asString(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(4.dp))
                state.capabilities.forEach { capability ->
                    CapabilityRow(capability = capability)
                }
            }
        }

        item {
            if (state.experimentalMetricsEnabled) {
                NoteCard(
                    title = T("experimental_metrics_title").asString(),
                    body = T("experimental_metrics_desc").asString(),
                )
            } else {
                NoteCard(
                    title = T("experimental_metrics_title").asString(),
                    body = T("experimental_metrics_disabled").asString(),
                )
            }
        }

        item { Spacer(Modifier.height(12.dp)) }
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
    badge: String,
    body: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        EvidenceBadge(text = badge)
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
    badge: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            EvidenceBadge(text = badge, compact = true)
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
private fun CapabilityRow(capability: com.synosoftware.battery.domain.DeviceCapability) {
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
            text = T("fallback_label", capability.fallback.asString()).asString(),
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
