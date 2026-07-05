# Session Rules

> V5 Session 编码约束。架构全文：`architecture/session_architecture.md`。ADR：`ADR/001-session.md`、`002-capabilities.md`、`003-ui-policy.md`、`004-extension.md`。

## 核心契约

| 概念 | 规则 |
|------|------|
| `QuestionSession` | 行为边界；含 capabilities、snapshot、events、handle(Command) |
| `SessionHost` | 仅 enter/leave；≤120 行；不转发 submit/goto |
| `SessionRegistry` | `KClass<Kind> → Creator`；O(1) |
| `SessionCommand` | UI → Session（CQRS 写侧） |
| `SessionEvent` | Session → Extension（读侧通知） |
| `SessionCapabilities` | **唯一**行为开关源；禁 Screen 双套 if |
| `SessionExtension` | FeatureExtension 订阅 Event；LifecycleExtension onStart/onDestroy |

## 新增模式 checklist

- [ ] `QuestionSessionKind` 新 data class  
- [ ] `*Session` + `*SessionCreator`  
- [ ] `SessionRegistryModule.register`  
- [ ] `SessionCapabilitiesPresets.forKind`  
- [ ] `UiPolicyFactory.from(capabilities)`  
- [ ] Route + `SessionHost`  
- [ ] ADR 若新 Capability 或行为开关  

## 初始化陷阱（必读）

子类 `override val kind` 时，**父类属性初始化器 / init 块不能读 `kind`** — JVM 上子类字段尚未赋值会为 null。

```kotlin
// ✅ 父类用独立构造参数
abstract class AbstractFooSession(
    sessionKind: QuestionSessionKind,
) : QuestionSession {
    final override val kind = sessionKind
    override val capabilities = SessionCapabilitiesPresets.forKind(sessionKind)
    init { engine.bindStrategy(sessionKind) }
}

// ❌ 父类 override val kind + 子类再 override → init 中 kind 可能 null
```

## Command 分发

- Screen / Effects → `session.handle(SessionCommand.…)`  
- 禁止 Screen 直接调 `bindings.engine.internal…`（除 bindings 公开 API）  

## Extension

- AI 写回：`SessionCommand.AppendNote` 等，经 `AppNavAiWritebackPipeline`  
- Extension 不持有 Session 引用；只收 Event + Snapshot  

## 测试

- `SessionRegistryTest` — Creator 注册  
- `ArchitectureTest` — 模块与 Session 边界  
- Policy/Strategy — `*PipelineTest` / `*FactoryTest`  

## 禁止

- 胖 `QuestionSessionController`  
- BrowseScreen 复制整屏 UI  
- Extension 内直接改 Engine 内部状态（应 Command）  
- 绕过 Capabilities 在 Screen 写 `if (isReview)`  
