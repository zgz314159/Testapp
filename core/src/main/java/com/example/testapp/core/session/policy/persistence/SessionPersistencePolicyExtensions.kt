package com.example.testapp.core.session.policy.persistence

import com.example.testapp.domain.session.persistence.SessionPersistenceConfig
import com.example.testapp.domain.session.persistence.SessionPersistenceContext
import com.example.testapp.domain.session.persistence.SessionPersistencePolicy

fun SessionPersistencePolicy.resolveConfig(
    context: SessionPersistenceContext = SessionPersistenceContext(),
): SessionPersistenceConfig = config(context)
