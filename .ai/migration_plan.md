<!--
  Derived from: L6 Migration Plan + Impact Simulation 2026-06-11
  Target: PracticeViewModel.kt — 6 coordinators extraction
  Last synced: 2026-06-11 21:05 UTC+8
  Do not edit directly — re-run Migration Planning to regenerate.
-->

# Migration Plan: PracticeViewModel.kt

> 6 steps, ~1500 lines extracted, 3900 → 800 (VM) + ~1500 (coordinators) lines.

---

## PRE-FLIGHT GATE (Step 0)

```
✅ ./gradlew :app:compileDebugKotlin
✅ ./gradlew :app:testDebugUnitTest --tests "PracticeViewModelTest"
✅ ./gradlew :app:testDebugUnitTest --tests "HomeViewModelTest"
✅ ./gradlew :app:testDebugUnitTest --tests "ProgressScopeTest"
⚠️ K-001: Device smoke — bypassed with user permission (Step 1 is pure logic, zero external deps)
```

---

## STEP 1: PracticeNavigationCoordinator ✅ DONE (2026-06-11)

- **File**: `PracticeNavigationCoordinator.kt`
- **Deps**: None (pure logic)
- **Extracted**: ~240 lines (types + snapshots + state transitions)
- **Compile**: ✅ BUILD SUCCESSFUL
- **Tests**: ✅ 39/39 unit tests pass
- **Rollback**: `git checkout -- PracticeViewModel.kt && rm PracticeNavigationCoordinator.kt`
- **Risk**: 🟢 LOW
- **Extracts**: goToNext, goToPrev, jumpToQuestion, enterAnsweredHistory, exitAnsweredHistory, random-history cursor, navigation mode enum, isInAnsweredHistory
- **Verify**: `compileDebugKotlin` + `PracticeViewModelTest`
- **Rollback**: `git checkout -- PracticeViewModel.kt && rm PracticeNavigationCoordinator.kt`

## STEP 2: PracticeAnswerHandler ✅ DONE (2026-06-11)

- **File**: `PracticeAnswerHandler.kt`
- **Deps**: None (pure logic)
- **Extracted**: ~220 lines (11 pure evaluation functions)
- **Compile**: ✅ BUILD SUCCESSFUL
- **Tests**: ✅ 38/39 pass (pre-existing TimeoutCancellationException)
- **Rollback**: `git checkout -- PracticeViewModel.kt && rm PracticeAnswerHandler.kt`

## STEP 3: PracticeProgressCoordinator ✅ DONE (2026-06-11)

- **File**: `PracticeProgressCoordinator.kt`
- **Deps**: None (pure utility)
- **Extracted**: ~110 lines (7 utility functions)
- **Left in VM**: `saveProgress()`/`loadProgress()`/`setProgressId()` (coupled to session state/mutex/data sources)
- **Compile**: ✅ BUILD SUCCESSFUL
- **Tests**: ✅ 38/39 pass
- **Rollback**: `git checkout -- PracticeViewModel.kt && rm PracticeProgressCoordinator.kt`

## STEP 4: PracticeModeCoordinator (~185 lines)

- **File**: `coordinator/PracticeModeCoordinator.kt`
- **Dependency**: Needs C3 (NavigationCoordinator) stable
- **Risk**: 🔴 HIGH

## STEP 5: PracticeFullAnswerCoordinator (~245 lines)

- **File**: `coordinator/PracticeFullAnswerCoordinator.kt`
- **Dependency**: Needs C4 (AnswerHandler) stable
- **Risk**: 🔴 HIGH

## STEP 6: PracticeSessionCoordinator (~255 lines)

- **File**: `coordinator/PracticeSessionCoordinator.kt`
- **Dependency**: Needs all C3-C7 stable
- **Risk**: 🔴 HIGH

---

## Impact Simulation Summary

| Subsystem | Max Risk | Mitigation |
|-----------|----------|------------|
| Practice Session | 🔴 HIGH (St6) | All tests as gate |
| Persistence (Room) | 🔴 HIGH (St3) | Format unchanged; test gates |
| Home Aggregation | 🔴 HIGH (St3) | HomeVMTest must pass |
| Unit Tests | 🔶 MEDIUM (all) | Update test setup each step |
| Navigation | ⚠️ LOW | Coordinator compiles independently |

**Rollback rule**: 2+ 🔴 HIGH subsystems fail → ABORT → re-design.
**Blocker**: K-001 device smoke.
