package com.example.testapp.presentation.screen.home

import android.os.SystemClock
import android.os.Trace
import android.util.Log
import android.view.Choreographer
import com.example.testapp.feature.practice.BuildConfig
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/** Debug-only diagnostics for cold-start Home scrolling. Filter Logcat with tag `HomePerf`. */
object HomePerformanceLog {
    const val TAG = "HomePerf"

    private val cardEntries = AtomicInteger()
    private val uniqueCards = ConcurrentHashMap.newKeySet<String>()
    private var sessionStartedNs = SystemClock.elapsedRealtimeNanos()

    val enabled: Boolean
        get() = BuildConfig.DEBUG

    fun resetSession() {
        if (!enabled) return
        sessionStartedNs = SystemClock.elapsedRealtimeNanos()
        cardEntries.set(0)
        uniqueCards.clear()
        event("home_enter thread=${Thread.currentThread().name}")
    }

    fun event(message: String) {
        if (!enabled) return
        val sinceEnterMs = (SystemClock.elapsedRealtimeNanos() - sessionStartedNs) / 1_000_000f
        Log.d(TAG, "t=%.1fms %s".format(sinceEnterMs, message))
    }

    fun <T> measure(section: String, block: () -> T): T {
        if (!enabled) return block()
        val traceName = "Home:$section".take(120)
        val startedNs = SystemClock.elapsedRealtimeNanos()
        Trace.beginSection(traceName)
        return try {
            block()
        } finally {
            Trace.endSection()
            val durationMs = (SystemClock.elapsedRealtimeNanos() - startedNs) / 1_000_000f
            event("measure section=$section durationMs=%.2f thread=${Thread.currentThread().name}".format(durationMs))
        }
    }

    fun cardEntered(fileName: String) {
        if (!enabled) return
        cardEntries.incrementAndGet()
        uniqueCards.add(fileName)
    }

    fun cardCounters(): CardCounters = CardCounters(
        entries = cardEntries.get(),
        unique = uniqueCards.size,
    )
}

data class CardCounters(
    val entries: Int,
    val unique: Int,
)

/** Counts skipped-vsync intervals while a Home lazy list is actively scrolling. */
class HomeScrollFrameMonitor(refreshRateHz: Float) : Choreographer.FrameCallback {
    private val choreographer = Choreographer.getInstance()
    private val expectedFrameNs = (1_000_000_000.0 / refreshRateHz.coerceAtLeast(30f)).toLong()
    private val frameDurationsMs = ArrayList<Float>(240)
    private var running = false
    private var startedNs = 0L
    private var previousFrameNs = 0L
    private var missedVsyncs = 0
    private var severeFramesLogged = 0
    private var countersAtStart = CardCounters(0, 0)

    fun start(firstVisible: Int, lastVisible: Int, totalItems: Int) {
        if (!HomePerformanceLog.enabled || running) return
        running = true
        startedNs = SystemClock.elapsedRealtimeNanos()
        previousFrameNs = 0L
        missedVsyncs = 0
        severeFramesLogged = 0
        frameDurationsMs.clear()
        countersAtStart = HomePerformanceLog.cardCounters()
        HomePerformanceLog.event(
            "scroll_start visible=$firstVisible..$lastVisible total=$totalItems " +
                "expectedFrameMs=${"%.2f".format(expectedFrameNs / 1_000_000f)}",
        )
        choreographer.postFrameCallback(this)
    }

    fun stop(firstVisible: Int, lastVisible: Int) {
        if (!running) return
        running = false
        choreographer.removeFrameCallback(this)
        val elapsedMs = (SystemClock.elapsedRealtimeNanos() - startedNs) / 1_000_000f
        val sorted = frameDurationsMs.sorted()
        val p95 = sorted.getOrNull((sorted.size * 0.95f).toInt().coerceAtMost(sorted.lastIndex)) ?: 0f
        val max = sorted.lastOrNull() ?: 0f
        val slowFrames = sorted.count { it > expectedFrameNs / 1_000_000f * 1.5f }
        val severeFrames = sorted.count { it > expectedFrameNs / 1_000_000f * 2.5f }
        val counters = HomePerformanceLog.cardCounters()
        HomePerformanceLog.event(
            "scroll_end visible=$firstVisible..$lastVisible durationMs=${"%.1f".format(elapsedMs)} " +
                "frames=${sorted.size} slow=$slowFrames severe=$severeFrames missedVsyncs=$missedVsyncs " +
                "p95Ms=${"%.2f".format(p95)} maxMs=${"%.2f".format(max)} " +
                "cardEntriesDelta=${counters.entries - countersAtStart.entries} " +
                "uniqueCardsDelta=${counters.unique - countersAtStart.unique}",
        )
    }

    override fun doFrame(frameTimeNanos: Long) {
        if (!running) return
        if (previousFrameNs != 0L) {
            val durationNs = frameTimeNanos - previousFrameNs
            val durationMs = durationNs / 1_000_000f
            frameDurationsMs += durationMs
            val skipped = (((durationNs + expectedFrameNs / 2) / expectedFrameNs).toInt() - 1).coerceAtLeast(0)
            missedVsyncs += skipped
            if (durationNs > expectedFrameNs * 5 / 2 && severeFramesLogged < 6) {
                severeFramesLogged++
                val counters = HomePerformanceLog.cardCounters()
                HomePerformanceLog.event(
                    "severe_frame durationMs=${"%.2f".format(durationMs)} skipped=$skipped " +
                        "cardEntries=${counters.entries} uniqueCards=${counters.unique}",
                )
            }
        }
        previousFrameNs = frameTimeNanos
        choreographer.postFrameCallback(this)
    }
}
