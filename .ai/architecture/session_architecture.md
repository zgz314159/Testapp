# Session 架构（V5 终版 · PowerAI / Testapp）

> **Canonical:** `.ai/architecture/session_architecture.md`（PowerAI Engineering OS L1）  
> 入口：`.ai/README.md` | ADR：`ADR/001-session.md`

> **原则：** 规划一步到位（接口边界），实现渐进式（BrowseSession 垂直切片先行）。  
> **评分定位：** 规划 99 / 首版落地 ~95（避免 Plugin Framework 与胖 Host）。

---

## 1. 总览

```text
NavHost (entry-scoped)
    │
    ▼
SessionHost (≤120 行)          enter / leave / session: StateFlow<QuestionSession?>
    │
    ▼
SessionRegistry (Map)          KClass<QuestionSessionKind> → SessionCreator  O(1)
    │
    ▼
QuestionSession                Practice | Browse | Review | Exam | …
    │
    ├── SessionCapabilities          （唯一行为开关源）
    ├── UiPolicyFactory → UiContract （推导，禁止手写两套）
    ├── SessionSnapshot (immutable)  Question + Ui + Analysis + Statistics
    ├── PersistenceStrategy | NavigationStrategy | RevealStrategy | ExitPolicy
    ├── SessionCommandHandler        UI / 系统输入
    ├── SessionEventFlow             Extension / Service 订阅
    └── List<SessionExtension>
            ├── LifecycleExtension   onStart / onDestroy
            └── FeatureExtension     onEvent(SessionEvent)
                    └── AiExtension（仅 Event + Snapshot，不持有 Session）
```

---

## 2. Command vs Event（CQRS-lite）

| 类型 | 方向 | 示例 |
|------|------|------|
| **SessionCommand** | UI / BackHandler → Session | `Back`, `SubmitAnswer`, `GoToQuestion(index)`, `SubmitExam` |
| **SessionEvent** | Session → Extension / 内部 | `SessionStarted`, `QuestionChanged`, `AnswerSubmitted`, `SessionDestroyed` |

- Screen **只发 Command**（经 `QuestionSession.handle(command)`）。
- Extension / Statistics / AI **只订阅 Event**。
- Session 处理 Command 后 emit Event。

---

## 3. SessionRegistry（Map，非线性 supports）

```kotlin
// core/session/SessionRegistry.kt
class SessionRegistry(
    creators: Map<KClass<out QuestionSessionKind>, SessionCreator>
) {
    fun create(kind: QuestionSessionKind, deps: SessionDeps, extensions: List<SessionExtension>): QuestionSession {
        val creator = creators[kind::class] ?: error("No creator for ${kind::class}")
        return creator.create(kind, deps, extensions)
    }
}
```

各 feature 在 Hilt Module `register`；**新增 Session 不修改 Registry 源码**。

---

## 4. Extension 分层

```kotlin
interface SessionExtension {
    fun supports(kind: QuestionSessionKind): Boolean = true
}

interface LifecycleExtension : SessionExtension {
    suspend fun onStart(context: SessionContext) {}
    suspend fun onDestroy() {}
}

interface FeatureExtension : SessionExtension {
    suspend fun onEvent(
        event: SessionEvent,
        snapshot: SessionSnapshot,
        dispatch: (SessionCommand) -> Unit = {}
    ) {}
}
```

- `SessionContext`：只读 `kind`, `capabilities`, `eventFlow`（**非**整个 Session）。
- `AiExtension : FeatureExtension` — 禁止 `QuestionSession` 类型依赖。

---

## 5. Snapshot 不可变

- 所有字段 `val`；集合用 `List` / `PersistentList`，**禁止** `MutableList` 入 Snapshot。
- Session 内部 `MutableStateFlow<PracticeSessionState>` → `map { toSnapshot() }` 每次 **新 copy**。
- 子结构：`QuestionSnapshot`, `UiSnapshot`, `AnalysisSnapshot`, `StatisticsSnapshot`。

---

## 6. Capabilities → UiContract

Session **仅**暴露 `capabilities`；`uiContract = UiPolicyFactory.from(capabilities)`。  
禁止 Session 子类同时手写 capabilities 与 bottomBar 配置。

---

## 7. 现有代码收编

| 现有 | V5 归属 |
|------|---------|
| `PracticeProgressLifecycleCoordinator` | `PracticePersistenceStrategy` |
| `NavigationController` + `NavigationHistory` | `PracticeNavigationStrategy` |
| `PracticeSessionExitPipeline` | `*ExitPolicy` |
| `PracticePinnedQuestionPipeline` | `BrowsePersistenceStrategy` |
| `PracticeScreenAnalysisSyncEffects` | `AiExtension.onEvent` |
| `globalPracticeViewModel` | 删除 → entry-scoped `SessionHost` |

---

## 8. 实施阶段

| Phase | 内容 |
|-------|------|
| P0 | 抽屉返回不交卷、Home 抽屉状态恢复、fill 门禁 |
| P1 | 取消 global VM，entry-scoped Host |
| P2a | domain/core 契约 + Registry + UiPolicyFactory + ArchTest 骨架 |
| P2b | **BrowseSession** 垂直切片 + 路由 Kind |
| P2c | PracticeSession + ReviewSession |
| P2d | ExamSession + AiExtension |
| P3 | UI/Pipeline 去重 ✅ |
| P4 | ExitPolicy 策略收编 ✅ |
| P5 | SessionAnalysisLoader（DB 读与 VM 解耦）✅ |
| P6 | 遗留 VM 退役 ✅ |
| P7 | Persistence/Navigation/Reveal Policy + SessionCommand CQRS + Flows 重命名 ✅ |
| P8 | 底栏/手势 CQRS + Navigation/Reveal Policy 接线 ✅ |
| P26–P44 | Strategy 收编 / Extension / 裸引擎 / ktlint CI ✅（见 `change_log.md`） |

**设备验证：** [K001_DEVICE_SMOKE.md](K001_DEVICE_SMOKE.md)（**CLOSED** — 2026-07-05 PASS）· [K007_EXAM_ROUTE_SMOKE.md](K007_EXAM_ROUTE_SMOKE.md)（**CLOSED**）

**长尾（按需）：** Coordinator 实体迁入 Strategy；`DrawerQuestionEdit`；Phase 10 `:app` VM 外置。

---

## 9. 架构测试与 ADR

- 规则见 `scripts/arch-test/` 与 `.ai/ADR/005-architecture-tests.md`
- 决策记录：`.ai/ADR/001` ~ `004`

---

## 10. LOC 红线（Architecture Guard）

| 类型 | 上限 |
|------|------|
| SessionHost / HostVM | 120 |
| 单个 QuestionSession | 300 |
| SessionCreator | 80 |
| Strategy 实现 | 200 |
| UiPolicyFactory | 120 |
