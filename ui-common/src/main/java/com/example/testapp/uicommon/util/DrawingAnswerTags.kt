package com.example.testapp.uicommon.util

private val DRAWING_IMAGES_REGEX = Regex("\\[DRAWING_IMAGES:([^\\]]+)]")
private val DRAWING_TABLES_REGEX = Regex("\\[DRAWING_TABLE:([^\\]]+)]")

fun stripDrawingTags(answer: String): String =
    answer
        .replace(DRAWING_IMAGES_REGEX, "")
        .replace(DRAWING_TABLES_REGEX, "")
        .trim()
