# Dependency Rules

> 文件级与模块级依赖约束。活数据：`current/dependency_graph.md`。

## 模块级

- **单向**：`app → feature → core → domain`，`data → domain`  
- **禁止循环**：任何 Gradle `implementation` 回指  
- **feature 隔离**：`:feature-practice` 与 `:feature-exam` 不互相依赖  

## 文件级注入红线

| 依赖数 | 状态 | 动作 |
|--------|------|------|
| ≤5 | 🟢 | 允许 |
| 6–8 | 🟡 | 优先 Facade |
| >8 | 🔴 | 必须先 Coordinator / UseCase Facade |

## 典型 Facade（已存在，优先用）

- `PracticeUseCaseFacade` / `ExamUseCaseFacade`  
- `SessionEngine` + `SessionProgressManager` + `SessionAnalysisLoader`  
- `*SessionDeps` — Session 创建期依赖束  

## 禁止依赖模式

- Screen → Repository 直接（经 Session / VM / UseCase）  
- Pipeline → Compose / Android Context  
- `:domain` → `android.*` / `androidx.*`  
- 复制 16 个 `@Inject` 构造函数而不抽 Facade  

## 扫描方法

1. 读 `current/dependency_graph.md` 热点表  
2. 只展开触达文件的 **直接 import**  
3. 新注入前数 constructor 参数 + 字段依赖  

## DI 规则

- Hilt Module 在 `:app` 或 feature 的 `di/`  
- Session Creator 在 `SessionRegistryModule` 注册  
- EntryPoint 仅用于 Compose 无法 `@Inject` 的边界  
