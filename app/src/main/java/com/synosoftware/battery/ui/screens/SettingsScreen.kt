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
    onDesignCapacitySelected: (Int?) -> Unit,
    onTemperatureUnitSelected: (TemperatureUnit) -> Unit,
    onExperimentalMetricsChanged: (Boolean) -> Unit,
    onThemeModeSelected: (ThemeMode) -> Unit,
    contentPadding: PaddingValues,
) {
    var designCapacityText by rememberSaveable { mutableStateOf(state.designCapacityMah?.toString().orEmpty()) }
    LaunchedEffect(state.designCapacityMah) {
        val expected = state.designCapacityMah?.toString().orEmpty()
        if (designCapacityText != expected) {
            designCapacityText = expected
        }
    }

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
                    text = T(R.string.settings_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                AppText(
                    text = T(R.string.settings_subtitle),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        item {
            SettingsCard(
                iconRes = R.drawable.lucide_battery_full,
                title = T(R.string.settings_target_title),
                subtitle = T(R.string.settings_target_description),
            ) {
                ChoiceRow(
                    choices = listOf(80, 85, 90, 100),
                    selected = state.targetChargePercent,
                    labelForChoice = { target -> T(R.string.value_percent, target) },
                    onSelected = onTargetSelected,
                )
            }
        }

        item {
            SettingsCard(
                iconRes = R.drawable.lucide_heart,
                title = T(R.string.settings_design_title),
                subtitle = T(R.string.settings_design_description),
            ) {
                OutlinedTextField(
                    value = designCapacityText,
                    onValueChange = { input ->
                        val digits = input.filter(Char::isDigit)
                        designCapacityText = digits
                        onDesignCapacitySelected(digits.toIntOrNull())
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { AppText(T(R.string.settings_design_label)) },
                    placeholder = { AppText(T(R.string.settings_design_placeholder)) },
                    supportingText = {
                        AppText(
                            text = T(R.string.settings_design_supporting),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    },
                )
            }
        }

        item {
            SettingsCard(
                iconRes = R.drawable.lucide_thermometer,
                title = T(R.string.settings_temperature_title),
                subtitle = T(R.string.settings_temperature_description),
            ) {
                ChoiceRow(
                    choices = TemperatureUnit.entries,
                    selected = state.temperatureUnit,
                    labelForChoice = { unit ->
                        when (unit) {
                            TemperatureUnit.CELSIUS -> T(R.string.settings_temperature_celsius)
                            TemperatureUnit.FAHRENHEIT -> T(R.string.settings_temperature_fahrenheit)
                        }
                    },
                    onSelected = onTemperatureUnitSelected,
                )
                if (state.currentSnapshot != null) {
                    AppText(
                        text = T(
                            R.string.settings_temperature_preview,
                            temperatureText(state.currentSnapshot.temperatureC, state.temperatureUnit),
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        item {
            SettingsCard(
                iconRes = R.drawable.lucide_settings,
                title = T(R.string.settings_theme_title),
                subtitle = T(R.string.settings_theme_description),
            ) {
                ChoiceRow(
                    choices = listOf(ThemeMode.SYSTEM, ThemeMode.LIGHT, ThemeMode.DARK),
                    selected = state.themeMode,
                    labelForChoice = { mode ->
                        when (mode) {
                            ThemeMode.SYSTEM -> T(R.string.settings_theme_system)
                            ThemeMode.LIGHT -> T(R.string.settings_theme_light)
                            ThemeMode.DARK -> T(R.string.settings_theme_dark)
                        }
                    },
                    onSelected = onThemeModeSelected,
                )
            }
        }

        item {
            SettingsCard(
                iconRes = R.drawable.lucide_zap,
                title = T(R.string.settings_experimental_title),
                subtitle = T(R.string.settings_experimental_description),
            ) {
                ToggleRow(
                    label = T(R.string.settings_experimental_toggle),
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
                    text = if (value) T(R.string.settings_toggle_on) else T(R.string.settings_toggle_off),
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
