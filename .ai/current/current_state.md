<!--
  Agent memory layer — sync after major phases or VERIFY milestones.
  Last updated: 2026-07-05 (P79 + Engineering OS)
  Canonical path: .ai/current/current_state.md
-->

# Current State (Memory Layer)

> **PowerAI Engineering OS · L3 现状层**  
> 入口：`.ai/README.md` | 宪法：`.ai/PROJECT_CONSTITUTION.md`  
> **Single entry point for agents.** Tactical smoke: [K001_DEVICE_SMOKE.md](../K001_DEVICE_SMOKE.md)  
> **文档同步：** 每完成 Phase 须更新 `change_log.md`、`current_state.md`、`loc_audit.md`、`refactoring_plan.md`、`tech_debt.md`（均在本 `current/` 目录）。

## Active tracks

| Track | Doc | Status |
|-------|-----|--------|
| Session / Strategy V5 (P26–P52) | `current/change_log.md`, `architecture/session_architecture.md` | **✅ 主链完成** — UI init/overlay/grade 经 Command |
| **Engineering OS** | `.ai/README.md`, `workflows/architecture_guard.md` | **✅ 2026-07-05** — 分层 OS 上线 |
| K-001 设备冒烟 | [K001_DEVICE_SMOKE.md](K001_DEVICE_SMOKE.md) | **✅ PASS** — 2026-07-05 真机 |
| K-007 Exam 路由冒烟 | [K007_EXAM_ROUTE_SMOKE.md](K007_EXAM_ROUTE_SMOKE.md) | **✅ PASS** — 2026-07-05 真机 |
| 归档 P79 | `refactoring_plan.md` Phase 26 | **✅ 完成** — Room 瘦身 + domain 命名 + 写回单测 |
| 模块化 P78 | `refactoring_plan.md` Phase 25 | **✅ 完成** |
| CI / lint | `.github/workflows/build.yml` | **✅** — ktlint + detekt（P78 收紧规则） |
| 自适应渐隐原子练习 | 根目录方案 + ADR-006 | **🟡 已实现，待联网环境编译门禁** |
| L6 Self-Evolving OS | `LEVEL6_OVERVIEW.md` | Deployed 2026-06-11 |

## System health: **🟢 STABLE**（逻辑层）

| Indicator | Value | Notes |
|-----------|-------|-------|
| God file (>1000 LOC) | 0 | ✅ |
| LOC >500 | 0 | `scripts/check-loc-over-500.ps1` / `.sh` |
| Session 主流程 | SessionHost + Engine | PracticeVM/ExamVM 已删 (P6) |
| Test coverage | LOW–MEDIUM | session + Home 单测；K-001 真机 PASS |
| `:app` 占比 | **~18% 行 / ~12% 文件** | 目标 ≤40% ✅（822 文件 / 57,549 行） |

**Allowed:** Strategy 长尾、Home/QuestionBank 组件继续下沉、小步导航改进。  
**Forbidden:** 无 K-001 门禁限制（已通过）。

## What is stable

- **Session:** Practice / Review / Exam / Browse / QuestionEdit 经 `SessionRegistry` + `SessionHost`
- **AdaptiveFading:** 独立 Session + 双池抽题 + 四阶段渐隐 + Room 独立调度状态；原题库只读
- **Command CQRS:** 选题/填空/导航/交卷/Init/Overlay/Lifecycle restore 经 `SessionCommand`（Handler 内 bindings 委托）
- **AI 同步:** `SessionAiAnalysisExtension` + `SessionExtensionEventWiring`（Host / 裸引擎 / QuestionEdit）
- **Policy:** Persistence / Navigation / Reveal / Exit 工厂 + `SessionCommand` CQRS 主路径
- **进度:** scoped `practice_*` id、Home `preferredHomePracticeProgress`（单测覆盖）
- **LOC:** 全仓库 >500 行清零；`PracticeScreen` ~170（app 薄包装）、`PracticeScreenContent` ~410（`:feature-practice`）
- **Home:** Screen + components + file VMs → `:feature-practice`；`:app` 仅 `HomeRoute`
- **QuestionBank:** 点击→Browse；**长按**→`question_edit` → `DrawerQuestionEditHost`
- **题库详情:** `onViewQuestionDetail` → Browse Session（`targetQuestionId=0`）；legacy `QuestionScreen` 已删
- **AI 叠层写回:** `AppNavAiWritebackPipeline` → `SessionCommand`（含 `AppendNote`）
- **Settings IO:** 全量在 `feature-settings`（含 coordinators / Uri pipeline）
- **FontSettings:** 接口 `:core`；`FontSettingsDataStore` + `FontSettingsRepositoryImpl` → `:data`
- **Progress:** `PracticeProgressLifecycleCoordinator` 328；load/save/apply/review/reset 经 Pipeline
- **ui-common:** `OptimizedFileCard` / `DraggingFileCard` 已下沉（P61）
- **CI:** JDK 21、`ktlintCheck`（强制）、session/arch 单测、`assembleDebug`
- **导航:** Practice / Exam / Review / 错题 / 收藏经 `SessionHost` 路由；AI 叠层经 `NavSessionOwners` bindings
- **Gesture:** `Swipeable`/`FractionalThreshold` 无代码引用 — TD-014 CLOSED

## What is not done

- 自适应渐隐 MVP 尚需在可联网 JDK 21 环境执行全量 Gradle 编译、lint、单测与 APK 冒烟。
- 个性化间隔、学习统计面板属于后续增强，不在 MVP 内。

## Session 迁移进度（P26–P63）

```
P26–P35 Strategy/Navigation/Exit/Browse/Review  ✅
P36–P40 Engine/Assembly/QuestionEdit/LOC       ✅
P41–P44 Extension写回/AnswerSubmitted/裸引擎   ✅
P45 K-001清单 + 文档同步 + ktlint收紧          ✅
P46 K-001 PASS + Browse 路由/抽屉接线          ✅
P47 AppNavHost → SessionHost 路由              ✅
P48 NavigationHistory Pipeline 收编 + LOC 瘦身 ✅
P49 History Gate 对称 + Practice prevQuestion 路线化 ✅
P50 bindings→Command 导航收编 + detekt + K-007 清单 ✅
P51 bindings 长尾 Command + PracticeScreen→feature-practice ✅
P52 Init/Overlay/Lifecycle + Exam init/grade Command ✅
P53 Phase 10a: WrongBook/Favorite VM + SoundEffects/SwipeReveal + Exam edit Command ✅
P54 Phase 10e/f: :feature-ai + data/network + AISync下沉 + ui-common pipelines + DrawerQuestionEdit→Session ✅
P55–P57 Phase 10h/j + Home shell + settings res + Progress 管道化 ✅
P58 Settings UI→feature-settings + Home 预防性拆分 + 文档同步 ✅
P59 P11d HomeFileListGrid + PracticeProgressLoadQuestionsPipeline ✅
P60 P11f HomeFileListColumn ✅
P61 OptimizedFileCard→ui-common + Progress apply/save Pipeline ✅
P62 Home→feature-practice + QuestionBank drawer + HomeRoute ✅
P63 FontSettingsRepositoryImpl→:data + DrawerQuestionEditHost→feature-practice ✅
P64 DrawerQuestionEdit 导航接线 + feature-practice 单测基建 ✅
P65 question_detail→Browse + Result/History/WrongBook→feature-practice ✅
P66 FavoriteScreen→feature-practice + FavoriteRoute ✅
P67 PracticeScreen/ExamScreen shell→feature + SessionCommandDispatch 删除 ✅
P68 AI overlay callbacks + SettingsImportFilePipeline + ImportResult 统一 ✅
```

## 2026-07-15 结果详情页

- `:feature-practice` 结果详情页已按仪表盘设计完成 UI 重构，原有 ViewModel、历史记录、导航和数据库链路保持不变。
- 本次/累计统计口径已统一，趋势图限定当前题库最近 9 次；详细说明见 [result_screen_redesign.md](result_screen_redesign.md)。
- 新增统计边界单元测试；全仓 `ktlintCheck`、模块单测和 performance APK 构建均通过。

## 2026-07-15 导入 / 绘图 / DeepSeek 题号

- Excel 旧版计算题格式解析正常（10–11 题）；加强同名（忽略扩展名）防重复导入。
- Docx 绘图题：章节标题「绘图题」可识别；答案图经 `DRAWING_IMAGES`；考试结果区同步展示。
- DeepSeek 全屏返回：SessionHost 不再在 dispose 时销毁会话，同 kind 复用，避免回到第 1 题。

## Quick verify

```bash
./gradlew ktlintCheck
./gradlew detekt
./gradlew :app:testDebugUnitTest --tests "com.example.testapp.core.session.*"
./gradlew :app:testDebugUnitTest --tests "com.example.testapp.presentation.session.*"
./gradlew :app:testDebugUnitTest --tests "com.example.testapp.arch.ArchitectureTest"
./gradlew assembleDebug
# Windows LOC: scripts/check-practice-screen-loc.ps1
# Linux CI:   bash scripts/check-loc-over-500.sh
```
