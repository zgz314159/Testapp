package com.example.testapp.presentation.screen.home

/** 抽屉浏览返回主页后恢复搜索态（与 Session 生命周期正交） */
data class HomeDrawerRestoreState(
    val openDrawer: Boolean = true,
    val searchQuery: String = "",
)

object HomeDrawerRestoreHolder {
    @Volatile
    var pending: HomeDrawerRestoreState? = null
}
