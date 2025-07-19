package com.example.testapp.domain.model


import java.time.LocalDateTime
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable

data class HistoryRecord(
    val score: Int,
    val total: Int,
    val unanswered: Int = 0,
    val fileName: String? = null,
    @Contextual
    val time: LocalDateTime = LocalDateTime.now()
)
