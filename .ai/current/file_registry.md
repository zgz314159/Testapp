<!--
  Derived from: PROJECT_SCAN_DEPENDENCY_REPORT.md, CURRENT_STATE.md, L6 Decomposition Blueprint 2026-06-11
  Last synced: 2026-06-11 21:15 UTC+8
  Do not edit directly — re-run L6 health scan to regenerate.
  Coverage: Top 30 hot files + 6 planned coordinator files (post-extraction).
-->

# File Registry (Hot Files Index)

> Top 30 files by risk/priority. Full registry deferred to `.ai/file_registry_full.md` when scan budget allows.

---

## 🔴 EXTREME / VERY HIGH

| Path | Module | Lines | Responsibilities | Injected Deps | Risk | Score |
|------|--------|-------|-----------------|---------------|------|-------|
| `app/.../presentation/screen/PracticeViewModel.kt` | `:app` | **~1982** (was ~3900) | 5+ | 15+ | 🔴 EXTREME | 98 |
| `app/.../presentation/screen/ExamViewModel.kt` | `:app` | **~663** (was ~2455) | **5 (薄委托层)** | 15+ | 🟡 MEDIUM | **45** |
| `app/.../presentation/screen/ExamNavigationHelper.kt` | `:app` | 130 | 2 (状态构建/导航) | 1 | 🟢 LOW | **22** |
| `app/.../presentation/screen/ExamLoadDelegate.kt` | `:app` | 182 | 1 (加载3合1) | 10 | 🟡 LOW-MEDIUM | **30** |
| `app/.../presentation/screen/ExamFillTransform.kt` | `:app` | 81 | 1 (Fill配置) | 1 | 🟢 LOW | **15** |
| `app/.../presentation/screen/ExamMemoryModeEngine.kt` | `:app` | 48 | 1 (记忆轮次) | 1 | 🟢 LOW | **10** |
| `app/.../presentation/screen/ExamAnswerRules.kt` | `:app` | 20 | 1 (答案判断) | 0 | 🟢 LOW | **5** |
| `data/.../data/repository/QuestionRepositoryImpl.kt` | `:data` | 1441 | 5 | 16 | 🔴 HIGH | 85 |

## 🟠 HIGH

| Path | Module | Lines | Responsibilities | Injected Deps | Risk | Score |
|------|--------|-------|-----------------|---------------|------|-------|
| `app/.../presentation/screen/SettingsScreen.kt` | `:app` | ~1218 | 4 | 隐式 | 🟠 HIGH | 78 |
| `app/.../presentation/screen/PracticeScreen.kt` | `:app` | ~1165 | 3 | 隐式 | 🟠 HIGH | 76 |
| `app/.../presentation/screen/SettingsViewModel.kt` | `:app` | ~1029 | 4 | 8+ | 🟠 HIGH | 74 |

## 🟡 MEDIUM

| Path | Module | Lines | Responsibilities | Injected Deps | Risk | Score |
|------|--------|-------|-----------------|---------------|------|-------|
| `app/.../presentation/screen/ExamScreen.kt` | `:app` | **390** (was 868) | 1 (UI编排层) | 隐式 | 🟢 LOW | **35** (was 66) |
| `app/.../presentation/screen/ExamEndFlow.kt` | `:app` | 25 | 1 (考试结束流程) | 1 | 🟢 LOW | **8** |
| `app/.../presentation/screen/ExamDialogState.kt` | `:app` | 24 | 1 (对话框状态) | 0 | 🟢 LOW | **5** |
| `app/.../presentation/screen/ExamFontController.kt` | `:app` | 47 | 1 (字体控制) | 1 | 🟢 LOW | **10** |
| `app/.../presentation/screen/ExamSessionStats.kt` | `:app` | 30 | 1 (会话评分) | 2 | 🟢 LOW | **8** |
| `app/.../presentation/screen/ExamAISyncEffects.kt` | `:app` | 52 | 1 (AI同步) | 4 | 🟢 LOW | **12** |
| `app/.../presentation/screen/ExamGestureNavigator.kt` | `:app` | 14 | 1 (手势状态) | 0 | 🟢 LOW | **5** |
| `app/.../presentation/screen/ExamAutoAdvanceTimer.kt` | `:app` | 36 | 1 (自动导航) | 1 | 🟢 LOW | **8** |
| `app/.../presentation/screen/HomeScreen.kt` | `:app` | 726 | 3 | 隐式 | 🟡 MEDIUM | 63 |
| `app/.../navigation/AppNavHost.kt` | `:app` | 675 | 1 | 所有Screen | 🟡 MEDIUM | 60 |
| `app/.../screen/components/InlineBlankQuestionContent.kt` | `:app` | 536 | 1 | 隐式 | 🟡 MEDIUM | — |
| `app/.../presentation/screen/WrongBookScreen.kt` | `:app` | 466 | 2 | 隐式 | 🟡 MEDIUM | — |
| `app/.../presentation/screen/FavoriteScreen.kt` | `:app` | 463 | 2 | 隐式 | 🟡 MEDIUM | — |
| `app/.../component/OptimizedFileCard.kt` | `:app` | 437 | 1 | 隐式 | 🟡 LOW-MEDIUM | — |
| `data/.../repository/WrongBookRepositoryImpl.kt` | `:data` | 394 | 3 | 4 | 🟡 LOW-MEDIUM | 50 |
| `app/.../data/datastore/FontSettingsDataStore.kt` | `:app` | 374 | 1 | DataStore | 🟢 LOW | 38 |

## 🟢 LOW (selected)

| Path | Module | Lines | Responsibilities | Risk |
|------|--------|-------|-----------------|------|
| `data/.../repository/FavoriteQuestionRepositoryImpl.kt` | `:data` | 273 | 3 | 🟢 LOW |
| `domain/.../util/FillQuestionTransformUtils.kt` | `:domain` | ~360 | 1 | 🟢 LOW |
| `domain/.../util/AnswerUtils.kt` | `:domain` | ~55 | 1 | 🟢 LOW |

## Test files

| Path | Lines | Coverage target |
|------|-------|-----------------|

---

## Key metrics

| Metric | Value |
|--------|-------|
| Total `.kt`/`.kts` files | ~225+ |
| Total lines | ~26,276 |
| Files >1000 lines | 6 |
| Files >500 lines | 10 |
| God VMs (>2000 lines) | 2 |
| Injected deps in `QuestionRepositoryImpl` | 16 (highest) |
| Empty feature modules | 4 (`feature-practice`, `feature-exam`, `ui-common`, `core`) |

---

## Coordinator extraction progress (6/6 pure + radical Phase 1)

| Path | Module | Lines (actual/est.) | Responsibility | External Deps | Risk | Status |
|------|--------|---------------------|----------------|---------------|------|--------|
| `app/.../screen/PracticeNavigationCoordinator.kt` | `:app` | **~542 (radical)** | Navigation state + nextQuestion/prevQuestion orchestration | 0 | 🟡 MEDIUM | ✅ Done |
| `app/.../screen/PracticeAnswerHandler.kt` | `:app` | ~220 | Pure answer-evaluation logic | 0 | 🟢 LOW | ✅ Done |
| `app/.../screen/PracticeProgressCoordinator.kt` | `:app` | **~110 (now exists)** | Pure progress utilities | 0 | 🟢 LOW | ✅ Done |
| `app/.../screen/PracticeModeCoordinator.kt` | `:app` | **~420 (radical)** | Mode config + 4 state-mutating methods | 0 | 🟡 MEDIUM | ✅ Done |
| `app/.../screen/PracticeFullAnswerCoordinator.kt` | `:app` | **~120** | Full-answer config + fill transform | 0 | 🟢 LOW | ✅ Done |
| `app/.../screen/PracticeSessionCoordinator.kt` | `:app` | **~585 (radical)** | Progress save/load + wrong/fav questions + supplementary loaders | 0 | 🟡 MEDIUM | ✅ Done |

**Post-extraction VM**: ~800 lines (orchestrator only). **Total**: 7 files, ~2300 lines vs current 3900 in 1 file.
