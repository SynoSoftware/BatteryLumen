# Data Schema

## Room tables

### `charge_sessions`

- `startedAtMs`
- `lastSeenAtMs`
- `endedAtMs`
- `startLevelPercent`
- `currentLevelPercent`
- `temperature` fields
- `timeAbove85Sec`
- `timeAbove90Sec`
- `chargingSource`
- `chargingState`
- `sampleCount`
- `lastNotifiedTargetPercent`
- `gainPercent`

Assessment fields such as quality, confidence, usefulness, and stress are derived by `BatteryDecisionEngine` on read, not stored as canonical session data.

## DataStore keys

- `target_charge_percent`
- `experimental_metrics_enabled`
- `temperature_unit`
