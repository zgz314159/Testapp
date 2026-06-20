<!--
  Derived from: KNOWN_ISSUES.md, 4_TECH_DEBT_MINER.md
  Last synced: 2026-06-14 23:51 UTC+8
  Do not edit directly — regenerate from source when source changes.
-->

# Technical Debt Inventory

> Single consolidated debt inventory. Merges `KNOWN_ISSUES.md` (K/D/P IDs) + `4_TECH_DEBT_MINER.md` (TD IDs).

---

## 🔴 HIGH — Blocking large refactor

| ID | Issue | Source |
|----|-------|--------|
| **K-001** | Device smoke not run: home-card persistence + atomic-bank re-entry | `KNOWN_ISSUES.md` |
| **TD-003** | No CI/CD | `4_TECH_DEBT_MINER.md` |
| **TD-001a** | `QuestionRepositoryImpl.kt` ~306 lines (AFTER extraction) | `4_TECH_DEBT_MINER.md` |

---

## 🟡 MEDIUM

| ID | Issue | Source |
|----|-------|--------|
| **K-007** | Exam route not smoke-tested | `KNOWN_ISSUES.md` |
| **D-001** | Some UI orchestration remains in `:app`; feature modules are active | `KNOWN_ISSUES.md` |
| **D-003** | No CI / ktlint / detekt | `KNOWN_ISSUES.md` |
| **D-004** | Many root `*_SUMMARY.md` files | `KNOWN_ISSUES.md` |
| **TD-006** | Remaining duplicated interaction patterns (PracticeVM↔ExamVM) — reduced via SessionEngine and coordinators | `4_TECH_DEBT_MINER.md` |
| **TD-007** | `SettingsScreen.kt` ~487 lines (refactor pending) | `4_TECH_DEBT_MINER.md` |
| **TD-011** | Progress scope in presentation layer | `4_TECH_DEBT_MINER.md` |
| **TD-014** | Swipeable/FractionalThreshold deprecation migration deferred; gesture behavior needs manual verification | Build warnings |

---

## 🟢 LOW

| ID | Issue | Source |
|----|-------|--------|
| **D-002** | Progress scope ID in wrong layer | `KNOWN_ISSUES.md` |
| **TD-010** | Missing ktlint/detekt | `4_TECH_DEBT_MINER.md` |
| **TD-011** | Progress scope in presentation layer | `4_TECH_DEBT_MINER.md` |
| **TD-012** | Root SUMMARY files excess | `4_TECH_DEBT_MINER.md` |
| **TD-013** | VM line counts stale in scan | `4_TECH_DEBT_MINER.md` |

---

## ⚠️ Patch watchlist

| ID | Concern | Source |
|----|---------|--------|
| **P-001** | Multi-mode historical questionId validation | `KNOWN_ISSUES.md` |
| **P-002** | Page/route-specific fixes | `KNOWN_ISSUES.md` |

---

## ✅ Closed

| Topic | Resolution |
|-------|------------|
| Storage root blank in import picker | System DocumentsUI hides `primary:` on Xperia 1 V/Android 15; workaround: in-app browser |
| Practice stuck on loading | `settingsReady` gate + empty-file sets `progressLoaded` |
| Same-file modes overwriting progress | Scoped ids + home `preferredHomePracticeProgress` |
| Async save ordering | Save ordering guard + regression test |
| Split answered/random history | Unified navigation state container |
| Exam modes overwriting each other | Scoped exam progress ids |
| Startup hard-coded progress delete | Removed |
| K-002 / TD-001: PracticeViewModel ~3900 lines | 已拆分 → ~800 lines (feature-practice) |
| K-003 / TD-004: ExamViewModel ~2455 lines | 已拆分；2026-06-14 residual edit/grade/statistics coordinators added → ~590 lines |
| TD-005: Feature modules empty stubs | 都活跃: feature-practice(26f), feature-exam(31f), ui-common(12f), core(13f) |
| TD-008: PracticeScreen ~1078 lines | 已拆分 → ~414 lines |
| D-005: Scan report VM line counts outdated | 已更新 |
| TD-009: SettingsViewModel dependency redline | `SettingsRepositoryFacade` 聚合 7 repositories；VM 注入 12→6 |
| FontSettingsDataStore getter/setter boilerplate | `PreferenceDelegate<T>` + `BooleanPreferenceDelegate` applied; public API preserved |
| Hard-coded API keys | `BuildConfig` reads from `local.properties` / env vars; source secrets removed |
| Duplicate Markdown normalizer | Single `:core` implementation retained; data-layer `Question.normalizeRichMarkdownFields` delegates to core |
| app → feature-exam private UI imports | Shared question edit/navigation components moved to `:ui-common`; app no longer imports feature-exam component package |
| app `presentation.screen.components` mega-package | Split into home/practice/questionbank/result component namespaces |
| PracticeViewModel progress lifecycle | `PracticeProgressLifecycleCoordinator` owns set/load/save/clear progress lifecycle |
| ExamViewModel Note/Analysis + reset responsibilities | `ExamArtifactStateCoordinator` and `ExamProgressResetCoordinator` extracted |
| Settings import/export loading state | `SettingsActionPipeline` owns action lifecycle |
| Release build/R8 stability | `:app:assembleRelease` passes; POI desktop drawing warning documented/suppressed where actionable |

---

## Compatibility constraints

- Changing `__scope=` progress id format → breaks Room rows, home aggregation, `deleteProgressByFileNamePattern`
- `legacyRandomScopedPracticeProgressId` — do not remove without migration plan

---

## Suggested fix order

1. Run K-001 device smoke (gate before any refactor)
2. Set up CI + ktlint/detekt (TD-003, TD-010)
3. Extract PracticeViewModel coordinators (TD-001) — ✅ DONE
4. **Extract QuestionRepositoryImpl parsers (TD-001a)** — ✅ DONE (7 extractors, 1619→306, 11 files)
5. **Extract SettingsViewModel coordinators + Facade + action pipeline** — ✅ DONE
6. Migrate to feature-practice (TD-005) — ✅ DONE for code modules; PracticeScreen remains blocked by app-only dependencies
7. Apply same pattern to ExamViewModel (TD-004, TD-006) — ✅ DONE, residual artifact/reset coordinators added 2026-06-14
8. Run release and full-build gates — ✅ DONE (`:app:assembleRelease`, `build`)
