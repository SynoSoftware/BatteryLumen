package com.synosoftware.battery.domain

import com.synosoftware.battery.i18n.text

object DeviceCapabilityMatrix {
    fun defaultCapabilities(): List<DeviceCapability> = listOf(
        DeviceCapability(
            key = "battery_level",
            label = text("capability_battery_level"),
            source = text("capability_battery_level_source"),
            unit = text("capability_percent_unit"),
            availability = text("capability_battery_level_availability"),
            reliabilityRule = text("capability_battery_level_rule"),
            evidenceGrade = EvidenceGrade.MEASURED,
            fallback = text("capability_battery_level_fallback"),
        ),
        DeviceCapability(
            key = "battery_temperature",
            label = text("capability_battery_temperature"),
            source = text("capability_battery_temperature_source"),
            unit = text("capability_celsius_unit"),
            availability = text("capability_battery_temperature_availability"),
            reliabilityRule = text("capability_battery_temperature_rule"),
            evidenceGrade = EvidenceGrade.MEASURED,
            fallback = text("capability_battery_temperature_fallback"),
        ),
        DeviceCapability(
            key = "plug_type",
            label = text("capability_plug_type"),
            source = text("capability_plug_type_source"),
            unit = text("capability_enum_unit"),
            availability = text("capability_plug_type_availability"),
            reliabilityRule = text("capability_plug_type_rule"),
            evidenceGrade = EvidenceGrade.MEASURED,
            fallback = text("capability_plug_type_fallback"),
        ),
        DeviceCapability(
            key = "voltage",
            label = text("capability_voltage"),
            source = text("capability_voltage_source"),
            unit = text("capability_millivolt_unit"),
            availability = text("capability_voltage_availability"),
            reliabilityRule = text("capability_voltage_rule"),
            evidenceGrade = EvidenceGrade.MEASURED,
            fallback = text("capability_voltage_fallback"),
        ),
        DeviceCapability(
            key = "current_now",
            label = text("capability_current_now"),
            source = text("capability_current_now_source"),
            unit = text("capability_microamp_unit"),
            availability = text("capability_current_now_availability"),
            reliabilityRule = text("capability_current_now_rule"),
            evidenceGrade = EvidenceGrade.MEASURED,
            fallback = text("capability_current_now_fallback"),
        ),
        DeviceCapability(
            key = "charge_counter",
            label = text("capability_charge_counter"),
            source = text("capability_charge_counter_source"),
            unit = text("capability_microamp_hour_unit"),
            availability = text("capability_charge_counter_availability"),
            reliabilityRule = text("capability_charge_counter_rule"),
            evidenceGrade = EvidenceGrade.MEASURED,
            fallback = text("capability_charge_counter_fallback"),
        ),
    )
}
