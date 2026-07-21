# LOC 审计（>500 行）

> 生成方式：`scripts/check-loc-over-500.ps1`（Windows）/ `scripts/check-loc-over-500.sh`（CI/Linux）  
> 最后复核：2026-07-19（`check-loc-over-500.ps1` PASS）

## 超过 500 行的文件

当前工作区没有超过 500 行的 Kotlin 文件。

| 文件 | 行数 | 状态 |
|------|------|------|
| `feature-exam/.../ExamViewModel.kt` | 500 | 已抽取 `ExamQuestionStatePipeline` |
| `feature-exam/.../ExamSessionEngine.kt` | 497 | 复用统一题目状态 Pipeline |
| `feature-practice/.../PracticeEditorCoordinator.kt` | 451 | 已抽取 `PracticeEditorStatePipeline` |

BYOK / 问答联网相关新文件均低于 500：`AiServiceSettingsScreen`、`QuestionCorrectionOrchestrator`、`AiWebSearchOrchestrator`、`AiWebSearchPromptPipeline`、`AiChatSourcesPipeline`、`AiChatSourcesViews`、`DeepSeekDirectClient`、`BochaDirectClient`、`RoutingAiBackend` 等；未新增 >500 行文件（2026-07-18 复扫通过）。

填空三框编辑：`QuestionEditDialog.kt` ~378、`QuestionEditFieldRows.kt` ~217，均低于 500（2026-07-18 复扫通过）。

Home 滚动/返回性能：新增 `HomeFileListCachePolicy.kt`，并调整 Home 列表/卡片模型及返回路径；未新增 >500 行文件（2026-07-18 复扫通过）。

Round16：新增 `DestructiveClearIsolationTest.kt`；Profile 语义 diff 与多机型门禁位于 PowerShell/JSON，不扩大 Kotlin 热点；`check-loc-over-500.ps1` 通过（2026-07-19）。

Round17：无 Kotlin 改动（仅 Gradle 依赖删除 + PowerShell/JSON）；`check-loc-over-500.ps1` 复扫通过（2026-07-19）。

Round18：Home 热路径优化（`HomeScreen`/`HomeFileList*`/`HomeQuestionBankCard`/`OptimizedFileCard`）；未新增 >500 文件；`check-loc-over-500.ps1` 通过（2026-07-19）。

Round20：新增 `HomeShaderCacheDiagnosticBenchmark.kt`（独立性能诊断，不进生产路径）；未新增 >500 文件。

Round21：仅 Baseline Profile PowerShell/JSON 包级语义门禁与报告，无 Kotlin 改动。

## 近期热点 LOC（Excel 兼容 + Phase Home）

| 文件 | 行数 | 备注 |
|------|------|------|
| `ExcelParserCellPipeline.kt` | ~197 | `:data` 表头识别 |
| `ExcelParserRowPipeline.kt` | ~210 | `:data` 行解析 |
| `ExcelImportAnswerNormalizePipeline.kt` | ~46 | `:data` 新增 |
| `ExcelQuestionParser.kt` | ~78 | `:data` |

## 门禁脚本

| 脚本 | 目标 | 阈值 |
|------|------|------|
| `check-practice-screen-loc.ps1` | PracticeScreen | 500 |
| `check-exam-screen-loc.ps1` | ExamScreenContent | 500 |
| `check-loc-over-500.ps1` / `.sh` | 全仓库 | 500 |

## 分解文档

- `.ai/practice_screen_decomposition.md`
- `.ai/exam_screen_decomposition.md`
- `.ai/navigation_controller_decomposition.md`
