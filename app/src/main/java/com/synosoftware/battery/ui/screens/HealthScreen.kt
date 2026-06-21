package com.synosoftware.battery.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text as AppText
import com.synosoftware.battery.BuildConfig
import com.synosoftware.battery.R
import com.synosoftware.battery.domain.EvidenceGrade
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.lineSeries
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.Fill
import com.synosoftware.battery.i18n.T
import com.synosoftware.battery.i18n.asString
import com.synosoftware.battery.i18n.confidenceText
import com.synosoftware.battery.i18n.healthApproxPercentText
import com.synosoftware.battery.i18n.healthCapacityRangeText
import com.synosoftware.battery.i18n.healthPercentRangeText
import com.synosoftware.battery.i18n.healthTrendText
import com.synosoftware.battery.ui.components.EvidenceBadge
import com.synosoftware.battery.ui.components.IconBadge
import com.synosoftware.battery.ui.components.PlainBadge
import com.synosoftware.battery.ui.model.BatteryHealthEstimateUi
import com.synosoftware.battery.ui.model.BatteryUiState
import com.synosoftware.battery.ui.model.MIN_USEFUL_SESSION_COUNT
import com.synosoftware.battery.ui.model.HealthTrendPointUi
import kotlin.math.roundToInt

@Composable
fun HealthScreen(
    state: BatteryUiState,
    onSeedDemoData: () -> Unit,
    contentPadding: PaddingValues,
) {
    val points = state.healthEvolution.points
    val estimate = state.healthEstimate
    val hasUsefulData = estimate.hasEstimate && points.isNotEmpty()
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            HealthStatusHeroCard(
                estimate = estimate,
                designCapacityMah = state.designCapacityMah,
            )
        }

        if (BuildConfig.DEBUG) {
            item {
                DebugSeedDataCard(onSeedDemoData = onSeedDemoData)
            }
        }

        if (hasUsefulData) {
            item {
                HealthTrendChartCard(
                    points = points,
                    estimate = estimate,
                )
            }
        }

        item { Spacer(Modifier.height(12.dp)) }
    }
}

@Composable
private fun HealthStatusHeroCard(
    estimate: BatteryHealthEstimateUi,
    designCapacityMah: Int?,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)),
        tonalElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconBadge(
                    resId = R.drawable.lucide_heart,
                    contentDescription = null,
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    AppText(
                        text = if (estimate.hasEstimate) {
                            T(R.string.health_current_title)
                        } else {
                            T(R.string.health_insufficient_title)
                        },
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    if (estimate.hasEstimate) {
                        AppText(
                            text = if (estimate.hasHealthPercent) {
                                T(R.string.health_estimated_health)
                            } else {
                                T(R.string.health_estimated_capacity)
                            },
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        AppText(
                            text = T(R.string.health_insufficient_body, MIN_USEFUL_SESSION_COUNT),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            if (estimate.hasEstimate) {
                val capacityMah = requireNotNull(estimate.estimatedCapacityMah)
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (estimate.hasHealthPercent) {
                        val healthPercent = requireNotNull(estimate.healthPercent)
                        AppText(
                            text = healthApproxPercentText(healthPercent).asString(),
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                        estimate.healthRangePercent?.let { range ->
                            AppText(
                                text = healthPercentRangeText(range).asString(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        AppText(
                            text = T(
                                R.string.health_capacity_reference,
                                designCapacityMah?.takeIf { it > 0 }?.let { T(R.string.value_mah, it) } ?: T(R.string.value_na),
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        AppText(
                            text = T(R.string.value_mah, capacityMah),
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.SemiBold,
                        )

                        estimate.likelyRangeMah?.let { range ->
                            AppText(
                                text = healthCapacityRangeText(range).asString(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PlainBadge(text = confidenceText(estimate.confidence).asString())
                        PlainBadge(text = healthTrendText(estimate.trend).asString())
                        EvidenceBadge(grade = EvidenceGrade.ESTIMATED)
                    }

                    AppText(
                        text = T(R.string.health_based_on_sessions, estimate.usefulSessionCount),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                LinearProgressIndicator(
                    progress = { estimate.usefulSessionCount.coerceAtMost(MIN_USEFUL_SESSION_COUNT).toFloat() / MIN_USEFUL_SESSION_COUNT.toFloat() },
                    modifier = Modifier.fillMaxWidth(),
                )
                AppText(
                    text = T(R.string.health_sessions_collected, estimate.usefulSessionCount, MIN_USEFUL_SESSION_COUNT),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                AppText(
                    text = T(R.string.health_collecting_data),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun HealthTrendChartCard(
    points: List<HealthTrendPointUi>,
    estimate: BatteryHealthEstimateUi,
) {
    val capacityColor = MaterialTheme.colorScheme.primary
    val context = LocalContext.current
    val modelProducer = remember { CartesianChartModelProducer() }
    val labels = remember(points) { points.map { it.label } }
    val capacitySeries = remember(points) { points.map { it.estimatedCapacityMah } }
    val lineLayer = remember(capacityColor) {
        LineCartesianLayer(
            lineProvider = LineCartesianLayer.LineProvider.series(
                LineCartesianLayer.Line(
                    fill = LineCartesianLayer.LineFill.single(Fill(capacityColor)),
                    stroke = LineCartesianLayer.LineStroke.Continuous(thickness = 3.dp, cap = StrokeCap.Round),
                    areaFill = LineCartesianLayer.AreaFill.single(Fill(capacityColor.copy(alpha = 0.12f))),
                ),
            ),
        )
    }
    val chart = rememberCartesianChart(
        lineLayer,
        startAxis = VerticalAxis.rememberStart(
            valueFormatter = CartesianValueFormatter { _, y, _ ->
                context.T(R.string.value_mah, y.roundToInt())
            },
        ),
        bottomAxis = HorizontalAxis.rememberBottom(
            valueFormatter = CartesianValueFormatter { _, x, _ ->
                labels.getOrNull(x.roundToInt()).orEmpty()
            },
        ),
    )

    LaunchedEffect(capacitySeries) {
        modelProducer.runTransaction {
            lineSeries {
                series(y = capacitySeries)
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f)),
        tonalElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    AppText(
                        text = T(R.string.health_trend_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    AppText(
                        text = T(R.string.health_trend_subtitle),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    PlainBadge(text = confidenceText(estimate.confidence).asString())
                    AppText(
                        text = T(R.string.health_trend_confidence, estimate.usefulSessionCount),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            CartesianChartHost(
                chart = chart,
                modelProducer = modelProducer,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
            )

            AppText(
                text = T(R.string.health_trend_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun DebugSeedDataCard(
    onSeedDemoData: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
        tonalElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                AppText(
                    text = T(R.string.health_debug_seed_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                AppText(
                    text = T(R.string.health_debug_seed_body),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Button(onClick = onSeedDemoData) {
                AppText(T(R.string.health_debug_seed_action))
            }
        }
    }
}
