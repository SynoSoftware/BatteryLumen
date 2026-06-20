# Capacity Trend

Show whether useful-session capacity estimates are stable, noisy, or declining. Use the last 12 useful points from `estimateCapacity(...)`.

Plot `estimatedCapacityMah` from useful sessions only. Keep weak sessions out of the line, show a confidence band, and avoid exact degradation rates.

- Useful and weak points are visually distinct.
- The trend can read stable, noisy, or declining.
- Low-confidence data does not look authoritative.
