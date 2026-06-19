# Health Estimate

## Feature
Battery health estimate and useful-capacity range.

## Why it matters
Users want to know whether their battery is actually degrading over time.

## Inputs needed
Useful charge sessions, charge gain, charge counter or current readings when available, session quality, and design capacity.

## Evidence grade
Estimated.

## Confidence rules
Low before enough useful sessions.
Medium when several useful sessions agree.
High only when readings are stable and device data is reliable.

## Overclaiming risks
Do not show fake precision.
Do not claim exact lifespan remaining.
Do not let weak sessions drive the estimate.

## Blocked by
Charge-session ledger, useful-vs-weak session classification, device capability matrix, and capacity model documentation.

## Acceptance tests
- Fewer than 5 useful sessions shows not enough data.
- Noisy sessions are stored but excluded.
- Stable large sessions produce an approximate range, not exact precision.
