# Health Estimate

Estimate useful capacity from charging sessions only after enough useful data exists.

The current gate is `MIN_USEFUL_SESSION_COUNT = 5`. Use charge gain and charge counter or current when available, exclude weak sessions, and show a range instead of fake precision. Health percent stays display-only when design capacity exists.

- The empty state explains the threshold.
- Stable sessions produce an approximate range.
- No UI claims exact lifespan remaining.
