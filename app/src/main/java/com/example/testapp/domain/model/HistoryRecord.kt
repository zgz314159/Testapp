package com.example.testapp.domain.model

import java.time.LocalDateTime

data class HistoryRecord(
    val score: Int,
    val total: Int,
    val time: LocalDateTime = LocalDateTime.now()
)
