package com.example.testapp.core.session.policy.reveal

import com.example.testapp.domain.session.reveal.SessionRevealConfig
import com.example.testapp.domain.session.reveal.SessionRevealPolicy

fun SessionRevealPolicy.resolveConfig(): SessionRevealConfig = config()
