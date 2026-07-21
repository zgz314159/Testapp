<!--
  Derived from: PROJECT_SCAN_DEPENDENCY_REPORT.md §4, ARCHITECTURE.md, L6 Decomposition Blueprint 2026-06-11
  Last synced: 2026-06-14 22:27 UTC+8
  Do not edit directly — regenerate when dependencies change.
  Maturity: M1+ — Gradle module deps + Top hot file coupling + Post-extraction coordinator deps.
-->

# Dependency Graph

> M1 level: Gradle explicit dependencies + internal coupling matrix for top hot files.
> M2/M3 (DI mapping, Compose component graph, full dependency map) deferred.

---

## 1. Gradle Module Dependencies (Explicit)

```
:app
 ├── :domain          (implementation project)
 └── :data            (implementation project)
      └── :domain     (implementation project)

:feature-practice     (no project deps)
:feature-exam         (no project deps)
:ui-common            (no project deps)
:core                 (no project deps)
```

**Direction**: `app → data → domain` (unidirectional, correct). No cycles detected.

---

## 2. Internal Coupling Matrix (Top Hot Files)

| File | Depends on |
|------|-----------|
| **PracticeViewModel** | PracticeSessionState, PracticeProgressScope, PracticeProgressRepository, ExamProgressRepository, QuestionRepositoryImpl, UseCase×N, DataStore |
| **ExamViewModel** | SessionEngine, ExamUseCaseFacade, ExamNavigation/Answer/Memory/Edit/Grade/Statistics coordinators |
| **PracticeScreen** | PracticeViewModel (all StateFlow), PracticeSessionState |
| **ExamScreen** | ExamViewModel (all StateFlow) |
| **SettingsViewModel** | SettingsRepositoryFacade, FontSettingsCoordinator, Import/JsonExport/ExcelExport coordinators |
| **SettingsScreen** | SettingsViewModel, QuizFileBrowser dialog |
| **HomeViewModel** | PracticeProgressRepository, ExamProgressRepository |
| **HomeScreen** | HomeViewModel |
| **AppNavHost** | All Screens (Practice, Exam, Home, Settings, WrongBook, Favorites) |
| **QuestionRepositoryImpl** | 11 DAOs, 3 Repos, DB, Initializer, Apache POI |

---

## 3. Hidden Coupling Points

| Type | Files | Risk |
|------|-------|------|
| **VM↔Screen dual coupling** | PracticeVM↔PracticeScreen, ExamVM↔ExamScreen | Both must be refactored together |
| **Code duplication** | PracticeVM ↔ ExamVM (60-70% similar navigation/answer/progress) | Fix one, may break other |
| **Scope ID format** | `PracticeProgressScope.kt` → all repos + home aggregation | Changing format breaks everything |
| **DI centralization** | All bindings in `:app/di/` | Modules can't compile independently |
| **Layer violation** | `PracticeProgressScope.kt` in `presentation.screen` | Should be in `:domain` |

---

## 3.1 2026-06-14 Facade / Coordinator Updates

| Area | Dependency change | Result |
|------|-------------------|--------|
| `SettingsViewModel` | 7 direct repositories → `SettingsRepositoryFacade` | Constructor injection 12→6 |
| `ExamViewModel` | edit/grade/statistics inline logic → dedicated coordinators | VM keeps public API, delegates residual contexts |
| `FontSettingsDataStore` | repeated `context.dataStore.data.map/edit` methods → `PreferenceDelegate<T>` / `BooleanPreferenceDelegate` | Public API preserved; read/write pipeline centralized |

---

## 4. Pipeline Dependency Chain

```
Load:      QuestionRepositoryImpl.importFromFilesWithOrigin()
           ↓
Normalize: PracticeProgressScope.buildPracticeProgressId()
           ↓
Transform: PracticeViewModel random/limited/memory modes
           ↓
Interact:  PracticeViewModel answer/showResult/history
           ↓
Persist:   PracticeProgressRepository.save()
           ↓
Aggregate: HomeViewModel.preferredHomePracticeProgress()
```

**Breaking any link in this chain = breaking practice flow entirely.**

---

## 6. Post-Extraction Coordinator Dependencies (Planned)

| Coordinator | Depends on | Pure Logic? |
|-------------|-----------|-------------|
| **PracticeNavigationCoordinator** | PracticeSessionState (MutableStateFlow) | ✅ |
| **PracticeAnswerHandler** | PracticeSessionState (MutableStateFlow) | ✅ |
| **PracticeProgressCoordinator** | PracticeSessionState + PracticeProgressRepository + PracticeProgressScope | ❌ |
| **PracticeModeCoordinator** | PracticeSessionState + NavigationCoordinator (shared) | ✅ |
| **PracticeFullAnswerCoordinator** | PracticeSessionState + AnswerHandler (shared) | ✅ |
| **PracticeSessionCoordinator** | PracticeSessionState + QuestionRepository + PracticeProgressRepository | ❌ |

**Post-extraction VM**: Only depends on 6 coordinators + `MutableStateFlow<PracticeSessionState>` + minimal repos for DI wiring.
**Pact:** No coordinator depends on `PracticeViewModel` — no cyclic dependency.

---

## 7. Extraction Order Dependency Chain

```
Step 1: NavigationCoordinator  (no deps)
   │
Step 2: AnswerHandler          (no deps, parallelizable with St1)
   │
Step 3: ProgressCoordinator    (no coordinator deps; 1 repo)
   │
Step 4: ModeCoordinator        (depends on NavigationCoordinator)
   │
Step 5: FullAnswerCoordinator  (depends on AnswerHandler)
   │
Step 6: SessionCoordinator     (depends on all C3-C7 stable)
```

---

## 8. Deferred to M2/M3
- Compose component dependency graph
- Repository→DAO exact mapping
- ViewModel→UseCase exact mapping
- Full 225+ file dependency tree
