package com.example.testapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.testapp.presentation.screen.settings.SettingsViewModel
import com.example.testapp.ui.theme.MyApplicationTheme
import com.example.testapp.uicommon.component.FontStyleProvider
import com.example.testapp.uicommon.component.LocalFontSize
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val fontSize by settingsViewModel.fontSize.collectAsState()
            val fontStyle by settingsViewModel.fontStyle.collectAsState()
            val darkTheme by settingsViewModel.darkTheme.collectAsState()
            val context = LocalContext.current
            // 启动时加载字体设置
            LaunchedEffect(Unit) {
                settingsViewModel.loadFontSettings()
            }
            MyApplicationTheme(darkTheme = darkTheme, dynamicColor = false) {
                CompositionLocalProvider(
                    LocalFontSize provides fontSize.sp
                ) {
                    FontStyleProvider(fontSize, fontStyle) {
                        com.example.testapp.presentation.navigation.AppNavHost(settingsViewModel = settingsViewModel)
                    }
                }
            }
        }
    }
}

