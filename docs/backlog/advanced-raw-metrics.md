# Advanced Raw Metrics

## Feature
Advanced raw battery metrics like voltage, current, charge counter, and plug details.

## Why it matters
These values are useful for power users and for improving future models.

## Inputs needed
BatteryManager support and device-specific availability rules.

## Evidence grade
Measured when available, unavailable otherwise.

## Confidence rules
Show raw metrics only when the device exposes them and the app can label them correctly.

## Overclaiming risks
Do not let raw metrics dominate the main UI.
Do not invent reliability where the device does not support it.

## Blocked by
Capability matrix, UI placement rules, and model validation.

## Acceptance tests
- Unsupported metrics show as unavailable.
- Supported metrics retain unit and source labels.
- Raw metrics stay secondary.
