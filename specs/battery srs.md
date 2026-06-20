# Software Requirements Specification: Battery Lumen Decision Assistant

## 1. Purpose

Build a free and open-source Android app that helps users make better battery decisions using evidence-backed battery science.

The app must answer:

* Is charging now good, normal, or risky?
* Why?
* Should I unplug, cool the phone, or continue?
* How long until the selected stop point?
* What is my estimated battery health?
* How trustworthy is that estimate?
* Which readings are measured, estimated, inferred, or unproven?

The app must not pretend to know more than it can measure.

## 2. Product Definition

The product is:

**Battery Decision Assistant + Measurement Ledger + Evidence Layer**

It has three jobs:

1. Give the user a clear recommendation now.
2. Log battery evidence over time.
3. Label every metric by evidence quality.

The app should compete on trust, not fake precision.

## 3. Scientific Positioning

The app must separate:

### Well-supported battery stress factors

These are supported by lithium-ion battery research and should drive the main product:

* Battery temperature
* State of charge
* Time spent at high state of charge
* Charge/discharge rate
* Depth of discharge / cycling range
* Calendar aging
* Cycle aging
* Capacity fade over time
* Increasing internal resistance, if measurable

### Useful but device-dependent estimates

These may be useful but must be labeled as estimates:

* Battery health %
* Estimated current capacity
* Time to target charge
* Time to full
* Time remaining
* Charge/discharge speed
* Session capacity estimate

### Weak, noisy, or not proven enough for main UX

These may be shown only with warnings:

* Exact wear from one charge session
* Exact lifetime cost of one session
* “Battery will last X times longer”
* “Charging efficiency %”
* Precise degradation caused by one hot event
* Exact remaining battery lifespan in months/years

The app may store these as experimental metrics, but it must not present them as proven truth.

## 4. Core Principle

The app must never say:

> This charge used exactly 0.006% of your battery life.

Instead, it should say:

> This charge happened under higher-risk conditions: warm battery, high charge level, and charging near full. Long-term capacity trend will show whether degradation is accelerating.

## 5. Primary User Outcome

Within 5 seconds, the user should know:

> “Am I treating my battery well right now?”

Example:

Stress: High
Reason: battery is 42°C while charging above 85%
Action: unplug now or let the phone cool
Evidence: temperature and battery percentage are direct readings
Confidence: high

## 6. Open-Source Strategy

The app must be fully open-source.

Required:

* Public repository
* Public issue tracker
* Public model documentation
* Public data schema
* Public changelog for model changes
* Local-first storage
* No hidden server model
* No proprietary scoring formula
* No telemetry by default
* No ads required for core use
* No paywalled health features

Recommended license:

* GPLv3 if the goal is to keep forks open.
* Apache 2.0 if the goal is maximum reuse.

Default recommendation:

Use GPLv3.

## 7. Evidence Classification

Every user-facing metric must have an evidence label.

### Evidence Grade A: Directly measured

Examples:

* Battery %
* Charging state
* Plug type
* Battery temperature
* Voltage, if available
* Current, if available
* Charge counter, if available
* Timestamp
* Session duration

### Evidence Grade B: Estimated from device readings

Examples:

* Time to target
* Time to full
* Estimated capacity
* Estimated health %
* Charging speed
* Discharge speed

### Evidence Grade C: Inferred risk

Examples:

* Thermal stress
* Charge-level stress
* Combined charging risk
* Data quality score
* “This session was healthier than usual”

### Evidence Grade D: Experimental / not proven enough

Examples:

* Wear per session
* Exact lifetime cost
* Lifetime extension multiplier
* Exact years remaining
* Charging efficiency score

Grade D metrics must never appear as primary product claims.

## 8. MVP Scope

The MVP must include:

1. Battery data collection
2. Charge session ledger
3. Live charging decision card
4. Temperature-risk estimate
5. Charge-level-risk estimate
6. Combined charging-risk estimate
7. Charge target alarm
8. Time to target
9. Time to full
10. Capacity estimate from useful charge sessions
11. Battery health estimate with confidence
12. Capacity trend graph
13. Data quality labels
14. Daily charging summary
15. Export
16. Model explanation screen

## 9. Explicit Non-Goals

The app must not:

* Claim exact battery lifespan remaining.
* Claim exact wear from one short session.
* Claim exact degradation from one hot charge.
* Claim it can stop charging unless the device supports it.
* Require root.
* Require account creation.
* Require cloud sync.
* Require always-on background monitoring.
* Become a phone cleaner.
* Push fear-based alerts.
* Hide uncertainty.
* Lock useful health features behind payment.

## 10. Core Models

## 10.1 Capacity Model

Answers:

> “How much useful battery capacity does my battery appear to have now?”

Inputs:

* Start battery %
* End battery %
* Battery % gained
* Charge added, if available
* Charge counter delta, if available
* Current integration, if reliable
* Charging duration
* Charging source
* Temperature range
* Session interruptions
* Device data availability

Output:

Health: ~77%
Range: 74–80%
Confidence: medium
Based on 12 useful charging sessions
Trend: slowly declining

### Capacity Model Rules

The app must:

* Prefer large charge sessions.
* Prefer sessions with stable readings.
* Reject short/noisy sessions.
* Use ranges, not fake precision.
* Show useful session count.
* Show confidence reason.

Bad:

Health: 77.38%

Good:

Health: ~77%
Likely range: 74–80%

## 10.2 Charge-Level Risk Model

Answers:

> “Is this charge percentage range associated with higher aging risk?”

Inputs:

* Current battery %
* Time above 80%
* Time above 85%
* Time above 90%
* Time above 95%
* Time near 100%
* Time below 5–10%
* Charging state
* Session duration

Output:

Charge-level risk: Medium
Reason: battery has spent 43 minutes above 85%

### Charge-Level Model Rules

The app should avoid saying:

> 85% is always safe and 86% is dangerous.

Instead, it should say:

> Higher charge levels generally increase aging risk, especially when combined with heat and time.

## 10.3 Thermal Risk Model

Answers:

> “Is the battery too hot for healthy charging?”

Inputs:

* Current battery temperature
* Average charging temperature
* Max charging temperature
* Time above configured thermal bands
* Whether phone is charging while hot
* Whether phone is near full while hot

Output:

Thermal risk: High
Reason: battery reached 42°C while charging

### Thermal Model Rules

Temperature thresholds are app policy thresholds, not universal chemistry constants.

Default practical bands:

* Under 35°C: low thermal concern
* 35–40°C: moderate concern
* 40–43°C: high concern
* 43–45°C: very high concern
* Above 45°C: severe concern

The app must explain:

> These thresholds are practical guidance bands. Actual degradation depends on battery chemistry, age, charge level, current, and duration.

## 10.4 Combined Charging Risk Model

Answers:

> “What should I do now?”

The main card must combine:

* Temperature risk
* Charge-level risk
* Charging state
* Time spent in risky state
* Charging speed/current, if available
* Screen-on/heavy-use state, if available

Example:

Charging risk: High
Main reason: battery is hot while charging above 85%
Action: unplug or cool the phone
Confidence: high

Expandable detail:

* Thermal risk: High
* Charge-level risk: Medium
* Combined risk: High

## 11. Live Decision Card

The home screen must prioritize one clear card.

Required fields:

* Current charging risk
* Main reason
* Recommended action
* Evidence label
* Confidence
* Time to target
* Time to full

Example:

Risk: High
Reason: 42°C while charging above 85%
Action: unplug now or let the phone cool
Best stop: 85% in 12 min
Full charge: 100% in 46 min
Evidence: direct temperature + direct battery level
Confidence: high

This card is the main product.

## 12. Charging Guidance

Default target:

* 85%

Supported targets:

* 80%
* 85%
* 90%
* 100%
* Custom

Required outputs:

* Selected stop point
* Time to target
* Time to full
* Whether continuing is useful
* Whether continuing increases risk

Example:

Best stop: 85% in 18 min
Full charge: 100% in 52 min
Continuing past 85% gives less daily value and usually increases aging risk, especially if the battery is warm.

## 13. Charge Alarm

The app must allow a charge target alarm.

When target is reached:

* Send notification
* Optional sound
* Optional vibration
* Explain reason

Example:

85% reached. Unplug now to reduce time spent near full.

The app must not claim it can stop charging unless the OS or device supports charge limiting.

## 14. Measurement Ledger

Every charge session must become evidence.

Each session record must include:

* Start time
* End time
* Start %
* End %
* Percentage gained
* Duration
* Charging source
* Average temperature
* Max temperature
* Time above 40°C
* Time above 43°C
* Time above 85%
* Time above 90%
* Screen-on time while charging
* Charge added, if available
* Estimated capacity, if usable
* Charge-level risk
* Thermal risk
* Combined risk
* Data quality
* Confidence reason
* Evidence grade

Purpose:

* Improve health estimates.
* Detect noisy sessions.
* Show why conclusions are credible.
* Prevent fake precision.

## 15. Data Quality Rules

The app must reject weak data instead of producing false confidence.

### Useful Capacity Session

A session is useful when:

* Charge gain is at least 30–40%.
* Charging source is stable.
* App was not killed.
* Session was not heavily interrupted.
* Battery readings are consistent.
* Device exposes enough readings.
* Temperature was not extreme.
* Charge counter or current readings are available and plausible.

### Weak Session

A session is weak when:

* Charge gain is too small.
* Wireless charging adds too much uncertainty.
* Phone was heavily used while charging.
* Temperature was high.
* Android restricted the app.
* Battery percentage jumped strangely.
* Data contradicts trend without repetition.
* Required readings are missing.
* Current or charge counter appears unreliable.

Weak sessions may be stored, but they must not strongly affect capacity estimates.

## 16. Capacity Trend

The app must show a long-term health trend.

Required:

* Individual capacity estimate points
* Moving average
* Trend line
* Confidence label
* Data quality warnings
* Plain-language conclusion

Examples:

* Battery appears stable.
* Battery is slowly declining.
* Recent readings are noisy.
* More charging sessions are needed.
* Drop appears real because multiple useful sessions agree.

The graph must support the conclusion, not force users to interpret noisy data alone.

## 17. Proven vs Unproven Metrics Registry

The app must include a “Metrics & Evidence” screen.

### Main Metrics

Shown prominently:

* Battery risk now
* Thermal risk
* Charge-level risk
* Battery health estimate
* Capacity trend
* Time to target
* Time to full

### Secondary Metrics

Shown in detail views:

* Voltage
* Current
* Charge/discharge speed
* Plug type
* Session duration
* Temperature history
* Charge session history
* Discharge session history

### Experimental Metrics

Shown only with warning:

* Wear per session
* Estimated lifetime cost
* Charging efficiency %
* Years remaining
* Lifetime multiplier

Required warning:

> This metric is experimental. It is not directly measured and should not be used as a precise battery-health claim.

## 18. Estimate Confidence

Every major estimate must include confidence.

Required confidence labels:

* Thermal risk confidence
* Charge-level risk confidence
* Combined risk confidence
* Capacity confidence
* Health confidence
* Time-to-target confidence
* Capacity trend confidence

Confidence levels:

* Low
* Medium
* High

Each confidence label must explain why.

Examples:

Confidence: high. Temperature and battery percentage are direct readings.

Confidence: medium. Most charging sessions agree, but some readings are noisy.

Confidence: low. Not enough useful charging sessions yet.

## 19. Research-Based UX Rules

The app must guide users toward actions supported by battery-aging research:

### Strong guidance

* Avoid charging while hot.
* Avoid staying near full charge for long periods.
* Avoid high heat combined with high charge level.
* Prefer moderate charging targets when full charge is not needed.
* Watch long-term capacity trend, not single-session noise.

### Softer guidance

These may help, but must not be overstated:

* Slower charging may reduce heat, but current alone is not always the dominant aging factor.
* Avoiding very deep discharge may help, but occasional low battery is not catastrophic.
* Short charging sessions are not inherently bad.
* Charging to 100% occasionally is acceptable when needed.

### Do not present as fact

* “Always keep battery between 20–80% or you are damaging it.”
* “Fast charging always kills batteries.”
* “One hot charge permanently ruins the battery.”
* “Stopping at 85% makes the battery last exactly X times longer.”
* “A single app can precisely calculate battery wear per session.”

## 20. Daily Summary

The app must generate a simple daily summary.

Required fields:

* Overall charging quality
* Max charging temperature
* High-temperature charging time
* Time above 85%
* Time above 90%
* Main issue
* Simple score
* Evidence grade

Example:

Today: Good
High-risk charging: 4 min
Main issue: battery reached 41°C while charging
Evidence: direct temperature readings

The summary should reinforce behavior, not scare the user.

## 21. Model Explanation Screen

The app must include a “How this works” screen.

For each model, show:

* Inputs used
* Inputs unavailable
* Evidence grade
* Confidence level
* Why confidence is low/medium/high
* Whether the value is measured, estimated, or inferred
* What is not proven
* Link to model documentation

Required sections:

* Capacity model
* Thermal risk model
* Charge-level risk model
* Combined risk model
* Data quality rules
* Experimental metrics

## 22. Useful Baseline Features

All useful baseline features should be free:

* Health estimate
* Capacity estimate
* Capacity history graph
* Charge alarm
* Time to full
* Time to target
* Charge session history
* Discharge session history
* Temperature tracking
* Charge/discharge speed
* Plug type
* Design capacity override
* Data export
* Advanced raw metrics
* Model explanation
* Data quality explanation

Raw metrics must remain secondary.

## 23. Main Screens

### Home

Purpose:

What should I do now?

Cards:

* Current charging risk
* Recommended action
* Time to target
* Battery health estimate
* Today’s summary

### Charging

Purpose:

What is happening during this charge?

Cards:

* Temperature
* Charging state
* Charge-level risk
* Thermal risk
* Combined risk
* Time to target
* Time to full
* Charge alarm

### Health

Purpose:

What condition is my battery in?

Cards:

* Health %
* Current estimated capacity
* Design capacity
* Capacity trend
* Confidence
* Useful session count
* Data quality notes

### History

Purpose:

What evidence is the app using?

Tabs:

* Charge sessions
* Capacity points
* Temperature events
* High-charge events
* Daily summaries

### Evidence

Purpose:

Which numbers are real?

Sections:

* Measured values
* Estimated values
* Inferred risks
* Experimental metrics
* Unavailable device readings

### Model

Purpose:

How does the app decide?

Sections:

* Capacity model
* Thermal risk model
* Charge-level risk model
* Combined risk model
* Confidence model
* Data quality rules
* Research notes

### Settings

Options:

* Target charge level
* Design capacity
* Notifications
* Temperature unit
* Data retention
* Export
* Advanced metrics
* Experimental metrics toggle
* Optional live monitor

## 24. Android Implementation Requirements

Recommended stack:

* Kotlin
* Jetpack Compose
* Room
* DataStore
* WorkManager
* BatteryManager

Optional later:

* UsageStatsManager
* Foreground service for explicit live charging monitor only

The MVP must not depend on always-on background monitoring.

## 25. Background Strategy

Default behavior:

* No persistent foreground service.
* Use battery and power events where possible.
* Record sessions opportunistically.
* Recalculate trends periodically.
* Provide best experience when app is opened during charging.

Optional later:

### Live Charging Monitor

Only if user explicitly enables it.

Rules:

* Runs only while charging.
* Shows visible notification.
* Stops when unplugged.
* Improves measurement accuracy.
* Not required for core app value.

## 26. Permissions

Required:

* Notifications for charge alarm.

Optional:

* Usage access for app drain insights.
* Foreground service for live charging monitor.

The app must still be useful without optional permissions.

## 27. Privacy

Local-first.

No required:

* Account
* Cloud sync
* Location
* Contacts
* Camera
* Microphone
* SMS
* Call logs

No analytics by default.

Optional diagnostics may exist only if:

* Explicitly opt-in
* Fully explained
* Viewable before sending
* Removable
* Anonymous where possible

Export formats:

* CSV
* JSON

## 28. Open-Source Requirements

Required repository docs:

* README
* Research notes
* Model documentation
* Data schema
* Privacy policy
* Device compatibility notes
* Known limitations
* Unsupported claims list
* Contributing guide

Required transparency:

* Every model version must be documented.
* Every threshold must explain whether it is research-backed, heuristic, or experimental.
* Every major UX claim must map to an evidence grade.
* Every experimental metric must be labeled.

## 29. Monetization

No paywall for core features.

Allowed:

* Donations
* GitHub Sponsors
* Open Collective
* Optional paid Play Store supporter version with identical features
* Paid support for OEM/device-specific debugging
* Bounties for device compatibility
* Ethical sponsorships clearly separated from product decisions

Not allowed:

* Ads that harm trust
* Selling user data
* Locking battery health behind payment
* Locking graphs behind payment
* Locking export behind payment
* Dark-pattern subscriptions
* Fake “Pro accuracy”

## 30. MVP Build Order

Build in this order:

1. Battery data collection
2. Evidence-grade system
3. Charge session ledger
4. Live decision card
5. Thermal risk model
6. Charge-level risk model
7. Combined risk model
8. Charge target alarm
9. Time to target/full
10. Capacity estimate points
11. Health estimate with confidence
12. Capacity trend
13. Daily summary
14. Export
15. Model explanation screen

Stop after step 9 if users do not find the app useful during charging.

## 31. Acceptance Criteria

The MVP succeeds if users can answer within 5 seconds:

* Is charging now good or risky?
* Why?
* Should I unplug?
* Is the battery too hot?
* How long until 85%?
* What is my estimated battery health?
* Can I trust that estimate?
* Is this number measured, estimated, inferred, or experimental?

## 32. Kill Criteria

Stop or pivot if:

* Users only use the charge alarm.
* Users do not reopen the app while charging.
* Capacity estimates are too noisy.
* Android background limits block useful measurement.
* Users still need to interpret raw stats.
* The app becomes another battery dashboard.
* Confidence labels do not improve trust.
* Open-source users cannot understand or verify the model.
* The app cannot avoid overclaiming.

## 33. Strongest Success Signal

The strongest signal is repeated charging-session use.

Target behavior:

User plugs in phone, opens app, sees charging risk, understands why, sets an alarm, and knows when to unplug.

The second strongest signal is trust in health estimates:

User believes the battery health trend because the app shows evidence, confidence, noisy-data handling, and what is not proven.

## 34. Product Philosophy

The app must be useful because it is honest.

It should say:

* This is measured.
* This is estimated.
* This is inferred.
* This is experimental.
* This is not known.
* This is what research supports.
* This is what the app cannot prove.

The product should not be “another battery stats app.”

It should be the open-source battery app that refuses to bullshit.
