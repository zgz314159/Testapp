# Architecture

> High-level map for agents. Implementation detail lives in code; tactical tasks in [REFACTOR_TODO.md](REFACTOR_TODO.md) and [PRACTICE_NAVIGATION_REMEDIATION_TODO.md](PRACTICE_NAVIGATION_REMEDIATION_TODO.md).

## Product shape

Android quiz/practice app: import question files, practice and exam modes, wrong-book, favorites, atomic-bank routes, home file cards with progress, settings (fonts, modes).

## Module dependency (Gradle)

```
:app
 ├── :domain
 └── :data → :domain

:feature-practice, :feature-exam, :ui-common, :core  (stubs / minimal — not primary homes for features yet)
:baseline-profile  (performance baseline)
```

**Rule:** `domain` has no project dependencies. `data` implements domain repositories. `app` hosts UI, ViewModels, DI modules, and most use-case wiring today.

## Layer responsibilities

| Layer | Location (today) | Responsibility | Must not |
|-------|------------------|----------------|----------|
| **UI** | `app/.../presentation/screen`, `components` | Compose screens, gestures, display state | Fix structural/normalization bugs locally with one-off branches |
| **Presentation / session** | `PracticeViewModel`, `ExamViewModel`, `HomeViewModel` | Session state, navigation modes, orchestrate use cases | Own low-level SQL or file parsing |
| **Domain** | `:domain` models, repository interfaces, some use cases | Business types, contracts | Depend on Android/framework |
| **Data** | `:data` Room, repositories, DataStore | Persistence, IO | UI rules |
| **DI** | `app/.../di/*` | Hilt bindings (e.g. `PersistenceUseCaseModule`) | Business logic |

### Pipeline mental model (target discipline)

Even when not separate packages, think in this order:

1. **Load** — questions / wrong-book / favorites / atomic-bank into session  
2. **Normalize** — indices, scope ids, question-set membership  
3. **Transform** — random, limited count, memory mode, full-answer derivation  
4. **Interact** — answer, showResult, navigation (including answered-history)  
5. **Persist** — `PracticeProgress` / `ExamProgress` async save  
6. **Aggregate** — home card progress across scoped rows  

**Export/UI must not patch normalize-layer mistakes.**

## Key subsystems

### Practice session

- **State:** `PracticeSessionState` + unified navigation state container in `PracticeViewModel`.
- **Navigation:** Explicit modes for forward/back and answered-history; no ad hoc parallel history fields.
- **Progress id:** `buildPracticeProgressId` in `PracticeProgressScope.kt` — encodes question count cap and memory-mode parameters; order vs random share scope per comment in VM.

### Exam session

- Parallel structure in `ExamViewModel`; scoped progress ids aligned with practice rules where applicable.
- Practice and exam **do not** share one runtime progress object.

### Persistence

- **Table:** `practice_progress` / exam progress entities via Room.
- **Use cases:** Wired in `PersistenceUseCaseModule` → `PracticeProgressRepository` / `ExamProgressRepository`.
- **Home:** Aggregates scoped entries via `preferredHomePracticeProgress` / `practiceProgressMatchesFile`.
- **Cleanup:** Pattern delete by file name for scoped rows.

### Navigation (Compose)

- `AppNavHost.kt` — routes include normal practice, wrong-book, favorites, atomic-bank; automation hooks documented in PRACTICE TODO (accessibility / startup route).

## Testing map

| Area | Tests |
|------|--------|
| Practice VM / navigation / save order | `app/src/test/.../PracticeViewModelTest.kt` |
| Home progress aggregation | `HomeViewModelTest.kt` |
| Scope id helpers | `ProgressScopeTest.kt` |
| Exam grading | `GradeExamUseCaseTest.kt` |

Device-side history routes: partially verified per PRACTICE TODO; **home + atomic-bank persistence smoke still open**.

## Refactor north star (not started in bulk)

1. Extract coordinators from God ViewModels (**one concern per PR**).  
2. Migrate practice UI/VM into `:feature-practice` by **screen**, not utils-only.  
3. Move progress id policy toward `domain` when persistence boundary is refactored.  

See [REFACTOR_TODO.md](REFACTOR_TODO.md) and [KNOWN_ISSUES.md](KNOWN_ISSUES.md).

## Agent read order

1. [CURRENT_STATE.md](CURRENT_STATE.md)  
2. Track TODO (practice vs refactor)  
3. This file for boundaries  
4. [TASK_LOG.md](TASK_LOG.md) for what changed last  
