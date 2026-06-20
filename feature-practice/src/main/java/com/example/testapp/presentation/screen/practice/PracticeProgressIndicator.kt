package com.example.testapp.presentation.screen.practice

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.testapp.feature.practice.R
import com.example.testapp.uicommon.component.LocalFontFamily
import com.example.testapp.uicommon.component.LocalFontSize
import com.example.testapp.presentation.screen.practice.localizedQuestionTypeLabel
import androidx.compose.ui.res.stringResource
import com.example.testapp.domain.model.Question

@Composable
fun PracticeProgressIndicator(
    question: Question,
    currentIndex: Int,
    totalQuestions: Int,
    answeredCount: Int,
    randomPractice: Boolean,
    modifier: Modifier = Modifier
) {
    val displayedProgressCount = if (randomPractice) {
        answeredCount.coerceIn(0, totalQuestions)
    } else {
        (currentIndex + 1).coerceAtMost(totalQuestions)
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            stringResource(R.string.question_type_prefix) + localizedQuestionTypeLabel(question.type),
            fontSize = LocalFontSize.current,
            fontFamily = LocalFontFamily.current
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            "$displayedProgressCount/$totalQuestions",
            fontSize = LocalFontSize.current,
            fontFamily = LocalFontFamily.current
        )
    }
    LinearProgressIndicator(
        progress = {
            if (totalQuestions > 0) displayedProgressCount.toFloat() / totalQuestions else 0f
        },
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
    )
}

