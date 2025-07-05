package com.example.testapp.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class WrongQuestion(
    val question: Question,
    val selected: Int
)
