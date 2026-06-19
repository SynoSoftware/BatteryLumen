# Product UI Specification: Open Battery Decision Assistant

## 1. Product Goal

Help the user make one clear decision:

**Am I treating my battery well right now, and what should I do?**

The app must show:

1. Advice first.
2. Evidence second.
3. Raw data last.

The app must be fluent, ergonomic, quiet, evidence-based, low-noise, and understandable by non-technical users.

It must not become a technical battery dashboard by default.

---

## 2. Core UX Principles

### 2.1 Anti-Precision Principle

The UI must structurally prevent fake precision.

Never show:

* Exact wear per session
* Exact lifetime lost
* Exact battery lifespan remaining
* “This charge cost X minutes of battery life”
* “Battery will last X times longer”
* “887% efficiency”
* Fake precise health values like `77.38%`

Show instead:

* `Battery stress: Normal`
* `Likely range: 74–80%`
* `Confidence: Medium`
* `Not enough data`
* `Ignored for health estimate`
* `Stored for history only`

If a value is noisy, the UI must range it, qualify it, or refuse to show it.

### 2.2 Calm Diagnostic Aesthetic

The app should feel precise, quiet, low-drama, and evidence-first.

Avoid:

* Fear language
* Disease metaphors
* Pulsing red alerts
* Battery “juice” animations
* Gamified fake scores
* Decorative technical clutter

Use:

* Clear typography
* Sparse layout
* Functional color
* Strong hierarchy
* Evidence labels
* Plain-language explanations

### 2.3 Progressive Disclosure

The first screen gives the action in 5 seconds.

Details, raw measurements, excluded data, confidence explanations, and model logic are available only when the user taps or scrolls deeper.

The main UX must answer:

**What is happening, is it good or stressful, and what should I do?**

---

## 3. Main Navigation

Use four main screens:

1. **Now**
2. **Health**
3. **Ledger**
4. **How it works**

Each screen has one job.

| Screen       | User question                                                  |
| ------------ | -------------------------------------------------------------- |
| Now          | What should I do right now?                                    |
| Health       | Is my battery actually degrading?                              |
| Ledger       | What evidence is the app using?                                |
| How it works | Which numbers are measured, estimated, inferred, or uncertain? |

Use bottom navigation with icons and text labels.

Do not use icon-only navigation. Clarity is more important than minimalism.

---

## 4. Evidence Labeling System

Every important value must communicate evidence quality.

### 4.1 Evidence Types

Use full labels on spacious screens:

* `Measured`
* `Estimated`
* `Inferred`
* `Experimental`

Use compact labels only in dense ledger rows:

* `[M]`
* `[E]`
* `[I]`
* `[X]`

### 4.2 Definitions

#### Measured

Direct reading from Android or device sensors.

Examples:

* Battery level
* Battery temperature
* Charging state
* Plug type
* Timestamp
* Session duration
* Voltage, if available
* Current, if available
* Charge counter, if available

#### Estimated

Calculated from measured values.

Examples:

* Time to target
* Time to full
* Battery health
* Capacity estimate
* Capacity trend
* Charging speed if derived from current or charge-rate readings

#### Inferred

A classification, judgment, or recommendation derived from measured and estimated values.

Examples:

* Battery stress
* Thermal stress
* Charge-level stress
* “Fast” charging label
* “Useful session”
* “Weak session”
* “Incomplete data”
* Recommended action

#### Experimental

A non-core metric that is not proven enough for normal user decisions.

Examples:

* Session impact scale
* Relative stress index
* Lifetime-risk proxy

Experimental metrics are disabled by default.

### 4.3 Evidence Interaction

Tapping an evidence label opens a bottom sheet explaining:

* What inputs were used
* Which inputs were unavailable
* Whether the value is measured, estimated, inferred, or experimental
* Why confidence is low, medium, or high
* What the app cannot prove

First-run tooltip:

> Every number in this app is labeled by evidence quality. We do not pretend to know more than we can measure.

---

## 5. Stress Language

Use **stress**, not **risk**, in the main UI.

Use:

* Battery stress
* Charging stress
* Thermal stress
* Charge-level stress

Avoid:

* Damage
* Harm
* Risk warning
* Battery ruined
* You are killing your battery

Reason:

* “Stress” is calmer.
* “Risk” can sound fear-based.
* Normal charging should not feel dangerous.

---

## 6. Stress States

Allowed states:

* Excellent
* Good
* Normal
* High stress
* Severe stress

Rules:

* Use **Normal** generously.
* Normal charging must not feel bad.
* High stress should mean the condition is worth changing.
* Severe stress should be rare and reserved for clear heat or combined high-stress conditions.

Example:

```text
Battery stress: Normal
Why: Temperature is reasonable and charge level is moderate.
Action: Continue charging.
Confidence: High
```

---

## 7. Screen: Now

## Purpose

Answer:

**What should I do right now?**

This is the main screen.

---

### 7.1 Charging State

Show one dominant card at the top.

Example:

```text
Battery stress
High stress

Battery is 42°C while charging above 85%.

Unplug now or let the phone cool.

Confidence: High
Based on direct temperature and battery level
```

Advice card order:

1. State
2. Why
3. Action
4. Confidence

Do not place evidence labels on every line inside the hero card. Use one clean evidence summary.

---

### 7.2 Live Telemetry

Show only the most useful direct values.

```text
Temp:   42°C      Measured
Level:  86%       Measured
State:  Charging  Measured
```

Do not show by default:

* Watts
* Volts
* Current
* mAh
* Cycle math
* Raw charging graphs

Those belong in expanded details or advanced views.

Expanded telemetry may show:

```text
Current:  1800 mA    Measured, if supported
Speed:    Fast       Inferred
Source:   AC         Measured
Voltage:  4.31 V     Measured, if supported
```

If a reading is unavailable, say so plainly.

---

### 7.3 Target and Alarm Card

Show:

```text
Best stop:       85%        Recommended
Time to target:  ~12 min    Estimated
Full charge:     ~46 min    Estimated

[ Set alarm for 85% ]
```

Context text:

> Continuing past 85% gives less daily value and increases aging stress, especially when warm.

Rules:

* `85%` is a recommendation, not an estimate.
* Time remaining is estimated.
* The app must not claim it can stop charging unless the device supports it.

---

### 7.4 Discharging State

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

## 8. Best Advice Rules

The app should always show the most useful advice based on the current condition.

### Cool and moderate charge

```text
Battery stress: Good
Why: Temperature and charge level are in a normal range.
Action: Continue charging.
```

### Warm while charging

```text
Battery stress: High stress
Why: Battery is warm while charging.
Action: Let the phone cool or avoid heavy use while charging.
```

### Hot while charging

```text
Battery stress: Severe stress
Why: Battery is 45°C while charging.
Action: Unplug now if you do not need more charge.
```

### Above 85%

```text
Battery stress: Normal / High stress
Why: Battery is near full. Staying near full adds aging stress over time.
Action: Unplug if you do not need 100%.
```

### At 100%

```text
Battery stress: High stress
Why: Battery is full. Staying full for long periods increases aging stress.
Action: Unplug when convenient.
```

### Charging to 100%

```text
Charging to 100% is fine when needed. Avoid staying full for long periods.
```

### Fast charging

```text
Fast charging is not automatically bad. Watch temperature.
```

Why:

```text
Charging speed matters mostly when it causes heat or combines with high charge level.
```

### Low battery

```text
Use normally. Recharge when convenient.
```

Why:

```text
Occasional low battery is not catastrophic. Avoid heat and heavy load when very low.
```

### Short charge session

```text
Recorded, but ignored for health estimate.
```

Why:

```text
Small charge changes are too noisy for reliable capacity estimation.
```

---

## 9. Background Warnings

The app may warn users while it is not on screen, but only for clear, actionable conditions.

Warnings must be:

* Opt-in
* Actionable
* Low-noise
* Evidence-based
* Never fear-based

Do not notify for normal charging.

Do not use screen overlays, fake emergency popups, or intrusive “you are damaging your battery” messages.

---

### 9.1 Charge Target Reached

Trigger:

* Battery reaches selected target: 80%, 85%, 90%, 100%, or custom.

Notification:

```text
85% reached
Unplug now if you do not need more charge.
```

This is the highest-value notification.

---

### 9.2 Hot While Charging

Trigger:

* Battery is hot while charging.

Notification:

```text
Battery is hot while charging
Let the phone cool or unplug if you do not need more charge.
```

---

### 9.3 Warm + Near Full for Sustained Time

Trigger:

* Battery is above the selected high-charge threshold and warm for a sustained period.

Notification:

```text
High charging stress
Battery is warm and near full. Unplug when convenient.
```

This should be less aggressive than hot-charging warnings.

---

### 9.4 Severe Heat

Trigger:

* Battery reaches severe heat while charging.

Notification:

```text
Severe battery heat
Unplug now or stop heavy use while charging.
```

This may use heads-up notification behavior if the user enabled urgent warnings.

---

## 10. Screen: Health

## Purpose

Answer:

**Is my battery actually getting worse?**

The Health screen must not update from weak data.

---

### 10.1 Not Enough Data State

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

Rules:

* Do not show fake battery health before enough evidence exists.
* Do not show a trend line before enough useful points exist.
* Explain what kind of sessions improve confidence.

---

### 10.2 Health Estimate State

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

Rules:

* Health is estimated unless the OS exposes a reliable health value.
* Capacity is estimated unless the OS exposes a true capacity/health value.
* Never label capacity points as measured.
* Never show fake precision like `77.38%`.

---

### 10.3 Capacity Trend Graph

Show:

* Solid dots: useful estimated capacity points
* Hollow dots: weak estimated points excluded from trend
* Moving average
* Shaded confidence band
* Subtle horizontal gridlines only

Do not show a sharp single line that implies certainty.

Below the graph, show one conclusion:

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

Do not force users to interpret noisy graph data themselves.

---

### 10.4 Health Advice

Advice depends on health and confidence.

Good health:

```text
Battery health looks normal.
Keep avoiding heat while charging.
```

Aging battery:

```text
Battery capacity appears reduced.
If daily battery life feels poor, replacement may be worth considering.
```

Low confidence:

```text
Do not make decisions from this estimate yet.
The app needs more useful sessions.
```

---

## 11. Screen: Ledger

## Purpose

Answer:

**What evidence is the app using?**

The Ledger should feel like a clean transaction log.

---

### 11.1 Session Row

Each row shows:

```text
+42%    10:00–10:45    40% → 82%    Useful [I]
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

### 11.2 Expanded Row

On tap:

```text
Max temp:        41°C       [M]
Time above 85%: 12 min     [M]
Screen on:       4 min      [M]
Capacity point:  Included   [E]

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

Last measured point: 68% at 10:32 [M]
Missing interval: 10:32–10:51 [I]

Reason:
Android restricted the app during charging.
```

---

### 11.3 Ledger Copy

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

## 12. Screen: How It Works

## Purpose

Answer:

**Which numbers are real, estimated, inferred, or uncertain?**

This screen should feel like a readable open-source README, not marketing copy.

---

### 12.1 Evidence Labels

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

### 12.2 Thermal Stress Model

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

### 12.3 Charge-Level Stress Model

Show:

```text
Higher charge levels generally increase aging stress, especially when the battery stays near full for long periods.
```

Required note:

```text
85% is a practical charging target, not a chemistry cliff. Charging to 100% is fine when needed; staying near full for long periods increases stress.
```

---

### 12.4 Capacity Model

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

### 12.5 Experimental Metrics

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

## 13. Empty States

### No Sessions Yet

```text
No charging sessions yet

Plug in your phone and open the app during charging to start building evidence.
```

### Not Enough Useful Sessions

```text
Not enough useful sessions yet

Health estimates need larger, stable charge sessions.

Useful sessions: 2 / 5
```

### Missing Device Readings

```text
Some readings are unavailable on this device

The app can still show temperature, charge level, and session history, but capacity confidence may be lower.
```

### Android Restricted App

```text
Session incomplete

Android restricted the app during charging, so this session was stored but excluded from health estimates.
```

---

## 14. Screen-Level Noise Rules

### Now

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

### Health

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

### Ledger

Show:

* Evidence records
* Raw measured details
* Why included or excluded

Do not turn Ledger into the main UX.

### How It Works

Show:

* Model explanations
* Evidence labels
* Limitations
* Experimental warnings

Do not show marketing copy.

---

## 15. Copy Rules

Use:

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

Avoid:

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

## 16. Final UX Standard

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
11. Are warnings actionable and low-noise?
12. Does the app avoid fear-based language?
