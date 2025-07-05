package com.example.testapp.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class FavoriteQuestion(
    val question: Question,
    val addedTime: Long = System.currentTimeMillis()
)

