package com.example.testapp.presentation.screen.home.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.testapp.data.datastore.FontSettingsDataStore
import com.example.testapp.feature.practice.R
import kotlinx.coroutines.launch

private val BRAND_BLUE = Color(0xFF4F8CFF)
private val INACTIVE_GRAY = Color(0xFF9AA8B9)

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

    // 题库 / 错题 / 收藏 / 记录 / 我的（错题/收藏图标与 Hero 统计条一致）
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

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .navigationBarsPadding(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
    ) {
        androidx.compose.material3.NavigationBar(
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
                        Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = if (selected) filled else outlined,
                                contentDescription = label,
                                modifier = Modifier.size(20.dp),
                                tint = if (selected) BRAND_BLUE else INACTIVE_GRAY,
                            )
                        }
                    },
                    label = {
                        Text(
                            text = label,
                            fontSize = 10.sp,
                            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
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
