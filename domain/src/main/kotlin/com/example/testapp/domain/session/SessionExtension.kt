package com.example.testapp.domain.session

interface SessionExtension {
    fun supports(kind: QuestionSessionKind): Boolean = true
}

interface LifecycleExtension : SessionExtension {
    suspend fun onStart(context: SessionContext) {}
    suspend fun onDestroy() {}
}

interface FeatureExtension : SessionExtension {
    suspend fun onEvent(
        event: SessionEvent,
        snapshot: SessionSnapshot,
        dispatch: (SessionCommand) -> Unit = {}
    ) {}
}
