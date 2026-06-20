package com.example.testapp.presentation.screen.home.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import com.example.testapp.R
import com.example.testapp.uicommon.component.LocalFontFamily
import com.example.testapp.uicommon.component.LocalFontSize
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(
    scrollBehavior: TopAppBarScrollBehavior,
    drawerState: DrawerState,
    onSettings: () -> Unit
) {
    val scope = rememberCoroutineScope()
    TopAppBar(
        title = {
            Text(
                stringResource(R.string.home_title),
                fontSize = LocalFontSize.current,
                fontFamily = LocalFontFamily.current
            )
        },
        navigationIcon = {
            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                Icon(Icons.Filled.Menu, contentDescription = "打开题库抽屉")
            }
        },
        actions = {
            IconButton(onClick = onSettings) {
                Icon(
                    Icons.Filled.Settings,
                    contentDescription = stringResource(R.string.settings_font)
                )
            }
        },
        scrollBehavior = scrollBehavior
    )
}

