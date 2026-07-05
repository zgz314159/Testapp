package com.example.testapp.domain.session.exit

fun interface SessionExitPolicy {
    fun resolve(request: SessionExitRequest): SessionExitAction
}
