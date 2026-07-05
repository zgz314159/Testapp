package com.example.testapp.presentation.session.practice

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.example.testapp.core.session.registry.EmptySessionDeps
import com.example.testapp.core.session.registry.SessionCreationContext
import com.example.testapp.core.session.registry.SessionRegistry
import com.example.testapp.core.session.strategy.edit.QuestionEditSessionStrategyBootstrap
import com.example.testapp.domain.session.QuestionSessionKind
import com.example.testapp.domain.session.SessionExtension
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@EntryPoint
@InstallIn(SingletonComponent::class)
interface PracticeSessionDepsEntryPoint {
    fun practiceSessionDeps(): PracticeSessionDeps
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface PracticeSessionRegistryEntryPoint {
    fun sessionRegistry(): SessionRegistry
}

@Composable
fun rememberPracticeSessionBindings(
    sessionKind: QuestionSessionKind? = null,
    extensions: List<SessionExtension> = emptyList(),
): PracticeScreenBindings {
    val context = LocalContext.current
    val deps =
        remember(context) {
            EntryPointAccessors.fromApplication(
                context.applicationContext,
                PracticeSessionDepsEntryPoint::class.java,
            ).practiceSessionDeps()
        }
    val scope = rememberCoroutineScope()
    val bindings =
        remember(deps, scope, sessionKind) {
            PracticeSessionEngine(scope, deps).also { engine ->
                sessionKind?.let { engine.bindStrategy(it) }
            }
        }
    PracticeSessionExtensionEffects(bindings, sessionKind, extensions)
    return bindings
}

/** 抽屉单题编辑：经 SessionRegistry 创建 QuestionEditSession */
@Composable
fun rememberQuestionEditSessionBindings(
    quizId: String,
    questionId: Int,
    extensions: List<SessionExtension> = emptyList(),
): PracticeScreenBindings {
    val context = LocalContext.current
    val registry =
        remember(context) {
            EntryPointAccessors.fromApplication(
                context.applicationContext,
                PracticeSessionRegistryEntryPoint::class.java,
            ).sessionRegistry()
        }
    val scope = rememberCoroutineScope()
    val kind =
        remember(quizId, questionId) {
            QuestionEditSessionStrategyBootstrap.kind(quizId, questionId)
        }
    val session =
        remember(registry, scope, kind, extensions) {
            registry.create(
                kind = kind,
                context = SessionCreationContext(EmptySessionDeps, scope),
                extensions = extensions,
            ) as QuestionEditSession
        }
    LaunchedEffect(session) {
        session.start()
    }
    DisposableEffect(session) {
        onDispose { scope.launch { session.destroy() } }
    }
    return session.bindings
}

/** 单测 / 非 Compose 场景创建引擎 */
fun createPracticeSessionEngine(
    scope: CoroutineScope,
    deps: PracticeSessionDeps,
    sessionKind: QuestionSessionKind? = null,
): PracticeScreenBindings =
    PracticeSessionEngine(scope, deps).also { engine ->
        sessionKind?.let { engine.bindStrategy(it) }
    }

/** 单测：经 Registry 创建 QuestionEditSession */
fun createQuestionEditSession(
    registry: SessionRegistry,
    scope: CoroutineScope,
    quizId: String,
    questionId: Int,
): QuestionEditSession {
    val kind = QuestionEditSessionStrategyBootstrap.kind(quizId, questionId)
    return registry.create(
        kind = kind,
        context = SessionCreationContext(EmptySessionDeps, scope),
        extensions = emptyList(),
    ) as QuestionEditSession
}
