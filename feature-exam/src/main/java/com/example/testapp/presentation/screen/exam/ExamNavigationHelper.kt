package com.example.testapp.presentation.screen.exam

import com.example.testapp.core.util.answerToOptionIndex
import com.example.testapp.core.util.resolveDisplayOptions
import com.example.testapp.domain.model.ExamProgress
import com.example.testapp.domain.model.Question
import com.example.testapp.domain.model.QuestionWithState
import com.example.testapp.domain.model.UnifiedQuestionState
import com.example.testapp.uicommon.component.AnswerCardDisplayInfo
import com.example.testapp.uicommon.component.AnswerCardDisplayInfoPipeline
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExamNavigationHelper @Inject constructor(
    private val answerRules: ExamAnswerRules
) {
    data class QuadLists(
        val analysis: List<String>, val sparkAnalysis: List<String>,
        val baiduAnalysis: List<String>, val notes: List<String>
    )

    fun shouldReuseSavedSourceOrder(
        saved: List<Int>, expectedCount: Int, expectedSeq: List<Int>, random: Boolean
    ): Boolean = ExamSavedOrderMatchPipeline.matches(saved, expectedSeq, random)

    fun buildExamQuestionState(
        questions: List<Question>, idx: Int,
        sel: List<List<Int>>, txt: List<String>, sr: List<Boolean>,
        an: List<String>, sp: List<String>, bd: List<String>, nt: List<String>
    ) = UnifiedQuestionState(
        questionId = questions.getOrNull(idx)?.id ?: -1,
        selectedOptions = sel.getOrElse(idx) { emptyList() },
        textAnswer = txt.getOrElse(idx) { "" },
        showResult = sr.getOrElse(idx) { false },
        analysis = an.getOrElse(idx) { "" },
        sparkAnalysis = sp.getOrElse(idx) { "" },
        baiduAnalysis = bd.getOrElse(idx) { "" },
        note = nt.getOrElse(idx) { "" }
    )

    fun buildCurrentStateMapByQuestionId(
        questions: List<Question>,
        sel: List<List<Int>>, txt: List<String>, sr: List<Boolean>,
        an: List<String>, sp: List<String>, bd: List<String>, nt: List<String>
    ): Map<Int, UnifiedQuestionState> {
        val result = mutableMapOf<Int, UnifiedQuestionState>()
        questions.forEachIndexed { i, q -> result[q.id] = UnifiedQuestionState(
            q.id, sel.getOrElse(i) { emptyList() }, txt.getOrElse(i) { "" },
            sr.getOrElse(i) { false }, an.getOrElse(i) { "" },
            sp.getOrElse(i) { "" }, bd.getOrElse(i) { "" }, nt.getOrElse(i) { "" }
        ) }
        return result
    }

    fun buildAnswerCardDisplayInfo(
        questions: List<Question>,
        allSourceQuestions: List<Question>,
        fullAnswerMode: Boolean
    ): Map<Int, AnswerCardDisplayInfo> =
        AnswerCardDisplayInfoPipeline.build(questions, allSourceQuestions, fullAnswerMode)

    fun currentFullAnswerCandidateIndices(
        questions: List<Question>,
        questionsWithState: List<QuestionWithState>,
        currentIndex: Int,
        eligible: List<Int>,
        fullAnswerModeActive: Boolean,
        fullAnswerRequireCorrect: Boolean,
        sessionGraded: Boolean,
        isCorrect: (Question, UnifiedQuestionState) -> Boolean
    ): List<Int> = ExamFullAnswerNavigation.resolveCandidateIndices(
        questions = questions,
        questionsWithState = questionsWithState,
        currentIndex = currentIndex,
        eligibleIndices = eligible,
        fullAnswerModeActive = fullAnswerModeActive,
        requireCorrect = fullAnswerRequireCorrect,
        sessionGraded = sessionGraded,
        isCorrect = isCorrect
    )

    fun navigateCandidateIndices(
        questions: List<Question>,
        questionsWithState: List<QuestionWithState>,
        currentIndex: Int,
        fullAnswerModeActive: Boolean,
        fullAnswerRequireCorrect: Boolean,
        sessionGraded: Boolean,
        memoryActive: Boolean,
        roundIds: Set<Int>,
        isCorrect: (Question, UnifiedQuestionState) -> Boolean
    ): List<Int> = currentFullAnswerCandidateIndices(
        questions = questions,
        questionsWithState = questionsWithState,
        currentIndex = currentIndex,
        eligible = questions.indices.filter { idx ->
            val q = questions.getOrNull(idx) ?: return@filter false
            idx != currentIndex && (!memoryActive || roundIds.isEmpty() || q.id in roundIds)
        },
        fullAnswerModeActive = fullAnswerModeActive,
        fullAnswerRequireCorrect = fullAnswerRequireCorrect,
        sessionGraded = sessionGraded,
        isCorrect = isCorrect
    )

    fun normalizeEditedSelectedOptions(sel: List<Int>, q: Question) = sel.distinct().filter { it in q.options.indices }

    fun shuffleOptionsIfNeeded(questions: List<Question>, random: Boolean, seed: Long): List<Question> {
        if (!random) return questions
        return questions.mapIndexed { idx, q ->
            val opts = resolveDisplayOptions(q); val ci = answerToOptionIndex(q)
            if (ci == null || opts.isEmpty()) q else {
                val r = java.util.Random(seed + idx)
                val pairs = opts.mapIndexed { i, o -> i to o }.shuffled(r)
                q.copy(options = pairs.map { it.second }, answer = ('A' + pairs.indexOfFirst { it.first == ci }).toString())
            }
        }
    }

    suspend fun preloadStoredArtifacts(
        questions: List<Question>, progress: ExamProgress?,
        getAnalysis: suspend (Int) -> String,
        getSpark: suspend (Int) -> String,
        getBaidu: suspend (Int) -> String,
        getNote: suspend (Int) -> String
    ): QuadLists {
        val an = MutableList(questions.size) { "" }; val sp = MutableList(questions.size) { "" }
        val bd = MutableList(questions.size) { "" }; val nt = MutableList(questions.size) { "" }
        questions.forEachIndexed { i, q ->
            val s = progress?.questionStateMap?.get(q.id)
            an[i] = s?.analysis?.takeIf { it.isNotBlank() } ?: getAnalysis(q.id)
            sp[i] = s?.sparkAnalysis?.takeIf { it.isNotBlank() } ?: getSpark(q.id)
            bd[i] = s?.baiduAnalysis?.takeIf { it.isNotBlank() } ?: getBaidu(q.id)
            nt[i] = s?.note?.takeIf { it.isNotBlank() } ?: getNote(q.id)
        }
        return QuadLists(an, sp, bd, nt)
    }
}
