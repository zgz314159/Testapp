package com.example.testapp.core.session.policy.navigation

import com.example.testapp.core.session.strategy.navigation.SessionNavigationOrchestrationResolver
import com.example.testapp.domain.session.navigation.SessionNavigationHistoryPhases
import com.example.testapp.domain.session.navigation.SessionNavigationOrchestration
import com.example.testapp.domain.session.navigation.SessionNavigationPolicy
import com.example.testapp.domain.session.navigation.SessionPostAnswerPhases

fun SessionNavigationPolicy.orchestration(): SessionNavigationOrchestration =
    SessionNavigationOrchestrationResolver.from(config())

fun SessionNavigationPolicy.postAnswerPhases(): SessionPostAnswerPhases = SessionPostAnswerPhases.from(orchestration())

fun SessionNavigationPolicy.historyPhases(): SessionNavigationHistoryPhases =
    SessionNavigationHistoryPhases.from(orchestration())
