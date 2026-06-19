# Software Requirements Document: Battery Stress & Health Assistant

## 1. Product Goal

Build a lightweight Android app that helps users make better battery decisions while charging.

The app must answer quickly:

* Is charging now good or bad?
* Why?
* Should I unplug, cool the phone, or continue?
* How long until the best stop point?
* Is my battery health estimate believable?

The app should avoid becoming a technical dashboard.

## 2. ROI Principle

Every feature must pass this test:

> Does this help the user make a better battery decision with low implementation complexity?

Prioritize features that are:

* Easy to understand
* Useful on day one
* Technically realistic on Android
* Valuable without always-on background monitoring
* Differentiated from AccuBattery-style dashboards

Delay features that require:

* Heavy background execution
* Complex permissions
* Root/device-specific behavior
* Exact battery-aging claims
* Large maintenance across OEMs

## 3. Product Positioning

This is a **battery decision assistant**, not a battery lab.

Primary promise:

> Know whether your current charging behavior is helping or hurting battery longevity.

Secondary promise:

> Track battery health over time with honest confidence labels.

## 4. Core User Value

The app converts raw battery state into plain advice.

Bad:

> 42°C, 4.31V, 1.8A, 87%

Good:

> Charging stress: High
> Reason: battery is hot while charging above 85%
> Action: unplug or let it cool
> Confidence: high

## 5. MVP Feature Set

### 5.1 Live Stress Card

Highest-priority feature.

Show:

* Stress level: Excellent / Good / Normal / High / Severe
* Main reason
* Recommended action
* Confidence

Example:

> Stress: High
> Reason: 42°C while charging above 85%
> Action: unplug now or let the phone cool
> Confidence: high

This is the main reason to open the app.

### 5.2 Temperature-Based Stress Model

The app must treat temperature as a first-class aging signal, not just a warning.

Base rules:

* Under 35°C: low stress
* 35–40°C: normal/moderate
* 40–43°C: high
* 43–45°C: very high
* Above 45°C: severe

Stress increases when heat combines with:

* Charging
* Fast charging
* Battery above 85%
* Battery above 95%
* Heavy use while charging
* Long time plugged in near full

Do not show exact lifetime loss. Use relative language:

* Better than normal
* Normal
* Worse than normal
* Much worse than normal

### 5.3 Charging Guidance

Show the next useful action.

Required:

* Best stop point
* Time to target
* Time to full
* Whether continuing is worth it

Default target:

* 85%

Supported targets:

* 80%
* 85%
* 90%
* 100%
* Custom

Example:

> Best stop: 85% in 18 min
> Full charge: 100% in 52 min
> Continuing past 85% gives less daily value and more aging stress.

### 5.4 Charge Alarm

High ROI because it gives immediate value.

User can set a target charge level.

When reached:

* Send notification
* Optional sound/vibration
* Explain why stopping helps

Example:

> 85% reached. Unplug now to reduce time spent near full.

The app must not claim it can stop charging unless the device supports it.

### 5.5 Battery Health Estimate

Estimate useful battery capacity from charge sessions.

Show:

* Estimated health %
* Estimated current capacity
* Design capacity
* Range
* Confidence
* Number of useful sessions

Example:

> Health: ~77%
> Range: 74–80%
> Confidence: medium
> Based on 12 useful charging sessions

Avoid fake precision.

Do not show:

> 77.38%

Show:

> ~77%

### 5.6 Capacity Trend

Show a simple health trend.

Required:

* Capacity estimate points
* Moving average
* Plain-language conclusion
* Confidence

Examples:

* Battery appears stable.
* Battery is slowly declining.
* Recent readings are noisy.
* More sessions are needed.
* Drop appears real because multiple sessions agree.

This should be useful without requiring the user to interpret the graph.

### 5.7 Estimate Confidence

Every major estimate needs confidence.

Required confidence labels:

* Stress confidence
* Health confidence
* Time-to-target confidence
* Capacity trend confidence

Examples:

> Confidence: high. Temperature and battery percentage are direct readings.

> Confidence: medium. Most charging sessions agree, but some readings are noisy.

This is a differentiator and prevents the app from feeling fake.

### 5.8 Charge Session History

Store each charge session.

Show:

* Start %
* End %
* Duration
* Average temperature
* Max temperature
* Charging source
* Stress summary
* Capacity estimate, if usable
* Data quality

Keep the summary simple.

Example:

> Good session. Max temperature 35°C. Useful for capacity estimate.

### 5.9 Daily Summary

One card per day.

Show:

* Charging quality
* Max charging temperature
* High-stress charging time
* Time above 85%
* Main issue
* Simple score

Example:

> Today: Good
> High-stress charging: 4 min
> Main issue: battery reached 41°C while charging.

## 6. Data Quality Rules

The app must reject weak data instead of producing fake confidence.

### Useful Capacity Session

A session is useful when:

* Charge gain is at least 30–40%
* Charging source is stable
* App was not killed
* Session was not heavily interrupted
* Battery readings are consistent
* Temperature was not extreme

### Weak Session

A session is weak when:

* Charge gain is too small
* Wireless charging was used
* Phone was heavily used while charging
* Temperature was high
* Android restricted the app
* Battery percentage jumped strangely
* Data contradicts the trend without repetition

Weak sessions may be stored but should not strongly affect health estimates.

## 7. Useful AccuBattery-Like Features to Keep

Include only the useful baseline features that support the main product:

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
* Optional advanced raw metrics

These features should support decision-making, not become the main UX.

## 8. Features to Delay

Delay unless users clearly request them:

* Detailed app-drain attribution
* Deep sleep analysis
* Widgets
* Prediction vs reality dashboard
* Advanced export
* Live foreground-service monitor
* Root charging control
* Device-specific charge limiting
* Cloud sync
* Account system
* Complex paid tier

Reason: these add effort, permissions, or maintenance before proving core value.

## 9. Android Implementation Requirements

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

## 10. Background Strategy

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

## 11. Permissions

Required:

* Notifications for charge alarm.

Optional:

* Usage access for app drain insights.
* Foreground service for live charging monitor.

The app must still be useful without optional permissions.

## 12. Privacy

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

Data stored locally:

* Battery readings
* Charge sessions
* Discharge sessions
* Capacity estimates
* Confidence metadata
* Daily summaries

## 13. Main Screens

### Home

Purpose: what should I do now?

Cards:

* Current stress
* Recommended action
* Time to target
* Health estimate
* Today’s summary

### Charging

Purpose: current session.

Cards:

* Temperature
* Charging state
* Stress reason
* Time to target
* Time to full
* Charge alarm

### Health

Purpose: long-term condition.

Cards:

* Health %
* Capacity estimate
* Capacity trend
* Confidence
* Useful sessions

### History

Purpose: evidence.

Tabs:

* Charge sessions
* Capacity points
* Stress events
* Daily summaries

### Settings

Options:

* Target charge level
* Design capacity
* Notifications
* Temperature unit
* Data retention
* Export
* Advanced metrics
* Optional live monitor

## 14. Free vs Paid

### Free

Must deliver real value:

* Live stress card
* Temperature warning
* Charge alarm
* Time to target/full
* Basic health estimate
* Recent session history

### Paid

Only after MVP proves retention:

* Full history
* Capacity trend graph
* Confidence analysis
* Daily summaries
* Export
* Advanced notifications
* Advanced raw metrics
* Prediction vs reality validation

Do not monetize fake precision.

## 15. Non-Goals

The app must not:

* Claim exact battery lifespan remaining.
* Claim exact wear from one short session.
* Claim it can stop charging on unsupported devices.
* Require root.
* Require account creation.
* Require always-on background monitoring.
* Become a phone cleaner.
* Push fear-based alerts.
* Overload users with raw metrics.

## 16. MVP Build Order

Build in this order:

1. Battery data collection
2. Live stress card
3. Temperature-based rules
4. Charge target alarm
5. Time to target/full
6. Charge session history
7. Capacity estimate points
8. Health estimate with confidence
9. Capacity trend
10. Daily summary

Stop after step 6 if users do not find the app useful during charging.

## 17. Success Criteria

The MVP succeeds if users can answer within 5 seconds:

* Is charging now good or bad?
* Why?
* Should I unplug?
* Is the battery too hot?
* How long until 85%?
* What is my battery health?
* Can I trust the health estimate?

## 18. Kill Criteria

Stop or pivot if:

* Users only use the charge alarm.
* Users do not reopen the app while charging.
* Capacity estimates are too noisy.
* Android background limits block useful measurement.
* Users still need to interpret raw stats.
* The app becomes another battery dashboard.

## 19. Strongest Success Signal

The best signal is repeated charging-session use.

Target behavior:

> User plugs in phone, opens app, sees charging stress, understands why, sets an alarm, and knows when to unplug.
