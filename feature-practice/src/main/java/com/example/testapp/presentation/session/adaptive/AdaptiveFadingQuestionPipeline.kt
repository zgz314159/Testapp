package com.example.testapp.presentation.session.adaptive

import com.example.testapp.core.util.FillAnswerPartDescriptor
import com.example.testapp.core.util.buildDerivedFillQuestionId
import com.example.testapp.core.util.splitFillAnswerDescriptors
import com.example.testapp.domain.QuestionTypes
import com.example.testapp.domain.model.AdaptiveAtomPool
import com.example.testapp.domain.model.AdaptiveAtomStage
import com.example.testapp.domain.model.AdaptiveAtomState
import com.example.testapp.domain.model.Question
import kotlin.random.Random

object AdaptiveFadingQuestionPipeline {
    private val displayBlankRegex = Regex("_{2,}|（\\s*）|\\(\\s*\\)|【\\s*】|\\[\\s*]")
    private val numericUnitRegex = Regex("[^0-9.,]+$")
    private const val DEFAULT_SESSION_SIZE = 20
    private const val MAX_SESSION_SIZE = 50

    data class PreparedSession(
        val questions: List<Question>,
        val states: List<AdaptiveAtomState>,
    )

    private data class Candidate(
        val source: Question,
        val blankIndex: Int,
        val answer: String,
        val tag: String,
        val weight: Int,
        val atomId: Int,
        val pool: AdaptiveAtomPool,
        val state: AdaptiveAtomState,
    )

    fun prepare(
        bankId: String,
        sourceQuestions: List<Question>,
        storedStates: List<AdaptiveAtomState>,
        requestedCount: Int,
        now: Long,
    ): PreparedSession {
        val statesById = storedStates.associateBy { it.atomId }
        val candidates = sourceQuestions.flatMap { source -> buildCandidates(bankId, source, statesById) }
        if (candidates.isEmpty()) return PreparedSession(emptyList(), emptyList())

        val selected = selectSessionCandidates(candidates, requestedCount, now)
        val answerPool = candidates.groupBy { it.tag }

        val rendered = selected.map { candidate ->
            val questionAndStage = render(candidate, answerPool[candidate.tag].orEmpty())
            questionAndStage.first to candidate.state.copy(stage = questionAndStage.second)
        }
        return PreparedSession(
            questions = rendered.map { it.first },
            states = rendered.map { it.second },
        )
    }

    private fun buildCandidates(
        bankId: String,
        source: Question,
        statesById: Map<Int, AdaptiveAtomState>,
    ): List<Candidate> {
        if (!QuestionTypes.isInlineBlank(source.type)) return emptyList()
        val blankCount = displayBlankRegex.findAll(source.content).count()
        val descriptors = splitFillAnswerDescriptors(source.answer).take(blankCount)
        val rankedCore = descriptors.indices
            .sortedWith(compareByDescending<Int> { descriptors[it].score ?: 0 }.thenBy { it })
            .take(3)
            .toSet()
        return descriptors.mapIndexedNotNull { index, descriptor ->
            val answer = descriptor.answerText.trim()
            if (answer.isBlank()) return@mapIndexedNotNull null
            val tag = descriptor.category.orEmpty().trim()
            val weight = descriptor.score ?: 0
            val pool = if (isMandatoryCore(tag) || index in rankedCore) AdaptiveAtomPool.CORE else AdaptiveAtomPool.PROBE
            val atomId = buildDerivedFillQuestionId(source.id, index)
            val existing = statesById[atomId]
            val state =
                existing?.copy(
                    sourceQuestionId = source.id,
                    blankIndex = index,
                    tag = tag,
                    weight = weight,
                    pool = if (existing.pool == AdaptiveAtomPool.CORE) existing.pool else pool,
                ) ?: AdaptiveAtomState(
                    bankId = bankId,
                    atomId = atomId,
                    sourceQuestionId = source.id,
                    blankIndex = index,
                    tag = tag,
                    weight = weight,
                    pool = pool,
                )
            Candidate(source, index, answer, tag, weight, atomId, state.pool, state)
        }
    }

    private fun isMandatoryCore(tag: String): Boolean =
        tag.contains("数据") || tag.contains("参数") || tag.contains("强制性动词")

    private fun selectSessionCandidates(
        candidates: List<Candidate>,
        requestedCount: Int,
        now: Long,
    ): List<Candidate> {
        val limit = (if (requestedCount <= 0) DEFAULT_SESSION_SIZE else requestedCount).coerceAtMost(MAX_SESSION_SIZE)
        val due = candidates.filter { it.state.dueAt <= now }
        val eligible = if (due.isNotEmpty()) due else candidates.sortedBy { it.state.dueAt }
        val comparator = compareByDescending<Candidate> { it.state.lapseCount }
            .thenByDescending { if (it.state.reviewCount > 0) 1 else 0 }
            .thenBy { it.state.dueAt }
            .thenByDescending { it.weight }
            .thenBy { it.blankIndex }
            .thenBy { it.atomId }
        val probes = eligible.filter { it.pool == AdaptiveAtomPool.PROBE }.sortedWith(comparator)
        val cores = eligible.filter { it.pool == AdaptiveAtomPool.CORE }.sortedWith(comparator)
        val probeLimit =
            if (probes.isEmpty() || limit < 7) {
                0
            } else {
                (limit * 15 / 100).coerceAtLeast(1)
            }
        val selected = mutableListOf<Candidate>()
        val usedArticles = mutableSetOf<Int>()

        fun addFrom(pool: List<Candidate>, targetSize: Int) {
            pool.forEach { candidate ->
                if (selected.size < targetSize && usedArticles.add(candidate.source.id)) {
                    selected += candidate
                }
            }
        }

        addFrom(probes, probeLimit)
        addFrom(cores, limit)
        addFrom(probes, limit)
        return selected.shuffled(Random(now / 86_400_000L)).take(limit)
    }

    private fun render(candidate: Candidate, sameTagCandidates: List<Candidate>): Pair<Question, AdaptiveAtomStage> {
        val stage = candidate.state.stage
        if (stage == AdaptiveAtomStage.CHOICE) {
            buildChoice(candidate, sameTagCandidates)?.let { return it to stage }
        }
        val effectiveStage = if (stage == AdaptiveAtomStage.CHOICE) AdaptiveAtomStage.HINTED else stage
        val content = rebuildContent(candidate, showBlank = true) +
            if (effectiveStage == AdaptiveAtomStage.HINTED) "\n\n提示：${buildHint(candidate)}" else ""
        return candidate.source.copy(
            id = candidate.atomId,
            content = content,
            type = QuestionTypes.BLANK,
            options = emptyList(),
            answer = candidate.answer,
            explanation = "自适应渐隐 · ${stageLabel(effectiveStage)}\n${candidate.source.explanation}",
        ) to effectiveStage
    }

    private fun buildChoice(candidate: Candidate, sameTagCandidates: List<Candidate>): Question? {
        val requiredUnit = numericUnit(candidate.answer)
        val distractors = sameTagCandidates.asSequence()
            .filter { it.source.id != candidate.source.id }
            .map { it.answer }
            .filter { it != candidate.answer }
            .filter { requiredUnit.isBlank() || numericUnit(it) == requiredUnit }
            .distinct()
            .toList()
            .shuffled(Random(candidate.atomId.toLong()))
            .take(3)
        if (distractors.size < 2) return null
        val options = (distractors + candidate.answer).shuffled(Random(candidate.atomId.toLong() * 31L))
        val correctIndex = options.indexOf(candidate.answer)
        return candidate.source.copy(
            id = candidate.atomId,
            content = rebuildContent(candidate, showBlank = true),
            type = QuestionTypes.SINGLE,
            options = options,
            answer = ('A' + correctIndex).toString(),
            explanation = "自适应渐隐 · ${stageLabel(AdaptiveAtomStage.CHOICE)}\n" +
                "正确填空：${candidate.answer}\n${candidate.source.explanation}",
        )
    }

    private fun rebuildContent(candidate: Candidate, showBlank: Boolean): String {
        val matches = displayBlankRegex.findAll(candidate.source.content).toList()
        val answers = splitFillAnswerDescriptors(candidate.source.answer).map(FillAnswerPartDescriptor::answerText)
        return buildString {
            var cursor = 0
            matches.forEachIndexed { index, match ->
                append(candidate.source.content.substring(cursor, match.range.first))
                append(if (index == candidate.blankIndex && showBlank) match.value else answers.getOrElse(index) { match.value })
                cursor = match.range.last + 1
            }
            append(candidate.source.content.substring(cursor))
        }
    }

    private fun buildHint(candidate: Candidate): String {
        val tag = candidate.tag.ifBlank { "原子知识点" }
        if (candidate.tag.contains("数据") || candidate.tag.contains("参数")) {
            return listOf(tag, numericUnit(candidate.answer).takeIf(String::isNotBlank)).filterNotNull().joinToString("，")
        }
        val first = candidate.answer.firstOrNull()?.toString().orEmpty()
        return "$tag，${candidate.answer.length}字${if (first.isNotBlank()) "，首字“$first”" else ""}"
    }

    private fun stageLabel(stage: AdaptiveAtomStage): String =
        when (stage) {
            AdaptiveAtomStage.CHOICE -> "选择识别"
            AdaptiveAtomStage.HINTED -> "提示回忆"
            AdaptiveAtomStage.RECALL -> "完全回忆"
            AdaptiveAtomStage.MATURE -> "熟练复习"
        }

    private fun numericUnit(answer: String): String {
        val trimmed = answer.trim()
        if (trimmed.none(Char::isDigit)) return ""
        return numericUnitRegex.find(trimmed)?.value?.trim()?.lowercase().orEmpty()
    }
}
