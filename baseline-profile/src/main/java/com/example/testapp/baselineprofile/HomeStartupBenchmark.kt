package com.example.testapp.baselineprofile

import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeStartupBenchmark {
    @get:Rule
    val macrobenchmarkRule = MacrobenchmarkRule()

    @Test
    fun coldStartAndScrollHome() {
        macrobenchmarkRule.measureRepeated(
            packageName = TARGET_PACKAGE,
            metrics = listOf(
                StartupTimingMetric(),
                FrameTimingMetric()
            ),
            iterations = 5,
            startupMode = StartupMode.COLD
        ) {
            HomeJourney.startHomeAndWait(this)
            HomeJourney.scrollHomeList(this)
        }
    }
}