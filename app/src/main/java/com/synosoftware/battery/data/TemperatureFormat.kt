package com.synosoftware.battery.data

import com.synosoftware.battery.data.preferences.TemperatureUnit
import com.synosoftware.battery.i18n.T
import com.synosoftware.battery.i18n.UiText
import java.util.Locale

fun temperatureText(valueC: Float?, unit: TemperatureUnit): UiText {
    if (valueC == null) {
        return T("value.na")
    }

    val value = when (unit) {
        TemperatureUnit.CELSIUS -> valueC
        TemperatureUnit.FAHRENHEIT -> valueC * 9f / 5f + 32f
    }
    val rounded = String.format(Locale.ROOT, "%.1f", value)

    return when (unit) {
        TemperatureUnit.CELSIUS -> T("value.temp.c", rounded)
        TemperatureUnit.FAHRENHEIT -> T("value.temp.f", rounded)
    }
}

fun sessionTemperatureText(
    maxTemperatureC: Float?,
    averageTemperatureC: Float?,
    unit: TemperatureUnit,
): UiText {
    val maxTemperature = maxTemperatureC?.let { temperatureText(it, unit) } ?: T("value.na")
    val averageTemperature = averageTemperatureC?.let { temperatureText(it, unit) } ?: T("value.na")

    return if (averageTemperatureC != null) {
        T("session.temperature.with.average", maxTemperature, averageTemperature)
    } else {
        T("session.temperature.title", maxTemperature)
    }
}
