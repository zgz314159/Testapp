<!--
  Derived from: ARCHITECTURE.md, L6 Architecture Design Report 2026-06-11
  Last synced: 2026-06-11 21:15 UTC+8
  Do not edit directly — regenerate from source when ARCHITECTURE.md changes.
-->

# Architecture Map

> Static architecture snapshot. Source: `ARCHITECTURE.md`.

## Product shape

Android quiz/practice app: import question files, practice and exam modes, wrong-book, favorites, atomic-bank routes, home file cards with progress, settings (fonts, modes).

## Module dependency (Gradle)

```
:app
 ├── :domain
 └── :data → :domain

:feature-practice, :feature-exam, :ui-common, :core  (stubs — not primary homes for features yet)
```

**Rule:** `domain` has no project dependencies. `data` implements domain repositories. `app` hosts UI, ViewModels, DI modules, and most use-case wiring today.

## Layer responsibilities

| Layer | Location (today) | Responsibility | Must not |
|-------|------------------|----------------|----------|
| **UI** | `app/.../presentation/screen`, `components` | Compose screens, gestures, display state | Fix structural bugs locally with one-off branches |
| **Presentation / session** | `PracticeViewModel`, `ExamViewModel`, `HomeViewModel` | Session state, navigation modes, orchestrate use cases | Own low-level SQL or file parsing |
| **Domain** | `:domain` models, repository interfaces, some use cases | Business types, contracts | Depend on Android/framework |
| **Data** | `:data` Room, repositories, DataStore | Persistence, IO | UI rules |
| **DI** | `app/.../di/*` | Hilt bindings | Business logic |

## Pipeline mental model

1. **Load** — questions / wrong-book / favorites / atomic-bank into session
2. **Normalize** — indices, scope ids, question-set membership
3. **Transform** — random, limited count, memory mode, full-answer derivation
4. **Interact** — answer, showResult, navigation (including answered-history)
5. **Persist** — `PracticeProgress` / `ExamProgress` async save
6. **Aggregate** — home card progress across scoped rows

**Export/UI must not patch normalize-layer mistakes.**

## Key subsystems

### Practice session
- **State:** `PracticeSessionState` + unified navigation state container
- **Navigation:** Explicit modes for forward/back and answered-history
- **Progress id:** `buildPracticeProgressId` in `PracticeProgressScope.kt`
- **🆕 Decomposition progress (6/6 pure + radical Phase 1):**
  - ✅ `PracticeNavigationCoordinator` — navigation modes + history
  - ✅ `PracticeAnswerHandler` — pure answer-evaluation logic
  - ✅ `PracticeProgressCoordinator` — progress utility functions
  - ✅ `PracticeModeCoordinator` — mode config + round planning + snapshots (radical: holds `_sessionState`)
  - ✅ `PracticeFullAnswerCoordinator` — full-answer config + fill transform
  - ✅ `PracticeSessionCoordinator` — placeholder (radical Phases 2-3 pending)
- **Pattern:** Single `MutableStateFlow<PracticeSessionState>` shared by all coordinators (radical mode)
- **Target:** 3900 → ~800 (VM) + ~1500 (coordinators) lines across 7 files

### Exam session
- Parallel structure in `ExamViewModel`; scoped progress ids aligned with practice rules
- Practice and exam **do not** share one runtime progress object

### Persistence
- **Table:** `practice_progress` / exam progress entities via Room
- **Use cases:** Wired in `PersistenceUseCaseModule`
- **Cleanup:** Pattern delete by file name for scoped rows

### Navigation (Compose)
- `AppNavHost.kt` — routes include normal practice, wrong-book, favorites, atomic-bank

## Testing map

| Area | Tests |
|------|--------|
| Practice VM / navigation / save order | `PracticeViewModelTest.kt` |
| Home progress aggregation | `HomeViewModelTest.kt` |
| Scope id helpers | `ProgressScopeTest.kt` |
| Exam grading | `GradeExamUseCaseTest.kt` |

## Refactor north star

1. Extract coordinators from God ViewModels (**one concern per PR**) — 🟢 **Designed** with 6-coordinator blueprint for PracticeVM
2. Migrate practice UI/VM into `:feature-practice` by **screen**
3. Move progress id policy toward `domain` when persistence boundary is refactored

**🆕 PracticeVM decomposition**: See [.ai/architecture_design_report.md](architecture_design_report.md) and [.ai/decomposition_blueprint.md](decomposition_blueprint.md).
**Migration**: 6-step plan in [.ai/migration_plan.md](migration_plan.md). **Blocker**: K-001 device smoke.

**🆕 QuestionRepositoryImpl decomposition**: See [.ai/architecture_design_report_repo.md](architecture_design_report_repo.md) and [.ai/decomposition_blueprint_repo.md](decomposition_blueprint_repo.md).
**Plan**: 7 parsers/extractors (Txt→Sqlite→Json→Docx→Excel→Metadata→Markdown), 1619→~430 (repo) + ~1190 (7 extractors) across 8 files.
**Blocker**: None — all parsers are pure `File → List<Question>` transforms with zero shared runtime state.
