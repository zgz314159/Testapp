package com.example.testapp.presentation.screen.home

object HomeAdaptiveModeEligibilityPipeline {
    fun isEligible(fileName: String): Boolean =
        fileName.endsWith(".sqlite", ignoreCase = true) ||
            fileName.endsWith(".db", ignoreCase = true)
}
