package com.example.testapp.core.session.registry

import kotlinx.coroutines.CoroutineScope

data class SessionCreationContext(
    val deps: SessionDeps,
    val scope: CoroutineScope,
)
