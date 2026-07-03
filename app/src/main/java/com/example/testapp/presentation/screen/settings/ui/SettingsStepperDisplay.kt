package com.example.testapp.presentation.screen.settings.ui

fun formatCountStepperDisplay(value: Int): String =
    if (value == 0) "∞" else value.toString()

fun formatBlankCountDisplay(value: Int): String =
    if (value == 0) "∞" else value.toString()
