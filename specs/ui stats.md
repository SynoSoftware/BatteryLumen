Aligned to the SRS and corrected for the omissions from the prior draft.

# Fixed Bucket Plan - Battery Lumen Decision Assistant

## Product intent

The app is not a generic battery dashboard.

It is a:

```text
Battery Decision Assistant + Measurement Ledger + Evidence Layer
```

The main UI should answer:

```text
Is charging now good, normal, or risky?
Why?
What should I do?
How long until my selected stop point?
What is my estimated battery health?
Can I trust the estimate?
Which readings are measured, estimated, inferred, or experimental?
```

The app must compete on trust, not fake precision.

---

# Evidence labels used across all buckets

Every user-facing metric must have an evidence label.

| Grade   | Meaning                          | Examples                                                                                                 |
| ------- | -------------------------------- | -------------------------------------------------------------------------------------------------------- |
| Grade A | Directly measured                | Battery %, charging state, plug type, temperature, voltage if available, current if available, timestamp |
| Grade B | Estimated from readings          | Time to target, time to full, time remaining, health %, estimated capacity, charging speed               |
| Grade C | Inferred risk                    | Thermal risk, charge-level risk, combined charging risk, data quality, session comparison                |
| Grade D | Experimental / not proven enough | Wear per session, exact lifetime cost, lifetime multiplier, exact years remaining, charging efficiency % |

Rule:

```text
Grade D metrics must never appear as primary product claims.
```

---

# Bucket 1 — Main Live Decision UI

## Purpose

Answer:

```text
What should I do right now?
```

This is the highest-priority UI. It belongs on the Home screen / Live Decision Card.

## Show

| Stat                   | UI priority | Evidence type                 | Notes                                             |
| ---------------------- | ----------: | ----------------------------- | ------------------------------------------------- |
| Charging risk now      |     Highest | Grade C                       | Main product output: Good / Normal / Risky / High |
| Main reason            |     Highest | Grade C from Grade A/B inputs | Example: “42°C while charging above 85%”          |
| Recommended action     |     Highest | Grade C guidance              | Example: continue, unplug, cool phone             |
| Charging state         |        High | Grade A                       | Charging / unplugged / full / unknown             |
| Battery % / SOC        |        High | Grade A                       | Needed for charge-level risk                      |
| Selected charge target |        High | User setting                  | Default: 85%                                      |
| Time to target         |        High | Grade B                       | Core utility feature                              |
| Time to full           |        High | Grade B                       | Useful comparison against target                  |
| Charge alarm status    |        High | App state                     | Off / armed / target reached                      |
| Continuing value       |        High | Grade C                       | Whether continuing is useful                      |
| Continuing risk        |        High | Grade C                       | Whether continuing increases aging risk           |
| Evidence label         |        High | Evidence layer                | Measured / estimated / inferred                   |
| Confidence             |        High | Confidence model              | Low / Medium / High                               |

## Example

```text
Risk: High
Reason: 42°C while charging above 85%
Action: unplug now or let the phone cool
Target: 85%
Time to target: reached
Full charge: 46 min
Alarm: target reached
Continuing: useful only if you need 100%
Risk if continuing: higher because battery is warm and near full
Evidence: direct temperature + direct battery level
Confidence: high
```

## Do not show here

```text
Raw voltage
Raw current
Charge counter
Capacity estimate points
Experimental wear estimates
Detailed model internals
Full temperature history
Full session logs
```

---

# Bucket 2 — Contextual Live / Charging UI

## Purpose

Support the live decision without turning the Home screen into a raw-stat dashboard.

These stats may appear on the Charging screen, expandable live detail, or secondary card.

## Show

| Stat                             | Placement                                                | Rule                                        |
| -------------------------------- | -------------------------------------------------------- | ------------------------------------------- |
| Battery temperature              | Charging screen always; Home when charging/risk-relevant | Core battery stress input                   |
| Thermal risk                     | Charging detail / expandable                             | Show when meaningful                        |
| Charge-level risk                | Charging detail / expandable                             | Show when high SOC or high-SOC time matters |
| Combined charging risk           | Charging screen                                          | Should match the main card conclusion       |
| Time hot while charging          | Detail                                                   | Show when heat is part of the reason        |
| Time above 85% today             | Detail / daily context                                   | Show when it affects guidance               |
| Time above 90% today             | Detail / daily context                                   | Show when near-full time is the issue       |
| Charging speed                   | Detail                                                   | Useful context, not a fear badge            |
| Screen-on while charging         | Detail                                                   | Show when it likely contributes to heat     |
| Plug type                        | Detail                                                   | Useful session context                      |
| Current charging source          | Detail                                                   | Wired / wireless / USB / AC if available    |
| Current battery temperature band | Detail                                                   | Low / moderate / high / very high / severe  |

## Example

```text
Thermal risk: High
Charge-level risk: Medium
Combined risk: High
Temperature: 42°C
Time hot while charging: 12 min
Time above 85% today: 54 min
Charging source: AC
Evidence: direct temperature + direct battery level
```

## Important rule

Do not present “fast charging” as automatically bad.

Better:

```text
Charging speed is high, but the main concern is heat while near full.
```

Worse:

```text
Fast charging is damaging your battery.
```

---

# Bucket 3 — Health UI

## Purpose

Answer:

```text
What condition is my battery in?
```

This belongs on the Health screen and optionally as a small secondary card on Home.

## Show

| Stat                         | Placement                               | Evidence type              |
| ---------------------------- | --------------------------------------- | -------------------------- |
| Battery health estimate      | Health card / Home secondary            | Grade B                    |
| Health range                 | Health card                             | Grade B                    |
| Estimated current capacity   | Health detail                           | Grade B                    |
| Design capacity              | Health detail / Settings                | Grade A/B depending source |
| Capacity trend               | Health screen                           | Grade B/C                  |
| Useful session count         | Health screen                           | Trust signal               |
| Capacity confidence          | Health screen                           | Confidence model           |
| Confidence reason            | Expandable detail                       | Trust explanation          |
| Data quality warning         | Health detail                           | Grade C                    |
| More sessions needed message | Health detail                           | Grade C                    |
| Typical cycling range        | Health detail                           | Long-term context          |
| Deep-discharge pattern       | Health detail                           | Long-term context          |
| Calendar-aging context       | Model / Health detail                   | Educational/contextual     |
| Cycle-aging context          | Model / Health detail                   | Educational/contextual     |
| Internal resistance          | Health detail only if directly measured | Grade A if measured        |

## Example

```text
Battery health: ~87%
Likely range: 84–90%
Trend: stable
Based on: 12 useful charge sessions
Confidence: medium
Reason: most sessions agree, but some readings were noisy
```

## Good vs bad

Good:

```text
Health: ~87%
Likely range: 84–90%
```

Bad:

```text
Health: 87.42%
```

---

# Bucket 4 — Daily Summary UI

## Purpose

Reinforce better charging habits without scaring the user.

This belongs on the Home screen as a secondary card and in History as daily summaries.

## Show

| Stat                           | Placement     | Evidence type               |
| ------------------------------ | ------------- | --------------------------- |
| Overall charging quality       | Daily summary | Grade C                     |
| Main daily issue               | Daily summary | Grade C                     |
| Max charging temperature today | Daily summary | Grade A                     |
| High-temperature charging time | Daily summary | Grade C from Grade A inputs |
| Time above 85% today           | Daily summary | Grade C from Grade A inputs |
| Time above 90% today           | Daily summary | Grade C from Grade A inputs |
| Simple score                   | Daily summary | Grade C                     |
| Evidence grade                 | Daily summary | Evidence layer              |

## Example

```text
Today: Normal
Main issue: spent 1h 20m above 90%
High-risk charging: 8 min
Max charging temperature: 41°C
Evidence: direct temperature + direct battery level
```

## Tone rule

Better:

```text
Today had some higher-risk charging because the phone stayed near full for a long time.
```

Worse:

```text
You damaged your battery today.
```

---

# Bucket 5 — Measurement Ledger / History

## Purpose

Show the evidence the app is using.

Every charge session should become evidence. This bucket is not primary UI, but it is core to product trust.

## Show

| Stat                          | Placement        |
| ----------------------------- | ---------------- |
| Charge session start time     | Session history  |
| Charge session end time       | Session history  |
| Start %                       | Session history  |
| End %                         | Session history  |
| Percentage gained             | Session history  |
| Session duration              | Session history  |
| Charging source               | Session detail   |
| Plug type                     | Session detail   |
| Average temperature           | Session detail   |
| Max temperature               | Session detail   |
| Time above 40°C               | Session detail   |
| Time above 43°C               | Session detail   |
| Time above 85%                | Session detail   |
| Time above 90%                | Session detail   |
| Screen-on time while charging | Session detail   |
| Charge added, if available    | Session detail   |
| Estimated capacity, if usable | Capacity history |
| Charge-level risk             | Session detail   |
| Thermal risk                  | Session detail   |
| Combined risk                 | Session detail   |
| Data quality                  | Session detail   |
| Confidence reason             | Session detail   |
| Evidence grade                | Session detail   |
| Weak/rejected session reason  | Session detail   |
| Capacity estimate points      | Capacity history |
| Moving average capacity trend | Capacity history |
| Temperature events            | History tab      |
| High-charge events            | History tab      |
| Daily summaries               | History tab      |
| Discharge sessions            | History tab      |
| Discharge speed               | Discharge detail |
| Discharge temperature events  | Discharge detail |

## Session example

```text
Session: 14:10–15:02
Battery: 42% → 86%
Duration: 52 min
Max temp: 41°C
Time above 85%: 6 min
Thermal risk: High
Charge-level risk: Medium
Combined risk: High
Data quality: Useful
Evidence: direct temperature + direct battery level
```

---

# Bucket 6 — Data Quality / Trust Detail

## Purpose

Explain why the app trusts or rejects data.

This bucket should appear in Health detail, Session detail, Evidence, and Model explanation.

## Show

| Item                              | Placement                   |
| --------------------------------- | --------------------------- |
| Useful session criteria           | Model / Health detail       |
| Weak session criteria             | Model / Session detail      |
| Rejected session reason           | Session detail              |
| Useful session count              | Health                      |
| Rejected/weak session count       | Health detail               |
| App killed / interrupted marker   | Session detail              |
| Wireless uncertainty marker       | Session detail              |
| Reading inconsistency marker      | Session detail              |
| Missing reading marker            | Evidence detail             |
| Current reliability marker        | Evidence detail             |
| Charge counter reliability marker | Evidence detail             |
| Temperature reliability marker    | Evidence detail             |
| Confidence reason                 | Everywhere estimates appear |
| Inputs unavailable                | Evidence / Model            |
| Data quality score/label          | Session detail / Health     |

## Useful capacity session criteria

A session is useful when:

```text
Charge gain is at least 30–40%
Charging source is stable
App was not killed
Session was not heavily interrupted
Battery readings are consistent
Device exposes enough readings
Temperature was not extreme
Charge counter or current readings are available and plausible
```

## Weak session criteria

A session is weak when:

```text
Charge gain is too small
Wireless charging adds uncertainty
Phone was heavily used while charging
Temperature was high
Android restricted the app
Battery percentage jumped strangely
Data contradicts trend without repetition
Required readings are missing
Current or charge counter appears unreliable
```

## Rule

Weak sessions may be stored, but they must not strongly affect capacity estimates.

---

# Bucket 7 — Evidence / Advanced Metrics

## Purpose

Expose raw readings and transparency without forcing normal users to interpret them.

This belongs in Evidence / Advanced Metrics.

## Show

| Stat                                 | Placement                                          | Evidence type               |
| ------------------------------------ | -------------------------------------------------- | --------------------------- |
| Voltage                              | Evidence / Advanced                                | Grade A if available        |
| Current                              | Evidence / Advanced                                | Grade A if available        |
| Charge counter                       | Evidence / Advanced                                | Grade A if available        |
| Raw temperature history              | Evidence detail                                    | Grade A                     |
| Raw capacity estimate points         | Evidence detail                                    | Grade B                     |
| Moving average inputs                | Evidence detail                                    | Grade B                     |
| Charge/discharge speed               | Evidence / Advanced                                | Grade B                     |
| Time remaining                       | Discharge detail / Evidence                        | Grade B                     |
| Plug type                            | Evidence / Session detail                          | Grade A                     |
| Battery technology string            | Advanced only                                      | Grade A but low usefulness  |
| BatteryManager property availability | Debug / Evidence                                   | Implementation transparency |
| Unsupported readings                 | Evidence                                           | Transparency                |
| API sentinel values                  | Debug only                                         | Developer/debug value       |
| Inputs unavailable                   | Evidence                                           | Trust layer                 |
| Measured values                      | Evidence                                           | Grade A                     |
| Estimated values                     | Evidence                                           | Grade B                     |
| Inferred risks                       | Evidence                                           | Grade C                     |
| Internal resistance                  | Evidence / Health detail only if directly measured | Grade A if available        |
| Model version                        | Model / About                                      | Transparency                |
| Research notes                       | Model screen                                       | Transparency                |

## Rule

```text
Raw metrics are allowed for transparency, but they must not compete with the live decision card.
```

---

# Bucket 8 — Model / Explanation Screen

## Purpose

Explain how the app decides.

This is required for trust, open-source verification, and avoiding overclaims.

## Show

| Section                    | Required content                                                   |
| -------------------------- | ------------------------------------------------------------------ |
| Capacity model             | Inputs used, rejected inputs, confidence rules, what is not proven |
| Thermal risk model         | Temperature bands, charging state, high-temp duration, confidence  |
| Charge-level risk model    | SOC bands, time above high SOC, confidence                         |
| Combined risk model        | How thermal + SOC + charging state produce action                  |
| Confidence model           | Why confidence is low/medium/high                                  |
| Data quality rules         | Useful/weak/rejected session rules                                 |
| Experimental metrics       | What is experimental and why                                       |
| Unsupported claims         | What the app refuses to claim                                      |
| Inputs unavailable         | What the device does not expose                                    |
| Evidence grades            | Measured / estimated / inferred / experimental                     |
| Threshold documentation    | Research-backed / heuristic / experimental                         |
| Model changelog            | What changed between model versions                                |
| Device compatibility notes | Known device/API limitations                                       |
| Known limitations          | What the app cannot prove                                          |
| Data schema                | Public explanation of stored records                               |

## Thermal threshold explanation

The app may use practical bands such as:

```text
Under 35°C: low thermal concern
35–40°C: moderate concern
40–43°C: high concern
43–45°C: very high concern
Above 45°C: severe concern
```

Required explanation:

```text
These thresholds are practical guidance bands. Actual degradation depends on battery chemistry, age, charge level, current, and duration.
```

---

# Bucket 9 — Settings / Configuration

## Purpose

User controls and preferences.

These are not stats and should not compete with the main decision UI.

## Show

| Item                                | Placement         |
| ----------------------------------- | ----------------- |
| Target charge level                 | Settings          |
| Custom target                       | Settings          |
| Charge alarm sound                  | Settings          |
| Charge alarm vibration              | Settings          |
| Notification permission status      | Settings          |
| Design capacity override            | Settings          |
| Temperature unit                    | Settings          |
| Data retention                      | Settings          |
| Export CSV / JSON                   | Settings          |
| Advanced metrics toggle             | Settings          |
| Experimental metrics toggle         | Settings          |
| Optional live charging monitor      | Settings          |
| Usage access / app drain permission | Optional settings |
| Privacy / diagnostics opt-in        | Settings          |
| Model documentation link            | Settings / Model  |
| About / license                     | Settings          |

## Default target

```text
85%
```

## Supported targets

```text
80%
85%
90%
100%
Custom
```

## Important rule

The app must not claim it can stop charging unless the OS/device actually supports charge limiting and the app can verify it.

---

# Bucket 10 — Experimental Metrics

## Purpose

Allow research and transparency while preventing fake precision.

These metrics may exist only behind an Experimental toggle and must carry a warning.

## Show only with warning

| Metric                                 | Placement                                       |
| -------------------------------------- | ----------------------------------------------- |
| Wear per session                       | Experimental only                               |
| Exact lifetime cost estimate           | Experimental only                               |
| Lifetime extension multiplier          | Experimental only                               |
| Exact years/months remaining           | Experimental only                               |
| Charging efficiency %                  | Experimental only                               |
| Precise degradation from one hot event | Experimental only                               |
| Cycle count estimate                   | Advanced/experimental unless directly available |

## Required warning

```text
This metric is experimental. It is not directly measured and should not be used as a precise battery-health claim.
```

## Important correction

A qualitative session comparison is not automatically experimental.

Allowed as Grade C inferred risk:

```text
This session was lower-risk than your usual sessions.
```

Only if based on:

```text
Temperature
State of charge
Time near full
Charging state
Session duration
Data quality
```

Avoid exact claims such as:

```text
This session was 37.2% healthier.
```

---

# Bucket 11 — Never Show as Normal Product Truth

## Purpose

Protect trust.

These claims should not appear as factual product outputs.

## Do not show

```text
This charge used exactly X% of your battery life.
This session cost X months of lifespan.
Your battery will last X times longer.
Fast charging always damages batteries.
Charging above 80% is always bad.
One hot charge permanently ruined your battery.
The battery will die in X months.
Stopping at 85% guarantees X lifespan gain.
This charge caused exactly X% degradation.
Charging efficiency is exactly X%.
The app stopped charging at 85%.
Charging will automatically stop at 85%.
Charge limiting is active.
```

The last three are allowed only if the device/OS actually supports charge limiting and the app can verify the state.

---

# MVP Screen Mapping

## Home

Purpose:

```text
What should I do now?
```

Show:

```text
Live decision card
Small battery health card
Today summary card
Charge alarm status
```

Do not show:

```text
Raw voltage
Raw current
Charge counter
C-rate
Experimental wear estimates
Full charts
Debug data
```

---

## Charging

Purpose:

```text
What is happening during this charge?
```

Show:

```text
Temperature
Charging state
Battery %
Selected target
Time to target
Time to full
Charge alarm
Thermal risk
Charge-level risk
Combined risk
Continuing value
Continuing risk
Charging speed
Plug type
Screen-on charging context when relevant
```

---

## Health

Purpose:

```text
What condition is my battery in?
```

Show:

```text
Health estimate
Likely range
Estimated current capacity
Design capacity
Capacity trend
Useful session count
Confidence
Confidence reason
Data quality notes
Typical cycling range
Deep-discharge pattern
Internal resistance if directly measured
```

---

## History

Purpose:

```text
What evidence is the app using?
```

Show:

```text
Charge sessions
Discharge sessions
Capacity points
Temperature events
High-charge events
Daily summaries
Weak/rejected sessions
Session data quality
Session evidence grade
```

---

## Evidence

Purpose:

```text
Which numbers are real?
```

Show:

```text
Measured values
Estimated values
Inferred risks
Experimental metrics
Unavailable readings
Raw values
Unsupported readings
Data quality internals
```

---

## Model

Purpose:

```text
How does the app decide?
```

Show:

```text
Capacity model
Thermal risk model
Charge-level risk model
Combined risk model
Confidence model
Data quality rules
Threshold explanations
Experimental metrics explanation
Unsupported claims
Research notes
Known limitations
Device compatibility notes
Model changelog
Data schema
```

---

## Settings

Purpose:

```text
Configure app behavior.
```

Show:

```text
Charge target
Notifications
Alarm sound/vibration
Design capacity
Temperature unit
Export
Data retention
Advanced metrics
Experimental metrics
Optional live monitor
Usage access permission
Privacy/diagnostics opt-in
About/license
```

---

# MVP Keep List

These should be visible in normal MVP UI.

```text
Charging risk now
Main reason
Recommended action
Charging state
Battery %
Battery temperature
Selected charge target
Time to target
Time to full
Charge alarm status
Continuing value
Continuing risk
Battery health estimate with range
Capacity trend
Useful session count
Confidence
Confidence reason
Evidence label
Daily charging quality
Main daily issue
```

---

# MVP Secondary List

These are useful, but should live in detail screens.

```text
Thermal risk breakdown
Charge-level risk breakdown
Combined risk breakdown
Charging speed
Plug type
Screen-on charging time
High-temperature charging minutes
Time above 85%
Time above 90%
Max charging temperature
Session duration
Charge session history
Discharge session history
Temperature history
Capacity estimate points
Rejected/weak session count
Data quality details
Inputs unavailable
Raw measured values
Estimated values
Inferred risks
```

---

# MVP Cut / Hide List

Remove these from normal user-facing UI.

```text
Wear per session
Exact lifetime cost
Exact years/months remaining
Lifetime multiplier
Charging efficiency %
Exact health decimals
Exact capacity decimals
Raw voltage on Home
Raw current on Home
C-rate
Cycle-count estimate unless directly available
Strict 20–80 warnings
Fast-charging fear warnings
Precise degradation from one event
Exact remaining lifespan
```

---

# Product Rules

## Rule 1

```text
If a metric does not help the user decide what to do, trust the estimate, or inspect the evidence, it does not belong in primary UI.
```

## Rule 2

```text
The live decision card must summarize. It must not become a raw battery dashboard.
```

## Rule 3

```text
Use ranges, not fake precision.
```

Good:

```text
Battery health: ~87%
Likely range: 84–90%
```

Bad:

```text
Battery health: 87.42%
```

## Rule 4

```text
Do not convert one charge session into exact battery wear.
```

Good:

```text
This charge happened under higher-risk conditions: warm battery, high charge level, and charging near full.
```

Bad:

```text
This charge used exactly 0.006% of your battery life.
```

## Rule 5

```text
Every major number must say whether it is measured, estimated, inferred, or experimental.
```

## Rule 6

```text
Confidence must be explained, not just displayed.
```

Good:

```text
Confidence: high. Temperature and battery percentage are direct readings.
```

Good:

```text
Confidence: low. Not enough useful charging sessions yet.
```

## Rule 7

```text
The app should guide behavior without fear-based alerts.
```

Better:

```text
Unplug now or let the phone cool to reduce time spent hot while near full.
```

Worse:

```text
Your battery is being destroyed.
```

---

# Final UI Priority

## Primary

```text
Risk
Reason
Action
Target
Time to target
Time to full
Charge alarm status
Continuing value/risk
Confidence
Evidence
```

## Secondary

```text
Health estimate
Health range
Capacity trend
Useful session count
Daily summary
Main daily issue
```

## Detail

```text
Thermal risk
Charge-level risk
Charging speed
Temperature history
High-SOC time
Session logs
Data quality
Raw readings
Unavailable inputs
```

## Experimental

```text
Wear per session
Lifetime cost
Years remaining
Lifetime multiplier
Charging efficiency
Exact degradation from one event
```
