# Coding Principles

> PowerAI 编码五条 + LOC 红线。Guard 执行时对照本文件。

## 五条设计原则

1. **文件单一功能** — 一文件一职责，≤3 个 responsibility  
2. **短小** — 遵守 LOC 红线；`当前 + 预估新增` 触线 → 先拆再改  
3. **无状态** — 业务判定放 `*Pipeline.kt`（纯函数 / object），不放 Composable / VM 大块  
4. **单一数据流** — UI 只订阅 StateFlow；副作用在 VM / Coordinator / Session 一处  
5. **面向管道** — 新分支逻辑命名 `*Pipeline.kt`，有意义行为补单测  

## LOC 红线

| 类型 | 🟢 安全 | 🟡 警告 | 🟠 必须先拆 | 🔴 禁止继续堆 |
|------|---------|---------|-------------|---------------|
| Compose Screen | ≤500 | 501–800 | 801+ | >1000 |
| ViewModel | ≤400 | 401–600 | 601–800 | >800 |
| Coordinator | ≤300 | 301–500 | 501–600 | >600 |
| Repository / Parser / 大型 UI | ≤500 | 501–700 | 701+ | >1000 |

**全仓库硬规则**：任意 `.kt` **>500 行** → 记入 `current/loc_audit.md`。

公式：`当前 LOC + 预估新增 = 预测合计`；预测 ≥ 🟠 → STOP。

## 职责红线

- 每文件 ≤3 个 responsibility  
- 新功能 = 新 responsibility → **新文件**，不扩 God 文件  

## 新代码落点

| 内容 | 位置 |
|------|------|
| 业务判定 | `*Pipeline.kt` + `*Test.kt` |
| UI 区块 | `components/<Screen>*.kt` |
| 对话框集合 | `*DialogsHost.kt` |
| 派生 StateFlow | `*SessionFlows.kt` |
| 会话/复盘/进度 | `*Coordinator.kt` |
| 跨 Practice↔Exam UI | `:ui-common` |
| Excel/JSON/解析 | `:data/.../parser/*Pipeline.kt` |

## 禁止

- 任意 >500 行文件堆新功能  
- Screen 内 DialogsHost、>30 行 inline Composable、大段 LaunchedEffect 业务链  
- VM 内 >20 行 inline `stateIn` / 导航 if-else  
- >15 行纯逻辑无 Pipeline  
- 跨模块复制整块 UI  

## 验证

```powershell
scripts/check-loc-over-500.ps1
scripts/check-practice-screen-loc.ps1   # 触达 PracticeScreen 时
scripts/check-exam-screen-loc.ps1       # 触达 ExamScreen 时
```
