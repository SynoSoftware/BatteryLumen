# UI/UX Specification: Open Battery Decision Assistant

## 1. Core UI/UX Philosophy

### Anti-Precision Principle

The UI must structurally prevent fake precision.

Rules:

* Percentages shown to users are rounded integers.
* Battery health is shown as approximate: `~77%`.
* Uncertain values are shown as ranges: `74–80%`.
* No exact wear-per-session numbers.
* No exact lifetime-loss claims.
* No “battery will last X times longer” claims.
* If a value is noisy, the UI must range it, qualify it, or refuse to show it.

The app should never render a number that implies certainty the model does not have.

### Calm Diagnostic Aesthetic

The app should feel precise, quiet, low-drama, and evidence-first.

Avoid:

* Scary health metaphors
* Disease language
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

### Progressive Disclosure

The first screen gives the action in 5 seconds.

Detailed science, model logic, raw measurements, and excluded data are available only when the user taps or scrolls deeper.

The main UX must answer:

> What is happening, is it good or risky, and what should I do?

## 2. Visual Language & Theming

### Colors

Colors are functional, not decorative.

Backgrounds:

* Light: pure white
* Dark: pure black

Surfaces:

* Light card: `#F8F9FA`
* Dark card: `#121212`
* Separation via spacing and subtle 1px borders
* Avoid heavy shadows

Semantic signals:

* Good / safe: muted slate-teal
* Moderate risk: warm amber
* High risk: matte terracotta

No bright red unless there is a severe thermal condition.

### Typography

Use:

* Prose and headings: Inter or Roboto
* Data and ledger values: Roboto Mono
* Tabular figures enabled for numeric alignment

Ledger rows must feel like strict records, not decorative cards.

## 3. Evidence Labeling System

Every important value must communicate how trustworthy it is.

### Evidence Types

Use full labels on spacious screens:

* `(Measured)`
* `(Estimated)`
* `(Inferred)`
* `(Experimental)`

Use compact labels only in dense tables:

* `[M]`
* `[E]`
* `[I]`
* `[X]`

### Definitions

#### Measured

Direct reading from Android or device sensors.

Examples:

* Battery level
* Battery temperature
* Plug type
* Charging status
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
* Charging speed if derived from readings

#### Inferred

A classification or recommendation derived from measured and estimated values.

Examples:

* Charging risk
* Thermal risk
* Charge-level risk
* “Fast” charging label
* “Useful session”
* “Weak session”
* “Incomplete data”
* Recommended action

#### Experimental

A non-core metric that is not proven enough to drive product decisions.

Examples:

* Session impact scale
* Lifetime-risk proxy
* Relative stress index
* Any metric that cannot be validated directly from device readings

Experimental metrics are disabled by default.

### Interaction

Tapping any evidence label opens a bottom sheet explaining:

* What inputs were used
* Which inputs were unavailable
* Whether the value is measured, estimated, inferred, or experimental
* Why confidence is low, medium, or high
* What the app cannot prove

### First-Run Explanation

Show a short tooltip:

> Every number in this app is labeled by evidence quality. We do not pretend to know more than we can measure.

## 4. Main Screen Architecture

Use standard Android bottom navigation with icons and text labels.

Tabs:

* Now
* Health
* Ledger
* Evidence

Do not use icon-only navigation. Clarity is more important than visual minimalism.

## 5. Screen 1: Now

Purpose:

> Am I treating my battery well right now?

This is the main product screen.

### Charging State

#### Hero Decision Card

Card style:

* Edge-to-edge or wide card
* 5% semantic color wash
* Clear border
* No heavy shadow

Content:

* Eyebrow: `CURRENT CHARGING RISK`
* Headline: `High`
* Reason: `Battery is 42°C while charging above 85%.`
* Action box: `Unplug now or let phone cool.`
* Evidence summary: `Confidence: High • Evidence: direct temperature + direct battery level`

Do not place individual evidence badges on every hero-line item. The summary is enough at this level.

### Live Telemetry Grid

Use tight tabular layout.

Example:

```text
Temp:     42°C    (Measured)
Level:    86%     (Measured)
Speed:    Fast    (Inferred)
```

Rules:

* `Temp` is measured if Android exposes battery temperature.
* `Level` is measured.
* `Speed: Fast` is inferred.
* If raw current or power is shown in advanced mode, label the raw reading separately according to availability.

Example advanced expansion:

```text
Current:  1800 mA    (Measured, if supported)
Speed:    Fast       (Inferred)
```

### Target & Alarm Card

Show target guidance without mixing recommendation and estimate.

Example:

```text
Best Stop:      85%       (Recommended)
Time to target: ~12 min   (Estimated)
Full charge:    ~46 min   (Estimated)
```

Primary action:

* Toggle: `Set alarm for 85%`

Context text:

> Continuing past 85% provides less daily value and increases aging risk at this temperature.

Rules:

* `85%` is a recommendation, not an estimate.
* Time remaining is estimated.
* The app must not claim it can stop charging unless the device supports it.

## 6. Screen 2: Health

Purpose:

> What condition is my battery in, and can I trust the estimate?

### Health Estimate Card

Example:

```text
~77% Useful Capacity    (Estimated)
Likely range: 74–80%
Confidence: Medium
Based on 12 useful sessions
```

Rules:

* Health is always estimated.
* Capacity is always estimated unless the OS exposes a true battery health value.
* Never label capacity points as measured.
* Never show fake precision like `77.38%`.

### Trend Visualization

Use a scatter plot with uncertainty.

Visual rules:

* X-axis: date
* Y-axis: estimated useful capacity or health %
* Very subtle horizontal gridlines only
* No heavy grid
* No decorative chart chrome
* No sharp single-line certainty

Data points:

* Solid dots: high-quality estimated capacity points
* Hollow dots: weak estimated capacity points excluded from trend calculation

Trend:

* Smooth moving average through solid dots only
* Shaded confidence band around the trend
* Confidence band communicates uncertainty visually

Example caption:

> Based on 12 useful sessions. 4 noisy sessions excluded.

Tapping the caption opens the filtered ledger.

### Health Interpretation

Below the chart, show a plain-language conclusion.

Examples:

* `Battery appears stable.`
* `Battery is slowly declining.`
* `Recent readings are noisy.`
* `More useful sessions are needed.`
* `Drop appears real because multiple useful sessions agree.`

Do not force users to interpret noisy graph data themselves.

## 7. Screen 3: Ledger

Purpose:

> What evidence is the app using?

The Ledger treats charge sessions like bank transactions.

### Layout

Use a dense, scannable list with tabular alignment.

Avoid bulky cards for every row.

### Row Design

Example:

```text
+42%    10:00–10:45    40% → 82%    Warm [I]
```

Columns:

* Gain
* Time range
* Battery range
* Tag

Tags:

* `Useful [I]`
* `Weak [I]`
* `Warm [I]`
* `Hot [I]`
* `Incomplete [I]`
* `Excluded [I]`

The tag is inferred because it is a classification.

### Expanded Row

On tap, expand inline.

Example:

```text
Max Temp:        41°C      [M]
Time > 85%:      12 min    [M]
Screen On:       4 min     [M]
Capacity point:  excluded  [I]

Reason:
Weak data — charge gain under 30%.
```

Rules:

* Raw measurements can be `[M]`.
* Session classification is `[I]`.
* Capacity point is `[E]` if calculated.
* Exclusion reason is `[I]`.

### Incomplete Data

If Android restricts the app or the app misses part of the session:

Show:

```text
Incomplete [I]
```

Expanded detail:

```text
Last measured point: 68% at 10:32 [M]
Missing interval: 10:32–10:51 [I]
Reason: app was restricted by Android [I]
```

Do not label incomplete data itself as measured.

## 8. Screen 4: Evidence

Purpose:

> Which numbers are real, and how does the app decide?

This screen should read like a clean open-source README.

### Layout

Use expandable sections.

Sections:

* Evidence labels
* Thermal risk model
* Charge-level risk model
* Combined risk model
* Capacity model
* Data quality rules
* Experimental metrics
* Known limitations

### Thermal Risk Model

Explain:

* Temperature is a proven battery-aging factor.
* Heat combined with charging and high state of charge is higher risk.
* The app uses practical guidance bands.

Required text:

> These are practical guidance bands, not absolute chemistry constants. Actual degradation depends on battery chemistry, age, charge level, current, and duration.

### Charge-Level Risk Model

Explain:

* Higher state of charge generally increases aging risk.
* Time near full matters more than crossing one exact percentage.
* 85% is a practical default, not a magic boundary.

Required text:

> 85% is a practical charging target, not a chemistry cliff. Charging to 100% is fine when needed; staying near full for long periods increases risk.

### Capacity Model

Correct wording:

> Capacity is estimated from battery percentage gained and charge/current readings when available. Voltage may be logged, but voltage alone is not treated as a reliable capacity source.

Explain:

* Large uninterrupted charge sessions are more useful.
* Short sessions are noisy.
* Wireless charging may add uncertainty.
* Heavy use while charging may reduce quality.
* Capacity trend matters more than one point.

### Experimental Metrics

Experimental metrics are disabled by default.

If enabled, they must use:

* Text scales
* Wide ranges
* Strong warnings

Allowed:

```text
Impact: Above average
Confidence: Low
```

Not allowed:

```text
Wear this session: 0.004%
Battery life used: 0.006%
This charge cost 12 minutes of lifespan
```

Required warning:

> This cannot be measured precisely from a single session.

## 9. Critical UX Flows

### Flow A: Rejecting Fake Data

Scenario:

User does a 5-minute car charge.

Behavior:

1. Session is recorded in Ledger.
2. Battery health is not recalculated from that session.
3. Ledger marks it as `Weak [I]`.
4. Expanded reason says: `Ignored for health estimate — charge too short.`

Outcome:

The user learns the app values data integrity over constant number updates.

### Flow B: Setting Target Alarm to 100%

Scenario:

User moves target from 85% to 100%.

Behavior:

1. Target changes to 100%.
2. Risk context turns amber if battery is warm or already high.
3. Subtext appears:

> Charging to 100% is fine when needed, but staying near full increases battery stress. The alarm will sound when full.

Do not guilt the user.

### Flow C: Explaining Low Confidence

Scenario:

User sees `Confidence: Low` on Health.

On tap, bottom sheet says:

> The app needs several deep, uninterrupted charges to establish a reliable estimate. Recommended: at least 5 useful sessions with over 30% charge gain. You currently have 2. Keep using your phone normally.

### Flow D: Hot Charging Warning

Scenario:

Battery reaches high temperature while charging.

Behavior:

* Hero card changes to high risk.
* Notification can appear if enabled.
* Message stays factual.

Example:

> Battery is 43°C while charging. Let the phone cool or unplug if you do not need more charge.

Avoid:

> Your battery is being destroyed.

## 10. Android Implementation & Performance Notes

### Minimal Background Impact

Do not claim zero background drain.

The app should minimize background work and make stale data visible.

Rules:

* Use local data first.
* Avoid persistent foreground service in MVP.
* Use battery/power events where possible.
* Show when sessions are incomplete.
* Show when Android restrictions affect accuracy.

### State Transitions

Use Jetpack Compose crossfades.

When switching from discharging to charging:

* Daily summary card dissolves into live decision card.
* No abrupt blinking.
* No unnecessary reload animation.

### Loading States

No blocking spinners for local data.

Use:

* Instant rendering from Room/DataStore
* Lightweight skeleton shimmer only if needed
* Cached graph state where possible

### Accessibility

Required:

* High contrast in light and dark mode
* Semantic colors must not be the only signal
* All risk states must include text
* Tap targets must meet Android accessibility guidelines
* Evidence labels must be screen-reader readable

Example screen reader text:

> Temperature, 42 degrees Celsius, measured.

## 11. UI Copy Rules

The app must use plain, honest wording.

Use:

* `Risk`
* `Confidence`
* `Measured`
* `Estimated`
* `Inferred`
* `Not enough data`
* `Ignored for health estimate`
* `Likely range`

Avoid:

* `Exact wear`
* `Battery life used`
* `Efficiency 887%`
* `Perfect charging`
* `Battery damage detected`
* `Guaranteed lifespan`
* `Pro accuracy`

## 12. Required Final Checks

Before implementation, every screen must pass these checks:

1. Does the user know what to do within 5 seconds?
2. Is every important number labeled by evidence quality?
3. Is any estimated value pretending to be measured?
4. Is any single-session metric implying exact battery wear?
5. Are noisy sessions excluded from health estimates?
6. Can the user see why a value has low confidence?
7. Does the UI avoid fear-based language?
8. Does the UI avoid making users interpret raw stats themselves?

If any answer fails, the screen must be revised.
