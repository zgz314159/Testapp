<!--
  Derived from: L6 Architecture Design Phase 2026-06-11
  Target: PracticeViewModel.kt (Score 98, ~3900 lines)
  Last synced: 2026-06-11 21:05 UTC+8
  Do not edit directly — re-run Architecture Design to regenerate.
  Frozen snapshot — 7 bounded contexts identified.
-->

# Architecture Design Report: PracticeViewModel.kt

> Target: Highest priority refactor candidate (Score 98/100, ~3900 lines, 7 responsibilities).

---

## 1. RESPONSIBILITY BREAKDOWN (7 Bounded Contexts)

| # | Bounded Context | Pipeline Stage | Lines (est.) | Risk |
|---|-----------------|---------------|-------------|------|
| **C1** | Question Loading | Load | ~400 | 🔴 Depends on QuestionRepositoryImpl |
| **C2** | Session State Management | Normalize | ~500 | 🔴 Foundation for all contexts |
| **C3** | Navigation Control | Interact | ~600 | 🔴 Historical bug high-density area |
| **C4** | Answer Processing | Interact | ~700 | 🟠 Multi-mode branching |
| **C5** | Progress Persistence | Persist | ~500 | 🔴 Async save ordering |
| **C6** | Full-Answer Mode | Transform+Interact | ~600 | 🟠 Overlaps with C4 |
| **C7** | Random/Special Modes | Transform | ~600 | 🟠 Overlaps with C3/C4 |

---

## 2. STATE OWNERSHIP MAP

| State | Current Owner | Suggested Owner | Rationale |
|-------|---------------|----------------|-----------|
| `PracticeSessionState` | PracticeViewModel | **PracticeSessionCoordinator** | Session data, not VM-exclusive |
| `currentIndex` | PracticeViewModel | **PracticeNavigationCoordinator** | Navigation-exclusive state |
| `navigationMode` | PracticeViewModel | **PracticeNavigationCoordinator** | Navigation mode |
| `answeredHistory` | PracticeViewModel | **PracticeNavigationCoordinator** | History back-tracking |
| `selectedOptions` | PracticeViewModel | **PracticeAnswerHandler** | Answer-exclusive state |
| `showResult` | PracticeViewModel | **PracticeAnswerHandler** | Answer display |
| `progressId` | PracticeViewModel | **PracticeProgressCoordinator** | Persistence-exclusive |
| `randomSeed/shuffledIndices` | PracticeViewModel | **PracticeModeCoordinator** | Mode control |
| `questionList` | PracticeViewModel | **PracticeSessionCoordinator** | Session data |
| `loading/ready` flags | PracticeViewModel | **PracticeSessionCoordinator** | Session lifecycle |

---

## 3. DEPENDENCY SEPARATION

| Bounded Context | External Dependencies | Pure Logic? |
|-----------------|----------------------|-------------|
| C1: Loader | QuestionRepository, PracticeProgressRepository | ❌ |
| C2: Session | PracticeProgressRepository (restore) | ❌ |
| C3: Navigation | **None** | ✅ |
| C4: Answer | **None** | ✅ |
| C5: Persistence | PracticeProgressRepository, PracticeProgressScope | ❌ |
| C6: Full-Answer | (depends on C4) | ✅ |
| C7: Modes | (depends on C3/C4) | ✅ |

**Key insight**: C3, C4, C6, C7 are pure logic → extract first.

---

## 4. COORDINATOR OPPORTUNITIES

| Coordinator | Hosts | Complexity | Priority | Lines |
|-------------|-------|-----------|--------|-------|
| **PracticeNavigationCoordinator** | C3 | 🟢 LOW | **1st** | ~330 |
| **PracticeAnswerHandler** | C4 | 🟡 MEDIUM | **2nd** | ~260 |
| **PracticeProgressCoordinator** | C5 | 🟡 MEDIUM | **3rd** | ~230 |
| **PracticeModeCoordinator** | C7 | 🔴 HIGH | **4th** | ~185 |
| **PracticeFullAnswerCoordinator** | C6 | 🔴 HIGH | **5th** | ~245 |
| **PracticeSessionCoordinator** | C1+C2 | 🔴 HIGH | **6th (last)** | ~255 |

**Expected**: 3900 rows → ~800 (VM) + 6×~250 (coordinators) = ~2300 across 7 files.

---

## 5. KEY DESIGN DECISIONS

1. **Coordinator pattern** (not UseCase) — coordinators have stateful sessions, suited for Compose StateFlow ecosystem
2. **Shared PracticeSessionState** — single source of truth via `MutableStateFlow`, all coordinators read/write through it
3. **Same-package extraction** — `presentation.screen.coordinator/` first, module migration later
4. **Backward-compatible** — VM public API unchanged; `PracticeScreen` needs zero modifications

---

## 6. EXTRACTION ORDER (Dependency Tree)

```
Step 0: Baseline (K-001 smoke + test pass + git tag)
  │
Step 1: PracticeNavigationCoordinator (C3, pure logic)
  │  VM: 3900 → 3570
  │
Step 2: PracticeAnswerHandler (C4, pure logic)
  │  VM: 3570 → 3310
  │
Step 3: PracticeProgressCoordinator (C5, 1 repo dep)
  │  VM: 3310 → 3080
  │
Step 4: PracticeModeCoordinator (C7, needs C3 stable)
  │  VM: 3080 → 2895
  │
Step 5: PracticeFullAnswerCoordinator (C6, needs C4 stable)
  │  VM: 2895 → 2650
  │
Step 6: PracticeSessionCoordinator (C1+C2, needs all stable)
  │  VM: 2650 → 800
  │
Final: ~2300 lines across 7 files
```
