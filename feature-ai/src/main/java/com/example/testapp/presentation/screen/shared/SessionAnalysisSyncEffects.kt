package com.example.testapp.presentation.screen.shared

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.example.testapp.di.SessionAnalysisLoaderEntryPoint
import dagger.hilt.android.EntryPointAccessors

@Composable
fun rememberSessionAnalysisLoader(): SessionAnalysisLoader {
    val context = LocalContext.current
    return remember(context) {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            SessionAnalysisLoaderEntryPoint::class.java,
        ).sessionAnalysisLoader()
    }
}
