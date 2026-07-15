# LOC 审计（>500 行）

> 生成方式：`scripts/check-loc-over-500.ps1`（Windows）/ `scripts/check-loc-over-500.sh`（CI/Linux）  
> 最后扫描：2026-07-15（Excel 题库兼容扩展）

## 超过 500 行的文件

**0 个** — 全仓库已清零 ✅

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
