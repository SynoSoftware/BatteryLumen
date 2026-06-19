# Health Confidence Rules

## Feature
Confidence policy for battery-health estimates and trend interpretation.

## Why it matters
The product promise depends on distinguishing trustworthy estimates from noisy ones.

## Inputs needed
Useful session count, session duration, charge gain, sensor availability, and session stability.

## Evidence grade
Estimated for the health estimate itself; inferred for the confidence label.

## Confidence rules
Low when the app has too few useful sessions or device readings are missing.
Medium when multiple useful sessions agree but some readings are imperfect.
High only when useful sessions are stable, sufficiently large, and consistent.

## Overclaiming risks
Do not use confidence as a cosmetic badge.
Do not imply certainty where the device data cannot support it.

## Blocked by
Ledger quality, capability matrix, and health estimation logic.

## Acceptance tests
- Very small sample counts stay low confidence.
- Conflicting sessions reduce confidence.
- Stable repeated sessions increase confidence.
