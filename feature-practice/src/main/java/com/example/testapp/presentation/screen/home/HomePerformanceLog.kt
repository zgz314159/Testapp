package com.example.testapp.presentation.screen.home

/**
 * Compatibility hooks retained at call sites so business code stays unchanged.
 * Runtime logging, frame sampling, and trace emission have been removed.
 */
object HomePerformanceLog {
    const val enabled: Boolean = false

    fun resetSession() = Unit

    fun event(@Suppress("UNUSED_PARAMETER") message: String) = Unit

    inline fun <T> measure(
        @Suppress("UNUSED_PARAMETER") section: String,
        block: () -> T,
    ): T = block()

    fun cardEntered(@Suppress("UNUSED_PARAMETER") fileName: String) = Unit

    fun cardCounters(): CardCounters = CardCounters(entries = 0, unique = 0)
}

data class CardCounters(
    val entries: Int,
    val unique: Int,
)

class HomeScrollFrameMonitor(@Suppress("UNUSED_PARAMETER") refreshRateHz: Float) {
    fun start(
        @Suppress("UNUSED_PARAMETER") firstVisible: Int,
        @Suppress("UNUSED_PARAMETER") lastVisible: Int,
        @Suppress("UNUSED_PARAMETER") totalItems: Int,
    ) = Unit

    fun stop(
        @Suppress("UNUSED_PARAMETER") firstVisible: Int,
        @Suppress("UNUSED_PARAMETER") lastVisible: Int,
    ) = Unit
}
