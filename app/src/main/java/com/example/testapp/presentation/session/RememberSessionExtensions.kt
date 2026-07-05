package com.example.testapp.presentation.session

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.example.testapp.di.SessionExtensionsEntryPoint
import com.example.testapp.domain.session.SessionExtension
import dagger.hilt.android.EntryPointAccessors

@Composable
fun rememberSessionExtensions(): List<SessionExtension> {
    val context = LocalContext.current
    return remember(context) {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            SessionExtensionsEntryPoint::class.java,
        ).sessionExtensions().toList()
    }
}
