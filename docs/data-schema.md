# Data Schema

## Room tables

### `charge_sessions`

- `startedAtMs`
- `lastSeenAtMs`
- `endedAtMs`
- `startLevelPercent`
- `currentLevelPercent`
- `startChargeCounterUah`
- `currentChargeCounterUah`
- `startTemperatureC`
- `currentTemperatureC`
- `maxTemperatureC`
- `averageTemperatureC`
- `timeAbove85Sec`
- `timeAbove90Sec`
- `timeAbove35Sec`
- `timeAbove40Sec`
- `timeAbove43Sec`
- `timeAbove45Sec`
- `timeAbove80Sec`
- `timeAbove95Sec`
- `chargingSource`
- `chargingState`
- `status`
- `sampleCount`
- `lastNotifiedTargetPercent`
- `gainPercent`

The session table stores raw exposure facts only. Assessment fields such as quality, confidence, usefulness, stress, and capacity trend are derived in domain code on read, not stored as canonical session data.

Health percent is not a canonical stored value. When a design-capacity setting exists, the UI may convert the capacity estimate in mAh into a percentage for display.

## DataStore keys

- `target_charge_percent`
- `experimental_metrics_enabled`
- `temperature_unit`
