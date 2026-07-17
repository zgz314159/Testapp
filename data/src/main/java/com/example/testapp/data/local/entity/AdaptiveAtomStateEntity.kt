package com.example.testapp.data.local.entity

import androidx.room.Entity

@Entity(
    tableName = "adaptive_atom_states",
    primaryKeys = ["bankId", "atomId"],
)
data class AdaptiveAtomStateEntity(
    val bankId: String,
    val atomId: Int,
    val sourceQuestionId: Int,
    val blankIndex: Int,
    val tag: String,
    val weight: Int,
    val pool: String,
    val stage: String,
    val correctStreak: Int,
    val lapseCount: Int,
    val reviewCount: Int,
    val dueAt: Long,
    val lastReviewedAt: Long,
)
