package com.example.testapp.domain.model

import kotlinx.serialization.Serializable

/**
 * 答题状态枚举（放到 domain 以便共享）
 */
@Serializable
enum class AnswerStatus {
    UNANSWERED,
    ANSWERED_NOT_SHOWN,
    CORRECT,
    INCORRECT
}
