package com.example.testapp.domain.session.persistence

data class SessionPersistenceConfig(
    val persistProgress: Boolean,
    val restoreProgress: Boolean,
    val saveOnNavigation: Boolean = persistProgress
)

/** 预留 Persistence 策略上下文扩展点 */
data class SessionPersistenceContext(val reserved: Boolean = false)

fun interface SessionPersistencePolicy {
    fun config(context: SessionPersistenceContext): SessionPersistenceConfig
}
