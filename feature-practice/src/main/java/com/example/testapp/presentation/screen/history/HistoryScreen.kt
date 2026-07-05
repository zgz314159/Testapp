package com.example.testapp.presentation.screen.history

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.testapp.feature.practice.R
import com.example.testapp.uicommon.component.LocalFontFamily
import com.example.testapp.uicommon.component.LocalFontSize
import com.example.testapp.uicommon.design.AppEmptyState
import java.time.format.DateTimeFormatter

@Composable
fun HistoryScreen(viewModel: HistoryViewModel = hiltViewModel()) {
    val historyList = viewModel.historyList.collectAsState()
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            stringResource(R.string.history_title),
            modifier = Modifier.align(Alignment.CenterHorizontally),
            fontSize = LocalFontSize.current,
            fontFamily = LocalFontFamily.current,
        )
        Spacer(modifier = Modifier.height(16.dp))
        if (historyList.value.isEmpty()) {
            AppEmptyState(
                message = stringResource(R.string.no_history),
                modifier = Modifier.weight(1f).fillMaxWidth(),
            )
        } else {
            historyList.value.forEachIndexed { idx, record ->
                val fileNameText = record.fileName?.let { "  [$it]" } ?: ""
                Text(
                    "${idx + 1}. ${record.score}/${record.total}$fileNameText  " +
                        stringResource(R.string.time_prefix) +
                        "${record.time.format(formatter)}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = LocalFontSize.current,
                        fontFamily = LocalFontFamily.current,
                    ),
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}
