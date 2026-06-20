package com.example.testapp.presentation.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.testapp.R
import com.example.testapp.domain.QuestionTypes

@Composable
fun localizedQuestionTypeLabel(type: String): String = when {
    QuestionTypes.isEssay(type) -> stringResource(R.string.essay_question)
    QuestionTypes.isComprehensive(type) -> stringResource(R.string.comprehensive_question)
    QuestionTypes.isShort(type) -> stringResource(R.string.short_answer)
    QuestionTypes.isSingle(type) -> stringResource(R.string.single_choice)
    QuestionTypes.isMulti(type) -> stringResource(R.string.multi_choice)
    QuestionTypes.isJudge(type) -> stringResource(R.string.judge_choice)
    QuestionTypes.isFill(type) -> stringResource(R.string.fill_blank)
    else -> type
}
