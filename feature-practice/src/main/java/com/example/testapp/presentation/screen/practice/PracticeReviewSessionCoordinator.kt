package com.example.testapp.presentation.screen.practice

import com.example.testapp.domain.model.PracticeSessionState
import com.example.testapp.domain.review.ReviewAnsweredSwipePipeline
import com.example.testapp.domain.review.ReviewBrowseSession
import com.example.testapp.presentation.screen.practice.navigation.AnsweredHistoryBackwardResult
import com.example.testapp.presentation.screen.practice.navigation.AnsweredHistoryForwardResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Review-mode session and answered-history swipe for [PracticeViewModel]. */
internal class PracticeReviewSessionCoordinator(
    private val sessionState: MutableStateFlow<PracticeSessionState>,
    private val reviewModeActive: MutableStateFlow<Boolean>,
    private val reviewReady: MutableStateFlow<Boolean>,
    private val scope: CoroutineScope,
    private val progressId: () -> String,
    private val loadReviewSession: (
        targetProgressId: String,
        sourceId: String,
        questionCount: Int,
        wrongBook: Boolean,
        favorite: Boolean,
        onReady: suspend () -> Unit
    ) -> Unit,
    private val scheduleNavigationSave: () -> Unit
) {
    private var reviewBrowseSession: ReviewBrowseSession? = null
    private var reviewAnsweredSwipeOrder: List<Int> = emptyList()

    fun enterReviewSession(targetProgressId: String) {
        scope.launch {
            reviewModeActive.value = true
            reviewReady.value = false
            if (PracticeReviewReusePipeline.canReuse(progressId(), targetProgressId, sessionState.value)) {
                applyReviewPresentation()
                return@launch
            }
            val target = com.example.testapp.core.common.parsePracticeReviewTarget(targetProgressId)
            loadReviewSession(
                target.progressId,
                target.quizFileName,
                target.questionCount,
                target.isWrongBookMode,
                target.isFavoriteMode
            ) {
                applyReviewPresentation()
            }
        }
    }

    private suspend fun applyReviewPresentation() {
        val state = sessionState.value
        val prepared = withContext(Dispatchers.Default) {
            PracticeReviewPresentationPipeline.prepare(state)
        }
        reviewBrowseSession = prepared.reviewBrowseSession
        reviewAnsweredSwipeOrder = prepared.reviewAnsweredSwipeOrder
        sessionState.value = state.copy(
            questionsWithState = prepared.questionsWithState,
            currentIndex = prepared.currentIndex,
            progressLoaded = true
        )
        reviewReady.value = true
    }

    fun canReviewBrowseBack(): Boolean = reviewBrowseSession?.canStepBack() == true

    fun canReviewBrowseForward(): Boolean = reviewBrowseSession?.canStepForward() == true

    fun tryNavigateReviewBrowse(delta: Int): Boolean {
        val session = reviewBrowseSession ?: return false
        val stepped = session.step(delta) ?: return true
        reviewBrowseSession = stepped
        sessionState.update { it.copy(currentIndex = stepped.currentIndex) }
        scheduleNavigationSave()
        return true
    }

    fun browseAnsweredHistoryOlder(): AnsweredHistoryBackwardResult? {
        if (reviewBrowseSession == null) return null
        val ordered = reviewAnsweredSwipeOrder
        if (ordered.isEmpty()) return AnsweredHistoryBackwardResult.NoMoreHistory
        val currentIndex = sessionState.value.currentIndex
        val target = ReviewAnsweredSwipePipeline.resolveOlderIndex(ordered, currentIndex)
        if (target == null) {
            return if (ReviewAnsweredSwipePipeline.isAtOldest(ordered, currentIndex)) {
                AnsweredHistoryBackwardResult.AtOldestAnswered
            } else {
                AnsweredHistoryBackwardResult.NoMoreHistory
            }
        }
        sessionState.update { it.copy(currentIndex = target) }
        scheduleNavigationSave()
        return AnsweredHistoryBackwardResult.Navigated
    }

    fun browseAnsweredHistoryNewer(): AnsweredHistoryForwardResult? {
        if (reviewBrowseSession == null) return null
        val ordered = reviewAnsweredSwipeOrder
        if (ordered.isEmpty()) return AnsweredHistoryForwardResult.AtLatestAnswered
        val currentIndex = sessionState.value.currentIndex
        val target = ReviewAnsweredSwipePipeline.resolveNewerIndex(ordered, currentIndex)
        if (target == null) return AnsweredHistoryForwardResult.AtLatestAnswered
        sessionState.update { it.copy(currentIndex = target) }
        scheduleNavigationSave()
        return AnsweredHistoryForwardResult.Navigated
    }
}
