# Module Rules

> Gradle 模块归属与边界。详情见 `current/module_map.md`、`current/architecture_map.md`。

## 模块职责

| 模块 | 放什么 | 禁止 |
|------|--------|------|
| `:domain` | 模型、Repository 接口、纯 UseCase | Android、Room、Compose |
| `:data` | Room、DataStore、Repository 实现、Parser | UI、导航 |
| `:core` | SessionEngine、Policy、Strategy、共享 Pipeline、FontSettings 接口 | Screen、Hilt Module（除纯 binding） |
| `:feature-practice` | Practice/Review/Browse Session、PracticeScreenContent、Practice 组件 | Exam 专用逻辑 |
| `:feature-exam` | Exam Session、ExamScreenContent、Exam 组件 | Practice 专用逻辑 |
| `:feature-ai` | AI Screen / VM / 网络封装 | 会话核心逻辑 |
| `:feature-settings` | Settings Screen、Import/Export Coordinator | Session 行为 |
| `:ui-common` | Practice↔Exam 共享 Composable | 业务 Pipeline |
| `:app` | NavHost、Route 薄包装、Hilt 根模块、EntryPoint、raw 资源注入 | 大块业务、新 God Screen |

## 依赖方向

```
:app → :feature-* → :core → :domain
:app → :data → :domain
:feature-* ↛ :app
:domain ↛ 任何上层
```

## 新功能落点决策树

```text
是否 Session 行为？
  是 → 新 Kind + Creator + ADR，或 Extension/Command
  否 → 是否持久化？
    是 → :data Repository / Parser
    否 → 是否跨 Practice+Exam UI？
      是 → :ui-common
      否 → 对应 :feature-*
```

## `:app` 瘦身目标

- `:app` 行数占比 ≤40%（当前 ~18%，保持）  
- 新 Screen Content 进 feature；`:app` 只保留 Route + 回调注入  

## 触达时必读

- `current/module_map.md` — 文件分布热点  
- `PROJECT_CONSTITUTION.md` — 依赖宪法  
