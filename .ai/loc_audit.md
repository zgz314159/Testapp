# LOC 审计（>500 行）

> 生成方式：`scripts/check-loc-over-500.ps1`  
> 最后扫描：2026-07-04（Phase 42 后）

## 超过 500 行的文件

**0 个** — 全仓库已清零 ✅

## 已完成拆分

| 文件 | 行数 | Phase |
|------|------|-------|
| `NavigationController.kt` | ~160 | 36 |
| `ExamScreenContent.kt` | ~404 | 37 |
| `ExamViewModel.kt` | ~471 | 38 |
| `PracticeViewModel.kt` | ~461 | 39 |
| `RichText.kt` | ~80 | 40 |
| `PracticeBasicComponents.kt` | 已删除 | 41 → 9 个组件文件 |
| `ExcelQuestionParser.kt` | ~95 | 42 |
| `PracticeScreen.kt` | ~416 | 35 |

## 门禁脚本

| 脚本 | 目标 | 阈值 |
|------|------|------|
| `check-practice-screen-loc.ps1` | PracticeScreen | 500 |
| `check-exam-screen-loc.ps1` | ExamScreenContent | 500 |
| `check-practice-vm-loc.ps1` | PracticeViewModel | 500 |
| `check-loc-over-500.ps1` | 全仓库报告 | 500 |

## 分解文档

- `.ai/navigation_controller_decomposition.md`
- `.ai/exam_screen_decomposition.md`
- `.ai/practice_screen_decomposition.md`
- `.ai/practice_viewmodel_decomposition.md`
