# LOC 审计（>500 行）

> 生成方式：`scripts/check-loc-over-500.ps1`（Windows）/ `scripts/check-loc-over-500.sh`（CI/Linux）  
> 最后扫描：2026-07-05（P79）

## 超过 500 行的文件

**0 个** — 全仓库已清零 ✅

## 近期热点 LOC（P63）

| 文件 | 行数 | 备注 |
|------|------|------|
| `FontSettingsRepositoryImpl.kt` | ~193 | `:data`（P63 自 app 迁入） |
| `DrawerQuestionEditHost.kt` | ~100 | `:feature-practice`（P63） |
| `QuestionEditSessionRoutePipeline.kt` | ~8 | `:core`（P64） |
| `QuestionAnalysisUseCases.kt` | ~52 | `:domain`（P79 自 `QuestionAnalysisRepository.kt` 重命名） |
| `HomeScreen.kt` | ~293 | `:feature-practice` |
| `QuestionBankDrawer.kt` | ~290 | `:feature-practice` |
| `PracticeProgressLifecycleCoordinator.kt` | 328 | P61 |
| `HomeFileListColumn.kt` | 275 | P60 |
| `HomeRoute.kt` | ~50 | `:app` 薄路由 |
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
