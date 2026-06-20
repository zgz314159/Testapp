package com.example.testapp.presentation.screen.exam

import com.example.testapp.domain.model.ExamProgress
import com.example.testapp.domain.model.UnifiedQuestionState
import com.example.testapp.domain.model.Question
import com.example.testapp.uicommon.component.AnswerCardDisplayInfo
import com.example.testapp.core.util.answerToOptionIndex
import com.example.testapp.core.util.answerToOptionIndices
import com.example.testapp.core.util.extractDerivedFillQuestionRound
import com.example.testapp.core.util.extractSourceQuestionId
import com.example.testapp.core.util.resolveDisplayOptions
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
    ): Boolean {
        if (saved.isEmpty() || saved.size != expectedCount) return false
        return if (random) saved != expectedSeq else saved == expectedSeq
    }

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
        )}
        return result
    }

    fun buildAnswerCardDisplayInfo(questions: List<Question>, allSourceQuestions: List<Question>): Map<Int, AnswerCardDisplayInfo> {
        val orderById = allSourceQuestions.mapIndexed { i, q -> q.id to (i + 1) }.toMap()
        return questions.associate { q ->
            val order = orderById[extractSourceQuestionId(q.id)] ?: questions.indexOf(q) + 1
            val round = extractDerivedFillQuestionRound(q.id)
            q.id to AnswerCardDisplayInfo(
                label = if (round != null) "$order-$round" else order.toString(), order = order, round = round
            )
        }
    }

    fun currentFullAnswerCandidateIndices(
        questions: List<Question>, currentIndex: Int, eligible: List<Int>,
        fullAnswerRequireCorrect: Boolean, buildState: (Int) -> UnifiedQuestionState
    ): List<Int> {
        if (eligible.isEmpty()) return emptyList()
        val cur = questions.getOrNull(currentIndex) ?: return eligible
        val sourceId = extractSourceQuestionId(cur.id)
        if (sourceId == cur.id)
            return eligible.filter { !buildState(it).showResult }.ifEmpty { eligible }
        val same = eligible.filter { extractSourceQuestionId(questions[it].id) == sourceId }
        val sameUnrev = same.filter { !buildState(it).showResult }
        if (sameUnrev.isNotEmpty()) return sameUnrev
        if (fullAnswerRequireCorrect) {
            val sameIncorrect = same.filter { i -> val s = buildState(i); s.showResult && !answerRules.isQuestionCorrect(questions[i], s) }
            if (sameIncorrect.isNotEmpty()) return sameIncorrect
        }
        val unrev = eligible.filter { !buildState(it).showResult }
        if (unrev.isNotEmpty()) return unrev
        if (fullAnswerRequireCorrect) {
            val incorrect = eligible.filter { i -> val s = buildState(i); s.showResult && !answerRules.isQuestionCorrect(questions[i], s) }
            if (incorrect.isNotEmpty()) return incorrect
        }
        return eligible
    }

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

    fun navigateCandidateIndices(
        questions: List<Question>, currentIndex: Int,
        fullAnswerRequireCorrect: Boolean, memoryActive: Boolean, roundIds: Set<Int>,
        buildState: (Int) -> UnifiedQuestionState
    ): List<Int> = currentFullAnswerCandidateIndices(
        questions = questions, currentIndex = currentIndex,
        eligible = questions.indices.filter { idx ->
            val q = questions.getOrNull(idx) ?: return@filter false
            idx != currentIndex && (!memoryActive || roundIds.isEmpty() || q.id in roundIds)
        },
        fullAnswerRequireCorrect = fullAnswerRequireCorrect, buildState = buildState
    )

    fun normalizeEditedSelectedOptions(sel: List<Int>, q: Question) = sel.distinct().filter { it in q.options.indices }
}
