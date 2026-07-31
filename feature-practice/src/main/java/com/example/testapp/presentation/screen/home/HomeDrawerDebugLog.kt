package com.example.testapp.presentation.screen.home

import android.util.Log
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue

/** 主页抽屉开合排查日志（统一 TAG=HomeDrawer，便于 logcat 过滤）。 */
internal object HomeDrawerDebugLog {
    const val TAG = "HomeDrawer"

    fun d(msg: String) {
        Log.d(TAG, msg)
    }

    fun open(reason: String, drawerState: DrawerState) {
        Log.d(
            TAG,
            "OPEN request reason=$reason " +
                "cur=${drawerState.currentValue} tgt=${drawerState.targetValue} " +
                "offset=${"%.3f".format(drawerState.currentOffset)}",
            Throwable("HomeDrawer.open stack"),
        )
    }

    fun close(reason: String, drawerState: DrawerState) {
        Log.d(
            TAG,
            "CLOSE request reason=$reason " +
                "cur=${drawerState.currentValue} tgt=${drawerState.targetValue} " +
                "offset=${"%.3f".format(drawerState.currentOffset)}",
            Throwable("HomeDrawer.close stack"),
        )
    }

    fun snapshot(where: String, drawerState: DrawerState, extra: String = "") {
        Log.d(
            TAG,
            "SNAP $where cur=${drawerState.currentValue} tgt=${drawerState.targetValue} " +
                "offset=${"%.3f".format(drawerState.currentOffset)} $extra",
        )
    }

    fun confirm(newValue: DrawerValue, drawerState: DrawerState): Boolean {
        Log.d(
            TAG,
            "CONFIRM -> $newValue " +
                "cur=${drawerState.currentValue} tgt=${drawerState.targetValue} " +
                "offset=${"%.3f".format(drawerState.currentOffset)}",
            Throwable("HomeDrawer.confirm stack"),
        )
        return true
    }
}
