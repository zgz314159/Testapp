package com.example.testapp.domain.model

data class FavoriteQuestion(
    val question: Question,
    val addedTime: Long = System.currentTimeMillis()
)

