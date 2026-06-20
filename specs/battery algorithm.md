## Research-backed position

The strongest battery-life factors we can responsibly use in an Android app are **battery temperature, state-of-charge history, time spent at high charge, charge/discharge current when reliable, cycle depth/range, and long-term capacity trend**. This matches how serious battery lifetime models are built: NREL’s BLAST documentation treats degradation as sensitive to temperature, SOC histories, current levels, and cycle depth/frequency; BLAST-Lite similarly models commercial Li-ion lifetime from temperature, SOC, depth-of-discharge, and charge/discharge rates. ([National Laboratory of the Rockies][1])

The app should **not** calculate “exact wear from this charge.” Real cell aging is chemistry-specific and path-dependent. Keil/Jossen’s calendar-aging study tested multiple Li-ion chemistries across 16 SOC levels and found that calendar aging does **not** rise smoothly with SOC; plateau regions exist, and high-SOC behavior depends strongly on electrode state. That supports a **risk-band model**, not fake precision. ([Technical University of Munich][2])

The uploaded SRS is aligned with this: it asks for a Battery Decision Assistant with measured/estimated/inferred/experimental labels, and explicitly rejects exact single-session wear claims.

---

# Best algorithm: State-Time Battery Stress Model

Use this as the core product algorithm:

> **Battery life risk = time spent in stressful states, weighted by temperature, SOC, charging state, and current reliability.**

Do not present the numeric score as chemistry truth. Use it internally to choose a clear user message.

---

## 1. Inputs

### Grade A: direct Android/device readings

Android exposes battery level, plug state, temperature, voltage, charge status, charge counter, current-now, current-average, and approximate full-charge time where supported. Battery percentage is integer capacity; charge counter is in microampere-hours; current readings are in microamperes; unsupported properties may return sentinel values. ([Android Developers][3])

```kotlin
data class BatterySample(
    val timestampMs: Long,
    val socPct: Int,                  // measured
    val temperatureC: Double?,         // measured if available
    val isCharging: Boolean,           // measured
    val plugType: PlugType?,           // measured
    val voltageMv: Int?,               // measured if available
    val currentUa: Int?,               // measured but must be validated
    val currentAvgUa: Int?,            // measured but hardware-dependent
    val chargeCounterUah: Int?,        // measured if supported
    val screenOn: Boolean?             // optional inferred/permission-dependent
)

enum class PlugType { AC, USB, WIRELESS, DOCK, UNKNOWN }
```

---

## 2. Stress factors we should actually model

| Factor                                  | Evidence-backed meaning                                                                                                                                                                        | Product use                               |
| --------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----------------------------------------- |
| **High temperature**                    | Aging mechanisms are strongly temperature-sensitive; large aging datasets and lifetime models treat temperature as a primary stressor. ([Nature][4])                                           | Main thermal-risk driver                  |
| **High SOC / high voltage time**        | Calendar aging depends on SOC and chemistry; high-SOC regimes are often worse, but not with one universal cliff. ([Technical University of Munich][2])                                         | Time-above-85/90/95 tracker               |
| **Heat + high SOC together**            | Both temperature and SOC history are primary degradation variables; combined exposure is a stronger warning than either alone. ([National Laboratory of the Rockies][1])                       | Highest-confidence user warning           |
| **High charge current / fast charging** | Current affects aging partly through heat and lithium-plating risk. Plating risk is especially relevant during fast charging, low temperature, high SOC, or high charge voltage. ([Nature][5]) | Secondary risk unless current is reliable |
| **Low-temperature fast charging**       | Lithium plating is a serious graphite-anode degradation mechanism and is associated with low temperature, high SOC, high voltage, and high charge rate. ([MDPI][6])                            | Warning only when cold + charging fast    |
| **Depth of discharge / SOC range**      | Cycle-aging datasets and models include DOD, SOC window, C-rate, and temperature as experimental variables. ([Nature][7])                                                                      | Daily/weekly behavior summary             |
| **Capacity trend**                      | Battery aging appears as capacity loss and impedance increase; large datasets track remaining usable capacity and impedance over time. ([Nature][4])                                           | Health estimate, not instant advice       |

---

# 3. Core risk model

## 3.1 Thermal severity

Use practical policy bands. These are **not universal chemistry constants**; they are product guidance bands.

```kotlin
enum class Severity { LOW, MODERATE, HIGH, VERY_HIGH, SEVERE }

fun thermalSeverity(tempC: Double?): Severity {
    if (tempC == null) return Severity.LOW

    return when {
        tempC < 35.0 -> Severity.LOW
        tempC < 40.0 -> Severity.MODERATE
        tempC < 43.0 -> Severity.HIGH
        tempC < 45.0 -> Severity.VERY_HIGH
        else -> Severity.SEVERE
    }
}
```

User explanation examples:

```text
LOW:
Battery temperature is in the normal practical range.

MODERATE:
Battery is warm. Charging is usually fine, but heat becomes more important near full charge.

HIGH:
Battery is hot while charging. Heat is one of the best-supported battery-aging stress factors.

VERY_HIGH / SEVERE:
Battery is very hot. Stop charging or cool the phone before continuing.
```

---

## 3.2 Charge-level severity

Do **not** say “85% safe, 86% dangerous.” Use bands.

```kotlin
fun socSeverity(socPct: Int, isCharging: Boolean): Severity {
    if (!isCharging) {
        return when {
            socPct >= 95 -> Severity.MODERATE
            socPct >= 90 -> Severity.LOW
            else -> Severity.LOW
        }
    }

    return when {
        socPct < 80 -> Severity.LOW
        socPct < 85 -> Severity.MODERATE
        socPct < 90 -> Severity.MODERATE
        socPct < 95 -> Severity.HIGH
        else -> Severity.VERY_HIGH
    }
}
```

User explanation examples:

```text
Below 80%:
Charge level is not the main concern right now.

80–90%:
Higher charge levels gradually increase calendar-aging risk, especially with heat and time.

90–95%:
Charging near full gives less daily value and usually increases time spent at high voltage.

95–100%:
Remaining plugged in near full is a higher-risk state, especially if warm.
```

---

## 3.3 Time-in-state ledger

Instant temperature or SOC alone is not enough. Track exposure over time.

```kotlin
data class SessionStressLedger(
    var chargingMinutes: Double = 0.0,

    var minutesAbove35C: Double = 0.0,
    var minutesAbove40C: Double = 0.0,
    var minutesAbove43C: Double = 0.0,
    var minutesAbove45C: Double = 0.0,

    var minutesAbove80: Double = 0.0,
    var minutesAbove85: Double = 0.0,
    var minutesAbove90: Double = 0.0,
    var minutesAbove95: Double = 0.0,

    var minutesHotAndAbove85: Double = 0.0,
    var minutesHotAndAbove90: Double = 0.0,
    var minutesVeryHotAndAbove90: Double = 0.0,

    var screenOnChargingMinutes: Double = 0.0,
    var wirelessChargingMinutes: Double = 0.0
)

fun updateLedger(
    ledger: SessionStressLedger,
    prev: BatterySample,
    now: BatterySample
) {
    val dtMin = ((now.timestampMs - prev.timestampMs).coerceAtLeast(0)) / 60_000.0
    if (dtMin <= 0 || dtMin > 30) return

    val isCharging = now.isCharging
    val soc = now.socPct
    val temp = now.temperatureC

    if (isCharging) ledger.chargingMinutes += dtMin

    if (temp != null) {
        if (temp >= 35.0) ledger.minutesAbove35C += dtMin
        if (temp >= 40.0) ledger.minutesAbove40C += dtMin
        if (temp >= 43.0) ledger.minutesAbove43C += dtMin
        if (temp >= 45.0) ledger.minutesAbove45C += dtMin
    }

    if (soc >= 80) ledger.minutesAbove80 += dtMin
    if (soc >= 85) ledger.minutesAbove85 += dtMin
    if (soc >= 90) ledger.minutesAbove90 += dtMin
    if (soc >= 95) ledger.minutesAbove95 += dtMin

    if (isCharging && temp != null && temp >= 40.0 && soc >= 85) {
        ledger.minutesHotAndAbove85 += dtMin
    }

    if (isCharging && temp != null && temp >= 40.0 && soc >= 90) {
        ledger.minutesHotAndAbove90 += dtMin
    }

    if (isCharging && temp != null && temp >= 43.0 && soc >= 90) {
        ledger.minutesVeryHotAndAbove90 += dtMin
    }

    if (isCharging && now.screenOn == true) {
        ledger.screenOnChargingMinutes += dtMin
    }

    if (isCharging && now.plugType == PlugType.WIRELESS) {
        ledger.wirelessChargingMinutes += dtMin
    }
}
```

---

# 4. Combined charging-risk algorithm

Use rule-based escalation first. Then use weighted exposure only for ordering reasons.

```kotlin
data class RiskDecision(
    val risk: Severity,
    val mainReason: String,
    val action: String,
    val evidence: List<EvidenceItem>,
    val confidence: Confidence,
    val detail: RiskDetail
)

enum class Confidence { LOW, MEDIUM, HIGH }

data class EvidenceItem(
    val label: String,
    val grade: EvidenceGrade
)

enum class EvidenceGrade {
    MEASURED,
    ESTIMATED,
    INFERRED,
    EXPERIMENTAL
}

data class RiskDetail(
    val thermal: Severity,
    val chargeLevel: Severity,
    val combined: Severity,
    val reasons: List<String>
)
```

```kotlin
fun decideChargingRisk(
    sample: BatterySample,
    ledger: SessionStressLedger,
    targetPct: Int = 85
): RiskDecision {
    val thermal = thermalSeverity(sample.temperatureC)
    val socRisk = socSeverity(sample.socPct, sample.isCharging)

    val reasons = mutableListOf<String>()
    var combined = maxSeverity(thermal, socRisk)

    if (sample.isCharging && sample.temperatureC != null) {
        if (sample.temperatureC >= 45.0) {
            combined = Severity.SEVERE
            reasons += "battery is above 45°C while charging"
        } else if (sample.temperatureC >= 43.0 && sample.socPct >= 90) {
            combined = Severity.SEVERE
            reasons += "battery is very hot while charging near full"
        } else if (sample.temperatureC >= 40.0 && sample.socPct >= 85) {
            combined = maxSeverity(combined, Severity.HIGH)
            reasons += "battery is hot while charging above 85%"
        } else if (sample.temperatureC >= 35.0 && sample.socPct >= 90) {
            combined = maxSeverity(combined, Severity.HIGH)
            reasons += "battery is warm while charging near full"
        }
    }

    if (ledger.minutesAbove95 >= 60) {
        combined = maxSeverity(combined, Severity.HIGH)
        reasons += "battery has spent over 1 hour near full"
    }

    if (ledger.minutesHotAndAbove85 >= 10) {
        combined = maxSeverity(combined, Severity.HIGH)
        reasons += "battery has spent meaningful time hot and above 85%"
    }

    if (ledger.minutesVeryHotAndAbove90 >= 3) {
        combined = Severity.SEVERE
        reasons += "battery has been very hot near full"
    }

    val mainReason = reasons.firstOrNull()
        ?: defaultReason(sample, thermal, socRisk)

    val action = recommendedAction(
        combined = combined,
        sample = sample,
        targetPct = targetPct
    )

    return RiskDecision(
        risk = combined,
        mainReason = mainReason,
        action = action,
        evidence = buildEvidence(sample),
        confidence = confidenceForRisk(sample),
        detail = RiskDetail(
            thermal = thermal,
            chargeLevel = socRisk,
            combined = combined,
            reasons = reasons
        )
    )
}
```

```kotlin
fun recommendedAction(
    combined: Severity,
    sample: BatterySample,
    targetPct: Int
): String {
    val soc = sample.socPct
    val temp = sample.temperatureC

    return when (combined) {
        Severity.SEVERE ->
            "Unplug now and let the phone cool before continuing."

        Severity.VERY_HIGH ->
            if (soc >= targetPct) {
                "Unplug now. Continuing gives little daily value and keeps the battery in a high-stress state."
            } else {
                "Pause charging or cool the phone before continuing."
            }

        Severity.HIGH ->
            when {
                temp != null && temp >= 40.0 ->
                    "Cool the phone. Avoid gaming, sun, thick cases, or wireless charging while hot."
                soc >= targetPct ->
                    "Unplug at the selected target to reduce time spent near full."
                else ->
                    "Continue only if you need the charge; otherwise stop at the target."
            }

        Severity.MODERATE ->
            if (soc >= targetPct) {
                "Unplug if you do not need more charge."
            } else {
                "Charging is acceptable. Stop at the selected target if full charge is not needed."
            }

        Severity.LOW ->
            "Charging conditions look normal."
    }
}
```

```kotlin
fun defaultReason(
    sample: BatterySample,
    thermal: Severity,
    socRisk: Severity
): String {
    return when {
        sample.temperatureC == null ->
            "temperature is unavailable, so risk is based mainly on charge level"
        thermal.ordinal > socRisk.ordinal ->
            "temperature is the main stress factor"
        socRisk.ordinal > thermal.ordinal ->
            "charge level is the main stress factor"
        else ->
            "charging conditions are within normal guidance bands"
    }
}
```

---

# 5. Current / fast-charging risk

Current readings should be optional because Android values are hardware-dependent. Android documents current-now/current-average, but the averaging period may depend on fuel-gauge hardware and configuration. ([Android Developers][8])

```kotlin
data class CurrentReliability(
    val usable: Boolean,
    val reason: String
)

fun validateCurrentReading(samples: List<BatterySample>): CurrentReliability {
    val values = samples.mapNotNull { it.currentUa }
    if (values.size < 5) return CurrentReliability(false, "not enough current readings")

    if (values.all { it == 0 || it == Int.MIN_VALUE }) {
        return CurrentReliability(false, "device does not expose usable current")
    }

    val chargingValues = samples.filter { it.isCharging }.mapNotNull { it.currentUa }
    val dischargingValues = samples.filter { !it.isCharging }.mapNotNull { it.currentUa }

    if (chargingValues.isNotEmpty() && dischargingValues.isNotEmpty()) {
        val chargingMedian = chargingValues.sorted()[chargingValues.size / 2]
        val dischargingMedian = dischargingValues.sorted()[dischargingValues.size / 2]

        if (chargingMedian == dischargingMedian) {
            return CurrentReliability(false, "current does not change with charging state")
        }
    }

    return CurrentReliability(true, "current changes plausibly with charging state")
}
```

Use current only for **secondary warnings**:

```kotlin
fun currentRisk(
    currentUa: Int?,
    estimatedCapacityMah: Double?,
    tempC: Double?,
    socPct: Int,
    isCharging: Boolean
): Severity {
    if (!isCharging || currentUa == null || estimatedCapacityMah == null) {
        return Severity.LOW
    }

    val amps = kotlin.math.abs(currentUa) / 1_000_000.0
    val capacityAh = estimatedCapacityMah / 1000.0
    if (capacityAh <= 0.0) return Severity.LOW

    val cRate = amps / capacityAh

    return when {
        tempC != null && tempC < 10.0 && cRate >= 0.5 ->
            Severity.HIGH
        tempC != null && tempC < 15.0 && socPct >= 80 && cRate >= 0.5 ->
            Severity.HIGH
        cRate >= 1.5 && tempC != null && tempC >= 35.0 ->
            Severity.HIGH
        cRate >= 1.0 && socPct >= 85 ->
            Severity.MODERATE
        else ->
            Severity.LOW
    }
}
```

User message:

```text
Fast charging is not automatically bad. The concern is high current combined with heat, cold battery conditions, or high charge level.
```

---

# 6. Capacity and health estimator

This must be separate from live risk. Health should be estimated from multiple useful sessions, not from one charge.

```kotlin
data class CapacityPoint(
    val timestampMs: Long,
    val estimatedFullCapacityMah: Double,
    val startSoc: Int,
    val endSoc: Int,
    val deltaSocPct: Int,
    val avgTempC: Double?,
    val maxTempC: Double?,
    val method: CapacityMethod,
    val quality: DataQuality
)

enum class CapacityMethod {
    CHARGE_COUNTER_DELTA,
    CURRENT_INTEGRATION
}

enum class DataQuality {
    USEFUL,
    WEAK,
    REJECTED
}
```

```kotlin
fun estimateCapacityFromChargeCounter(
    start: BatterySample,
    end: BatterySample
): CapacityPoint? {
    val startCounter = start.chargeCounterUah ?: return null
    val endCounter = end.chargeCounterUah ?: return null

    val deltaSocPct = end.socPct - start.socPct
    if (deltaSocPct < 30) return null

    val deltaUah = endCounter - startCounter
    if (deltaUah <= 0) return null

    val estimatedFullUah = deltaUah / (deltaSocPct / 100.0)
    val estimatedFullMah = estimatedFullUah / 1000.0

    return CapacityPoint(
        timestampMs = end.timestampMs,
        estimatedFullCapacityMah = estimatedFullMah,
        startSoc = start.socPct,
        endSoc = end.socPct,
        deltaSocPct = deltaSocPct,
        avgTempC = null,
        maxTempC = null,
        method = CapacityMethod.CHARGE_COUNTER_DELTA,
        quality = DataQuality.USEFUL
    )
}
```

Robust health estimate:

```kotlin
data class HealthEstimate(
    val healthPctApprox: Int?,
    val likelyRangePct: IntRange?,
    val confidence: Confidence,
    val usefulSessionCount: Int,
    val explanation: String
)

fun estimateHealth(
    capacityPoints: List<CapacityPoint>,
    designCapacityMah: Double
): HealthEstimate {
    val useful = capacityPoints
        .filter { it.quality == DataQuality.USEFUL }
        .map { it.estimatedFullCapacityMah }
        .filter { it in (0.4 * designCapacityMah)..(1.2 * designCapacityMah) }

    if (useful.size < 3) {
        return HealthEstimate(
            healthPctApprox = null,
            likelyRangePct = null,
            confidence = Confidence.LOW,
            usefulSessionCount = useful.size,
            explanation = "not enough useful charging sessions yet"
        )
    }

    val sorted = useful.sorted()
    val medianMah = sorted[sorted.size / 2]
    val lowMah = sorted[(sorted.size * 0.25).toInt()]
    val highMah = sorted[(sorted.size * 0.75).toInt().coerceAtMost(sorted.lastIndex)]

    val health = ((medianMah / designCapacityMah) * 100).toInt()
    val low = ((lowMah / designCapacityMah) * 100).toInt()
    val high = ((highMah / designCapacityMah) * 100).toInt()

    val confidence = when {
        useful.size >= 10 && (high - low) <= 6 -> Confidence.HIGH
        useful.size >= 5 && (high - low) <= 12 -> Confidence.MEDIUM
        else -> Confidence.LOW
    }

    return HealthEstimate(
        healthPctApprox = health,
        likelyRangePct = low..high,
        confidence = confidence,
        usefulSessionCount = useful.size,
        explanation = "based on ${useful.size} useful charging sessions"
    )
}
```

User display:

```text
Battery health: ~87%
Likely range: 84–90%
Confidence: medium
Based on 7 useful charging sessions.
```

Do **not** display:

```text
Battery health: 87.42%
This charge consumed 0.006% of battery life.
```

---

# 7. Time-to-target algorithm

Android has `computeChargeTimeRemaining()`, but Android itself labels it an approximation and returns `-1` when it cannot compute a value. ([Android Developers][3])

Better model:

```kotlin
data class ChargeRateBucket(
    val fromSocInclusive: Int,
    val toSocExclusive: Int,
    val plugType: PlugType,
    val tempBand: TempBand,
    val medianPctPerMinute: Double,
    val sampleCount: Int
)

enum class TempBand { COOL, NORMAL, WARM, HOT, UNKNOWN }

fun estimateTimeToTarget(
    currentSoc: Int,
    targetSoc: Int,
    currentSessionRates: Map<IntRange, Double>,
    historicalBuckets: List<ChargeRateBucket>,
    plugType: PlugType,
    tempBand: TempBand
): Pair<Int?, Confidence> {
    if (targetSoc <= currentSoc) return 0 to Confidence.HIGH

    var minutes = 0.0
    var confidence = Confidence.HIGH

    for (soc in currentSoc until targetSoc) {
        val band = bucketForSoc(soc)

        val sessionRate = currentSessionRates[band]
        val historicalRate = historicalBuckets
            .filter {
                soc >= it.fromSocInclusive &&
                soc < it.toSocExclusive &&
                it.plugType == plugType &&
                it.tempBand == tempBand &&
                it.sampleCount >= 3
            }
            .maxByOrNull { it.sampleCount }
            ?.medianPctPerMinute

        val rate = sessionRate ?: historicalRate

        if (rate == null || rate <= 0.0) {
            return null to Confidence.LOW
        }

        if (sessionRate == null) confidence = Confidence.MEDIUM

        minutes += 1.0 / rate
    }

    return minutes.toInt() to confidence
}
```

User display:

```text
Best stop: 85% in about 14 min
Full charge: about 48 min
Confidence: medium — based on this charger’s recent charging speed.
```

---

# 8. Explanation engine

The explanation should always say:

1. What was measured.
2. What was inferred.
3. Why the action helps.
4. What is not known.

```kotlin
fun explainDecision(decision: RiskDecision, sample: BatterySample): String {
    val measured = mutableListOf<String>()
    measured += "${sample.socPct}% battery"

    sample.temperatureC?.let {
        measured += "${"%.1f".format(it)}°C battery temperature"
    }

    if (sample.isCharging) measured += "charging state"

    return buildString {
        append("Risk: ${decision.risk}\n")
        append("Reason: ${decision.mainReason}\n")
        append("Action: ${decision.action}\n")
        append("Measured: ${measured.joinToString(", ")}.\n")
        append("Inferred: combined battery stress from temperature, charge level, and time in state.\n")
        append("Not claimed: exact battery wear from this session.")
    }
}
```

Example:

```text
Risk: High
Reason: battery is 42°C while charging above 85%
Action: unplug now or let the phone cool
Measured: 88% battery, 42.0°C battery temperature, charging state.
Inferred: heat + high charge level is a higher-risk condition.
Not claimed: exact battery wear from this session.
```

---

# 9. Daily summary algorithm

```kotlin
data class DailyBatterySummary(
    val date: String,
    val overall: Severity,
    val maxChargingTempC: Double?,
    val hotChargingMinutes: Double,
    val minutesAbove85: Double,
    val minutesAbove90: Double,
    val mainIssue: String,
    val advice: String,
    val evidenceGrade: EvidenceGrade
)

fun summarizeDay(sessions: List<SessionStressLedger>): DailyBatterySummary {
    val totalHotAbove85 = sessions.sumOf { it.minutesHotAndAbove85 }
    val totalAbove90 = sessions.sumOf { it.minutesAbove90 }
    val totalAbove85 = sessions.sumOf { it.minutesAbove85 }
    val totalAbove40 = sessions.sumOf { it.minutesAbove40C }

    val overall = when {
        totalHotAbove85 >= 20 -> Severity.HIGH
        totalAbove40 >= 30 -> Severity.HIGH
        totalAbove90 >= 120 -> Severity.MODERATE
        totalAbove85 >= 240 -> Severity.MODERATE
        else -> Severity.LOW
    }

    val mainIssue = when {
        totalHotAbove85 >= 20 -> "battery spent time hot while above 85%"
        totalAbove40 >= 30 -> "battery spent time hot while charging"
        totalAbove90 >= 120 -> "battery spent a long time near full"
        totalAbove85 >= 240 -> "battery spent several hours above 85%"
        else -> "charging behavior looked normal"
    }

    val advice = when (mainIssue) {
        "battery spent time hot while above 85%" ->
            "Avoid charging while hot, especially near full."
        "battery spent time hot while charging" ->
            "Cool the phone while charging. Avoid sun, gaming, thick cases, and wireless charging when warm."
        "battery spent a long time near full" ->
            "Use an 80–85% target when full charge is not needed."
        "battery spent several hours above 85%" ->
            "Reduce time spent plugged in after reaching your target."
        else ->
            "No major change needed."
    }

    return DailyBatterySummary(
        date = "",
        overall = overall,
        maxChargingTempC = null,
        hotChargingMinutes = totalAbove40,
        minutesAbove85 = totalAbove85,
        minutesAbove90 = totalAbove90,
        mainIssue = mainIssue,
        advice = advice,
        evidenceGrade = EvidenceGrade.INFERRED
    )
}
```

Display:

```text
Today: Good
Hot charging: 3 min
Time above 85%: 46 min
Main issue: none
Advice: no major change needed
Evidence: measured battery level + measured temperature; risk is inferred
```

---

# 10. User guidance rules

## Strong recommendations

```text
Avoid charging while hot.
Avoid staying near full charge for long periods.
Avoid heat + high charge level together.
Use 80–85% as a daily target when full charge is not needed.
Charge to 100% when you need it, but avoid holding it there for hours.
Watch long-term health trend, not one session.
```

## Softer recommendations

```text
Fast charging is not automatically bad; risk rises when it creates heat or happens near full/cold conditions.
Short charging sessions are not bad by themselves.
Occasional 100% charging is fine when useful.
Occasional low battery is not catastrophic.
```

## Claims to ban

```text
This charge used exactly X% of battery life.
Stopping at 85% makes the battery last exactly X times longer.
Fast charging always kills batteries.
You must always stay between 20% and 80%.
One hot charge permanently ruined the battery.
```

---

# 11. MVP implementation order

```text
1. Collect BatterySample from BatteryManager.
2. Create SessionStressLedger.
3. Implement thermalSeverity().
4. Implement socSeverity().
5. Implement combined risk escalation.
6. Show live decision card.
7. Add target alarm.
8. Add time-to-target estimate.
9. Add charge-session history.
10. Add useful-session capacity estimator.
11. Add health estimate with confidence.
12. Add daily summary.
13. Add model explanation screen.
```

---

# 12. Final product behavior

The app should answer in one card:

```text
Charging risk: High

Why:
Battery is 42°C while charging above 85%.

Action:
Unplug now or cool the phone before continuing.

Best stop:
85% reached.

Full charge:
About 38 min.

Evidence:
Battery level and temperature are direct readings.
Combined risk is inferred from battery-aging research.

Confidence:
High for current risk.
Medium for time estimate.
No exact wear estimate claimed.
```

This is the defensible algorithm: **measure real states, accumulate time in risky states, explain the strongest evidence-backed reason, and refuse fake precision.**

[1]: https://research-hub.nlr.gov/en/publications/battery-lifetime-analysis-and-simulation-tool-blast-documentation/ "
        Battery Lifetime Analysis and Simulation Tool (BLAST) Documentation
      \-  National Laboratory of the Rockies"
[2]: https://portal.fis.tum.de/en/publications/calendar-aging-of-lithium-ion-batteries-i-impact-of-the-graphite-/?utm_source=chatgpt.com "Calendar aging of lithium-ion batteries i. impact of the graphite anode ..."
[3]: https://developer.android.com/reference/android/os/BatteryManager "BatteryManager  |  API reference  |  Android Developers"
[4]: https://www.nature.com/articles/s41597-024-03831-x "Comprehensive battery aging dataset: capacity and impedance fade measurements of a lithium-ion NMC/C-SiO cell | Scientific Data"
[5]: https://www.nature.com/articles/s41560-023-01194-y "High-throughput Li plating quantification for fast-charging battery design | Nature Energy"
[6]: https://www.mdpi.com/1996-1073/13/13/3458 "A Study on the Influence of Lithium Plating on Battery Degradation | MDPI"
[7]: https://www.nature.com/articles/s41597-024-03859-z "A multi-stage lithium-ion battery aging dataset using various experimental design methodologies | Scientific Data"
[8]: https://developer.android.com/reference/kotlin/android/os/BatteryManager "BatteryManager  |  API reference  |  Android Developers"


Yes — this is mainly for **phone batteries**, specifically modern **smartphone lithium-ion / lithium-polymer batteries** in Android devices.

The algorithm is shaped around what a phone app can realistically measure:

* battery %
* charging state
* plug type
* battery temperature
* voltage/current when available
* charge counter when available
* charging-session history

That matches the uploaded product spec, which defines this as a **free/open-source Android app** using `BatteryManager`, local session logging, charge alarms, time-to-target, thermal risk, charge-level risk, and battery-health estimates.

The research principles also apply broadly to lithium-ion batteries in laptops, tablets, e-bikes, and EVs, but the **actual app algorithm should be phone-specific** because:

1. Phones expose limited battery data.
2. Phone battery chemistry and charging firmware are not always known.
3. Android cannot reliably stop charging on most devices.
4. Temperature is usually battery-pack temperature, not full internal cell telemetry.
5. Current/capacity readings vary heavily by device.

So the right framing is:

> This is a reality-based battery decision algorithm for Android phone batteries, using lithium-ion aging research but only making claims supported by phone-accessible measurements.

The safest product name would be something like:

**Battery Lumen Decision Assistant for Android Phones**

Not:

**Universal Lithium-Ion Battery Health Scientist**
