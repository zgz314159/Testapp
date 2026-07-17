package com.example.testapp.presentation.screen.practice.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import com.example.testapp.domain.QuestionTypes
import com.example.testapp.domain.model.Question
import com.example.testapp.feature.practice.R
import com.example.testapp.presentation.screen.practice.PracticeAutoAdvanceController
import com.example.testapp.presentation.screen.practice.PracticeFontController
import com.example.testapp.presentation.screen.practice.PracticeQuestionContent
import com.example.testapp.presentation.screen.practice.PracticeResultSection
import com.example.testapp.presentation.screen.practice.localizedQuestionTypeLabel
import com.example.testapp.presentation.session.practice.PracticeScreenBindings
import com.example.testapp.uicommon.component.FillAnswerRoundLabel
import com.example.testapp.uicommon.component.LocalFontFamily
import com.example.testapp.uicommon.component.LocalFontSize
import com.example.testapp.uicommon.design.AnalysisSectionTone
import com.example.testapp.uicommon.design.AppCard
import com.example.testapp.uicommon.design.AppSpacing
import com.example.testapp.uicommon.design.QuestionSessionActionRow
import com.example.testapp.uicommon.design.QuestionSessionHeader
import com.example.testapp.uicommon.design.QuestionSessionSideAction
import com.example.testapp.uicommon.util.buildPracticeAnswerResult

@Composable
fun PracticeScreenQuestionScrollContent(
    questions: List<Question>,
    question: Question,
    currentIndex: Int,
    isReviewMode: Boolean,
    textAnswer: String,
    showResult: Boolean,
    resultDisplayReady: Boolean,
    selectedOption: List<Int>,
    displayOptions: List<String>,
    resolvedFillAnswer: String,
    correctIndices: List<Int>,
    fc: PracticeFontController,
    questionCopyText: String,
    analysisText: String?,
    sparkText: String?,
    baiduText: String?,
    noteList: List<String>,
    bindings: PracticeScreenBindings,
    autoAdvance: PracticeAutoAdvanceController,
    onOpenQuestionList: () -> Unit,
    onRetryCurrent: () -> Unit,
    onRetryWrongBlanks: () -> Unit,
    onViewExplanation: (String) -> Unit,
    onEditCorrectAnswer: (String, Int, Int) -> Unit,
    onEditNote: (String, Int, Int) -> Unit,
    onViewDeepSeek: (String, Int, Int) -> Unit,
    onViewSpark: (String, Int, Int) -> Unit,
    onViewBaidu: (String, Int, Int) -> Unit,
    onDeleteExplanation: () -> Unit,
    onDeleteNote: () -> Unit,
    onDeleteAnalysis: (String) -> Unit,
    onTextAnswerChange: (String) -> Unit,
    onOptionClick: (Int) -> Unit,
    onSubmitOption: (Int?) -> Unit
) {
    Spacer(modifier = Modifier.height(AppSpacing.lg))
    QuestionSessionHeader(
        questionTypeLabel = stringResource(R.string.question_type_prefix) +
            localizedQuestionTypeLabel(question.type),
        currentIndex = currentIndex,
        total = questions.size,
        questionListLabel = stringResource(R.string.total_questions, questions.size),
        onOpenQuestionList = onOpenQuestionList
    ) {
        FillAnswerRoundLabel(
            questionId = question.id,
            sessionQuestionIds = questions.map { it.id },
            modifier = Modifier.padding(bottom = AppSpacing.sm)
        ) { round, total ->
            Text(
                text = stringResource(R.string.fill_full_answer_round_template, round, total),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = (LocalFontSize.current.value - 1f).coerceAtLeast(12f).sp,
                    fontFamily = LocalFontFamily.current,
                    color = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
    Spacer(modifier = Modifier.height(AppSpacing.sm))
    AppCard {
        key(currentIndex) {
            PracticeQuestionContent(
                question = question,
                textAnswer = textAnswer,
                showResult = showResult,
                selectedOption = selectedOption,
                displayOptions = displayOptions,
                resolvedFillAnswer = resolvedFillAnswer,
                questionFontSize = fc.questionFontSize,
                questionLineSpacing = fc.questionLineSpacing,
                questionLetterSpacing = fc.questionLetterSpacing,
                onAnswerChange = if (isReviewMode) { {} } else onTextAnswerChange,
                onOptionClick = if (isReviewMode) { {} } else onOptionClick,
                submitCurrentAnswer = if (isReviewMode) { {} } else onSubmitOption
            )
        }
    }
    val retryCurrentLabel = stringResource(R.string.retry_current_question)
    val retryWrongLabel = stringResource(R.string.retry_wrong_blanks)
    QuestionSessionActionRow(
        questionCopyText = questionCopyText,
        leadingAction = if (showResult && !isReviewMode) {
            QuestionSessionSideAction(
                contentDescription = retryCurrentLabel,
                onClick = {
                    autoAdvance.cancel()
                    onRetryCurrent()
                }
            )
        } else {
            null
        },
        trailingAction = if (showResult && !isReviewMode && QuestionTypes.isInlineBlank(question.type)) {
            QuestionSessionSideAction(
                contentDescription = retryWrongLabel,
                onClick = {
                    autoAdvance.cancel()
                    onRetryWrongBlanks()
                }
            )
        } else {
            null
        }
    )

    if (resultDisplayReady) {
        val answerResult = buildPracticeAnswerResult(
            question = question,
            textAnswer = textAnswer,
            selectedOption = selectedOption,
            resolvedFillAnswer = resolvedFillAnswer,
            displayOptions = displayOptions,
            correctIndices = correctIndices
        )
        val answerResultText = if (answerResult.allCorrect) {
            stringResource(R.string.answer_correct)
        } else {
            stringResource(R.string.answer_wrong_format, answerResult.correctText)
        }
        PracticeResultSection(
            question = question,
            showResult = showResult,
            textAnswer = textAnswer,
            resolvedFillAnswer = resolvedFillAnswer,
            correctIndices = correctIndices,
            displayOptions = displayOptions,
            selectedOption = selectedOption,
            questionFontSize = fc.questionFontSize,
            questionLineSpacing = fc.questionLineSpacing,
            questionLetterSpacing = fc.questionLetterSpacing,
            allCorrect = answerResult.allCorrect,
            answerResultText = answerResultText,
            onInteraction = { autoAdvance.cancel() },
            onDoubleTap = {
                autoAdvance.cancel()
                val rawAnswer = question.answer.ifBlank { resolvedFillAnswer }
                onEditCorrectAnswer(rawAnswer.ifBlank { " " }, question.id, currentIndex)
            },
        )
        val analysisPrefix = stringResource(R.string.analysis_prefix)
        val cancelAutoAdvance = { autoAdvance.cancel() }
        PracticeRichAnalysisSection(
            text = question.explanation.takeIf { it.isNotBlank() },
            tone = AnalysisSectionTone.Explanation,
            label = analysisPrefix,
            fontSize = fc.questionFontSize,
            lineHeight = fc.questionLineSpacing,
            letterSpacing = fc.questionLetterSpacing,
            onInteraction = cancelAutoAdvance,
            onDoubleTap = { onViewExplanation(analysisPrefix + question.explanation) },
            onLongPress = onDeleteExplanation
        )
        val note = noteList.getOrNull(currentIndex)
        PracticeRichAnalysisSection(
            text = note?.takeIf { it.isNotBlank() },
            tone = AnalysisSectionTone.Note,
            label = stringResource(R.string.note_prefix),
            fontSize = fc.questionFontSize,
            lineHeight = fc.questionLineSpacing,
            letterSpacing = fc.questionLetterSpacing,
            onInteraction = cancelAutoAdvance,
            onDoubleTap = { onEditNote(note!!, question.id, currentIndex) },
            onLongPress = onDeleteNote
        )
        PracticeRichAnalysisSection(
            text = analysisText?.takeIf { it.isNotBlank() },
            tone = AnalysisSectionTone.DeepSeek,
            label = stringResource(R.string.practice_section_deepseek),
            fontSize = fc.questionFontSize,
            lineHeight = fc.questionLineSpacing,
            letterSpacing = fc.questionLetterSpacing,
            onInteraction = cancelAutoAdvance,
            onDoubleTap = { onViewDeepSeek(questionCopyText, question.id, currentIndex) },
            onLongPress = { onDeleteAnalysis("deepseek") }
        )
        PracticeRichAnalysisSection(
            text = sparkText?.takeIf { it.isNotBlank() },
            tone = AnalysisSectionTone.Spark,
            label = stringResource(R.string.practice_section_spark),
            fontSize = fc.questionFontSize,
            lineHeight = fc.questionLineSpacing,
            letterSpacing = fc.questionLetterSpacing,
            onInteraction = cancelAutoAdvance,
            onDoubleTap = { onViewSpark(sparkText ?: "", question.id, currentIndex) },
            onLongPress = { onDeleteAnalysis("spark") }
        )
        PracticeRichAnalysisSection(
            text = baiduText?.takeIf { it.isNotBlank() },
            tone = AnalysisSectionTone.Baidu,
            label = stringResource(R.string.practice_section_baidu),
            fontSize = fc.questionFontSize,
            lineHeight = fc.questionLineSpacing,
            letterSpacing = fc.questionLetterSpacing,
            onInteraction = cancelAutoAdvance,
            onDoubleTap = { onViewBaidu(baiduText ?: "", question.id, currentIndex) },
            onLongPress = { onDeleteAnalysis("baidu") }
        )
    } else {
        Spacer(modifier = Modifier.height(AppSpacing.md))
    }
    Spacer(modifier = Modifier.height(AppSpacing.lg))
}
