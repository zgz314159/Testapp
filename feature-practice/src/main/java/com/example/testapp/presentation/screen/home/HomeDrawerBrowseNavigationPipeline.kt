package com.example.testapp.presentation.screen.home

/** 抽屉 Browse 离开主页前快照；与 Session 生命周期正交 */
object HomeDrawerBrowseNavigationPipeline {
    fun captureRestoreBeforeBrowse(searchQuery: String) {
        HomeDrawerRestoreHolder.pending =
            HomeDrawerRestoreState(
                openDrawer = true,
                searchQuery = searchQuery.trim(),
            )
    }
}
