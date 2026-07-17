package com.example.testapp.presentation.screen.home.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.testapp.data.datastore.FontSettingsDataStore
import com.example.testapp.feature.practice.R
import com.example.testapp.presentation.screen.home.design.HomeDesignTokens
import kotlinx.coroutines.launch

private val BRAND_BLUE = Color(0xFF4F8CFF)
private val INACTIVE_GRAY = Color(0xFF7A8A9E)
private val SELECTED_ICON_BG = Color(0xFF4F8CFF)
private val IDLE_ICON_BG = Color(0xFFF3F6FB)

@Composable
fun HomeBottomBar(
    bottomNavIndex: Int,
    onNavChange: (Int) -> Unit,
    onWrongBook: () -> Unit,
    onFavoriteBook: () -> Unit,
    onHistory: () -> Unit,
    onSettings: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    fun selectTab(index: Int) {
        onNavChange(index)
        scope.launch { FontSettingsDataStore.setLastSelectedNav(context, index) }
    }

    val tabs = listOf(
        Triple(Icons.Filled.Book, Icons.Outlined.Book, stringResource(R.string.home_bottom_library)) to
            ({ selectTab(0) }),
        Triple(Icons.Filled.ErrorOutline, Icons.Outlined.ErrorOutline, stringResource(R.string.home_bottom_wrongbook)) to
            ({ selectTab(1); onWrongBook() }),
        Triple(Icons.Filled.Bookmarks, Icons.Outlined.Bookmarks, stringResource(R.string.home_bottom_favorite)) to
            ({ selectTab(2); onFavoriteBook() }),
        Triple(Icons.Filled.History, Icons.Outlined.History, stringResource(R.string.home_bottom_records)) to
            ({ selectTab(3); onHistory() }),
        Triple(Icons.Filled.Person, Icons.Outlined.Person, stringResource(R.string.home_bottom_mine)) to
            ({ selectTab(4); onSettings() }),
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .navigationBarsPadding(),
        shape = RoundedCornerShape(28.dp),
        color = Color.White,
        tonalElevation = HomeDesignTokens.elevationBottomBarTonal,
        shadowElevation = HomeDesignTokens.elevationBottomBar,
    ) {
        NavigationBar(
            modifier = Modifier.fillMaxWidth(),
            containerColor = Color.Transparent,
            tonalElevation = 0.dp,
        ) {
            tabs.forEachIndexed { index, (icons, onClick) ->
                val (filled, outlined, label) = icons
                val selected = bottomNavIndex == index
                NavigationBarItem(
                    selected = selected,
                    onClick = onClick,
                    icon = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            HomeBottomNavIcon(
                                filled = filled,
                                outlined = outlined,
                                label = label,
                                selected = selected,
                            )
                        }
                    },
                    label = {
                        Text(
                            text = label,
                            fontSize = 10.sp,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (selected) BRAND_BLUE else INACTIVE_GRAY,
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Color.Transparent,
                    ),
                )
            }
        }
    }
}

@Composable
private fun HomeBottomNavIcon(
    filled: ImageVector,
    outlined: ImageVector,
    label: String,
    selected: Boolean,
) {
    Surface(
        modifier = Modifier.size(38.dp),
        shape = CircleShape,
        color = if (selected) SELECTED_ICON_BG else IDLE_ICON_BG,
        tonalElevation = if (selected) 3.dp else 1.dp,
        shadowElevation = if (selected) {
            HomeDesignTokens.elevationNavIconSelected
        } else {
            HomeDesignTokens.elevationNavIconIdle
        },
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = if (selected) filled else outlined,
                contentDescription = label,
                modifier = Modifier.size(if (selected) 20.dp else 18.dp),
                tint = if (selected) Color.White else INACTIVE_GRAY,
            )
        }
    }
}
