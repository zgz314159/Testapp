package com.example.testapp.presentation.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.testapp.presentation.component.LocalFontFamily
import com.example.testapp.presentation.component.LocalFontSize
import com.example.testapp.data.datastore.FontSettingsDataStore
import kotlinx.coroutines.launch
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun DeepSeekScreen(
    text: String,
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val globalFontSize by settingsViewModel.fontSize.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val storedSize by FontSettingsDataStore
        .getDeepSeekFontSize(context, Float.NaN)
        .collectAsState(initial = Float.NaN)
    var screenFontSize by remember { mutableStateOf(globalFontSize) }
    var fontLoaded by remember { mutableStateOf(false) }
    LaunchedEffect(storedSize) {
        if (!storedSize.isNaN()) {
            screenFontSize = storedSize
            fontLoaded = true
        }
    }
    LaunchedEffect(screenFontSize, fontLoaded) {
        if (fontLoaded) {
            FontSettingsDataStore.setDeepSeekFontSize(context, screenFontSize)
        }
    }
    var menuExpanded by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxSize()) {
        IconButton(onClick = { menuExpanded = true }, modifier = Modifier.align(Alignment.TopEnd)) {
            Icon(Icons.Filled.MoreVert, contentDescription = "设置")
        }
        DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
            DropdownMenuItem(text = { Text("放大字体") }, onClick = {
                screenFontSize = (screenFontSize + 2).coerceAtMost(32f)
                coroutineScope.launch { FontSettingsDataStore.setDeepSeekFontSize(context, screenFontSize) }
                menuExpanded = false
            })
            DropdownMenuItem(text = { Text("缩小字体") }, onClick = {
                screenFontSize = (screenFontSize - 2).coerceAtLeast(14f)
                coroutineScope.launch { FontSettingsDataStore.setDeepSeekFontSize(context, screenFontSize) }
                menuExpanded = false
            })
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            contentAlignment = Alignment.TopStart
        ) {
            Text(
                text = text,
                fontSize = screenFontSize.sp,
                fontFamily = LocalFontFamily.current
            )
        }
    }
}