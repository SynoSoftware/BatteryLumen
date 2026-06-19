package com.synosoftware.battery.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.userPreferencesDataStore by preferencesDataStore(name = "user_preferences")

enum class TemperatureUnit {
    CELSIUS,
    FAHRENHEIT,
}

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
}

data class UserPreferences(
    val targetChargePercent: Int = 85,
    val experimentalMetricsEnabled: Boolean = false,
    val temperatureUnit: TemperatureUnit = TemperatureUnit.CELSIUS,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
)

class SettingsRepository(
    private val context: Context,
) {
    private val targetKey = intPreferencesKey("target_charge_percent")
    private val experimentalKey = booleanPreferencesKey("experimental_metrics_enabled")
    private val temperatureUnitKey = stringPreferencesKey("temperature_unit")
    private val themeModeKey = stringPreferencesKey("theme_mode")

    val preferences: Flow<UserPreferences> = context.userPreferencesDataStore.data.map { prefs ->
        prefs.toUserPreferences()
    }

    suspend fun setTargetChargePercent(percent: Int) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[targetKey] = percent.coerceIn(50, 100)
        }
    }

    suspend fun setExperimentalMetricsEnabled(enabled: Boolean) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[experimentalKey] = enabled
        }
    }

    suspend fun setTemperatureUnit(unit: TemperatureUnit) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[temperatureUnitKey] = unit.name
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[themeModeKey] = mode.name
        }
    }

    private fun Preferences.toUserPreferences(): UserPreferences {
        val targetChargePercent = this[targetKey] ?: 85
        val experimentalMetricsEnabled = this[experimentalKey] ?: false
        val temperatureUnit = runCatching {
            TemperatureUnit.valueOf(this[temperatureUnitKey] ?: TemperatureUnit.CELSIUS.name)
        }.getOrDefault(TemperatureUnit.CELSIUS)
        val themeMode = runCatching {
            ThemeMode.valueOf(this[themeModeKey] ?: ThemeMode.SYSTEM.name)
        }.getOrDefault(ThemeMode.SYSTEM)
        return UserPreferences(
            targetChargePercent = targetChargePercent,
            experimentalMetricsEnabled = experimentalMetricsEnabled,
            temperatureUnit = temperatureUnit,
            themeMode = themeMode,
        )
    }
}
