package com.example.testapp.presentation.navigation

import androidx.compose.runtime.Composable
import com.example.testapp.presentation.screen.ai.ExplanationScreen

@Composable
fun ExplanationRoute(
    text: String,
    onBack: () -> Unit,
) {
    ExplanationScreen(text = text, onBack = onBack)
}
