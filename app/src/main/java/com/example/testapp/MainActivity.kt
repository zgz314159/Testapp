package com.example.testapp


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.testapp.presentation.screen.SettingsViewModel
import com.example.testapp.ui.theme.MyApplicationTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import com.example.testapp.presentation.component.FontStyleProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.example.testapp.presentation.component.LocalFontSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.sp


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val fontSize by settingsViewModel.fontSize.collectAsState()
            val fontStyle by settingsViewModel.fontStyle.collectAsState()
            val darkTheme by settingsViewModel.darkTheme.collectAsState()
            val context = LocalContext.current
            // 启动时加载字体设置
            LaunchedEffect(Unit) {
                settingsViewModel.loadFontSettings(context)
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
