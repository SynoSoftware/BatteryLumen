package com.synosoftware.battery.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text as AppText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.lineSeries
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.Fill
import com.synosoftware.battery.R
import com.synosoftware.battery.i18n.T
import com.synosoftware.battery.i18n.asString
import com.synosoftware.battery.ui.components.EvidenceBadge
import com.synosoftware.battery.ui.components.IconBadge
import com.synosoftware.battery.ui.components.MetricTile
import com.synosoftware.battery.ui.components.SectionHeader
import com.synosoftware.battery.ui.model.BatteryUiState
import com.synosoftware.battery.ui.model.HealthTrendPointUi
import kotlin.math.roundToInt

@Composable
fun HealthScreen(
    state: BatteryUiState,
    contentPadding: PaddingValues,
) {
    val points = state.healthEvolution.points
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            SectionHeader(
                title = T("health_page_title").asString(),
                subtitle = T("health_subtitle").asString(),
            )
        }

        if (state.usefulSessionCount < 5) {
            item {
                HealthStatusCard(
                    message = state.healthMessage.asString(),
                )
            }
        }

        if (points.isEmpty()) {
            item {
                HealthEmptyStateCard()
            }
        } else {
            item {
                HealthEvolutionChartCard(points = points)
            }

            item {
                LatestComparisonRow(points = points)
            }
        }

        item { Spacer(Modifier.height(12.dp)) }
    }
}

@Composable
private fun HealthStatusCard(
    message: String,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconBadge(
                resId = R.drawable.lucide_info,
                contentDescription = null,
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                AppText(
                    text = T("health_status_title").asString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                AppText(
                    text = message,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun HealthEmptyStateCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f)),
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            IconBadge(
                resId = R.drawable.lucide_heart,
                contentDescription = null,
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AppText(
                    text = T("health_evolution_empty_title").asString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                AppText(
                    text = T("health_evolution_empty_body").asString(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun HealthEvolutionChartCard(
    points: List<HealthTrendPointUi>,
) {
    val measuredColor = MaterialTheme.colorScheme.primary
    val tempColor = MaterialTheme.colorScheme.tertiary
    val percentColor = MaterialTheme.colorScheme.secondary
    val modelProducer = remember { CartesianChartModelProducer() }
    val labels = remember(points) { points.map { it.label } }
    val measuredSeries = remember(points) { points.map { it.measuredPercent } }
    val tempSeries = remember(points) { points.map { it.temperatureEstimatePercent } }
    val percentSeries = remember(points) { points.map { it.percentOnlyEstimatePercent } }
    val lineLayer = remember(measuredColor, tempColor, percentColor) {
        LineCartesianLayer(
            lineProvider = LineCartesianLayer.LineProvider.series(
                LineCartesianLayer.Line(
                    fill = LineCartesianLayer.LineFill.single(Fill(measuredColor)),
                    stroke = LineCartesianLayer.LineStroke.Continuous(thickness = 3.dp, cap = StrokeCap.Round),
                    areaFill = LineCartesianLayer.AreaFill.single(Fill(measuredColor.copy(alpha = 0.12f))),
                ),
                LineCartesianLayer.Line(
                    fill = LineCartesianLayer.LineFill.single(Fill(tempColor)),
                    stroke = LineCartesianLayer.LineStroke.Continuous(thickness = 2.5.dp, cap = StrokeCap.Round),
                ),
                LineCartesianLayer.Line(
                    fill = LineCartesianLayer.LineFill.single(Fill(percentColor)),
                    stroke = LineCartesianLayer.LineStroke.Continuous(thickness = 2.5.dp, cap = StrokeCap.Round),
                ),
            ),
        )
    }
    val chart = rememberCartesianChart(
        lineLayer,
        startAxis = VerticalAxis.rememberStart(
            valueFormatter = CartesianValueFormatter { _, y, _ ->
                "${y.roundToInt()}%"
            },
        ),
        bottomAxis = HorizontalAxis.rememberBottom(
            valueFormatter = CartesianValueFormatter { _, x, _ ->
                labels.getOrNull(x.roundToInt()).orEmpty()
            },
        ),
    )

    LaunchedEffect(measuredSeries, tempSeries, percentSeries) {
        modelProducer.runTransaction {
            lineSeries {
                series(y = measuredSeries)
                series(y = tempSeries)
                series(y = percentSeries)
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
                        text = T("health_history_title").asString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    AppText(
                        text = T("health_history_subtitle").asString(),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                EvidenceBadge(text = T("evidence_estimated").asString())
            }

            CartesianChartHost(
                chart = chart,
                modelProducer = modelProducer,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
            )

            EvolutionLegendRow(
                measuredColor = measuredColor,
                tempColor = tempColor,
                percentColor = percentColor,
            )

            AppText(
                text = T("health_evolution_note").asString(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun EvolutionLegendRow(
    measuredColor: androidx.compose.ui.graphics.Color,
    tempColor: androidx.compose.ui.graphics.Color,
    percentColor: androidx.compose.ui.graphics.Color,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        EvolutionLegendItem(
            color = measuredColor,
            label = T("health_evolution_measured").asString(),
            modifier = Modifier.weight(1f),
        )
        EvolutionLegendItem(
            color = tempColor,
            label = T("health_evolution_temp").asString(),
            modifier = Modifier.weight(1f),
        )
        EvolutionLegendItem(
            color = percentColor,
            label = T("health_evolution_percent").asString(),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun EvolutionLegendItem(
    color: androidx.compose.ui.graphics.Color,
    label: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Spacer(
                modifier = Modifier
                    .size(10.dp)
                    .background(color, MaterialTheme.shapes.small),
            )
            AppText(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun LatestComparisonRow(
    points: List<HealthTrendPointUi>,
) {
    val latest = points.last()
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        AppText(
            text = T("health_evolution_latest").asString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            MetricTile(
                modifier = Modifier.weight(1f),
                iconRes = R.drawable.lucide_battery_full,
                title = T("health_evolution_measured").asString(),
                value = T("value_percent", latest.measuredPercent.roundToInt()).asString(),
                evidence = T("evidence_measured").asString(),
            )
            MetricTile(
                modifier = Modifier.weight(1f),
                iconRes = R.drawable.lucide_thermometer,
                title = T("health_evolution_temp").asString(),
                value = T("value_percent", latest.temperatureEstimatePercent.roundToInt()).asString(),
                evidence = T("evidence_estimated").asString(),
            )
            MetricTile(
                modifier = Modifier.weight(1f),
                iconRes = R.drawable.lucide_history,
                title = T("health_evolution_percent").asString(),
                value = T("value_percent", latest.percentOnlyEstimatePercent.roundToInt()).asString(),
                evidence = T("evidence_estimated").asString(),
            )
        }
    }
}
