package com.example.testapp.uicommon.component.stepper

object StepperInputParsePipeline {
    fun clamp(value: Int, minValue: Int, maxValue: Int): Int =
        value.coerceIn(minValue, maxValue)

    fun parseDigits(raw: String, minValue: Int, maxValue: Int): Int? {
        val digits = raw.filter { it.isDigit() }
        if (digits.isEmpty()) return null
        return digits.toIntOrNull()?.let { clamp(it, minValue, maxValue) }
    }
}
