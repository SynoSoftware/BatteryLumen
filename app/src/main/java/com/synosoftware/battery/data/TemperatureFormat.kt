package com.synosoftware.battery.data

import com.synosoftware.battery.R
import com.synosoftware.battery.data.preferences.TemperatureUnit
import com.synosoftware.battery.i18n.TR
import com.synosoftware.battery.i18n.UiText
import java.util.Locale

fun temperatureText(valueC: Float?, unit: TemperatureUnit): UiText {
    if (valueC == null) {
        return TR(R.string.value_na)
    }

    val value = when (unit) {
        TemperatureUnit.CELSIUS -> valueC
        TemperatureUnit.FAHRENHEIT -> valueC * 9f / 5f + 32f
    }
    val rounded = String.format(Locale.ROOT, "%.1f", value)

    return when (unit) {
        TemperatureUnit.CELSIUS -> TR(R.string.value_temp_c, rounded)
        TemperatureUnit.FAHRENHEIT -> TR(R.string.value_temp_f, rounded)
    }
}

fun sessionTemperatureText(
    maxTemperatureC: Float?,
    averageTemperatureC: Float?,
    unit: TemperatureUnit,
): UiText {
    val maxTemperature = maxTemperatureC?.let { temperatureText(it, unit) } ?: TR(R.string.value_na)
    val averageTemperature = averageTemperatureC?.let { temperatureText(it, unit) } ?: TR(R.string.value_na)

    return if (averageTemperatureC != null) {
        TR(R.string.session_temperature_with_average, maxTemperature, averageTemperature)
    } else {
        TR(R.string.session_temperature_title, maxTemperature)
    }
}
