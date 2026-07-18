package com.example.testapp.presentation.screen.ai

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.testapp.domain.model.QuestionCorrectionApplySelection
import com.example.testapp.domain.model.QuestionCorrectionSuggestion
import com.example.testapp.feature.ai.R
import com.example.testapp.uicommon.design.AppOverlayMetrics
import com.example.testapp.uicommon.design.appOverlayContainerColor
import com.example.testapp.uicommon.design.appOverlayDialogShape

@Composable
fun AiQuestionCorrectionPreviewDialog(
    originalContent: String,
    originalOptions: List<String>,
    originalAnswer: String,
    suggestion: QuestionCorrectionSuggestion,
    onDismiss: () -> Unit,
    onApply: (QuestionCorrectionApplySelection) -> Unit,
) {
    val context = LocalContext.current
    var applyContent by remember { mutableStateOf(true) }
    var applyOptions by remember { mutableStateOf(suggestion.options.isNotEmpty()) }
    var applyAnswer by remember { mutableStateOf(true) }

    Dialog(onDismissRequest = onDismiss) {
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 640.dp),
            shape = appOverlayDialogShape(),
            colors = CardDefaults.elevatedCardColors(containerColor = appOverlayContainerColor()),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = AppOverlayMetrics.dialogElevation),
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = stringResource(R.string.ai_correct_preview_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = if (suggestion.verifiedOnline) {
                        stringResource(R.string.ai_correct_verified_online)
                    } else {
                        stringResource(R.string.ai_correct_unverified)
                    },
                    color = if (suggestion.verifiedOnline) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp),
                )
                Text(
                    text = stringResource(
                        R.string.ai_correct_confidence_format,
                        (suggestion.confidence * 100).toInt(),
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(12.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = suggestion.reason.ifBlank {
                            stringResource(R.string.ai_correct_default_reason)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    DiffBlock(
                        label = stringResource(R.string.ai_correct_field_content),
                        before = originalContent,
                        after = suggestion.content,
                        checked = applyContent,
                        onCheckedChange = { applyContent = it },
                    )
                    if (suggestion.options.isNotEmpty()) {
                        DiffBlock(
                            label = stringResource(R.string.ai_correct_field_options),
                            before = originalOptions.mapIndexed { i, o ->
                                "${'A' + i}. $o"
                            }.joinToString("\n"),
                            after = suggestion.options.mapIndexed { i, o ->
                                "${'A' + i}. $o"
                            }.joinToString("\n"),
                            checked = applyOptions,
                            onCheckedChange = { applyOptions = it },
                        )
                    }
                    DiffBlock(
                        label = stringResource(R.string.ai_correct_field_answer),
                        before = originalAnswer,
                        after = suggestion.answer,
                        checked = applyAnswer,
                        onCheckedChange = { applyAnswer = it },
                    )
                    if (suggestion.sources.isNotEmpty()) {
                        Text(
                            text = stringResource(R.string.ai_correct_sources),
                            fontWeight = FontWeight.SemiBold,
                        )
                        suggestion.sources.forEach { source ->
                            Text(
                                text = source.title.ifBlank { source.url },
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(enabled = source.url.isNotBlank()) {
                                        runCatching {
                                            context.startActivity(
                                                Intent(Intent.ACTION_VIEW, Uri.parse(source.url)),
                                            )
                                        }
                                    },
                            )
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel))
                    }
                    TextButton(
                        onClick = {
                            onApply(
                                QuestionCorrectionApplySelection(
                                    applyContent = applyContent,
                                    applyOptions = applyOptions,
                                    applyAnswer = applyAnswer,
                                    applyExplanation = false,
                                ),
                            )
                        },
                        enabled = applyContent || applyOptions || applyAnswer,
                    ) {
                        Text(stringResource(R.string.ai_correct_apply))
                    }
                }
            }
        }
    }
}

@Composable
private fun DiffBlock(
    label: String,
    before: String,
    after: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = checked, onCheckedChange = onCheckedChange)
            Text(text = label, fontWeight = FontWeight.SemiBold)
        }
        Text(
            text = stringResource(R.string.ai_correct_before_format, before.ifBlank { "—" }),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = stringResource(R.string.ai_correct_after_format, after.ifBlank { "—" }),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
