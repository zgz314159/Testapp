# ADR-005: 架构测试（ArchUnit + Detekt）

## Status

Accepted (2026-07-05)

## Context

- 文档约束易被违反；feature 互引、Session 引用 Compose 会导致腐化。

## Decision

在 **P2a** 引入 `:core:arch-test` 或 `app/src/test` ArchUnit 套件 + Detekt 自定义规则。

### ArchUnit 规则（首批）

| ID | 规则 |
|----|------|
| AT-01 | `feature-practice` 不得依赖 `feature-exam`（反之亦然） |
| AT-02 | `domain` 不得依赖 `android.*` / `androidx.compose.*` |
| AT-03 | `..session..` 包不得依赖 `androidx.compose.*` |
| AT-04 | `SessionExtension` 实现不得依赖 `QuestionSession` 具体类（仅 Context/Event/Snapshot） |
| AT-05 | `BrowseSession` 不得调用 `PracticeSessionExitPipeline.ShowSubmitDialog` 路径（Browse 专用 ExitPolicy 单测 + 可选 Arch 命名约定） |

### Detekt 规则（首批）

| ID | 规则 |
|----|------|
| DT-01 | 禁止 `PracticeScreen` 新增 `isXxxMode` 布尔参数（regex / 自定义 Rule） |
| DT-02 | 单文件 LOC >500 失败（与 `check-loc-over-500.ps1` 对齐） |
| DT-03 | `SessionHost` / `QuestionSessionHostViewModel` 不得出现 `fun submit` / `fun goTo`（Host 保持瘦） |

### 执行

```powershell
./gradlew :app:testDebugUnitTest --tests "*.ArchTest*"
./gradlew detekt
```

CI 与 pre-commit 按需接入。

## Consequences

- 架构边界可执行；ADR 与测试互为文档。

## Alternatives Considered

- 仅文档 — 不足，历史已发生 VM 3900 行回潮。
