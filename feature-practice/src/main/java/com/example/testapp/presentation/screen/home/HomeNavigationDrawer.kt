package com.example.testapp.presentation.screen.home

import androidx.activity.compose.BackHandler
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeNavigationDrawer(
    drawerState: DrawerState,
    drawerContent: @Composable () -> Unit,
    gesturesEnabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()
    val dismissDrawer: () -> Unit = {
        scope.launch { drawerState.close() }
    }
    val drawerOpen by remember {
        derivedStateOf {
            drawerState.currentValue == DrawerValue.Open ||
                drawerState.targetValue == DrawerValue.Open
        }
    }

    BackHandler(enabled = drawerOpen, onBack = dismissDrawer)

    // 抽屉已打开时始终允许手势关闭；关闭态由调用方控制（拖拽题库卡片期间禁用，
    // 防止拖拽手势被 ModalNavigationDrawer 的水平 anchoredDraggable 抢占而误开抽屉）。
    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = gesturesEnabled || drawerOpen,
        drawerContent = drawerContent,
        content = content
    )
}
