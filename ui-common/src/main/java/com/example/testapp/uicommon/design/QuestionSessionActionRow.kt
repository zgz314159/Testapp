package com.example.testapp.uicommon.design



import android.content.ClipData
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.example.testapp.uicommon.R
import kotlinx.coroutines.launch



data class QuestionSessionSideAction(

    val contentDescription: String,

    val onClick: () -> Unit,

    val icon: ImageVector = Icons.Filled.Refresh

)



@Composable
fun QuestionSessionActionRow(

    questionCopyText: String,

    modifier: Modifier = Modifier,

    leadingAction: QuestionSessionSideAction? = null,

    trailingAction: QuestionSessionSideAction? = null,

    copySuccessMessage: String = stringResource(R.string.uicommon_copy_question_success),

    copyContentDescription: String = stringResource(R.string.uicommon_copy_question)

) {

    if (questionCopyText.isBlank() && leadingAction == null && trailingAction == null) return



    val context = LocalContext.current

    val clipboard = LocalClipboard.current

    val clipboardScope = rememberCoroutineScope()



    Row(

        modifier = modifier.fillMaxWidth(),

        verticalAlignment = Alignment.CenterVertically

    ) {

        Box(

            modifier = Modifier.weight(1f),

            contentAlignment = Alignment.CenterStart

        ) {

            leadingAction?.let { action ->

                IconButton(onClick = action.onClick) {

                    Icon(

                        imageVector = action.icon,

                        contentDescription = action.contentDescription

                    )

                }

            }

        }

        if (questionCopyText.isNotBlank()) {

            IconButton(onClick = {

                clipboardScope.launch {

                    clipboard.setClipEntry(

                        ClipEntry(ClipData.newPlainText("question", questionCopyText))

                    )

                    Toast.makeText(context, copySuccessMessage, Toast.LENGTH_SHORT).show()

                }

            }) {

                Icon(

                    imageVector = Icons.Filled.ContentCopy,

                    contentDescription = copyContentDescription

                )

            }

        }

        Box(

            modifier = Modifier.weight(1f),

            contentAlignment = Alignment.CenterEnd

        ) {

            trailingAction?.let { action ->

                IconButton(onClick = action.onClick) {

                    Icon(

                        imageVector = action.icon,

                        contentDescription = action.contentDescription

                    )

                }

            }

        }

    }

}


