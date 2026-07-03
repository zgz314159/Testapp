package com.example.testapp.uicommon.component.stepper

object StepperScoreRangePipeline {
    fun normalize(
        min: Int,
        max: Int,
        floor: Int = 1,
        ceiling: Int = 10
    ): Pair<Int, Int> {
        val clampedMin = min.coerceIn(floor, ceiling)
        val clampedMax = max.coerceIn(clampedMin, ceiling)
        return clampedMin to clampedMax
    }

    fun withMin(currentMin: Int, currentMax: Int, newMin: Int, floor: Int = 1, ceiling: Int = 10): Pair<Int, Int> {
        val min = newMin.coerceIn(floor, ceiling)
        val max = currentMax.coerceAtLeast(min).coerceIn(floor, ceiling)
        return normalize(min, max, floor, ceiling)
    }

    fun withMax(currentMin: Int, currentMax: Int, newMax: Int, floor: Int = 1, ceiling: Int = 10): Pair<Int, Int> {
        val min = currentMin.coerceIn(floor, ceiling)
        val max = newMax.coerceIn(min, ceiling)
        return normalize(min, max, floor, ceiling)
    }
}
