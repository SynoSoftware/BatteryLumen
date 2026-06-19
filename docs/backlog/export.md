# Export

## Feature
CSV and JSON export for sessions, summaries, and models.

## Why it matters
Users should be able to inspect and move their own data.

## Inputs needed
Ledger rows, device capabilities, and model metadata.

## Evidence grade
Measured for raw session fields; inferred for derived summaries.

## Confidence rules
Export should preserve quality labels and not flatten them into raw numbers only.

## Overclaiming risks
Do not export derived values without their evidence labels.
Do not silently omit failed or weak sessions.

## Blocked by
Stable schema and ledger fields.

## Acceptance tests
- JSON contains the model metadata.
- CSV includes evidence and confidence columns.
- Weak sessions remain identifiable.

