package com.synosoftware.battery.domain

import com.synosoftware.battery.i18n.T

object DeviceCapabilityMatrix {
    fun defaultCapabilities(): List<DeviceCapability> = listOf(
        DeviceCapability(
            key = "battery_level",
            label = T("capability.battery.level.label"),
            source = T("capability.battery.level.source"),
            unit = T("capability.percent.unit"),
            availability = T("capability.battery.level.availability"),
            reliabilityRule = T("capability.battery.level.rule"),
            evidenceGrade = EvidenceGrade.MEASURED,
            fallback = T("capability.battery.level.fallback"),
        ),
        DeviceCapability(
            key = "battery_temperature",
            label = T("capability.battery.temperature.label"),
            source = T("capability.battery.temperature.source"),
            unit = T("capability.celsius.unit"),
            availability = T("capability.battery.temperature.availability"),
            reliabilityRule = T("capability.battery.temperature.rule"),
            evidenceGrade = EvidenceGrade.MEASURED,
            fallback = T("capability.battery.temperature.fallback"),
        ),
        DeviceCapability(
            key = "plug_type",
            label = T("capability.plug.type.label"),
            source = T("capability.plug.type.source"),
            unit = T("capability.enum.unit"),
            availability = T("capability.plug.type.availability"),
            reliabilityRule = T("capability.plug.type.rule"),
            evidenceGrade = EvidenceGrade.MEASURED,
            fallback = T("capability.plug.type.fallback"),
        ),
        DeviceCapability(
            key = "voltage",
            label = T("capability.voltage.label"),
            source = T("capability.voltage.source"),
            unit = T("capability.millivolt.unit"),
            availability = T("capability.voltage.availability"),
            reliabilityRule = T("capability.voltage.rule"),
            evidenceGrade = EvidenceGrade.MEASURED,
            fallback = T("capability.voltage.fallback"),
        ),
        DeviceCapability(
            key = "current_now",
            label = T("capability.current.now.label"),
            source = T("capability.current.now.source"),
            unit = T("capability.microamp.unit"),
            availability = T("capability.current.now.availability"),
            reliabilityRule = T("capability.current.now.rule"),
            evidenceGrade = EvidenceGrade.MEASURED,
            fallback = T("capability.current.now.fallback"),
        ),
        DeviceCapability(
            key = "charge_counter",
            label = T("capability.charge.counter.label"),
            source = T("capability.charge.counter.source"),
            unit = T("capability.microamp.hour.unit"),
            availability = T("capability.charge.counter.availability"),
            reliabilityRule = T("capability.charge.counter.rule"),
            evidenceGrade = EvidenceGrade.MEASURED,
            fallback = T("capability.charge.counter.fallback"),
        ),
    )
}
