package com.example.testapp.uicommon.design

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.testapp.uicommon.R

@Composable
fun QuestionCopyActionRow(
    questionCopyText: String,
    modifier: Modifier = Modifier,
    successMessage: String = stringResource(R.string.uicommon_copy_question_success),
    contentDescription: String = stringResource(R.string.uicommon_copy_question)
) {
    QuestionSessionActionRow(
        questionCopyText = questionCopyText,
        modifier = modifier,
        copySuccessMessage = successMessage,
        copyContentDescription = contentDescription
    )
}
