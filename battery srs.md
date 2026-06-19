# Software Requirements Specification: Open Battery Decision Assistant

## 1. Purpose

Build the best free and open-source Android battery app for users who want to understand battery health, charging stress, and long-term degradation without fake precision or technical overload.

The app must answer:

* Is charging now good, normal, or bad?
* Why?
* Should I unplug, cool the phone, or continue?
* How long until the best stop point?
* What is my estimated battery health?
* Can I trust that estimate?
* What evidence is the app using?

## 2. Product Strategy

This is an open-source, no-paywall battery decision assistant.

Strategy:

> Give users the best battery-health experience for free, make every model transparent, and compete on trust.

No premium tier.
No locked history.
No locked graphs.
No locked export.
No fake “Pro” health features.
No ads required for core use.

The moat is credibility:

* Open algorithms
* Local-first data
* Transparent confidence
* Exportable evidence
* No hidden scoring
* No fake exact lifespan claims

## 3. Product Definition

The product is:

**Battery Decision Assistant + Measurement Ledger**

It has two jobs:

1. Give the user a clear recommendation now.
2. Log enough evidence over time to make health estimates believable.

## 4. Core Differentiator

Most battery apps show stats.

This app must show:

* What is happening.
* Whether it is good or bad.
* Why.
* What action to take.
* What was measured.
* What was estimated.
* What was inferred.
* How confident the app is.
* What evidence supports the conclusion.

Primary differentiation:

* Separate charge-level stress from thermal stress.
* Combine both into one clear recommendation.
* Track capacity trend with confidence.
* Reject noisy data instead of pretending precision.
* Make all calculations inspectable.

## 5. Primary User Outcome

Within 5 seconds, the user should know:

> “Am I treating my battery well right now?”

Example:

Stress: High
Reason: battery is 42°C while charging above 85%
Action: unplug now or let the phone cool
Confidence: high
Evidence: temperature and battery percentage are direct readings

## 6. MVP Scope

The MVP must include:

1. Battery data collection
2. Charge session ledger
3. Live charging stress card
4. Temperature-based stress estimate
5. Charge-level stress estimate
6. Combined charging decision
7. Charge target alarm
8. Time to target
9. Time to full
10. Capacity estimate from useful charge sessions
11. Battery health estimate with confidence
12. Capacity trend graph
13. Daily charging summary
14. Data export
15. Open model explanations

## 7. Explicit Non-Goals

The app must not:

* Require account creation.
* Require cloud sync.
* Require root.
* Require always-on background monitoring.
* Claim exact battery lifespan remaining.
* Claim exact wear from one short session.
* Pretend unsupported devices can stop charging.
* Become a phone cleaner.
* Push fear-based alerts.
* Hide uncertainty.
* Lock useful health features behind a paywall.

## 8. Core Concepts

### 8.1 Capacity Estimate

Answers:

> “How much useful battery capacity do I still have?”

Based on charge sessions.

Inputs:

* Start battery %
* End battery %
* Battery % gained
* Charge added, if available
* Charging duration
* Charging source
* Temperature range
* Session quality

Output:

Health: ~77%
Range: 74–80%
Confidence: medium
Based on 12 useful charging sessions
Trend: slowly declining

### 8.2 Charge-Level Stress

Answers:

> “Is this battery percentage range stressful?”

Inputs:

* Current battery %
* Time above 80%
* Time above 85%
* Time above 90%
* Time near 100%
* Time below 5–10%
* Charging state

Output:

Charge-level stress: Medium
Reason: battery is above 85%

### 8.3 Thermal Stress

Answers:

> “Is the battery too hot?”

Inputs:

* Current battery temperature
* Average charging temperature
* Max charging temperature
* Time above 40°C
* Time above 43°C
* Time above 45°C
* Whether phone is charging while hot

Output:

Thermal stress: High
Reason: battery reached 42°C while charging

### 8.4 Combined Stress

Answers:

> “What should I do now?”

The combined stress is the main user-facing decision.

Example:

Charging stress: High
Main reason: battery is hot while charging above 85%
Action: unplug or cool the phone
Confidence: high

Expandable detail:

* Charge-level stress: Medium
* Thermal stress: High
* Combined stress: High

## 9. Stress Model Requirements

### 9.1 Thermal Zones

Use practical temperature zones:

* Under 35°C: low thermal stress
* 35–40°C: normal to moderate stress
* 40–43°C: high stress
* 43–45°C: very high stress
* Above 45°C: severe stress

### 9.2 Stress Multipliers

Stress increases when heat combines with:

* Charging
* Fast charging
* Battery above 85%
* Battery above 95%
* Heavy phone use while charging
* Long time plugged in near full

The app must not show exact lifetime loss.

Use relative language:

* Better than normal
* Normal
* Worse than normal
* Much worse than normal

## 10. Live Decision Card

The home screen must prioritize one clear card.

Required fields:

* Current stress level
* Main reason
* Recommended action
* Confidence
* Evidence
* Time to target charge
* Time to full charge

Example:

Stress: High
Reason: 42°C while charging above 85%
Action: unplug now or let the phone cool
Best stop: 85% in 12 min
Full charge: 100% in 46 min
Confidence: high

This card is the main product.

## 11. Charging Guidance

Default target:

* 85%

Supported targets:

* 80%
* 85%
* 90%
* 100%
* Custom

Required outputs:

* Best stop point
* Time to target
* Time to full
* Whether continuing is useful
* Whether continuing increases stress

Example:

Best stop: 85% in 18 min
Full charge: 100% in 52 min
Continuing past 85% gives less daily value and more aging stress.

## 12. Charge Alarm

The app must allow a charge target alarm.

When target is reached:

* Send notification
* Optional sound
* Optional vibration
* Explain reason

Example:

85% reached. Unplug now to reduce time spent near full.

The app must not claim it can stop charging unless the device supports it.

## 13. Measurement Ledger

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
* Charge-level stress
* Thermal stress
* Combined stress
* Data quality
* Confidence reason

Purpose:

* Improve health estimates.
* Detect noisy sessions.
* Show why conclusions are credible.
* Prevent fake precision.

## 14. Capacity Estimation

The app must estimate capacity from useful charge sessions.

Basic method:

estimated capacity = charge added / battery percentage gained

Example:

Battery gained: 40%
Charge added: 1,350 mAh
Estimated capacity: 3,375 mAh

User-facing output:

Health: ~77%
Range: 74–80%
Confidence: medium
Based on 12 useful sessions

Do not show fake precision.

Bad:

Health: 77.38%

Good:

Health: ~77%

## 15. Capacity Trend

The app must show a long-term health trend.

Required:

* Individual capacity estimate points
* Moving average
* Trend line
* Confidence label
* Plain-language conclusion

Examples:

* Battery appears stable.
* Battery is slowly declining.
* Recent readings are noisy.
* More charging sessions are needed.
* Drop appears real because multiple sessions agree.

The graph must support the conclusion, not force users to interpret noisy data alone.

## 16. Estimate Confidence

Every major estimate must include confidence.

Required confidence labels:

* Stress confidence
* Thermal stress confidence
* Charge-level stress confidence
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

## 17. Data Quality Rules

The app must reject weak data instead of producing false confidence.

### 17.1 Useful Capacity Session

A session is useful when:

* Charge gain is at least 30–40%
* Charging source is stable
* App was not killed
* Session was not heavily interrupted
* Battery readings are consistent
* Temperature was not extreme
* Charge counter/current readings are available or sufficiently reliable

### 17.2 Weak Session

A session is weak when:

* Charge gain is too small
* Wireless charging was used
* Phone was heavily used while charging
* Temperature was high
* Android restricted the app
* Battery percentage jumped strangely
* Data contradicts trend without repetition
* Required readings are missing

Weak sessions may be stored but must not strongly affect capacity estimates.

## 18. Daily Summary

The app must generate a simple daily summary.

Required fields:

* Overall charging quality
* Max charging temperature
* High-stress charging time
* Time above 85%
* Time above 90%
* Main issue
* Simple score

Example:

Today: Good
High-stress charging: 4 min
Main issue: battery reached 41°C while charging.

The summary should reinforce behavior, not scare the user.

## 19. Useful Baseline Features

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

## 20. Open-Source Requirements

The project must be open-source from the start.

Required:

* Public repository
* Clear README
* Transparent roadmap
* Public issue tracker
* Public model documentation
* Public data schema
* Reproducible builds where practical
* No hidden server dependency
* No proprietary scoring model
* No telemetry by default

Recommended license:

* GPLv3 if the goal is to keep forks open.
* Apache 2.0 if the goal is maximum adoption and reuse.

Default recommendation:

Use GPLv3 for a trust-first consumer app.

## 21. Transparency Requirements

The app must include an “How this estimate works” screen.

For each major estimate, show:

* Inputs used
* Inputs unavailable
* Confidence level
* Why confidence is low/medium/high
* Whether the value is measured, estimated, or inferred
* Link to model documentation

Example:

Battery health is estimated from charging sessions. This estimate is medium confidence because 12 sessions were measured and 9 agree within the expected range.

## 22. Privacy Requirements

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

Data stored locally:

* Battery readings
* Charge sessions
* Discharge sessions
* Capacity estimates
* Confidence metadata
* Daily summaries

Export formats:

* CSV
* JSON

## 23. Main Screens

### 23.1 Home

Purpose:

What should I do now?

Cards:

* Current stress
* Recommended action
* Time to target
* Battery health estimate
* Today’s summary

### 23.2 Charging

Purpose:

What is happening during this charge?

Cards:

* Temperature
* Charging state
* Charge-level stress
* Thermal stress
* Combined stress
* Time to target
* Time to full
* Charge alarm

### 23.3 Health

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

### 23.4 History

Purpose:

What evidence is the app using?

Tabs:

* Charge sessions
* Capacity points
* Stress events
* Daily summaries

### 23.5 Model

Purpose:

How does the app decide?

Sections:

* Capacity model
* Thermal stress model
* Charge-level stress model
* Combined stress model
* Confidence model
* Data quality rules

### 23.6 Settings

Options:

* Target charge level
* Design capacity
* Notifications
* Temperature unit
* Data retention
* Export
* Advanced metrics
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

## 27. Monetization

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

## 28. Competition Strategy

The app should beat existing battery apps by giving more trust for free.

Against metric-heavy apps:

* Simpler main UX
* Better explanations
* Transparent confidence
* Open models

Against paid/pro apps:

* Free full history
* Free graphs
* Free export
* Free confidence analysis

Against built-in OEM battery settings:

* Better explanation
* Better history
* Better health trend
* Better stress reasoning

## 29. MVP Build Order

Build in this order:

1. Battery data collection
2. Charge session ledger
3. Live stress card
4. Temperature-based stress rules
5. Charge-level stress rules
6. Combined stress decision
7. Charge target alarm
8. Time to target/full
9. Capacity estimate points
10. Health estimate with confidence
11. Capacity trend
12. Daily summary
13. Export
14. Model explanation screen

Stop after step 8 if users do not find the app useful during charging.

## 30. Acceptance Criteria

The MVP succeeds if users can answer within 5 seconds:

* Is charging now good or bad?
* Why?
* Should I unplug?
* Is the battery too hot?
* How long until 85%?
* What is my estimated battery health?
* Can I trust that estimate?
* Is the app using real evidence or guessing?

## 31. Kill Criteria

Stop or pivot if:

* Users only use the charge alarm.
* Users do not reopen the app while charging.
* Capacity estimates are too noisy.
* Android background limits block useful measurement.
* Users still need to interpret raw stats.
* The app becomes another battery dashboard.
* Confidence labels do not improve trust.
* Open-source users cannot understand or verify the model.

## 32. Strongest Success Signal

The strongest signal is repeated charging-session use.

Target behavior:

User plugs in phone, opens app, sees charging stress, understands why, sets an alarm, and knows when to unplug.

The second strongest signal is trust in health estimates:

User believes the battery health trend because the app shows the evidence, confidence, and noisy-data handling behind it.

## 33. Long-Term Vision

Become the reference open-source battery health app for Android.

Long-term goals:

* Best public battery stress model
* Best open capacity-estimation ledger
* Best user-facing confidence system
* Community-tested device compatibility
* Transparent model improvements
* No fake precision
* No paywall
* No bullshit
