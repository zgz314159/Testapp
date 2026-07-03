package com.example.testapp.uicommon.design

import kotlin.math.roundToInt

object QuestionTypographyBounds {
    const val FONT_MIN = 12
    const val FONT_MAX = 42
    const val FONT_STEP = 2
    const val LINE_SPACING_MIN_TENTHS = 10
    const val LINE_SPACING_MAX_TENTHS = 22
    const val LETTER_SPACING_MAX_TENTHS = 20
}

fun fontSizeToStep(size: Float): Int {
    val step = ((size - QuestionTypographyBounds.FONT_MIN) / QuestionTypographyBounds.FONT_STEP)
        .roundToInt()
    val maxStep = (QuestionTypographyBounds.FONT_MAX - QuestionTypographyBounds.FONT_MIN) /
        QuestionTypographyBounds.FONT_STEP
    return step.coerceIn(0, maxStep)
}

fun stepToFontSize(step: Int): Float {
    val maxStep = (QuestionTypographyBounds.FONT_MAX - QuestionTypographyBounds.FONT_MIN) /
        QuestionTypographyBounds.FONT_STEP
    val clamped = step.coerceIn(0, maxStep)
    return QuestionTypographyBounds.FONT_MIN + clamped * QuestionTypographyBounds.FONT_STEP.toFloat()
}

fun lineSpacingToTenths(spacing: Float): Int {
    return (spacing * 10f).roundToInt().coerceIn(
        QuestionTypographyBounds.LINE_SPACING_MIN_TENTHS,
        QuestionTypographyBounds.LINE_SPACING_MAX_TENTHS
    )
}

fun tenthsToLineSpacing(tenths: Int): Float {
    return tenths.coerceIn(
        QuestionTypographyBounds.LINE_SPACING_MIN_TENTHS,
        QuestionTypographyBounds.LINE_SPACING_MAX_TENTHS
    ) / 10f
}

fun letterSpacingToTenths(spacing: Float): Int {
    return (spacing * 10f).roundToInt().coerceIn(0, QuestionTypographyBounds.LETTER_SPACING_MAX_TENTHS)
}

fun tenthsToLetterSpacing(tenths: Int): Float {
    return tenths.coerceIn(0, QuestionTypographyBounds.LETTER_SPACING_MAX_TENTHS) / 10f
}

fun formatLineSpacingDisplay(spacing: Float): String = "${String.format("%.1f", spacing)}x"

fun formatLetterSpacingDisplay(spacing: Float): String = String.format("%.1f", spacing)
