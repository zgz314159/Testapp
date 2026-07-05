# Compose Rules

## Screen 结构

```text
*Route.kt          — Nav 入口，SessionHost，回调注入（≤80 行目标）
*Screen.kt          — 薄包装或 app 层 raw 资源注入
*ScreenContent.kt   — 主 UI（feature 模块）
components/         — 底栏、滚动区、对话框、叠层
*Effects.kt         — LaunchedEffect / collect 副作用（无业务 if 链）
*Overlays.kt        — 对话框 Host 聚合
```

## 禁止（Screen / Route）

- `DialogsHost` 在 Screen 内重复定义  
- >30 行 inline `@Composable` — 提取到 `components/`  
- >15 行 LaunchedEffect 业务链 — 改 VM / Session / Effects 文件  
- 模式布尔：`isReviewMode`、`isWrongBook` 驱动行为 — 用 `SessionCapabilities` / Route 参数转 Kind  
- 直接调用 Repository / UseCase — 经 bindings / session.handle(Command)  

## State 订阅

- UI 只 `collectAsState` / `collectAsStateWithLifecycle`  
- 不在 Composable 内 `remember { mutableStateOf }` 存业务状态  

## Session 集成

```kotlin
SessionHost(kind = kind) { session ->
    val hosted = session as AbstractPracticeQuestionSession
    PracticeScreen(bindings = hosted.bindings, sessionHosted = true, …)
}
```

- `kind` 用 `remember(keys) { QuestionSessionKind.… }`  
- 禁止在 Session 子类 `override val kind` 于父类 init 中读取 — 见 `session_rules.md`  

## 共享 UI

- Practice + Exam 共用 → `:ui-common`  
- 单 feature 专用 → `feature-*/components/`  

## 手势 / 导航

- 导航判定 → `*NavigationPipeline` + Strategy  
- BackHandler → 发 `SessionCommand.Back` 或 Route 回调  
