package com.example.testapp.presentation.session.adaptive

import com.example.testapp.domain.model.AdaptiveAtomPool
import com.example.testapp.domain.model.AdaptiveAtomState
import com.example.testapp.domain.repository.AdaptiveAtomRepository
import com.example.testapp.domain.session.QuestionSessionKind
import com.example.testapp.domain.session.QuestionSnapshot
import com.example.testapp.domain.session.SessionEvent
import com.example.testapp.domain.session.SessionSnapshot
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class AdaptiveFadingProgressExtensionTest {
    @Test
    fun `submitted answer advances matching adaptive atom`() =
        runBlocking {
            val repository = FakeAdaptiveAtomRepository(initialState())
            val extension = AdaptiveFadingProgressExtension(repository)

            extension.onEvent(
                event = SessionEvent.AnswerSubmitted(index = 0, questionId = -1),
                snapshot =
                    SessionSnapshot(
                        kind = QuestionSessionKind.AdaptiveFading("bank.sqlite"),
                        questions = listOf(QuestionSnapshot(-1, "题干", "填空题", showResult = true, isCorrect = true)),
                    ),
                dispatch = {},
            )

            assertEquals(1, repository.state.correctStreak)
            assertEquals(1, repository.state.reviewCount)
        }

    private fun initialState() =
        AdaptiveAtomState(
            bankId = "bank.sqlite",
            atomId = -1,
            sourceQuestionId = 1,
            blankIndex = 0,
            tag = "强制性动词",
            weight = 10,
            pool = AdaptiveAtomPool.CORE,
        )
}

private class FakeAdaptiveAtomRepository(
    var state: AdaptiveAtomState,
) : AdaptiveAtomRepository {
    override suspend fun getStates(bankId: String): List<AdaptiveAtomState> = listOf(state)

    override suspend fun getState(bankId: String, atomId: Int): AdaptiveAtomState = state

    override suspend fun upsertStates(states: List<AdaptiveAtomState>) {
        state = states.single()
    }

    override suspend fun deleteByBank(bankId: String) = Unit
}
