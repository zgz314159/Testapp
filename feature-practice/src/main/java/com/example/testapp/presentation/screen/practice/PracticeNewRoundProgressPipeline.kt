package com.example.testapp.presentation.screen.practice

import com.example.testapp.domain.model.PracticeProgress
import com.example.testapp.domain.model.Question

/** 练习新轮次 progress 快照 */
object PracticeNewRoundProgressPipeline {

    fun build(
        prior: PracticeProgress?,
        progressId: String,
        seed: Long,
        sessionId: String,
        questions: List<Question>
    ): PracticeProgress = PracticeProgress(
        id = progressId,
        currentIndex = 0,
        answeredList = emptyList(),
        selectedOptions = emptyList(),
        showResultList = emptyList(),
        analysisList = emptyList(),
        sparkAnalysisList = emptyList(),
        baiduAnalysisList = emptyList(),
        noteList = emptyList(),
        timestamp = seed,
        sessionId = sessionId,
        fixedQuestionOrder = questions.map { it.id },
        questionStateMap = prior?.questionStateMap.orEmpty()
    )
}
