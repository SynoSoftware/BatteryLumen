# Data Schema

`charge_sessions` is the canonical record of charging evidence.

Stored columns:

- Time: `startedAtMs`, `lastSeenAtMs`, `endedAtMs`
- Level: `startLevelPercent`, `currentLevelPercent`, `gainPercent`
- Charge counter: `startChargeCounterUah`, `currentChargeCounterUah`
- Temperature: `startTemperatureC`, `currentTemperatureC`, `maxTemperatureC`, `averageTemperatureC`
- Heat exposure: `timeAbove35Sec`, `timeAbove40Sec`, `timeAbove43Sec`, `timeAbove45Sec`
- Charge exposure: `timeAbove80Sec`, `timeAbove85Sec`, `timeAbove90Sec`, `timeAbove95Sec`
- State: `chargingSource`, `chargingState`, `status`, `sampleCount`, `lastNotifiedTargetPercent`

`status` uses `ACTIVE`, `COMPLETED`, or `INCOMPLETE`.

Domain code derives stress, quality, confidence, and health display values on read.

`DataStore` keeps `target_charge_percent`, `design_capacity_mah`, `experimental_metrics_enabled`, `temperature_unit`, and `theme_mode`.

Health percent is display-only and requires a design-capacity setting.
