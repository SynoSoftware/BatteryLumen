package com.synosoftware.battery.domain

import com.synosoftware.battery.R
import com.synosoftware.battery.i18n.TR

object DeviceCapabilityMatrix {
    fun defaultCapabilities(): List<DeviceCapability> = listOf(
        DeviceCapability(
            key = "battery_level",
            label = TR(R.string.capability_battery_level_label),
            source = TR(R.string.capability_battery_level_source),
            unit = TR(R.string.capability_percent_unit),
            availability = TR(R.string.capability_battery_level_availability),
            reliabilityRule = TR(R.string.capability_battery_level_rule),
            evidenceGrade = EvidenceGrade.MEASURED,
            fallback = TR(R.string.capability_battery_level_fallback),
        ),
        DeviceCapability(
            key = "battery_temperature",
            label = TR(R.string.capability_battery_temperature_label),
            source = TR(R.string.capability_battery_temperature_source),
            unit = TR(R.string.capability_celsius_unit),
            availability = TR(R.string.capability_battery_temperature_availability),
            reliabilityRule = TR(R.string.capability_battery_temperature_rule),
            evidenceGrade = EvidenceGrade.MEASURED,
            fallback = TR(R.string.capability_battery_temperature_fallback),
        ),
        DeviceCapability(
            key = "plug_type",
            label = TR(R.string.capability_plug_type_label),
            source = TR(R.string.capability_plug_type_source),
            unit = TR(R.string.capability_enum_unit),
            availability = TR(R.string.capability_plug_type_availability),
            reliabilityRule = TR(R.string.capability_plug_type_rule),
            evidenceGrade = EvidenceGrade.MEASURED,
            fallback = TR(R.string.capability_plug_type_fallback),
        ),
        DeviceCapability(
            key = "voltage",
            label = TR(R.string.capability_voltage_label),
            source = TR(R.string.capability_voltage_source),
            unit = TR(R.string.capability_millivolt_unit),
            availability = TR(R.string.capability_voltage_availability),
            reliabilityRule = TR(R.string.capability_voltage_rule),
            evidenceGrade = EvidenceGrade.MEASURED,
            fallback = TR(R.string.capability_voltage_fallback),
        ),
        DeviceCapability(
            key = "current_now",
            label = TR(R.string.capability_current_now_label),
            source = TR(R.string.capability_current_now_source),
            unit = TR(R.string.capability_microamp_unit),
            availability = TR(R.string.capability_current_now_availability),
            reliabilityRule = TR(R.string.capability_current_now_rule),
            evidenceGrade = EvidenceGrade.MEASURED,
            fallback = TR(R.string.capability_current_now_fallback),
        ),
        DeviceCapability(
            key = "charge_counter",
            label = TR(R.string.capability_charge_counter_label),
            source = TR(R.string.capability_charge_counter_source),
            unit = TR(R.string.capability_microamp_hour_unit),
            availability = TR(R.string.capability_charge_counter_availability),
            reliabilityRule = TR(R.string.capability_charge_counter_rule),
            evidenceGrade = EvidenceGrade.MEASURED,
            fallback = TR(R.string.capability_charge_counter_fallback),
        ),
    )
}
