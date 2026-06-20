# Model Documentation

## Current models

- Live battery risk
- Thermal stress
- Charge-level stress
- Session quality
- Current-session reliability
- Capacity estimation

## Rules

- Risk is a state-time model, not a single-session health penalty
- Higher temperature increases stress
- Higher charge level increases stress
- Long time near threshold levels increases stress
- Confidence reflects how complete and usable the current evidence is
- Raw session exposure stays in the ledger; derived stress, quality, and trend stay in domain code
- Capacity estimates are reported in mAh with a likely range and confidence
- Health percent is only a display conversion when design capacity is available
