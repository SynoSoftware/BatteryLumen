Based on the product goal, the main UI should only answer: **is this risky, why, what should I do, when should I unplug, and can I trust the estimate?** The spec already says raw metrics must remain secondary, experimental metrics must be warned/hidden, and the app should avoid becoming another battery dashboard.

# Bucket 1 — Do not show in primary UI

These either do not matter enough, are too noisy, or invite fake precision.

| Item                                       |                    UI decision | Why                                                                                 |
| ------------------------------------------ | -----------------------------: | ----------------------------------------------------------------------------------- |
| Exact wear from one charge session         |       Hide / experimental only | Not directly measurable. High bullshit risk.                                        |
| “This charge cost X% battery life”         |            Never show as truth | Fake precision.                                                                     |
| Exact lifetime cost of one session         |       Hide / experimental only | Not defensible from phone readings.                                                 |
| Exact remaining lifespan in months/years   |                    Do not show | Too many unknowns: chemistry, prior use, firmware, temperature history.             |
| “Battery will last X times longer”         |                    Do not show | Overclaims causality.                                                               |
| Lifetime extension multiplier              |       Hide / experimental only | Same issue as above.                                                                |
| Precise degradation from one hot event     |                    Do not show | One event cannot be translated into exact capacity loss.                            |
| Charging efficiency %                      |       Hide / experimental only | Nice lab metric, poor phone UX metric.                                              |
| Exact health decimals, e.g. `87.42%`       |                    Do not show | Use `~87%` and a range instead.                                                     |
| Exact capacity decimals, e.g. `3921.6 mAh` |                    Do not show | Use approximate capacity and confidence.                                            |
| Voltage as a primary number                |                  Advanced only | Useful for debugging, not for user decisions.                                       |
| Raw current in µA / mA                     |                  Advanced only | Device-dependent and often unreliable.                                              |
| C-rate                                     | Advanced / developer mode only | Too technical; depends on estimated capacity.                                       |
| Charge counter raw µAh                     |                  Advanced only | Useful internally, not meaningful to most users.                                    |
| BatteryManager property availability       |              Debug screen only | Implementation detail.                                                              |
| API sentinel values / unsupported readings |                     Debug only | Do not expose unless explaining missing data.                                       |
| Battery technology string                  |                  Advanced only | Usually unhelpful and not enough to infer chemistry-specific aging.                 |
| Internal resistance                        |  Hide unless actually measured | Most phones do not expose it reliably.                                              |
| “Fast charging is bad” badge               |                    Do not show | Current alone is not the main story; heat + SOC + time matter more.                 |
| “20–80 rule” as strict warning             |                    Do not show | Too dogmatic; should be softer guidance.                                            |
| “Charging cycles used today”               |                  Advanced only | Partial cycles are easy to misinterpret.                                            |
| Cycle count estimate                       |  Advanced / health detail only | Android often does not expose reliable cycle count.                                 |
| Per-app drain insights                     |      Optional separate feature | More about battery runtime than battery aging.                                      |
| Screen-on charging time as a main stat     |                    Detail only | Useful only when it explains heat.                                                  |
| Wireless charging warning as default       |         Do not show by default | Only matters if it causes heat or noisy capacity data.                              |
| Plug type as a prominent metric            |                    Detail only | Relevant only as context for speed/heat.                                            |
| Session start/end timestamps               |                   History only | Evidence ledger, not decision UI.                                                   |
| Percentage gained in session               |                   History only | Useful context, not core decision.                                                  |
| Raw average/max temperature list           |            History/detail only | Main UI should summarize risk, not show logs.                                       |
| Time above 40°C / 43°C as raw numbers      |                    Detail only | Main UI should convert this to “hot while charging for X min” only when meaningful. |
| Time above 80/85/90/95 as raw full table   |                    Detail only | Main UI should show only the main reason.                                           |
| Data quality internals                     |                    Detail only | Main UI needs confidence, not every rejection rule.                                 |
| Model version                              |      About / model screen only | Important for transparency, not daily UX.                                           |
| Research notes                             |              Model screen only | Useful for trust, not primary flow.                                                 |
| Export controls                            |                  Settings only | Not part of daily decision.                                                         |
| Experimental metrics toggle                |                  Settings only | Should not invite casual use.                                                       |

# Bucket 2 — Can show because useful, but not primary

These are okay to expose, but they should not compete with the main decision card.

| Item                                 |                      Best placement | Why                                                                    |
| ------------------------------------ | ----------------------------------: | ---------------------------------------------------------------------- |
| Current battery temperature          | Charging detail / secondary on card | Important when hot, otherwise just context.                            |
| Thermal risk breakdown               |                   Expandable detail | Main card only needs the combined conclusion.                          |
| Charge-level risk breakdown          |                   Expandable detail | Useful, but not as important as action.                                |
| Time above 85% today                 |              Daily summary / detail | Helpful habit feedback.                                                |
| Time above 90% today                 |              Daily summary / detail | Helpful if user leaves phone near full.                                |
| Max charging temperature today       |                       Daily summary | Good summary metric.                                                   |
| High-temperature charging minutes    |                       Daily summary | More useful than raw temperature chart.                                |
| Charging speed                       |                     Charging detail | Nice to know; not necessarily battery-health advice.                   |
| Time to full                         |                           Secondary | User wants it, but “time to target” is more aligned with battery care. |
| Time to target                       |                   Primary/secondary | Important if target alarm is core.                                     |
| Plug type                            |                      Session detail | Useful context, not main value.                                        |
| Voltage                              |                    Advanced metrics | Useful for nerds/debugging.                                            |
| Current                              |                    Advanced metrics | Useful only if device readings are plausible.                          |
| Charge/discharge speed               |                  Details / advanced | Nice for transparency, not core health signal.                         |
| Session duration                     |                             History | Useful context.                                                        |
| Charge session history               |                         History tab | Important evidence, not home screen.                                   |
| Discharge session history            |                         History tab | Lower priority than charge sessions.                                   |
| Temperature history chart            |                      History/detail | Useful for patterns, but avoid making users interpret it.              |
| Capacity estimate points             |                       Health detail | Good evidence, but should be summarized.                               |
| Moving average capacity trend        |                       Health screen | More important than individual points.                                 |
| Useful session count                 |                       Health screen | Good trust signal.                                                     |
| Rejected/weak sessions count         |                       Health detail | Good transparency, not home.                                           |
| Design capacity                      |              Health settings/detail | Needed for health estimate, but not daily UX.                          |
| Design capacity override             |                            Settings | Power-user feature.                                                    |
| Confidence reason                    |         Expandable under confidence | Main UI can show “Confidence: high/medium/low.”                        |
| Inputs unavailable                   |               Evidence/model screen | Good honesty, not primary.                                             |
| Evidence grade labels                |                Small label / detail | Important trust layer, but should not dominate UI.                     |
| Raw measured values                  |                     Evidence screen | Good transparency.                                                     |
| Estimated values                     |                     Evidence screen | Good transparency.                                                     |
| Inferred risks                       |                     Evidence screen | Good transparency.                                                     |
| Daily charging score                 |      Home secondary / daily summary | Useful habit feedback if simple.                                       |
| Charge alarm sound/vibration options |                            Settings | Useful, not core science.                                              |
| Optional live monitor                |                            Settings | Valuable for accuracy, but not required.                               |
| Usage access / app drain             |                    Optional feature | Runtime feature, not battery-aging core.                               |

# UI priority recommendation

## Home screen

Show only:

1. **Charging risk**
2. **Main reason**
3. **Recommended action**
4. **Time to target**
5. **Charge alarm**
6. **Battery health summary**
7. **Confidence**

Example:

```text
Risk: High
Reason: battery is 42°C while charging above 85%
Action: unplug now or cool the phone
Best stop: 85% reached
Confidence: high
```

## Expandable detail

Show:

```text
Thermal risk: High
Charge-level risk: Medium
Time hot while charging: 12 min
Time above 85% today: 54 min
Evidence: measured temperature + measured battery level
```

## Advanced / Evidence screen

Show:

```text
Voltage
Current
Charge counter
Plug type
Raw temperature history
Session logs
Rejected sessions
Unavailable readings
Experimental metrics
```

# Cut list for MVP UI

Remove these from normal user-facing UI:

```text
Wear per session
Exact lifetime cost
Exact years remaining
Lifetime multiplier
Charging efficiency %
Exact health decimals
Exact capacity decimals
Raw voltage on home
Raw current on home
C-rate
Cycle-count estimate
Strict 20–80 warnings
Fast-charging fear warnings
Precise degradation from one event
```

# Keep list for MVP UI

Keep these visible:

```text
Risk now
Reason
Action
Battery temperature when relevant
Charge level
Time to target
Time to full
Charge alarm
Battery health estimate with range
Capacity trend
Confidence
Evidence label
Daily summary
```

Best product rule:

> If a metric does not change what the user should do now, it does not belong on the main UI.


Yes. There is a **third bucket**:

# Bucket 3 — Must show in the main UI

These are the important stats. They directly answer the user’s core question: **“Am I treating my battery well right now, and what should I do?”**

| Stat                           | Main UI priority | Why it matters                                                              |
| ------------------------------ | ---------------: | --------------------------------------------------------------------------- |
| **Charging risk now**          |          Highest | The main product output. Good / normal / risky.                             |
| **Main reason**                |          Highest | Prevents black-box scoring. Example: “42°C while charging above 85%.”       |
| **Recommended action**         |          Highest | Converts data into behavior. Example: unplug, continue, cool phone.         |
| **Battery temperature**        |             High | One of the strongest actionable aging-risk signals.                         |
| **Battery % / SOC**            |             High | Needed to explain high-charge risk and target decisions.                    |
| **Charging state**             |             High | Risk depends heavily on whether the phone is charging right now.            |
| **Time to target**             |             High | Makes the charge target useful. Example: “85% in 12 min.”                   |
| **Selected charge target**     |             High | Gives the user a concrete stop point. Default: 85%.                         |
| **Charge target alarm status** |             High | Key utility feature; turns advice into action.                              |
| **Confidence**                 |             High | Protects trust. Example: “High: direct temperature + battery level.”        |
| **Evidence label**             |             High | Shows whether the number is measured, estimated, inferred, or experimental. |
| **Battery health estimate**    |      Medium-high | Important, but not the top live decision unless user is on Health screen.   |
| **Health range**               |      Medium-high | Better than fake precision. Example: `~87%, likely 84–90%`.                 |
| **Useful session count**       |      Medium-high | Explains whether health estimate is trustworthy.                            |
| **Capacity trend**             |      Medium-high | More important than one health number. Stable / declining / noisy.          |
| **Today’s charging quality**   |           Medium | Good summary for habits.                                                    |
| **Main daily issue**           |           Medium | Example: “Spent 2h above 90%” or “Hot charging for 18 min.”                 |

# Main UI should probably contain only this

```text
Risk: High
Reason: battery is 42°C while charging above 85%
Action: unplug now or cool the phone
Target: 85%
Time to target: reached
Confidence: high
Evidence: measured temperature + measured battery level
```

# Home screen hierarchy

## 1. Live decision card

Must show:

```text
Risk
Reason
Action
Target
Time to target
Confidence
Evidence
```

## 2. Small health card

Show:

```text
Battery health: ~87%
Likely range: 84–90%
Trend: slowly declining / stable / noisy
Confidence: medium
```

## 3. Daily summary card

Show:

```text
Today: Good / Normal / Risky
Main issue: hot charging / long time near full / none
```

# Most important stats ranked

1. **Charging risk now**
2. **Recommended action**
3. **Main reason**
4. **Battery temperature**
5. **Battery % / SOC**
6. **Time to target**
7. **Selected target**
8. **Confidence**
9. **Evidence label**
10. **Battery health estimate with range**
11. **Capacity trend**
12. **Useful session count**
13. **Daily charging quality**

Everything else is either **advanced**, **nice-to-have**, or **should not be shown as normal UI**.
