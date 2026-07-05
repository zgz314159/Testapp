package com.example.testapp.presentation.session.host

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.core.session.registry.EmptySessionDeps
import com.example.testapp.core.session.registry.SessionCreationContext
import com.example.testapp.core.session.registry.SessionRegistry
import com.example.testapp.domain.session.QuestionSession
import com.example.testapp.domain.session.QuestionSessionHost
import com.example.testapp.domain.session.QuestionSessionKind
import com.example.testapp.domain.session.SessionExtension
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class QuestionSessionHostViewModel
    @Inject
    constructor(
        private val registry: SessionRegistry,
        private val extensions: Set<@JvmSuppressWildcards SessionExtension>,
    ) : ViewModel(), QuestionSessionHost {
        private val _session = MutableStateFlow<QuestionSession?>(null)
        override val session: StateFlow<QuestionSession?> = _session.asStateFlow()

        override suspend fun enter(kind: QuestionSessionKind) {
            leave()
            if (!registry.hasCreator(kind)) {
                error("No SessionCreator for ${kind::class.simpleName}")
            }
            val created =
                registry.create(
                    kind = kind,
                    context = SessionCreationContext(EmptySessionDeps, viewModelScope),
                    extensions = extensions.toList(),
                )
            created.start()
            _session.value = created
        }

        override suspend fun leave() {
            _session.value?.destroy()
            _session.value = null
        }
    }
