# Capacity Trend

## Feature
Long-term capacity trend graph with confidence band and point quality labels.

## Why it matters
Users should see whether the battery appears stable, noisy, or declining.

## Inputs needed
Capacity estimates over time, useful-session flags, and confidence values.

## Evidence grade
Estimated and inferred.

## Confidence rules
Low when there are too few points or the points are noisy.
Medium when recent points agree.
High only when multiple useful sessions cluster around the same trend.

## Overclaiming risks
Do not force a sharp line through noisy data.
Do not imply exact degradation rates from weak sessions.

## Blocked by
Health estimation, confidence rules, and charting implementation.

## Acceptance tests
- Useful points render differently from weak points.
- Confidence band is visible.
- Trend copy can say stable, declining, or noisy.

