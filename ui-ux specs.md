# UI/UX Specification: Open Battery Decision Assistant (Final)

## 1. Core UI/UX Philosophy
*   **The "Anti-Precision" Principle:** The UI must structurally prevent fake precision. Percentages are integers (`~77%`). Ranges are visual bands, not single points. If a number is noisy, the UI blurs it, ranges it, or refuses to show it.
*   **Calm Diagnostic Aesthetic:** Precise, quiet, low-drama, evidence-first. The app should feel reliable and objective, never cold, scary, or over-authoritative. No disease metaphors, no pulsing neon alarms, no "battery health liquid" animations. Pure data and clear typography.
*   **Progressive Disclosure:** In 5 seconds, the user gets the action. Only by tapping or scrolling do they get the dense, scientific ledger.

## 2. Visual Language & Theming
*   **Colors (Functional, not Decorative):**
    *   *Backgrounds:* Pure White (Light) / Pure Black (Dark).
    *   *Surfaces (Cards):* Minimal contrast (`#F8F9FA` Light / `#121212` Dark). Separation is achieved through spacing and subtle 1px borders.
    *   *Semantic Signals:*
        *   Optimal/Safe: Muted Slate-Teal.
        *   Moderate Risk: Warm Amber.
        *   High Risk: Matte Terracotta.
*   **Typography:**
    *   *Prose & Headers:* Clean Sans-Serif (Inter or Roboto).
    *   *Data:* Monospace (Roboto Mono) with **tabular figures**. Numbers must align perfectly vertically so the ledger reads like a strict data table.

## 3. The Evidence Labeling System
To build trust without causing visual clutter, evidence labels adapt to available space. Clarity never suffers for minimalism.

*   **Main / Spacious Screens:** Use full text tags next to metrics.
    *   `(Measured)` | `(Estimated)` | `(Inferred)` | `(Experimental)`
*   **Dense / Tabular Screens (Ledger):** Use compact abbreviations.
    *   `[M]` | `[E]` | `[I]` | `[X]`
*   **First-Run Onboarding:** A brief tooltip explains: *"Every number in this app is labeled by its evidence quality. We don't pretend to know more than we can measure."*
*   *Interaction:* Tapping any label opens a bottom sheet explaining exactly how the number was derived.

---

## 4. Main Screens Architecture

**Standard Android Bottom Navigation:**
Must use **Icons + Text Labels** to maximize clarity.
`[Icon] Now` | `[Icon] Health` | `[Icon] Ledger` | `[Icon] Evidence`

### Screen 1: "Now" (The Live Decision Screen)
*Purpose: Answer "Am I treating my battery well right now?" in 5 seconds.*

**State: Charging**
*   **Top: The Decision Card (Hero)**
    *   Edge-to-edge card with a 5% background wash of the semantic color (Teal/Amber/Terracotta).
    *   *Eyebrow:* CURRENT CHARGING RISK
    *   *Headline (Massive):* **High**
    *   *Reason:* "Battery is 42°C while charging above 85%."
    *   *Action Box (Outlined):* **Unplug now or let phone cool.**
    *   *Evidence Summary (Bottom of card):* `Confidence: High` • `Evidence: Direct temperature + battery level` *(Note: Summary replaces individual badges here to reduce clutter).*
*   **Middle: The Live Telemetry Data (Tight Grid)**
    *   `Temp:    42°C   (Measured)`
    *   `Level:   86%    (Measured)`
    *   `Speed:   Fast   (Estimated)`
*   **Bottom: Target & Alarm Card**
    *   *Left:* `Best Stop: 85% in ~12 min (Estimated)`
    *   *Right:* A large toggle switch: **Set Alarm for 85%**.
    *   *Context Text:* "Continuing past 85% provides less daily value and increases aging risk at this temperature."

### Screen 2: "Health" (The Reality-Check Screen)
*Purpose: Show the long-term trend without fake precision.*

*   **Top: Health Estimate Card**
    *   *Headline:* **~77% Useful Capacity**
    *   *Sub-headline:* `Likely range: 74–80%`
    *   *Confidence Summary:* `Confidence: Medium` • `Based on Estimated Capacity`
*   **Middle: The Trend Visualization (Scatter Plot)**
    *   *Visual Structure:* A pure X/Y scatter plot without distracting gridlines.
    *   *Data Points:*
        *   **Solid dots:** High-quality *Estimated* capacity points `(Estimated)`.
        *   **Hollow dots:** Weak *Estimated* capacity points (excluded from trend calculation).
    *   *The Line:* A smooth moving average drawn *only* through the solid dots, surrounded by a **shaded confidence band** (74–80%).
*   **Bottom: The Quality Ledger**
    *   Text: "Based on 12 useful sessions. 4 noisy sessions excluded." *(Tappable to view ledger filters).*

### Screen 3: "Ledger" (The Measurement Log)
*Purpose: Treat charge sessions like bank transactions.*

*   **Layout:** A dense, scannable list using tabular alignments.
*   **Row Design (Uses compact tags):**
    *   *Col 1 (Gain):* `+42%` (Large, Monospace)
    *   *Col 2 (Time):* `10:00 - 10:45`
    *   *Col 3 (Range):* `40% → 82%`
    *   *Col 4 (Tag):* `Warm [I]` or `Useful [I]` (Small pill).
*   **Expansion (On Tap):** The row expands downward smoothly.
    *   Reveals raw measurements: `Max Temp: 41°C [M]`, `Time > 85%: 12 min [M]`.
    *   Displays why it was flagged: "Weak Data: Charge gain under 30%."

### Screen 4: "Evidence" (The Model Documentation)
*Purpose: The open-source transparency hub.*

*   **Layout:** Reads like a clean markdown document or GitHub Readme.
*   **Sections (Expandable Accordions):**
    *   *Thermal Risk Model:* Explains the 35°C / 40°C / 43°C bands. Explicitly states: "These are practical guidance bands, not absolute chemistry constants."
    *   *Capacity Model:* Explains how solid dots are estimated from measured voltage/charge counters over time.
    *   *Experimental Metrics:* *(Disabled by default)* If toggled on in settings, experimental outputs use text scales (e.g., "Impact: High") or massive ranges, accompanied by a permanent warning: *"Cannot be measured precisely from a single session."*

---

## 5. Critical UX Flows & Friction

### A. Rejecting Fake Data (The "Weak Session" Flow)
1.  User does a 5-minute charge in the car.
2.  They open the app. The app does *not* recalculate their battery health.
3.  Instead, the Ledger logs the short charge and marks it with a hollow dot and the text: "Ignored for health estimate (charge too short)."
4.  *Outcome:* The user learns the app values data integrity over giving them a constant dopamine hit of updating numbers.

### B. Setting the Target Alarm
1.  User slides the target to 100%.
2.  *Immediate UI Feedback:* The "Best Stop" text changes to amber.
3.  A sub-text appears: "Charging to 100% is fine when needed, but staying near full increases battery stress. The alarm will sound when full."

### C. Explaining Confidence Levels
1.  User sees `Confidence: Low` on their Health tab.
2.  They tap the word "Low".
3.  A bottom sheet slides up: *"The app requires at least 5 deep, uninterrupted charges (over 30% gain) to establish a reliable estimate. You currently have 2. Keep using your phone normally."*

---

## 6. Android Implementation & Performance Notes
*   **Minimal Background Impact:** The app does not pretend to have zero background drain. Instead, the UI explicitly shows when data is stale. If the OS restricted the app during a charge, the Ledger row uses a dotted line and tags the session as `Incomplete Data (Measured)`.
*   **State Transitions:** Use Jetpack Compose crossfades. Going from "Discharging" to "Charging" smoothly dissolves the Daily Summary card into the Live Decision card.
*   **No Spinners:** Data is local (Room/DataStore). Calculations happen quickly. If a large moving average takes a moment to process, use a subtle skeleton shimmer on the graph, never a blocking loading wheel.