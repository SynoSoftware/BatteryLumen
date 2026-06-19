# Software Requirements Document: Battery Stress & Health Assistant

## 1. Product Goal

Build a small Android app that gives users immediate, practical battery advice:

* Is charging now good or bad?
* Is the battery too hot?
* Should I unplug now or keep charging?
* How long until the best stop point?
* Is my battery actually degrading?
* Can I trust the app’s estimate?

The app should maximize user value with the least technical surface area.

## 2. Product Positioning

This is not a battery dashboard.

It is a **battery decision assistant**.

Main promise:

> Know whether you are treating your battery well right now.

Secondary promise:

> Track real battery health over time without fake precision.

## 3. ROI Principle

Prioritize features that create clear user value quickly.

High ROI:

* Live charging advice
* Temperature warning
* Charge target alarm
* Time to 80% / 85%
* Battery health estimate
* Capacity trend
* Confidence label

Low ROI:

* Too many raw graphs
* Exact wear per session
* Complex app-drain attribution
* Root charging control
* Always-on background monitoring
* Over-precise lifetime cost claims

## 4. Core User Value

The app must convert battery data into decisions.

Bad UX:

> Temperature: 42°C
> Current: 1830 mA
> Voltage: 4.28 V
> Wear: 0.06 cycles

Good UX:

> Charging stress: High
> Reason: battery is hot while charging above 85%
> Action: unplug or let the phone cool
> Confidence: high

## 5. Main Features

### 5.1 Live Stress Card

The home screen must show one primary card:

* Stress level: Excellent / Good / Normal / High / Severe
* Main reason
* Recommended action
* Confidence

Example:

> Stress: High
> Reason: 42°C while charging above 85%
> Action: unplug now
> Confidence: high

This is the highest-value feature.

### 5.2 Temperature-Based Stress Model

The app must estimate battery aging stress using temperature as a primary input.

Base thermal zones:

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
* Heavy usage while charging
* Long time plugged in near full

The app should not claim exact lifespan loss.

### 5.3 Charging Guidance

Show:

* Recommended stop point
* Time until recommended stop
* Time until full
* Whether continuing is worth it
* Whether current charging is healthy

Default target:

* 85%

Other options:

* 80%
* 90%
* 100%
* Custom

Example:

> Best stop: 85% in 18 min
> Full charge: 100% in 52 min
> Continuing past 85% gives less value and more stress.

### 5.4 Charge Alarm

User can set a target charge level.

When reached:

* Send notification
* Optional sound
* Explain why stopping helps

Example:

> 85% reached. Unplug now to reduce time spent near full.

This creates immediate first-day value.

### 5.5 Battery Health Estimate

Estimate real capacity from charging sessions.

Show:

* Estimated health
* Estimated current capacity
* Original/design capacity
* Range
* Confidence
* Useful session count

Example:

> Health: ~77%
> Range: 74–80%
> Confidence: medium
> Based on 12 useful charge sessions

Avoid fake precision like 77.38%.

### 5.6 Capacity Trend

Show a simple long-term graph:

* Capacity points
* Moving average
* Trend
* Confidence

The graph must include a plain-language conclusion:

* Stable
* Slowly declining
* Declining quickly
* Too noisy to tell
* More sessions needed

Example:

> Battery health appears stable. Recent readings are noisy but within the expected range.

### 5.7 Estimate Confidence

Every major estimate must include confidence:

* Stress: high / medium / low
* Health: high / medium / low
* Time remaining: high / medium / low
* Time to target: high / medium / low

The app must explain why.

Example:

> Confidence: medium. Most sessions agree, but three recent measurements look noisy.

### 5.8 Session History

Record charging sessions.

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

Example:

> Good session. Max temperature 35°C. Useful for capacity estimate.

### 5.9 Daily Summary

Show one daily summary card:

* Charging quality
* High-stress charging time
* Max charging temperature
* Time above 85%
* Battery treatment score
* Main issue

Example:

> Today: Good
> High-stress charging: 4 min
> Main issue: battery reached 41°C during charging.

### 5.10 Basic Discharge Estimate

Show:

* Estimated time remaining
* Screen-on estimate
* Screen-off estimate
* Current drain level

Keep it simple.

Avoid building a complex power-user drain dashboard in MVP.

## 6. Useful Baseline Features to Include

The app should include the useful expected battery-app features:

* Health estimate
* Capacity estimate
* Capacity history graph
* Charge session history
* Discharge session history
* Charge alarm
* Time to full
* Time to target
* Time remaining
* Temperature tracking
* Charge/discharge speed
* Plug type
* Design capacity override
* Data export
* Optional advanced metrics

But these should support the main decision assistant, not replace it.

## 7. Features to Delay

Delay these unless users ask for them:

* Detailed app-drain attribution
* Deep sleep analysis
* Widgets
* CSV/JSON export
* Advanced raw metric screens
* Prediction vs reality dashboard
* Live foreground service mode
* Paid tier
* Root/device-specific charging control

These are useful, but not needed to prove value.

## 8. MVP Scope

Build only the smallest version that proves the app is useful.

### MVP Must Have

1. Live stress card
2. Temperature-based stress model
3. Charge target alarm
4. Time to target/full
5. Charge session history
6. Capacity estimate points
7. Health estimate with confidence
8. Capacity trend graph
9. Daily summary

### MVP Must Not Have

* Account system
* Cloud sync
* Always-on foreground service
* Root features
* Complex dashboards
* Exact lifetime-cost claims
* App cleaner features
* Fear-based notifications

## 9. Monetization Strategy

Free version should provide immediate value.

Free:

* Live stress card
* Temperature warnings
* Charge alarm
* Basic health estimate
* Recent session history

Paid:

* Full history
* Capacity trend graph
* Confidence analysis
* Daily summaries
* Advanced notifications
* Export
* Advanced raw metrics
* Prediction vs reality validation

Premium must sell clarity and history, not fake precision.

## 10. Android Implementation Requirements

Recommended stack:

* Kotlin
* Jetpack Compose
* Room
* DataStore
* WorkManager
* BatteryManager
* Optional UsageStatsManager
* Optional foreground service only for explicit live charging monitor

Core functionality should work without always-on background monitoring.

## 11. Background Strategy

Default:

* No persistent foreground service.
* Use battery and power connection events where possible.
* Record sessions opportunistically.
* Recalculate trends periodically.
* Show most value when app is opened or while charging.

Optional:

### Live Charging Monitor

Only if user explicitly enables it.

* Runs only while charging.
* Shows visible notification.
* Improves accuracy.
* Stops when unplugged.

## 12. Privacy Requirements

Local-first.

No requirement for:

* Account
* Cloud sync
* Location
* Contacts
* Camera
* Microphone
* SMS
* Call logs

Data stays on device unless the user exports it.

## 13. Main Screens

### Home

Purpose: answer what to do now.

Cards:

* Stress now
* Recommended action
* Time to target
* Health estimate
* Today’s summary

### Charging

Purpose: understand the current session.

Cards:

* Temperature
* Charging state
* Stress reason
* Time to target
* Time to full
* Charge alarm

### Health

Purpose: understand long-term condition.

Cards:

* Health %
* Capacity estimate
* Capacity trend
* Confidence
* Useful sessions

### History

Purpose: show evidence.

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
* Live monitor
* Data retention
* Export
* Advanced metrics

## 14. Success Criteria

The MVP succeeds if users can answer these within 5 seconds:

* Is charging now good or bad?
* Why?
* Should I unplug?
* Is the phone too hot?
* How long until 85%?
* What is my battery health?
* Can I trust that estimate?
* Is my battery getting worse?

## 15. Product Kill Criteria

Stop or pivot if:

* Users only open it once.
* Charge alarm is the only feature people value.
* Capacity estimates are too noisy to trust.
* Android background limits make core value unreliable.
* Users still feel they need to interpret raw data.
* The app becomes another technical battery dashboard.

## 16. Product Success Signal

The strongest signal is not downloads.

It is repeated use during charging.

Target behavior:

> User plugs in phone, opens app, sees whether charging is healthy, sets alarm, and understands when to unplug.
