package com.example.testapp.presentation.screen.exam

import com.example.testapp.domain.model.PracticeSessionState
import com.example.testapp.domain.review.ReviewAnsweredSwipePipeline
import com.example.testapp.domain.review.ReviewBrowseSession
import com.example.testapp.domain.review.SessionReviewPresentation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Review-mode browse and swipe for [ExamViewModel]. */
internal class ExamReviewSessionCoordinator(
    private val sessionState: MutableStateFlow<PracticeSessionState>,
    private val reviewModeActive: MutableStateFlow<Boolean>,
    private val scope: CoroutineScope,
    private val progressId: () -> String,
    private val setProgressId: (String) -> Unit,
    private val resetArtifactLoadedFlags: () -> Unit,
    private val loadReviewSession: (
        targetProgressId: String,
        quizFile: String,
        questionCount: Int,
        random: Boolean,
        wrongBook: Boolean,
        favorite: Boolean
    ) -> Unit,
    private val scheduleNavigationSave: () -> Unit
) {
    private var reviewBrowseSession: ReviewBrowseSession? = null
    private var reviewAnsweredSwipeOrder: List<Int> = emptyList()

    fun enterReviewSession(
        targetProgressId: String,
        quizFile: String,
        questionCount: Int,
        random: Boolean,
        wrongBook: Boolean = false,
        favorite: Boolean = false
    ) {
        scope.launch {
            reviewModeActive.value = true
            if (progressId() == targetProgressId && sessionState.value.questionsWithState.isNotEmpty()) {
                applyReviewPresentation()
                return@launch
            }
            setProgressId(targetProgressId)
            resetArtifactLoadedFlags()
            sessionState.update { it.copy(progressLoaded = false) }
            loadReviewSession(targetProgressId, quizFile, questionCount, random, wrongBook, favorite)
            sessionState.first { it.progressLoaded }
            applyReviewPresentation()
        }
    }

    private fun applyReviewPresentation() {
        val state = sessionState.value
        val presentation = SessionReviewPresentation.prepare(state.questionsWithState)
        reviewBrowseSession = ReviewBrowseSession(presentation.displayOrder)
        reviewAnsweredSwipeOrder = ReviewAnsweredSwipePipeline.buildOrder(presentation.questionsWithState)
        sessionState.value = state.copy(
            questionsWithState = presentation.questionsWithState,
            currentIndex = reviewBrowseSession!!.currentIndex,
            finished = true,
            progressLoaded = true
        )
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

    fun browseReviewAnsweredOlder(): ExamReviewSwipeOutcome {
        if (reviewBrowseSession == null) return ExamReviewSwipeOutcome.NoHistory
        val (outcome, target) = ExamReviewSwipePipeline.browseOlder(
            reviewAnsweredSwipeOrder,
            sessionState.value.currentIndex
        )
        if (target != null) {
            sessionState.update { it.copy(currentIndex = target) }
            scheduleNavigationSave()
        }
        return outcome
    }

    fun browseReviewAnsweredNewer(): ExamReviewSwipeOutcome {
        if (reviewBrowseSession == null) return ExamReviewSwipeOutcome.AtLatest
        val (outcome, target) = ExamReviewSwipePipeline.browseNewer(
            reviewAnsweredSwipeOrder,
            sessionState.value.currentIndex
        )
        if (target != null) {
            sessionState.update { it.copy(currentIndex = target) }
            scheduleNavigationSave()
        }
        return outcome
    }
}
