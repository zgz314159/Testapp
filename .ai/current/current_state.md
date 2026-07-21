<!--
  Agent memory layer — sync after major phases or VERIFY milestones.
  Last updated: 2026-07-05 (P79 + Engineering OS)
  Canonical path: .ai/current/current_state.md
-->

# Current State (Memory Layer)

> **PowerAI Engineering OS · L3 现状层**  
> 入口：`.ai/README.md` | 宪法：`.ai/PROJECT_CONSTITUTION.md`  
> **Single entry point for agents.**
> **文档同步：** 每完成 Phase 须更新 `change_log.md`、`current_state.md`、`loc_audit.md`、`refactoring_plan.md`、`tech_debt.md`（均在本 `current/` 目录）。

## 2026-07-19 当前状态覆盖

- 最新真机结论仍为 Round21 Gate PASS；本轮补充完成 Debug Kotlin 编译/APK 打包、相关模块 JVM 单测、完整 `ktlintCheck` 与 LOC 门禁，均 PASS。
- Release 签名不再包含明文密码；凭据来自本地 `signing.properties` 或 CI 环境变量。
- Kotlin `>500 LOC` 当前为 0：`ExamViewModel` 500、`ExamSessionEngine` 497、`PracticeEditorCoordinator` 451。
- 大文件导入已降低峰值副本：Room 分批单事务写入，TXT 逐行解析。
- AI 当前是整包响应而非 chunk 流；已补单活跃请求与取消生命周期。
- 答题页 JLatex 位图原有 `remember` 保留；本地图片和 SVG 请求对象增加稳定复用。

## Active tracks

| Track | Doc | Status |
|-------|-----|--------|
| Session / Strategy V5 (P26–P52) | `current/change_log.md`, `architecture/session_architecture.md` | **✅ 主链完成** — UI init/overlay/grade 经 Command |
| **Engineering OS** | `.ai/README.md`, `workflows/architecture_guard.md` | **✅ 2026-07-05** — 分层 OS 上线 |
| 归档 P79 | `refactoring_plan.md` Phase 26 | **✅ 完成** — Room 瘦身 + domain 命名 + 写回单测 |
| 模块化 P78 | `refactoring_plan.md` Phase 25 | **✅ 完成** |
| CI / lint | `.github/workflows/build.yml` | **✅** — ktlint + detekt（P78 收紧规则） |
| 自适应渐隐原子练习 | 根目录方案 + ADR-006 | **🟡 已实现，待联网环境编译门禁** |
| 共用答题页立体视觉 | `:ui-common` question-session chrome | **🟡 已实现，待本地 Android 编译与真机视觉冒烟** |
| AI 联网纠题 / BYOK | ADR-008 | **🟡 已实现** — 用户自备 DeepSeek + 检索 Key（博查/Tavily 双通道，优先博查）；问答输入栏可显式开启联网；托管额度插口预留；待真机填 Key 验收 |
| L6 Self-Evolving OS | `LEVEL6_OVERVIEW.md` | Deployed 2026-06-11 |

## System health: **🟢 STABLE**（逻辑层）

| Indicator | Value | Notes |
|-----------|-------|-------|
| God file (>1000 LOC) | 0 | ✅ |
| LOC >500 | 0 | `scripts/check-loc-over-500.ps1` / `.sh` |
| Session 主流程 | SessionHost + Engine | PracticeVM/ExamVM 已删 (P6) |
| Automated tests | REMOVED | 2026-07-19 收尾清理：测试源码、Benchmark、性能报告及测试依赖已移除 |
| `:app` 占比 | **~18% 行 / ~12% 文件** | 目标 ≤40% ✅（822 文件 / 57,549 行） |

**Allowed:** Strategy 长尾、Home/QuestionBank 组件继续下沉、小步导航改进。  
**Forbidden:** 无 K-001 门禁限制（已通过）。

## What is stable

- **Session:** Practice / Review / Exam / Browse / QuestionEdit 经 `SessionRegistry` + `SessionHost`
- **AdaptiveFading:** 独立 Session + 双池抽题 + 四阶段渐隐 + Room 独立调度状态；原题库只读
- **Question session UI:** Practice / Exam / Adaptive 共用悬浮顶栏、题型卡、选项卡与立体底栏；业务回调和 Session 边界不变
- **Command CQRS:** 选题/填空/导航/交卷/Init/Overlay/Lifecycle restore 经 `SessionCommand`（Handler 内 bindings 委托）
- **AI 同步:** `SessionAiAnalysisExtension` + `SessionExtensionEventWiring`（Host / 裸引擎 / QuestionEdit）
- **Policy:** Persistence / Navigation / Reveal / Exit 工厂 + `SessionCommand` CQRS 主路径
- **进度:** scoped `practice_*` id、Home `preferredHomePracticeProgress`（单测覆盖）
- **LOC:** 全仓库 >500 行清零；`PracticeScreen` ~170（app 薄包装）、`PracticeScreenContent` ~410（`:feature-practice`）
- **Home:** Screen + components + file VMs → `:feature-practice`；`:app` 仅 `HomeRoute`
- **QuestionBank:** 点击→Browse；**长按**→`question_edit` → `DrawerQuestionEditHost`
- **题库详情:** `onViewQuestionDetail` → Browse Session（`targetQuestionId=0`）；legacy `QuestionScreen` 已删
- **AI 叠层写回:** `AppNavAiWritebackPipeline` → `SessionCommand`（含 `AppendNote`）
- **AI BYOK:** 用户自备 DeepSeek + 检索 Key（博查/Tavily，都填优先博查；加密本机）；`AiBackend` 路由；托管 entitlement 插口；纠题仅回填编辑草稿
- **AI 问答联网:** DeepSeek 问答输入栏联网图标按钮；开启后先检索再回答，正文编号引用；来源以「N 个网页」胶囊 + 底部搜索结果列表展示（`AiChatSourcesPipeline` 拆分，编辑回写重挂来源块）；关闭保持普通问答
- **AI 纠题入口:** 练习/考试/题库抽屉统一 `AiQuestionEditDialog`；设置 → AI 服务管理 Key
- **Settings IO:** 全量在 `feature-settings`（含 coordinators / Uri pipeline）
- **FontSettings:** 接口 `:core`；`FontSettingsDataStore` + `FontSettingsRepositoryImpl` → `:data`
- **Progress:** `PracticeProgressLifecycleCoordinator` 328；load/save/apply/review/reset 经 Pipeline
- **ui-common:** `OptimizedFileCard` / `DraggingFileCard` 已下沉（P61）
- **CI:** JDK 21、`ktlintCheck`、`detekt`、`assembleDebug`
- **导航:** Practice / Exam / Review / 错题 / 收藏经 `SessionHost` 路由；AI 叠层经 `NavSessionOwners` bindings
- **Gesture:** `Swipeable`/`FractionalThreshold` 无代码引用 — TD-014 CLOSED

## What is not done

- 自适应渐隐 MVP 尚需在可联网 JDK 21 环境执行全量 Gradle 编译、lint、单测与 APK 冒烟。
- 共用答题页立体视觉尚需在本地 Android 环境执行编译，并对窄屏、长选项、深色模式和 IME 做真机冒烟。
- AI BYOK：真机填写 DeepSeek + 博查（或 Tavily）Key 后验收解析与联网纠题；托管支付尚未实现。
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
- 统计边界与页面重构已完成；当前收尾验证只保留静态检查和生产构建。

## 2026-07-15 导入 / 绘图 / DeepSeek 题号

- Excel 旧版计算题格式解析正常（10–11 题）；加强同名（忽略扩展名）防重复导入。
- Docx 绘图题：章节标题「绘图题」可识别；答案图经 `DRAWING_IMAGES`；考试结果区同步展示。
- DeepSeek 全屏返回：SessionHost 不再在 dispose 时销毁会话，同 kind 复用，避免回到第 1 题。

## Quick verify

```bash
./gradlew ktlintCheck
./gradlew detekt
./gradlew assembleDebug
# Windows LOC: scripts/check-practice-screen-loc.ps1
# Linux CI:   bash scripts/check-loc-over-500.sh
```
