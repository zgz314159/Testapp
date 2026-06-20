package com.example.testapp.presentation.screen.ai

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.testapp.R
import com.example.testapp.data.datastore.FontSettingsDataStore
import com.example.testapp.presentation.screen.settings.SettingsViewModel
import com.example.testapp.uicommon.component.LocalFontFamily
import kotlinx.coroutines.launch

@Composable
fun ExplanationScreen(
    text: String,
    navController: NavController? = null,
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val globalFontSize by settingsViewModel.fontSize.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val storedSize by FontSettingsDataStore
        .getPracticeFontSize(context, Float.NaN)
        .collectAsState(initial = Float.NaN)
    val storedLineSpacing by FontSettingsDataStore
        .getPracticeLineSpacing(context, Float.NaN)
        .collectAsState(initial = Float.NaN)
    val storedLetterSpacing by FontSettingsDataStore
        .getPracticeLetterSpacing(context, Float.NaN)
        .collectAsState(initial = Float.NaN)
    var screenFontSize by remember { mutableStateOf(globalFontSize) }
    var screenLineSpacing by remember { mutableStateOf(1.3f) }
    var screenLetterSpacing by remember { mutableStateOf(0f) }
    var fontLoaded by remember { mutableStateOf(false) }
    LaunchedEffect(storedSize) {
        if (!storedSize.isNaN()) {
            screenFontSize = storedSize
            fontLoaded = true
        }
    }
    LaunchedEffect(storedLineSpacing) {
        if (!storedLineSpacing.isNaN()) screenLineSpacing = storedLineSpacing
    }
    LaunchedEffect(storedLetterSpacing) {
        if (!storedLetterSpacing.isNaN()) screenLetterSpacing = storedLetterSpacing
    }
    LaunchedEffect(screenFontSize, fontLoaded) {
        if (fontLoaded) {
            FontSettingsDataStore.setPracticeFontSize(context, screenFontSize)
        }
    }
    var menuExpanded by remember { mutableStateOf(false) }
    val increaseFontText = stringResource(R.string.increase_font)
    val decreaseFontText = stringResource(R.string.decrease_font)
    val settingsText = stringResource(R.string.settings)

    BackHandler {
        navController?.popBackStack()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = text,
                style = TextStyle(
                    fontSize = screenFontSize.sp,
                    fontFamily = LocalFontFamily.current,
                    lineHeight = (screenFontSize * screenLineSpacing).sp,
                    letterSpacing = screenLetterSpacing.sp
                )
            )
        }

        Box(modifier = Modifier.align(Alignment.TopEnd)) {
            IconButton(onClick = { menuExpanded = true }) {
                Icon(Icons.Filled.MoreVert, contentDescription = settingsText)
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                DropdownMenuItem(text = { Text(increaseFontText) }, onClick = {
                    screenFontSize = (screenFontSize + 2).coerceAtMost(32f)
                    coroutineScope.launch {
                        FontSettingsDataStore.setPracticeFontSize(context, screenFontSize)
                    }
                    menuExpanded = false
                })
                DropdownMenuItem(text = { Text(decreaseFontText) }, onClick = {
                    screenFontSize = (screenFontSize - 2).coerceAtLeast(14f)
                    coroutineScope.launch {
                        FontSettingsDataStore.setPracticeFontSize(context, screenFontSize)
                    }
                    menuExpanded = false
                })
            }
        }
    }
}