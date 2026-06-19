# Live Monitor

## Feature
Optional explicit live charging monitor.

## Why it matters
This is the most reliable path for richer unattended observation, but it should not be required for core value.

## Inputs needed
Foreground service behavior, user opt-in, and visible notification handling.

## Evidence grade
Measured for live snapshots; inferred for higher-level conclusions.

## Confidence rules
Higher than opportunistic capture, but still limited by device support.

## Overclaiming risks
Do not pretend the app can monitor forever in the background for free.
Do not imply hidden tracking.

## Blocked by
Observed value from the initial charging assistant and permission UX.

## Acceptance tests
- User opt-in is explicit.
- The service runs only while charging.
- It stops when unplugged.
