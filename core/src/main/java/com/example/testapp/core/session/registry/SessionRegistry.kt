package com.example.testapp.core.session.registry

import com.example.testapp.domain.session.QuestionSession
import com.example.testapp.domain.session.QuestionSessionKind
import com.example.testapp.domain.session.SessionExtension
import kotlin.reflect.KClass

class SessionRegistry(
    private val creators: Map<KClass<out QuestionSessionKind>, SessionCreator>,
) {
    fun create(
        kind: QuestionSessionKind,
        context: SessionCreationContext,
        extensions: List<SessionExtension> = emptyList(),
    ): QuestionSession {
        val creator =
            creators[kind::class]
                ?: error("No SessionCreator registered for ${kind::class.simpleName}")
        return creator.create(kind, context, extensions)
    }

    fun hasCreator(kind: QuestionSessionKind): Boolean = creators.containsKey(kind::class)

    class Builder {
        private val creators = mutableMapOf<KClass<out QuestionSessionKind>, SessionCreator>()

        fun register(
            kindClass: KClass<out QuestionSessionKind>,
            creator: SessionCreator,
        ): Builder {
            creators[kindClass] = creator
            return this
        }

        fun build(): SessionRegistry = SessionRegistry(creators.toMap())
    }

    companion object {
        fun builder(): Builder = Builder()
    }
}
