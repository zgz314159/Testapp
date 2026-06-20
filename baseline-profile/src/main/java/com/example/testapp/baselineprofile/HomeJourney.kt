package com.example.testapp.baselineprofile

import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until

internal const val TARGET_PACKAGE = "com.example.testapp"

internal object HomeJourney {
    private const val HomeCardMarker = "home_file_card:"
    private const val HomeLoadTimeoutMs = 5_000L

    fun startHomeAndWait(scope: MacrobenchmarkScope) = with(scope) {
        pressHome()
        startActivityAndWait()
        device.wait(Until.hasObject(By.descContains(HomeCardMarker)), HomeLoadTimeoutMs)
        device.waitForIdle()
    }

    fun scrollHomeList(scope: MacrobenchmarkScope) = with(scope) {
        val centerX = device.displayWidth / 2
        val downStart = (device.displayHeight * 0.82f).toInt()
        val downEnd = (device.displayHeight * 0.24f).toInt()
        val upStart = (device.displayHeight * 0.24f).toInt()
        val upEnd = (device.displayHeight * 0.78f).toInt()

        device.swipe(centerX, downStart, centerX, downEnd, 28)
        device.waitForIdle()
        device.swipe(centerX, upStart, centerX, upEnd, 28)
        device.waitForIdle()
    }
}