package com.example.testapp.presentation.screen.home.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.example.testapp.R
import com.example.testapp.data.datastore.FontSettingsDataStore
import com.example.testapp.uicommon.component.LocalFontFamily
import com.example.testapp.uicommon.component.LocalFontSize
import kotlinx.coroutines.launch

@Composable
fun HomeBottomBar(
    bottomNavIndex: Int,
    onNavChange: (Int) -> Unit,
    onWrongBook: () -> Unit,
    onFavoriteBook: () -> Unit,
    onHistory: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    fun selectTab(index: Int) {
        onNavChange(index)
        scope.launch { FontSettingsDataStore.setLastSelectedNav(context, index) }
    }

    NavigationBar {
        NavigationBarItem(
            selected = bottomNavIndex == 0,
            onClick = {
                selectTab(0)
                onWrongBook()
            },
            icon = {
                Icon(Icons.Filled.Warning, contentDescription = stringResource(R.string.nav_wrongbook))
            },
            label = {
                Text(
                    stringResource(R.string.nav_wrongbook),
                    fontSize = LocalFontSize.current,
                    fontFamily = LocalFontFamily.current
                )
            }
        )
        NavigationBarItem(
            selected = bottomNavIndex == 1,
            onClick = {
                selectTab(1)
                onFavoriteBook()
            },
            icon = {
                Icon(Icons.Filled.Favorite, contentDescription = stringResource(R.string.nav_favorite))
            },
            label = {
                Text(
                    stringResource(R.string.nav_favorite),
                    fontSize = LocalFontSize.current,
                    fontFamily = LocalFontFamily.current
                )
            }
        )
        NavigationBarItem(
            selected = bottomNavIndex == 2,
            onClick = {
                selectTab(2)
                onHistory()
            },
            icon = {
                Icon(Icons.AutoMirrored.Filled.FactCheck, contentDescription = stringResource(R.string.nav_records))
            },
            label = {
                Text(
                    stringResource(R.string.nav_records),
                    fontSize = LocalFontSize.current,
                    fontFamily = LocalFontFamily.current
                )
            }
        )
        NavigationBarItem(
            selected = bottomNavIndex == 3,
            onClick = { selectTab(3) },
            icon = {
                Icon(Icons.Filled.SwapHoriz, contentDescription = stringResource(R.string.nav_mode))
            },
            label = {
                Text(
                    stringResource(R.string.nav_mode),
                    fontSize = LocalFontSize.current,
                    fontFamily = LocalFontFamily.current
                )
            }
        )
    }
}

