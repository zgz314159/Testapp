package com.example.testapp.presentation.screen.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.testapp.uicommon.component.LocalFontFamily
import com.example.testapp.uicommon.component.LocalFontSize
import com.example.testapp.uicommon.design.AppSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeStartQuizSheet(
    visible: Boolean,
    pendingFileName: String,
    bottomNavIndex: Int,
    onDismiss: () -> Unit,
    onStartQuiz: (String) -> Unit,
    onStartExam: (String) -> Unit,
    onStartWrongBookQuiz: (String) -> Unit,
    onStartWrongBookExam: (String) -> Unit,
    onStartFavoriteQuiz: (String) -> Unit,
    onStartFavoriteExam: (String) -> Unit
) {
    if (!visible) return

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .padding(
                    top = AppSpacing.md,
                    bottom = AppSpacing.lg,
                    start = AppSpacing.lg,
                    end = AppSpacing.lg
                )
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = pendingFileName,
                fontSize = LocalFontSize.current,
                fontFamily = LocalFontFamily.current
            )
            Spacer(Modifier.height(AppSpacing.lg))
            Button(
                onClick = {
                    onDismiss()
                    when (bottomNavIndex) {
                        0 -> onStartWrongBookQuiz(pendingFileName)
                        1 -> onStartFavoriteQuiz(pendingFileName)
                        else -> onStartQuiz(pendingFileName)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("开始练习")
            }
            Spacer(Modifier.height(AppSpacing.sm))
            Button(
                onClick = {
                    onDismiss()
                    when (bottomNavIndex) {
                        0 -> onStartWrongBookExam(pendingFileName)
                        1 -> onStartFavoriteExam(pendingFileName)
                        else -> onStartExam(pendingFileName)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("开始考试")
            }
            Spacer(Modifier.height(AppSpacing.sm))
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("取消")
            }
        }
    }
}
