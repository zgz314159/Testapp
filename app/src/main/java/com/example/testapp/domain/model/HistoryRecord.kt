package com.example.testapp.domain.model


import java.time.LocalDateTime
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable

data class HistoryRecord(
    val score: Int,
    val total: Int,
    @Contextual
    val time: LocalDateTime = LocalDateTime.now()
)
