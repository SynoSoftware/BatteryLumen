$ErrorActionPreference = 'Stop'

function Add-TreeEntry {
    param(
        [System.Collections.IDictionary]$Node,
        [string[]]$Parts,
        [string]$Value
    )

    if ($Parts.Count -eq 1) {
        if (-not $Node.Contains($Parts[0])) {
            $Node[$Parts[0]] = [ordered]@{ '_' = $Value }
            return
        }

        $child = $Node[$Parts[0]]
        if ($child -is [System.Collections.IDictionary]) {
            $child['_'] = $Value
            return
        }

        $Node[$Parts[0]] = [ordered]@{ '_' = $Value }
        return
    }

    if (-not $Node.Contains($Parts[0])) {
        $Node[$Parts[0]] = [ordered]@{}
    }

    Add-TreeEntry -Node ([System.Collections.IDictionary]$Node[$Parts[0]]) -Parts $Parts[1..($Parts.Count - 1)] -Value $Value
}

function Get-HumanizedValue {
    param(
        [string]$Key
    )

    $tokens = $Key -split '_' | Where-Object { $_ -and $_ -notmatch '^v\d+$' }
    $words = foreach ($token in $tokens) {
        switch -Regex ($token) {
            '^ac$' { 'AC'; continue }
            '^usb$' { 'USB'; continue }
            '^ui$' { 'UI'; continue }
            '^v0$' { 'v0'; continue }
            default { $token.Substring(0, 1).ToUpperInvariant() + $token.Substring(1).ToLowerInvariant() }
        }
    }

    return ($words -join ' ')
}

$overrides = [ordered]@{
    'allow_notifications' = 'Allow notifications'
    'above_85_label' = 'Above 85'
    'above_90_label' = 'Above 90'
    'battery_stress_label' = 'Battery Stress'
    'battery_health_title' = 'Battery Health'
    'battery_tab_health' = 'Health'
    'battery_tab_how_it_works' = 'How It Works'
    'battery_tab_ledger' = 'Ledger'
    'battery_tab_now' = 'Now'
    'best_stop_label' = 'Best Stop'
    'capability_battery_level' = 'Battery Level'
    'capability_battery_level_availability' = 'Available on every device'
    'capability_battery_level_fallback' = 'Show battery percent only'
    'capability_battery_level_rule' = 'Use the broadcast snapshot as the current reading.'
    'capability_battery_level_source' = 'Android battery broadcast'
    'capability_battery_temperature' = 'Battery Temperature'
    'capability_battery_temperature_availability' = 'Available on many devices'
    'capability_battery_temperature_fallback' = 'Hide temperature when missing'
    'capability_battery_temperature_rule' = 'Use the measured temperature when the device reports it.'
    'capability_battery_temperature_source' = 'BatteryManager'
    'capability_celsius_unit' = 'Celsius'
    'capability_charge_counter' = 'Charge Counter'
    'capability_charge_counter_availability' = 'Available on some devices'
    'capability_charge_counter_fallback' = 'Hide charge counter when missing'
    'capability_charge_counter_rule' = 'Use the remaining charge counter when the device exposes it.'
    'capability_charge_counter_source' = 'BatteryManager'
    'capability_current_now' = 'Current Now'
    'capability_current_now_availability' = 'Available on some devices'
    'capability_current_now_fallback' = 'Hide current when missing'
    'capability_current_now_rule' = 'Use the instantaneous current when the device exposes it.'
    'capability_current_now_source' = 'BatteryManager'
    'capability_enum_unit' = 'Enum'
    'capability_matrix_subtitle' = 'What this device can expose'
    'capability_matrix_title' = 'Device Capability Matrix'
    'capability_microamp_hour_unit' = 'Microamp hour'
    'capability_microamp_unit' = 'Microamp'
    'capability_millivolt_unit' = 'Millivolt'
    'capability_percent_unit' = 'Percent'
    'capability_plug_type' = 'Plug Type'
    'capability_plug_type_availability' = 'Available on most devices'
    'capability_plug_type_fallback' = 'Treat the source as unknown'
    'capability_plug_type_rule' = 'Use the reported plug type as a hint.'
    'capability_plug_type_source' = 'Android battery broadcast'
    'capability_voltage' = 'Voltage'
    'capability_voltage_availability' = 'Available on many devices'
    'capability_voltage_fallback' = 'Hide voltage when missing'
    'capability_voltage_rule' = 'Use the reported millivolts when available.'
    'capability_voltage_source' = 'BatteryManager'
    'change_label' = 'Change'
    'confidence_label' = 'Confidence: %1$s'
    'confidence_summary' = 'Confidence: %1$s. %2$s'
    'confidence_reason_high' = 'Enough readings and useful sessions'
    'confidence_reason_low' = 'Too little data yet'
    'confidence_reason_medium' = 'Some readings are still missing'
    'continue_charging_or_set_target' = 'Continue charging or set a target'
    'current_label' = 'Current'
    'current_session_label' = 'Current Session'
    'decision_action_avoid_full' = 'Avoid charging to full'
    'decision_action_continue' = 'Continue charging'
    'decision_action_cool' = 'Cool the device or unplug'
    'decision_action_not_charging' = 'Start charging to see guidance'
    'decision_action_unplug_if_not_needed' = 'Unplug if you do not need a full charge'
    'decision_action_unplug_now' = 'Unplug now'
    'decision_reason_at_target' = 'At target while charging'
    'decision_reason_hot' = 'Battery is hot'
    'decision_reason_hot_charging' = 'Charging while hot: %1$s°C'
    'decision_reason_near_full' = 'Near full while charging'
    'decision_reason_not_charging' = 'Not charging'
    'decision_reason_reasonable' = 'Charging looks reasonable'
    'enable_charge_alerts' = 'Enable Charge Alerts'
    'enable_notifications' = 'Enable notifications'
    'evidence_badge' = '%1$s (%2$s)'
    'evidence_estimated' = 'Estimated'
    'evidence_experimental' = 'Experimental'
    'evidence_inferred' = 'Inferred'
    'evidence_measured' = 'Measured'
    'evidence_summary_label' = 'Evidence Summary'
    'fallback_label' = 'Fallback: %1$s'
    'full_charge_label' = 'Full Charge'
    'health_documented_in_backlog' = 'Health work is documented in backlog'
    'health_estimation_backlog' = 'Health estimation is still in backlog'
    'health_no_estimate_count' = 'Not enough useful sessions yet'
    'health_no_estimate_v0' = 'No battery health estimate in v0'
    'health_not_enough_data' = 'Not enough useful data yet'
    'health_subtitle' = 'Battery health stays hidden until useful data exists'
    'health_title' = 'Health'
    'health_useful_sessions' = 'Useful sessions'
    'health_useful_sessions_count' = '%1$s useful sessions'
    'how_it_works_estimated' = 'Estimated'
    'how_it_works_estimated_desc' = 'Values can be predicted from the sessions we have.'
    'how_it_works_experimental' = 'Experimental'
    'how_it_works_experimental_desc' = 'Experimental values stay clearly labeled and hidden from claims.'
    'how_it_works_inferred' = 'Inferred'
    'how_it_works_inferred_desc' = 'Inferred values combine measured signals with model rules.'
    'how_it_works_measured' = 'Measured'
    'how_it_works_measured_desc' = 'Measured values come directly from the device.'
    'how_it_works_subtitle' = 'What is measured, inferred, and estimated'
    'how_it_works_title' = 'How It Works'
    'ledger_no_sessions' = 'No sessions yet'
    'ledger_no_sessions_hint' = 'Charge while the app is open or monitoring to build the ledger'
    'ledger_subtitle' = 'Saved charging sessions and evidence'
    'ledger_title' = 'Ledger'
    'level_label' = 'Level'
    'live_telemetry' = 'Live Telemetry'
    'measured_values_direct' = 'Measured values are direct'
    'no_active_session_yet' = 'No active session yet'
    'no_battery_health_estimate_v0' = 'No battery health estimate in v0'
    'no_battery_snapshot_yet' = 'No battery snapshot yet'
    'value_delta_percent' = '+%1$s%%'
    'value_min_short' = '~%1$s min'
    'value_mv' = '%1$s mV'
    'value_na' = 'Not available'
    'value_percent' = '%1$s%%'
    'value_temp_c' = '%1$s°C'
    'value_ua' = '%1$s uA'
    'notification_channel_charge_target' = 'Charge target alerts'
    'notification_channel_charge_target_description' = 'Notifies when charging reaches the selected target'
    'now_subtitle' = 'Live battery guidance and target timing'
    'now_title' = 'Now'
    'open_app_while_charging' = 'Open the app while charging'
    'recommended_label' = 'Recommended'
    'set_alarm_target' = 'Set alarm for %1$s'
    'session_assessment_incomplete' = 'Incomplete session'
    'session_assessment_useful' = 'Useful session'
    'session_assessment_weak' = 'Weak session'
    'session_delta_label' = 'From %1$s%% to %2$s%%'
    'session_headline_delta' = 'Gained %1$s'
    'session_label' = 'Session'
    'session_quality_active' = 'Active'
    'session_quality_incomplete' = 'Incomplete'
    'session_quality_useful' = 'Useful'
    'session_quality_weak' = 'Weak'
    'session_started' = 'Started'
    'session_temperature' = 'Max %1$s°C'
    'session_temperature_with_average' = 'Max %1$s°C, avg %2$s°C'
    'session_time_above_85' = '%1$s above 85%%'
    'session_time_above_90' = '%1$s above 90%%'
    'session_time_range' = '%1$s to %2$s'
    'source_label' = 'Source'
    'state_label' = 'State'
    'stored_only_label' = 'Stored only'
    'stress_card_inferred' = 'Stress card is inferred'
    'stress_excellent' = 'Excellent'
    'stress_good' = 'Good'
    'stress_high_stress' = 'High stress'
    'stress_normal' = 'Normal'
    'stress_severe_stress' = 'Severe stress'
    'thermal_band_35_40' = '35-40°C'
    'thermal_band_40_43' = '40-43°C'
    'thermal_band_43_45' = '43-45°C'
    'thermal_band_over_45' = 'Above 45°C'
    'thermal_band_under_35' = 'Under 35°C'
    'thermal_model_desc' = 'Heat is one of the strongest known battery-aging factors. The app raises stress when the battery is warm or hot, especially while charging or near full.'
    'thermal_model_note' = 'These are practical guidance bands, not absolute chemistry constants. Actual degradation depends on battery chemistry, age, charge level, current, and duration.'
    'thermal_model_title' = 'Thermal Model'
    'capacity_model_desc' = 'Battery health is estimated from useful charge sessions. Large uninterrupted charges are more useful than short, noisy ones.'
    'capacity_model_title' = 'Capacity Model'
    'capacity_model_voltage_note' = 'Voltage may be logged, but voltage alone is not treated as a reliable capacity source.'
    'experimental_metrics_desc' = 'Experimental metrics are disabled by default. They are useful for research, but not for precise battery-health claims.'
    'experimental_metrics_note' = 'This cannot be measured precisely from a single session.'
    'experimental_metrics_title' = 'Experimental Metrics'
    'target_label' = 'Target'
    'target_reached_body' = 'Battery is at %1$s%%'
    'target_reached_snackbar' = 'Target reached at %1$s%%'
    'target_reached_title' = 'Target reached'
    'target_timing_wait' = 'Wait for enough charging data'
    'temperature_label' = 'Temperature'
    'time_to_target_label' = 'Time to Target'
    'theme_toggle' = 'Toggle theme'
    'unit_label' = 'Unit'
    'useful_label' = 'Useful'
    'voltage_label' = 'Voltage'
    'waiting_for_battery_data' = 'Waiting for battery data'
}

$repoRoot = Split-Path $PSScriptRoot -Parent
$sourceRoot = Join-Path $repoRoot 'app/src/main/java'
$outputDir = Join-Path $repoRoot 'app/src/main/assets/i18n'

$keys = [System.Collections.Generic.HashSet[string]]::new()

Get-ChildItem $sourceRoot -Recurse -File -Filter *.kt | ForEach-Object {
    $content = Get-Content $_.FullName -Raw
    foreach ($match in [regex]::Matches($content, 'T\("([^"]+)"')) {
        $key = $match.Groups[1].Value
        if ($key.Contains('$')) {
            continue
        }
        [void]$keys.Add($key)
    }
}
$dynamicKeys = @(
    'charging_source_ac',
    'charging_source_dock',
    'charging_source_unknown',
    'charging_source_usb',
    'charging_source_wireless',
    'charging_state_charging',
    'charging_state_discharging',
    'charging_state_full',
    'charging_state_unknown',
    'confidence_high',
    'confidence_low',
    'confidence_medium',
    'evidence_estimated',
    'evidence_experimental',
    'evidence_inferred',
    'evidence_measured',
    'session_source_ac',
    'session_source_dock',
    'session_source_unknown',
    'session_source_usb',
    'session_source_wireless',
    'stress_excellent',
    'stress_good',
    'stress_high_stress',
    'stress_normal',
    'stress_severe_stress'
)

foreach ($key in $dynamicKeys) {
    [void]$keys.Add($key)
}

$tree = [ordered]@{}

foreach ($key in $keys | Sort-Object) {
    $value = if ($overrides.Contains($key)) { $overrides[$key] } else { Get-HumanizedValue $key }
    Add-TreeEntry -Node $tree -Parts ($key -split '_') -Value $value
}

$json = $tree | ConvertTo-Json -Depth 32
New-Item -ItemType Directory -Force -Path $outputDir | Out-Null
Set-Content -Path (Join-Path $outputDir 'en.json') -Value $json -Encoding utf8
