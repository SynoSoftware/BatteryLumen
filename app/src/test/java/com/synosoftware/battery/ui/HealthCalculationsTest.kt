package com.synosoftware.battery.ui

import com.synosoftware.battery.ui.model.HealthTrendPointUi
import com.synosoftware.battery.ui.model.HealthTrendState
import org.junit.Assert.assertEquals
import org.junit.Test

class HealthCalculationsTest {
    @Test
    fun buildHealthEstimateUsesChronologicalSamplesForTrendAndDesignCapacityForPercent() {
        val estimate = buildHealthEstimate(
            points = listOf(
                HealthTrendPointUi(label = "Jan 1", estimatedCapacityMah = 4200f),
                HealthTrendPointUi(label = "Jan 8", estimatedCapacityMah = 4100f),
                HealthTrendPointUi(label = "Jan 15", estimatedCapacityMah = 4000f),
                HealthTrendPointUi(label = "Jan 22", estimatedCapacityMah = 3900f),
                HealthTrendPointUi(label = "Jan 29", estimatedCapacityMah = 3800f),
            ),
            designCapacityMah = 5000,
        )

        assertEquals(HealthTrendState.DECLINING, estimate.trend)
        assertEquals(4000, estimate.estimatedCapacityMah)
        assertEquals(80, estimate.healthPercent)
        assertEquals(78..82, estimate.healthRangePercent)
    }
}
