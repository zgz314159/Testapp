package com.example.testapp.core.session

import com.example.testapp.domain.session.FeatureExtension
import com.example.testapp.domain.session.QuestionSessionKind
import com.example.testapp.domain.session.SessionCommand
import com.example.testapp.domain.session.SessionEvent
import com.example.testapp.domain.session.SessionSnapshot

object SessionExtensionNotifier {
    suspend fun notify(
        event: SessionEvent,
        snapshot: SessionSnapshot,
        kind: QuestionSessionKind,
        extensions: List<FeatureExtension>,
        dispatch: (SessionCommand) -> Unit,
    ) {
        extensions.filter { it.supports(kind) }.forEach { ext ->
            ext.onEvent(event, snapshot, dispatch)
        }
    }
}
