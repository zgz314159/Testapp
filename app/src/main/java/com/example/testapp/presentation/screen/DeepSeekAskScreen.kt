package com.example.testapp.presentation.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.testapp.presentation.component.LocalFontFamily
import com.example.testapp.presentation.component.LocalFontSize

@Composable
fun DeepSeekAskScreen(
    text: String,
    navController: NavController? = null,
    viewModel: DeepSeekAskViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val result by viewModel.result.collectAsState()

    LaunchedEffect(text) {
        viewModel.ask(text)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = text,
            fontSize = LocalFontSize.current,
            fontFamily = LocalFontFamily.current
        )
        Text(
            text = result,
            modifier = Modifier.padding(top = 8.dp),
            fontSize = LocalFontSize.current,
            fontFamily = LocalFontFamily.current
        )
    }
}