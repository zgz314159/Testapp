# LOC 审计（>500 行）

> 生成方式：`scripts/check-loc-over-500.ps1`（Windows）/ `scripts/check-loc-over-500.sh`（CI/Linux）  
> 最后扫描：2026-07-13（Phase Home Redesign）

## 超过 500 行的文件

**0 个** — 全仓库已清零 ✅

## 近期热点 LOC（Phase Home Redesign）

| 文件 | 行数 | 备注 |
|------|------|------|
| `FontSettingsRepositoryImpl.kt` | ~193 | `:data` |
| `DrawerQuestionEditHost.kt` | ~100 | `:feature-practice` |
| `HomeScreen.kt` | ~315 | `:feature-practice`（Phase Home） |
| `HomeFileListColumn.kt` | 131 | `:feature-practice`（Phase Home） |
| `HomeDashboardPipeline.kt` | 165 | `:feature-practice`新增 |
| `HomeQuestionBankCard.kt` | 145 | `:feature-practice`新增 |
| `HomeBottomBar.kt` | 109 | `:feature-practice`修改 |
| `HomeGreetingHeader.kt` | 90 | `:feature-practice`新增 |
| `HomeContinueStudyCard.kt` | 158 | `:feature-practice`新增 |
| `OptimizedFileCard.kt` | ~340 | `:ui-common`（新增 visualContent 参数） |
| `QuestionBankDrawer.kt` | ~290 | `:feature-practice` |
| `PracticeProgressLifecycleCoordinator.kt` | 328 | P61 |
| `PracticeScreenContent.kt` | ~410 | `:feature-practice` |
| `ExamScreenContent.kt` | ~404 | `:feature-exam` |

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
