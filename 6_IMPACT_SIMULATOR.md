# IMPACT SIMULATOR

> 在执行进化前，模拟对全系统各子系统的影响。

---

## SUBSYSTEM INVENTORY (本项目)

| # | 子系统 | 关键资产 | 退化症状 |
|---|--------|---------|---------|
| 1 | **Practice Session** | `PracticeViewModel`, `PracticeScreen`, `PracticeSessionState` | 导航状态不一致、进度丢失 |
| 2 | **Exam Session** | `ExamViewModel`, `ExamScreen`, `ExamProgress` | 评分错误、进度覆盖 |
| 3 | **Persistence (Room)** | `practice_progress` 表, `exam_progress` 表 | 数据丢失、scope id 不匹配 |
| 4 | **Home Aggregation** | `HomeViewModel`, `preferredHomePracticeProgress` | 进度卡片显示错误 |
| 5 | **Import Pipeline** | `SettingsViewModel`, `QuizFileBrowser`, `QuestionRepositoryImpl` | 导入失败、文件解析错误 |
| 6 | **Navigation (Compose)** | `AppNavHost`, 路由参数 | 路由失效、回退栈异常 |
| 7 | **Wrong Book / Favorites** | `WrongBookScreen`, `FavoriteScreen`, 对应 Repository | 错题/收藏数据丢失 |
| 8 | **Settings / Fonts** | `SettingsScreen`, `FontSettingsDataStore` | 设置丢失 |
| 9 | **Unit Tests** | `PracticeViewModelTest`, `HomeViewModelTest`, `ProgressScopeTest` | 测试失效或误报 |
| 10 | **Build System** | `build.gradle.kts`, Hilt, Compose compiler | 编译失败、DI 注入异常 |

---

## IMPACT SIMULATION MATRIX

对每次进化提案，评估对每个子系统的影响：

| 子系统 | 影响等级 | 典型风险 |
|--------|---------|---------|
| Practice Session | — | — |
| Exam Session | — | — |
| Persistence | — | — |
| Home Aggregation | — | — |
| Import Pipeline | — | — |
| Navigation | — | — |
| Wrong Book / Favorites | — | — |
| Settings / Fonts | — | — |
| Unit Tests | — | — |
| Build System | — | — |

### 影响等级定义

| 等级 | 含义 |
|------|------|
| ✅ NONE | 无影响 |
| ⚠️ LOW | 可能有边缘影响，需验证 |
| 🔶 MEDIUM | 接口变更，需适配 |
| 🔴 HIGH | 数据结构变更，可能破坏兼容性 |
| ❌ BLOCKED | 进化不可执行，需降低影响后重试 |

---

## EXAMPLE: 提取 PracticeNavigationCoordinator 的影响模拟

| 子系统 | 影响等级 | 典型风险 |
|--------|---------|---------|
| Practice Session | 🔶 MEDIUM | 导航逻辑迁移，需确保所有入口正确 |
| Exam Session | ✅ NONE | 无关 |
| Persistence | ✅ NONE | 无关（不改进度保存） |
| Home Aggregation | ✅ NONE | 无关 |
| Import Pipeline | ✅ NONE | 无关 |
| Navigation | ⚠️ LOW | 路由参数不变，但需验证 |
| Wrong Book / Favorites | ⚠️ LOW | 共用 Practice 路由 |
| Settings / Fonts | ✅ NONE | 无关 |
| Unit Tests | 🔶 MEDIUM | 需更新测试中的 VM 初始化 |
| Build System | ⚠️ LOW | 新增文件需注册 |

**结论**: 风险等级 MEDIUM，可执行。需 special attention 到 Practice Session + Unit Tests。

---

## RISK PROPAGATION RULES

1. 若 2 个以上子系统 🔴 HIGH → **中止进化**，重新设计策略
2. 若 Persistence 子系统 🔴 HIGH → **中止进化**，必须设计 migration
3. 若 Unit Tests 子系统 🔴 HIGH → 需先更新测试，再执行进化
4. 若 Build System 子系统 🔴 → 需先解决编译问题
