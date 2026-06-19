package com.synosoftware.battery.domain

import com.synosoftware.battery.i18n.T

object DeviceCapabilityMatrix {
    fun defaultCapabilities(): List<DeviceCapability> = listOf(
        DeviceCapability(
            key = "battery_level",
            label = T("capability_battery_level"),
            source = T("capability_battery_level_source"),
            unit = T("capability_percent_unit"),
            availability = T("capability_battery_level_availability"),
            reliabilityRule = T("capability_battery_level_rule"),
            evidenceGrade = EvidenceGrade.MEASURED,
            fallback = T("capability_battery_level_fallback"),
        ),
        DeviceCapability(
            key = "battery_temperature",
            label = T("capability_battery_temperature"),
            source = T("capability_battery_temperature_source"),
            unit = T("capability_celsius_unit"),
            availability = T("capability_battery_temperature_availability"),
            reliabilityRule = T("capability_battery_temperature_rule"),
            evidenceGrade = EvidenceGrade.MEASURED,
            fallback = T("capability_battery_temperature_fallback"),
        ),
        DeviceCapability(
            key = "plug_type",
            label = T("capability_plug_type"),
            source = T("capability_plug_type_source"),
            unit = T("capability_enum_unit"),
            availability = T("capability_plug_type_availability"),
            reliabilityRule = T("capability_plug_type_rule"),
            evidenceGrade = EvidenceGrade.MEASURED,
            fallback = T("capability_plug_type_fallback"),
        ),
        DeviceCapability(
            key = "voltage",
            label = T("capability_voltage"),
            source = T("capability_voltage_source"),
            unit = T("capability_millivolt_unit"),
            availability = T("capability_voltage_availability"),
            reliabilityRule = T("capability_voltage_rule"),
            evidenceGrade = EvidenceGrade.MEASURED,
            fallback = T("capability_voltage_fallback"),
        ),
        DeviceCapability(
            key = "current_now",
            label = T("capability_current_now"),
            source = T("capability_current_now_source"),
            unit = T("capability_microamp_unit"),
            availability = T("capability_current_now_availability"),
            reliabilityRule = T("capability_current_now_rule"),
            evidenceGrade = EvidenceGrade.MEASURED,
            fallback = T("capability_current_now_fallback"),
        ),
        DeviceCapability(
            key = "charge_counter",
            label = T("capability_charge_counter"),
            source = T("capability_charge_counter_source"),
            unit = T("capability_microamp_hour_unit"),
            availability = T("capability_charge_counter_availability"),
            reliabilityRule = T("capability_charge_counter_rule"),
            evidenceGrade = EvidenceGrade.MEASURED,
            fallback = T("capability_charge_counter_fallback"),
        ),
    )
}
