package com.example.testapp.presentation.screen.settings.ui

data class ScoreRangeStepperDescriptions(
    val min: String,
    val max: String
)

fun resolveOptionalCountStepperDescription(
    count: Int,
    allLabel: String,
    countedLabel: String
): String = if (count == 0) allLabel else countedLabel

fun resolveScoreRangeStepperDescriptions(
    rangeLabel: String,
    minValue: Int,
    maxValue: Int,
    minTemplate: String,
    maxTemplate: String
): ScoreRangeStepperDescriptions = ScoreRangeStepperDescriptions(
    min = minTemplate.format(rangeLabel, minValue),
    max = maxTemplate.format(rangeLabel, maxValue)
)
