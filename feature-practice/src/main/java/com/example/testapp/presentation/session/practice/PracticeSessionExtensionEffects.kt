package com.example.testapp.presentation.session.practice

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.example.testapp.core.session.SessionExtensionEventWiring
import com.example.testapp.core.session.SessionExtensionNotifier
import com.example.testapp.domain.session.FeatureExtension
import com.example.testapp.domain.session.QuestionSessionKind
import com.example.testapp.domain.session.SessionCommand
import com.example.testapp.domain.session.SessionEvent
import com.example.testapp.domain.session.SessionExtension
import com.example.testapp.presentation.session.browse.BrowseSessionSnapshotMapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * 裸 [PracticeSessionEngine] 路径：将 sessionState 变化桥接为 Extension 事件（对齐 AbstractPracticeQuestionSession）。
 */
@Composable
fun PracticeSessionExtensionEffects(
    bindings: PracticeScreenBindings,
    sessionKind: QuestionSessionKind?,
    extensions: List<SessionExtension>,
    onDispatch: (SessionCommand) -> Unit = { PracticeSessionCommandHandler.dispatch(bindings, it) },
) {
    val kind = sessionKind ?: return
    val featureExtensions =
        remember(extensions, kind) {
            extensions.filterIsInstance<FeatureExtension>().filter { it.supports(kind) }
        }
    if (featureExtensions.isEmpty()) return

    val parentScope = rememberCoroutineScope()
    DisposableEffect(bindings, kind, featureExtensions) {
        val effectScope = CoroutineScope(parentScope.coroutineContext + SupervisorJob())
        val notify: suspend (SessionEvent) -> Unit = { event ->
            val snapshot = BrowseSessionSnapshotMapper.toSnapshot(bindings.sessionState.value)
            SessionExtensionNotifier.notify(event, snapshot, kind, featureExtensions, onDispatch)
        }
        val questionJob =
            SessionExtensionEventWiring.launchQuestionChangedEvents(
                effectScope,
                bindings.sessionState,
            ) { index, questionId ->
                effectScope.launch {
                    notify(SessionEvent.QuestionChanged(index, questionId))
                }
            }
        val answerJob =
            SessionExtensionEventWiring.launchAnswerSubmittedEvents(
                effectScope,
                bindings.sessionState,
            ) { index, questionId ->
                effectScope.launch {
                    notify(SessionEvent.AnswerSubmitted(index, questionId))
                }
            }
        onDispose {
            questionJob.cancel()
            answerJob.cancel()
            effectScope.cancel()
        }
    }
}
