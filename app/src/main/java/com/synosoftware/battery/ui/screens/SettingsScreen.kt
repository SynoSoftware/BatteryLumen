package com.synosoftware.battery.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text as AppText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.synosoftware.battery.R
import com.synosoftware.battery.data.preferences.TemperatureUnit
import com.synosoftware.battery.data.preferences.ThemeMode
import com.synosoftware.battery.data.temperatureText
import com.synosoftware.battery.i18n.T
import com.synosoftware.battery.i18n.asString
import com.synosoftware.battery.ui.components.IconBadge
import com.synosoftware.battery.ui.model.BatteryUiState

@Composable
fun SettingsScreen(
    state: BatteryUiState,
    onTargetSelected: (Int) -> Unit,
    onTemperatureUnitSelected: (TemperatureUnit) -> Unit,
    onExperimentalMetricsChanged: (Boolean) -> Unit,
    onThemeModeSelected: (ThemeMode) -> Unit,
    contentPadding: PaddingValues,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                AppText(
                    text = T("settings_page_title").asString(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                AppText(
                    text = T("settings_subtitle").asString(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        item {
            SettingsCard(
                iconRes = R.drawable.lucide_battery_full,
                title = T("settings_target_title").asString(),
                subtitle = T("settings_target_desc").asString(),
            ) {
                ChoiceRow(
                    choices = listOf(80, 85, 90, 100),
                    selected = state.targetChargePercent,
                    labelForChoice = { target -> T("value_percent", target).asString() },
                    onSelected = onTargetSelected,
                )
            }
        }

        item {
            SettingsCard(
                iconRes = R.drawable.lucide_thermometer,
                title = T("settings_temperature_title").asString(),
                subtitle = T("settings_temperature_desc").asString(),
            ) {
                ChoiceRow(
                    choices = TemperatureUnit.entries,
                    selected = state.temperatureUnit,
                    labelForChoice = { unit ->
                        when (unit) {
                            TemperatureUnit.CELSIUS -> T("settings_temperature_celsius").asString()
                            TemperatureUnit.FAHRENHEIT -> T("settings_temperature_fahrenheit").asString()
                        }
                    },
                    onSelected = onTemperatureUnitSelected,
                )
                if (state.currentSnapshot != null) {
                    AppText(
                        text = T(
                            "settings_temperature_preview",
                            temperatureText(state.currentSnapshot.temperatureC, state.temperatureUnit),
                        ).asString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        item {
            SettingsCard(
                iconRes = R.drawable.lucide_settings,
                title = T("settings_theme_title").asString(),
                subtitle = T("settings_theme_desc").asString(),
            ) {
                ChoiceRow(
                    choices = listOf(ThemeMode.SYSTEM, ThemeMode.LIGHT, ThemeMode.DARK),
                    selected = state.themeMode,
                    labelForChoice = { mode ->
                        when (mode) {
                            ThemeMode.SYSTEM -> T("settings_theme_system").asString()
                            ThemeMode.LIGHT -> T("settings_theme_light").asString()
                            ThemeMode.DARK -> T("settings_theme_dark").asString()
                        }
                    },
                    onSelected = onThemeModeSelected,
                )
            }
        }

        item {
                SettingsCard(
                iconRes = R.drawable.lucide_zap,
                title = T("experimental_metrics_title").asString(),
                subtitle = T("experimental_metrics_desc").asString(),
            ) {
                ToggleRow(
                    label = T("settings_experimental_toggle").asString(),
                    value = state.experimentalMetricsEnabled,
                    onValueChanged = onExperimentalMetricsChanged,
                )
            }
        }

        item { Spacer(Modifier.height(12.dp)) }
    }
}

@Composable
private fun SettingsCard(
    iconRes: Int,
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit,
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
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
                IconBadge(resId = iconRes, contentDescription = null)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    AppText(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    AppText(
                        text = subtitle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            content()
        }
    }
}

@Composable
private fun <T> ChoiceRow(
    choices: List<T>,
    selected: T,
    labelForChoice: @Composable (T) -> String,
    onSelected: (T) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        choices.forEach { choice ->
            FilterChip(
                selected = choice == selected,
                onClick = { onSelected(choice) },
                label = { AppText(text = labelForChoice(choice)) },
            )
        }
    }
}

@Composable
private fun ToggleRow(
    label: String,
    value: Boolean,
    onValueChanged: (Boolean) -> Unit,
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                AppText(
                    text = label,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                AppText(
                    text = if (value) T("settings_toggle_on").asString() else T("settings_toggle_off").asString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(
                checked = value,
                onCheckedChange = onValueChanged,
            )
        }
    }
}
