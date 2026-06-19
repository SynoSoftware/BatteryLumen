package com.synosoftware.battery.data

import com.synosoftware.battery.data.preferences.TemperatureUnit
import com.synosoftware.battery.i18n.T
import com.synosoftware.battery.i18n.UiText
import java.util.Locale

fun temperatureText(valueC: Float?, unit: TemperatureUnit): UiText {
    if (valueC == null) {
        return T("value_na")
    }

    val value = when (unit) {
        TemperatureUnit.CELSIUS -> valueC
        TemperatureUnit.FAHRENHEIT -> valueC * 9f / 5f + 32f
    }
    val rounded = String.format(Locale.ROOT, "%.1f", value)

    return when (unit) {
        TemperatureUnit.CELSIUS -> T("value_temp_c", rounded)
        TemperatureUnit.FAHRENHEIT -> T("value_temp_f", rounded)
    }
}

fun sessionTemperatureText(
    maxTemperatureC: Float?,
    averageTemperatureC: Float?,
    unit: TemperatureUnit,
): UiText {
    val maxTemperature = maxTemperatureC?.let { temperatureText(it, unit) } ?: T("value_na")
    val averageTemperature = averageTemperatureC?.let { temperatureText(it, unit) } ?: T("value_na")

    return if (averageTemperatureC != null) {
        T("session_temperature_with_average", maxTemperature, averageTemperature)
    } else {
        T("session_temperature", maxTemperature)
    }
}
