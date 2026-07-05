package com.example.testapp.core.session.registry

/** 会话创建依赖；P2b 起由 feature Creator 注入 UseCase / SessionEngine 等 */
interface SessionDeps

object EmptySessionDeps : SessionDeps
