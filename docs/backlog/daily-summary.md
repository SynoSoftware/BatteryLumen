# Daily Summary

## Feature
Per-day charging quality summary.

## Why it matters
This gives a compact behavior summary without requiring users to inspect individual sessions.

## Inputs needed
Daily sessions, max temperature, time above charge thresholds, and session quality.

## Evidence grade
Inferred.

## Confidence rules
Medium only when the day has enough observed charging time.
Low when the device was mostly unobserved.

## Overclaiming risks
Do not turn the summary into a moral score.
Do not present a single bad session as a verdict on the battery.

## Blocked by
Session aggregation and health/trend logic.

## Acceptance tests
- A quiet day can still produce a summary.
- A hot day highlights temperature and time-above-threshold.
- Weak observations are labeled.
