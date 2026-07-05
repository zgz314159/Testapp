# PowerAI Project Constitution

> **最高优先级。** 当 `.ai/rules/`、`.cursor/rules/`、ADR 或任务指令发生冲突时，以本文件为准。

## 不可违反的原则

1. **V5 Session 架构是唯一核心行为边界**  
   `SessionHost` → `SessionRegistry` → `QuestionSession`；Screen 只渲染、只发 `SessionCommand`，不发模式 if。

2. **禁止未经 ADR 批准的核心抽象**  
   新 Manager / Controller / GlobalState / 胖 Host / 第二套行为开关，必须先写 ADR。

3. **必须优先复用已有能力**  
   优先：`Session`、`Strategy`、`Extension`、`Service`、`Capabilities`、`Pipeline`、`Coordinator`。  
   禁止复制整块 `Pipeline` / `Coordinator` / `Repository` / `Service`。

4. **架构修改必须同步文档**  
   触及模块边界、Session 契约、依赖方向时，同步更新：`.ai/ADR/`、`.ai/current/`、`.ai/change_log.md`（或 `.ai/current/change_log.md`）。

5. **编码前必须通过 Architecture Guard**  
   未完成 Guard 六步（Scan → Dependency → Reuse → ADR → Impact → Plan），**禁止写实现代码**。

6. **单一数据流 + 无状态管道**  
   UI 订阅 StateFlow；>15 行纯业务判定进 `*Pipeline.kt` + 单测；副作用集中在 VM / Coordinator / Session。

7. **LOC 红线不可绕过**  
   预测 `当前 LOC + 新增` 触线 → 先拆再改；全仓库 `>500` 行进入 `loc_audit` 待拆清单。

8. **上下文加载顺序不可跳过**  
   见 `.ai/README.md`；禁止直接读 Task 描述就开始编码。

## 模块依赖宪法

```
:domain     — 无 Android 依赖；模型 + 接口 + 纯 UseCase
:data       → :domain
:core       → :domain（session engine、policy、pipeline）
:feature-*  → :core, :domain, :ui-common（按 feature）
:app        → feature 模块 + :data + :domain（薄壳：路由、DI、Hilt EntryPoint）
```

**禁止**：`:domain` 依赖 `:app` / `:data`；feature 间横向耦合；在 `:app` 堆业务逻辑。

## 决策层级

| 层级 | 文档 | 用途 |
|------|------|------|
| L0 宪法 | `PROJECT_CONSTITUTION.md` | 冲突裁决 |
| L1 架构 | `.ai/architecture/` | 长期结构规则 |
| L2 ADR | `.ai/ADR/` | 已接受决策记录 |
| L3 现状 | `.ai/current/` | 可变的地图 / 状态 / LOC |
| L4 流程 | `.ai/workflows/` | 每次任务怎么执行 |
| L5 细则 | `.ai/rules/` | 命名、Compose、Session 编码规范 |

## 元数据

| 字段 | 值 |
|------|-----|
| 项目 | PowerAI / Testapp |
| 架构版本 | V5 Session |
| 生效 | 2026-07-05 |
| 维护 | 架构变更时由负责人或 Agent 同步更新 |
