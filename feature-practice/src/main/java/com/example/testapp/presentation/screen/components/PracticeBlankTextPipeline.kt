package com.example.testapp.presentation.screen.components

internal val PracticeBlankRegex = Regex("_{2,}|（\\s*）|\\(\\s*\\)|【\\s*】|\\[\\s*\\]")
internal val PracticeTextResponseBlankRegex = PracticeBlankRegex
internal val PracticeRedundantBlankSeparatorRegex =
    Regex("[、,，;；:：]\\s*(?=(?:_{2,}|（\\s*）|\\(\\s*\\)|【\\s*】|\\[\\s*\\])|$)")
