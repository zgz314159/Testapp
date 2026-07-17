# LOC 审计（>500 行）

> 生成方式：`scripts/check-loc-over-500.ps1`（Windows）/ `scripts/check-loc-over-500.sh`（CI/Linux）  
> 最后扫描：2026-07-16（自适应渐隐原子练习）

## 超过 500 行的文件

当前 HEAD 基线仍有 3 个超过 500 行的文件；本次触及的 `PracticeSessionEngine.kt` 已保持在 498 行。

| 文件 | 行数 | 状态 |
|------|------|------|
| `feature-exam/.../ExamViewModel.kt` | 538 | 既有基线，待拆 |
| `feature-exam/.../ExamSessionEngine.kt` | 551 | 既有基线，待拆 |
| `feature-practice/.../PracticeEditorCoordinator.kt` | 578 | 既有基线，待拆 |
| `feature-practice/.../PracticeSessionEngine.kt` | 498 | 本次守住红线 ✅ |

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
