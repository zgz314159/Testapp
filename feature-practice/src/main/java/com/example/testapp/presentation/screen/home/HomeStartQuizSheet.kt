package com.example.testapp.presentation.screen.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.testapp.feature.practice.R
import com.example.testapp.uicommon.component.LocalFontFamily
import com.example.testapp.uicommon.component.LocalFontSize
import com.example.testapp.uicommon.design.AppSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeStartQuizSheet(
    visible: Boolean,
    pendingFileName: String,
    hasProgress: Boolean,
    onDismiss: () -> Unit,
    onStartQuiz: (String) -> Unit,
    onStartExam: (String) -> Unit,
    onRestart: (String) -> Unit,
) {
    if (!visible) return

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .padding(
                    top = AppSpacing.md,
                    bottom = AppSpacing.lg,
                    start = AppSpacing.lg,
                    end = AppSpacing.lg,
                )
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = pendingFileName,
                fontSize = LocalFontSize.current,
                fontFamily = LocalFontFamily.current,
            )
            Spacer(modifier = Modifier.height(AppSpacing.lg))
            Button(
                onClick = {
                    onDismiss()
                    onStartQuiz(pendingFileName)
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    if (hasProgress) {
                        stringResource(R.string.home_continue_practice)
                    } else {
                        stringResource(R.string.home_start_practice)
                    },
                )
            }
            Spacer(modifier = Modifier.height(AppSpacing.sm))
            Button(
                onClick = {
                    onDismiss()
                    onStartExam(pendingFileName)
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    if (hasProgress) {
                        stringResource(R.string.home_continue_exam)
                    } else {
                        stringResource(R.string.home_start_exam)
                    },
                )
            }
            if (hasProgress) {
                Spacer(modifier = Modifier.height(AppSpacing.sm))
                OutlinedButton(
                    onClick = {
                        onRestart(pendingFileName)
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.home_restart_quiz))
                }
            }
            Spacer(modifier = Modifier.height(AppSpacing.sm))
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.cancel))
            }
        }
    }
}
