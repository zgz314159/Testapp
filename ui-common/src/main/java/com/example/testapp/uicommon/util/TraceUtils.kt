package com.example.testapp.uicommon.util


inline fun <T> traceSection(@Suppress("UNUSED_PARAMETER") name: String, block: () -> T): T = block()
