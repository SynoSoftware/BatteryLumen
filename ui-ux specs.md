# Product UI Specification: Open Battery Decision Assistant

## 1. Product UI Goal

The app should help the user make one clear decision:

**Am I treating my battery well right now, and what should I do?**

The UI must be:

* Fluent
* Ergonomic
* Quiet
* Evidence-based
* Low-noise
* No fake precision
* No fear language
* No technical dashboard by default

The app should show advice first, evidence second, raw data last.

---

# 2. Main Navigation

Use four main screens:

1. **Now**
2. **Health**
3. **Ledger**
4. **How it works**

Each screen has one job.

| Screen       | User question                                    |
| ------------ | ------------------------------------------------ |
| Now          | What should I do right now?                      |
| Health       | Is my battery actually degrading?                |
| Ledger       | What evidence is the app using?                  |
| How it works | Which numbers are real, estimated, or uncertain? |

---

# 3. Global UI Rules

## 3.1 No fake precision

Never show:

* Exact wear per session
* Exact lifetime lost
* Exact battery lifespan remaining
* “This charge cost X minutes of battery life”
* “Battery will last X times longer”
* “887% efficiency”

Show instead:

* `Low / Normal / High stress`
* `Likely range: 74–80%`
* `Confidence: Medium`
* `Not enough data`
* `Ignored for health estimate`

## 3.2 Evidence labels

Every important value must be labeled as:

* **Measured**: directly read from the device
* **Estimated**: calculated from measured data
* **Inferred**: a judgment, classification, or recommendation
* **Experimental**: not proven enough for normal users

Examples:

```text
Temp: 42°C — Measured
Time to 85%: ~12 min — Estimated
Battery stress: High — Inferred
Session impact: Above average — Experimental
```

## 3.3 Advice hierarchy

Every advice card should follow this order:

1. **State**
2. **Why**
3. **Action**
4. **Confidence**

Example:

```text
Battery stress: High
Why: Battery is hot while charging above 85%.
Action: Unplug now or let the phone cool.
Confidence: High
```

---

# 4. Screen: Now

## Purpose

Answer:

**What should I do right now?**

This is the main screen.

---

## 4.1 Charging state

### Top card: Battery stress

Show one dominant card.

Example:

```text
Battery stress
High

Battery is 42°C while charging above 85%.

Unplug now or let the phone cool.

Confidence: High
Based on direct temperature and battery level
```

Stress states:

* Excellent
* Good
* Normal
* High stress
* Severe stress

Use **Normal** generously. Normal charging should not feel bad.

---

## 4.2 Best advice rules

Show the most useful advice based on the current condition.

### If battery is cool and below target

```text
Battery stress: Good
Why: Temperature is normal and charge level is moderate.
Action: Continue charging.
```

### If battery is warm but not severe

```text
Battery stress: High
Why: Battery is warm while charging.
Action: Let the phone cool or avoid heavy use while charging.
```

### If battery is hot

```text
Battery stress: Severe
Why: Battery is 45°C while charging.
Action: Unplug now if you do not need more charge.
```

### If battery is above 85%

```text
Battery stress: Normal / High
Why: Battery is near full. Staying near full adds aging stress over time.
Action: Unplug if you do not need 100%.
```

### If battery is at 100%

```text
Battery stress: High
Why: Battery is full. Staying full for long periods increases aging stress.
Action: Unplug when convenient.
```

Do not say:

```text
You are damaging your battery.
```

Say:

```text
This condition increases aging stress over time.
```

---

## 4.3 Live telemetry

Show only the most useful direct values.

```text
Temp:   42°C      Measured
Level:  86%       Measured
State:  Charging  Measured
```

Do not show watts, volts, current, cycles, or mAh on the default Now screen.

Those belong in expanded details.

---

## 4.4 Target and alarm card

Show:

```text
Best stop:       85%        Recommended
Time to target:  ~12 min    Estimated
Full charge:     ~46 min    Estimated

[ Set alarm for 85% ]
```

Context text:

```text
Continuing past 85% gives less daily value and increases aging stress, especially when warm.
```

If user selects 100%:

```text
Charging to 100% is fine when needed. Avoid staying full for long periods.
```

---

## 4.5 Discharging state

When unplugged, the Now screen should not pretend to be a charging assistant.

Show:

```text
Battery use
Normal

Temperature is normal and battery level is safe.

Estimated remaining: ~5h 20m
Confidence: Medium
```

If battery is very low:

```text
Battery level is low
Use normally, but avoid heavy load if the phone is hot.

Estimated remaining: ~32 min
```

Avoid:

```text
Low battery is damaging your battery.
```

---

# 5. Screen: Health

## Purpose

Answer:

**Is my battery actually getting worse?**

The Health screen should not update from weak data.

---

## 5.1 Not enough data state

Before enough useful sessions exist, show:

```text
Not enough useful sessions yet

We need several larger, uninterrupted charges before estimating battery health.

Useful sessions: 2 / 5
Recommended: charges with over 30% gain
```

Actions:

```text
View Ledger
How estimates work
```

Do not show fake battery health before enough evidence exists.

---

## 5.2 Health estimate state

Show:

```text
~77% Useful Capacity
Estimated

Likely range: 74–80%
Confidence: Medium
Based on 12 useful sessions
```

Meaning text:

```text
Your phone appears to store about 23% less energy than when new.
```

If confidence is low:

```text
This estimate may change as more useful sessions are recorded.
```

---

## 5.3 Capacity trend graph

Show:

* Solid dots: useful estimated capacity points
* Hollow dots: weak estimated points excluded from trend
* Moving average
* Shaded confidence band
* Subtle horizontal gridlines only

Do not show a sharp single line that implies certainty.

Below graph, show one conclusion:

```text
Battery appears stable.
```

or:

```text
Battery is slowly declining.
```

or:

```text
Recent readings are noisy. More useful sessions are needed.
```

or:

```text
Drop appears real because multiple useful sessions agree.
```

---

## 5.4 Health advice

Advice should depend on health and confidence.

### Good health

```text
Battery health looks normal.
Keep avoiding heat while charging.
```

### Aging battery

```text
Battery capacity appears reduced.
If daily battery life feels poor, replacement may be worth considering.
```

### Low confidence

```text
Do not make decisions from this estimate yet.
The app needs more useful sessions.
```

---

# 6. Screen: Ledger

## Purpose

Answer:

**What evidence is the app using?**

The Ledger should feel like a clean transaction log.

---

## 6.1 Session row

Each row shows:

```text
+42%    10:00–10:45    40% → 82%    Useful
```

Possible tags:

* Useful
* Weak
* Warm
* Hot
* Incomplete
* Excluded

Tags are inferred.

---

## 6.2 Expanded row

On tap:

```text
Max temp:        41°C       Measured
Time above 85%: 12 min     Measured
Screen on:       4 min      Measured
Capacity point:  Included   Estimated

Why useful:
Charge gain was over 30%, readings were stable, and the session was not interrupted.
```

For weak session:

```text
Capacity point: Excluded

Why excluded:
Charge gain was under 30%, so this session is too noisy for health estimation.
```

For incomplete session:

```text
Incomplete session

Last measured point: 68% at 10:32
Missing interval: 10:32–10:51

Reason:
Android restricted the app during charging.
```

---

## 6.3 Ledger advice

Ledger should teach data quality without lecturing.

Good copy:

```text
Recorded, but ignored for health estimate.
```

```text
Useful for health estimate.
```

```text
Noisy session. Stored for history only.
```

Bad copy:

```text
Bad charge.
```

```text
Invalid session.
```

```text
You charged incorrectly.
```

---

# 7. Screen: How it works

## Purpose

Answer:

**Which numbers are real, estimated, inferred, or uncertain?**

This screen should feel like a readable open-source README.

---

## 7.1 Evidence labels section

Show:

```text
Measured
Read directly from Android or device sensors.

Estimated
Calculated from measured values.

Inferred
A judgment or recommendation based on measured and estimated values.

Experimental
Not proven enough for normal decisions.
```

---

## 7.2 Thermal stress model

Show:

```text
Heat is one of the strongest known battery-aging factors.

The app flags higher stress when the battery is warm or hot, especially while charging or near full.
```

Practical bands:

```text
Under 35°C: low concern
35–40°C: moderate concern
40–43°C: high concern
43–45°C: very high concern
Above 45°C: severe concern
```

Required note:

```text
These are practical guidance bands, not absolute chemistry constants. Actual degradation depends on battery chemistry, age, charge level, current, and duration.
```

---

## 7.3 Charge-level stress model

Show:

```text
Higher charge levels generally increase aging stress, especially when the battery stays near full for long periods.
```

Required note:

```text
85% is a practical charging target, not a chemistry cliff. Charging to 100% is fine when needed; staying near full for long periods increases stress.
```

---

## 7.4 Capacity model

Show:

```text
Battery health is estimated from useful charge sessions.

The app compares how much the battery percentage increased with charge/current readings when available.
```

Required note:

```text
Voltage may be logged, but voltage alone is not treated as a reliable capacity source.
```

Explain:

```text
Large uninterrupted charges are more useful.
Short charges are noisy.
Wireless charging may add uncertainty.
Heavy use while charging may reduce quality.
Trend matters more than one reading.
```

---

## 7.5 Experimental metrics

Experimental metrics are disabled by default.

Allowed output:

```text
Impact: Above average
Confidence: Low
```

Never allowed:

```text
Wear this session: 0.004%
Battery life used: 0.006%
This charge cost 12 minutes of lifespan
```

Required warning:

```text
This cannot be measured precisely from a single session.
```

---

# 8. Best Advice Library

The app should use consistent advice.

## Hot while charging

```text
Let the phone cool or unplug if you do not need more charge.
```

Why:

```text
Heat while charging increases aging stress.
```

## High charge level

```text
Unplug if you do not need 100%.
```

Why:

```text
Staying near full increases aging stress over time.
```

## Cool and moderate charge

```text
Continue charging.
```

Why:

```text
Temperature and charge level are in a normal range.
```

## Charging to 100%

```text
Charging to 100% is fine when needed. Avoid staying full for long periods.
```

Why:

```text
The concern is time spent near full, especially when warm.
```

## Low battery

```text
Use normally. Recharge when convenient.
```

Why:

```text
Occasional low battery is not catastrophic. Avoid heat and heavy load when very low.
```

## Fast charging

```text
Fast charging is not automatically bad. Watch temperature.
```

Why:

```text
Charging speed matters mostly when it causes heat or combines with high charge level.
```

## Short charge session

```text
Recorded, but ignored for health estimate.
```

Why:

```text
Small charge changes are too noisy for reliable capacity estimation.
```

---

# 9. Copy Rules

## Use

* Battery stress
* Charging stress
* Useful capacity
* Likely range
* Confidence
* Measured
* Estimated
* Inferred
* Not enough data
* Ignored for health estimate
* Stored for history only
* Continue charging
* Unplug when convenient
* Let phone cool

## Avoid

* Damage detected
* Battery ruined
* Exact wear
* Lifetime cost
* Efficiency score
* Perfect charging
* Bad user behavior
* You are killing your battery
* Guaranteed lifespan
* Pro accuracy

---

# 10. Empty States

## No sessions yet

```text
No charging sessions yet

Plug in your phone and open the app during charging to start building evidence.
```

## Not enough useful sessions

```text
Not enough useful sessions yet

Health estimates need larger, stable charge sessions.
Useful sessions: 2 / 5
```

## Missing device readings

```text
Some readings are unavailable on this device

The app can still show temperature, charge level, and session history, but capacity confidence may be lower.
```

## Android restricted app

```text
Session incomplete

Android restricted the app during charging, so this session was stored but excluded from health estimates.
```

---

# 11. Screen-Level Noise Rules

## Now

Show only:

* Stress
* Reason
* Action
* Confidence
* Temp
* Level
* State
* Best stop
* Time to target
* Alarm

Do not show:

* Graphs
* Raw current
* Raw voltage
* mAh
* Cycle math
* History table

## Health

Show only:

* Estimated health
* Range
* Confidence
* Useful session count
* Trend graph
* Interpretation

Do not show:

* Session-by-session raw stats
* Exact wear
* Lifetime prediction

## Ledger

Show:

* Evidence records
* Raw measured details
* Why included or excluded

Do not turn Ledger into the main UX.

## How it works

Show:

* Model explanations
* Evidence labels
* Limitations
* Experimental warnings

Do not show marketing copy.

---

# 12. Final UX Standard

Every screen must pass:

1. Is the main answer visible in 5 seconds?
2. Is the advice clear?
3. Is the reason clear?
4. Is uncertainty visible?
5. Are measured, estimated, and inferred values separated?
6. Is fake precision impossible?
7. Is normal charging treated as normal?
8. Does the screen avoid raw-stat overload?
9. Does the user know what to do next?
10. Would a non-technical user understand it?
