package com.example.testapp.core.session.policy.persistence

import com.example.testapp.domain.session.persistence.SessionPersistenceConfig
import com.example.testapp.domain.session.persistence.SessionPersistenceContext
import com.example.testapp.domain.session.persistence.SessionPersistencePolicy

object BrowsePersistencePolicy : SessionPersistencePolicy {
    override fun config(context: SessionPersistenceContext) =
        SessionPersistenceConfig(
            persistProgress = false,
            restoreProgress = false,
            saveOnNavigation = false,
        )
}

object PracticePersistencePolicy : SessionPersistencePolicy {
    override fun config(context: SessionPersistenceContext): SessionPersistenceConfig =
        SessionPersistenceConfig(
            persistProgress = true,
            restoreProgress = true,
            saveOnNavigation = true,
        )
}

object ReviewPersistencePolicy : SessionPersistencePolicy {
    override fun config(context: SessionPersistenceContext) =
        SessionPersistenceConfig(
            persistProgress = false,
            restoreProgress = true,
            saveOnNavigation = false,
        )
}

object ExamPersistencePolicy : SessionPersistencePolicy {
    override fun config(context: SessionPersistenceContext) =
        SessionPersistenceConfig(
            persistProgress = true,
            restoreProgress = true,
            saveOnNavigation = true,
        )
}
