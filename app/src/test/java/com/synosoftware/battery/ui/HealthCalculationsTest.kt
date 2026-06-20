package com.synosoftware.battery.ui

import com.synosoftware.battery.domain.CapacityPoint
import com.synosoftware.battery.domain.CapacityTrend
import com.synosoftware.battery.domain.DataQuality
import com.synosoftware.battery.domain.estimateCapacity
import org.junit.Assert.assertEquals
import org.junit.Test

class HealthCalculationsTest {
    @Test
    fun estimateCapacityUsesChronologicalSamplesAndUiMapperUsesDesignCapacityForPercent() {
        val estimate = estimateCapacity(
            listOf(
                point(1_000L, 4_200.0),
                point(2_000L, 4_100.0),
                point(3_000L, 4_000.0),
                point(4_000L, 3_900.0),
                point(5_000L, 3_800.0),
            ),
        )

        val ui = estimate.toUi(5_000)

        assertEquals(CapacityTrend.DECLINING, estimate.trend)
        assertEquals(4_000, estimate.estimatedCapacityMah)
        assertEquals(80, ui.healthPercent)
        assertEquals(78..82, ui.healthRangePercent)
    }

    private fun point(timestampMs: Long, capacityMah: Double): CapacityPoint {
        return CapacityPoint(
            timestampMs = timestampMs,
            estimatedFullCapacityMah = capacityMah,
            quality = DataQuality.USEFUL,
        )
    }
}
