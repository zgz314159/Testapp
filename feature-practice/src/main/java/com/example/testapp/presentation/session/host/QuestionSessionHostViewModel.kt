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

        /**
         * 同 kind 已在会话中则复用（AI 全屏等子路由 dispose 后再 enter 时不得重建，否则会回到第 1 题）。
         * 真正离开练习/考试路由时由 [onCleared] 销毁。
         */
        override suspend fun enter(kind: QuestionSessionKind) {
            val existing = _session.value
            if (SessionHostEnterReusePipeline.shouldReuseExisting(existing?.kind, kind)) {
                return
            }
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

        override fun onCleared() {
            val active = _session.value
            _session.value = null
            if (active != null) {
                kotlinx.coroutines.runBlocking {
                    runCatching { active.destroy() }
                }
            }
            super.onCleared()
        }
    }
