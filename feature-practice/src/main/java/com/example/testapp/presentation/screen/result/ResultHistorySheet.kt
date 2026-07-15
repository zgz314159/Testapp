package com.example.testapp.presentation.screen.result



import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.testapp.domain.model.HistoryRecord
import com.example.testapp.feature.practice.R
import com.example.testapp.uicommon.design.AppContentMediumText
import com.example.testapp.uicommon.design.AppEmptyStateInline
import com.example.testapp.uicommon.design.AppLazyBottomSheet
import com.example.testapp.uicommon.design.AppSpacing
import com.example.testapp.uicommon.screen.result.formatResultHistoryLine
import java.time.format.DateTimeFormatter



@Composable
fun ResultHistorySheet(

    visible: Boolean,

    historyList: List<HistoryRecord>,

    onDismiss: () -> Unit

) {

    if (!visible) return



    val formatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss") }

    val lines = remember(historyList) {

        historyList.mapIndexed { index, record ->

            formatResultHistoryLine(index, record, record.time.format(formatter))

        }

    }



    AppLazyBottomSheet(onDismiss = onDismiss) {

        Text(

            text = "历史成绩记录",

            style = MaterialTheme.typography.titleMedium,

            modifier = Modifier.padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm)

        )

        if (lines.isEmpty()) {

            AppEmptyStateInline(

                message = stringResource(R.string.no_history),

                modifier = Modifier.fillMaxWidth()

            )

        } else {

            LazyColumn(

                modifier = Modifier.fillMaxSize(),

                contentPadding = PaddingValues(horizontal = AppSpacing.md, vertical = AppSpacing.sm)

            ) {

                itemsIndexed(lines) { index, line ->

                    AppContentMediumText(

                        text = line,

                        modifier = Modifier.padding(vertical = AppSpacing.xs)

                    )

                    if (index < lines.lastIndex) {

                        HorizontalDivider(modifier = Modifier.padding(vertical = AppSpacing.xs))

                    }

                }

            }

        }

    }

}


