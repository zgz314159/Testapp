package com.example.testapp.core.session.policy.exit

import com.example.testapp.core.session.policy.ExitPolicyFactory
import com.example.testapp.domain.session.QuestionSessionKind
import com.example.testapp.domain.session.exit.SessionExitConfig
import com.example.testapp.domain.session.exit.SessionExitContext

fun ExitPolicyFactory.resolveConfig(
    kind: QuestionSessionKind,
    context: SessionExitContext = SessionExitContext(),
): SessionExitConfig = configForKind(kind, context)
