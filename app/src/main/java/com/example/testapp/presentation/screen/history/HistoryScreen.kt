package com.example.testapp.presentation.screen.history

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.res.stringResource
import com.example.testapp.R
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.time.format.DateTimeFormatter
import com.example.testapp.uicommon.component.LocalFontSize
import com.example.testapp.uicommon.component.LocalFontFamily

@Composable
fun HistoryScreen(viewModel: HistoryViewModel = hiltViewModel()) {
    val historyList = viewModel.historyList.collectAsState()
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(stringResource(R.string.history_title), modifier = Modifier.align(Alignment.CenterHorizontally), fontSize = LocalFontSize.current, fontFamily = LocalFontFamily.current)
        Spacer(modifier = Modifier.height(16.dp))
        if (historyList.value.isEmpty()) {
            Text(
                stringResource(R.string.no_history),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = LocalFontSize.current,
                    fontFamily = LocalFontFamily.current
                ),
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            historyList.value.forEachIndexed { idx, record ->
                val fileNameText = record.fileName?.let { "  [$it]" } ?: ""
                Text(
                    "${idx + 1}. ${record.score}/${record.total}$fileNameText  " + stringResource(R.string.time_prefix) + "${record.time.format(formatter)}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = LocalFontSize.current,
                        fontFamily = LocalFontFamily.current
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

