<!--
  Part of: Level 6 Self-Evolving Engineering OS — Architecture Guard
  Role: Pre-implementation gate that prevents architecture decay on every feature addition
  Created: 2026-06-13
  Must be loaded BEFORE any feature implementation begins.
-->

# 13 — Architecture Guard (架构守门员)

> **CRITICAL**: This is the single most important defense against architecture decay.
> Without this guard, every feature addition slowly bloats files back toward God-class territory.
> Activate this guard on EVERY feature request BEFORE writing implementation code.

---

## Why this exists

```
History:
  PracticeViewModel was 3900 lines → split to 724 lines
  QuestionRepositoryImpl was 1619 lines → split to 306 lines
  ExamViewModel was 2455 lines → split to 415 lines

If future features are added WITHOUT guard checks:
  PracticeViewModel 724 → 2800 lines (within 6 months)
  All bounded-context work undone.
```

The Architecture Guard prevents re-bloat by enforcing rules BEFORE any new code is written.

---

## Core Rule 1: No Direct Feature Implementation

### FORBIDDEN workflow

```
User: "Add AI analysis feature"
Agent: *writes code directly into PracticeViewModel*
```

### REQUIRED workflow

```
User: "Add AI analysis feature"

Agent MUST respond with:

  Follow Level 6 OS. Execute Architecture Placement Review.

  Feature Request: AI Analysis

  1. Which module owns this feature?
     :feature-ai | :data | :kb | :pipeline

  2. Which existing files may change?

  3. Does any file exceed:
     - LOC limit
     - Dependency limit
     - Responsibility limit

  If limits exceeded:
     Generate extraction plan first.
     Do not implement.
```

---

## Core Rule 2: File LOC Redlines

### ViewModel

| Level | Lines | Action |
|-------|-------|--------|
| 🟢 Safe | ≤400 | Allowed |
| 🟡 Warning | 401–600 | Warn + suggest split |
| 🟠 Danger | 601–800 | Must split before adding |
| 🔴 Forbidden | >800 | Split mandatory |

### Repository

| Level | Lines | Action |
|-------|-------|--------|
| 🟢 Safe | ≤500 | Allowed |
| 🟡 Warning | 501–700 | Warn + suggest split |
| 🟠 Danger | 701–1000 | Must split before adding |
| 🔴 Forbidden | >1000 | Split mandatory |

### Compose Screen

| Level | Lines | Action |
|-------|-------|--------|
| 🟢 Safe | ≤500 | Allowed |
| 🟡 Warning | 501–800 | Warn + suggest split |
| 🟠 Danger | 801–1000 | Must split before adding |
| 🔴 Forbidden | >1000 | Split mandatory |

### PracticeScreen enforced budget (Phase 35+)

| Rule | Detail |
|------|--------|
| LOC redline | **500** (stricter than generic Screen 1000) |
| Gate script | `scripts/check-practice-screen-loc.ps1` — run before PR / after edits |
| 全仓库扫描 | `scripts/check-loc-over-500.ps1` — 报告见 `.ai/loc_audit.md` |
| Decomposition spec | `.ai/practice_screen_decomposition.md` |
| Forbidden | New `private fun DialogsHost` in `PracticeScreen.kt`; inline >30-line composable blocks |

### Coordinator / Delegate

| Level | Lines | Action |
|-------|-------|--------|
| 🟢 Safe | ≤300 | Allowed |
| 🟡 Warning | 301–500 | Warn + suggest split |
| 🟠 Danger | 501–600 | Must split before adding |
| 🔴 Forbidden | >600 | Split mandatory |

### Pre-implementation check formula

```
Current lines + Estimated new lines = Predicted total

If Predicted total ≥ Danger line:
  STOP. Split first. Then add feature.
```

---

## Core Rule 3: Responsibility Count Redline

**Rule**: Each file MUST have ≤3 responsibilities.

### Example violations

```
SettingsViewModel (before split):
  1. Font settings → ✅
  2. Import logic  → ✅
  3. Export logic  → ✅
  4. File management → ❌ 4th responsibility = VIOLATION
  → Must split into coordinators
```

### Pre-implementation check

```
Q: Does this new feature add a NEW responsibility?
   YES → Create new file. Do not expand existing file.
   NO  → Allowed to extend existing file (within LOC limits).
```

---

## Core Rule 4: Dependency Count Redline

| Dep Count | Status | Action |
|-----------|--------|--------|
| ≤5 | 🟢 Normal | Allowed |
| 6–8 | 🟡 Warning | Prefer Facade/Coordinator |
| >8 | 🔴 Forbidden | Must introduce Facade / Coordinator / UseCase |

### Example violation

```
QuestionRepositoryImpl (before split):
  16 injected dependencies → 🔴 FORBIDDEN
  Must introduce extractors (TxtParser, SqliteParser, etc.)
```

### Pre-implementation check

```
Q: Will adding this feature increase dependency count from 8 → 9?
   YES → STOP. Introduce Facade / Coordinator / UseCase first.
   NO  → Allowed to proceed.
```

---

## Core Rule 5: Architecture Placement Review (MANDATORY)

### Required before EVERY feature implementation

```markdown
Follow Level 6 OS.

Execute Architecture Placement Review.

New Feature: [FEATURE_NAME]

## Module Ownership
- Owner module: [ :feature-xxx | :data | :domain | :app ]

## Impacted Files

| File | Current LOC | Est. New LOC | Predicted Total | LOC Check | Dep Check | Resp Check |
|------|-------------|-------------|-----------------|-----------|-----------|------------|
| X | N | M | N+M | ✅/⚠️/❌ | ✅/⚠️/❌ | YES/NO |
| Y | ... | ... | ... | ... | ... | ... |

## Risk Assessment
- [ ] No file exceeds LOC redline
- [ ] No file exceeds dependency redline (>8)
- [ ] No file gains a 4th responsibility
- [ ] No file listed above has zero unit tests

## Decision
✅ ALL CHECKS PASSED — Proceed to implementation
OR
❌ CHECKS FAILED — Generate extraction plan first. Do not implement.
```

---

## Core Rule 6: The Correct Feature Request Format

### ❌ NEVER say

```
"Help me implement XXX"
```

### ✅ ALWAYS say

```
Follow Level 6 OS.

Feature Request: XXX

Execute:
1. Architecture Placement Review
2. Impact Analysis
3. Risk Analysis

Only then generate implementation plan.
```

---

## Mature Workflow (Level 6 Complete)

```
Feature Request
    ↓
Architecture Placement Review   ← GUARD (this file)
    ↓
Impact Analysis                 ← 6_IMPACT_SIMULATOR
    ↓
Dependency Check               ← dependency_graph.md
    ↓
LOC Check                      ← file_registry.md
    ↓
Responsibility Check           ← architecture_map.md
    ↓
Implementation Plan            ← 5_EVOLUTION_STRATEGY_PLANNER
    ↓
Code                           ← 7_CONTROLLED_EXECUTION_ENGINE
    ↓
Regression Check               ← 8_LIVE_REGRESSION_MONITOR
    ↓
Memory Sync                    ← 11_MEMORY_SYNC_PROTOCOL
    ↓
Update Maps                    ← architecture_map.md + file_registry.md + dependency_graph.md
```

Not:

```
Feature Request → Code
```

---

## Integration with Existing Level 6 System

### Position in Level 6 pipeline

```
LEVEL6_OVERVIEW.md
 ├── 1_EVOLUTION_ENGINE.md          ← Trigger detector
 ├── 2_SYSTEM_HEALTH_ENGINE.md      ← Health scan
 ├── 3_ARCHITECTURE_DRIFT_ANALYZER  ← Drift detection
 ├── 4_TECH_DEBT_MINER.md           ← Debt mining
 ├── 5_EVOLUTION_STRATEGY_PLANNER   ← Strategy design
 ├── 6_IMPACT_SIMULATOR.md          ← Full-system impact
 ├── 7_CONTROLLED_EXECUTION_ENGINE  ← Step-by-step execution
 ├── 8_LIVE_REGRESSION_MONITOR.md   ← Regression validation
 ├── 9_POST_EVOLUTION_VALIDATOR     ← Stability check
 ├── 10_EVOLUTION_REPORT_ENGINE.md  ← Final report
 ├── 11_MEMORY_SYNC_PROTOCOL.md     ← Sync rules
 ├── 12_CONTEXT_LOADING_RULES.md    ← Token budget
 └── 13_ARCHITECTURE_GUARD.md       ← THIS FILE — pre-implementation gate
```

### Trigger rule

This guard activates BEFORE:
- `5_EVOLUTION_STRATEGY_PLANNER`
- `7_CONTROLLED_EXECUTION_ENGINE`

This guard is bypassed AFTER:
- Architecture Placement Review ✅
- All redline checks PASSED

---

## Current state reference (2026-06-13)

### Files at risk

| File | Lines | Redline | Status |
|------|-------|---------|--------|
| PracticeNavigationCoordinator | 617 | 600 (danger) | ⏸️ Suspended |
| PracticeSessionCoordinator | 641 | 600 (danger) | ⏸️ Suspended |
| FontSettingsDataStore | 555 | 1000 (repo) | ⏸️ Suspended |
| PracticeViewModel | 724 | 800 (VM) | ⚠️ Warning zone |
| PracticeScreen | 390 | 500 (Screen enforced) | ✅ Safe |
| HomeScreen | 399 | 1000 (Screen) | ✅ Safe |

### Current dependency hotspots

| File | Deps | Redline | Status |
|------|------|---------|--------|
| PracticeViewModel | ~15 | >8 forbidden | 🔴 Already at risk |
| ExamViewModel | ~12 | >8 forbidden | 🔴 Already at risk |
| PracticeSessionCoordinator | ~10 | >8 forbidden | 🔴 Already at risk |

### Resolved (post Phase A-G + H~12)

| File | Before | After | Improvement |
|------|--------|-------|-------------|
| PracticeViewModel | 3900 | 724 | −81% |
| ExamViewModel | 2455 | 415 | −83% |
| QuestionRepositoryImpl | 1619 | 306 | −81% |
| SettingsViewModel | 1178 | 416 | −65% |
| SettingsScreen | 1359 | 487 | −64% |
| HomeScreen | 1068 | 399 | −63% |
| PracticeScreen | 1279 | 499 | −61% |
| RichText | 892 | 252 | −72% |
| AppNavHost | 741 | 295 | −60% |

---

## Quick reference card (for agents)

```
BEFORE writing ANY code for a new feature:

□ Step 1: Read 13_ARCHITECTURE_GUARD.md (this file)
□ Step 2: Determine module ownership
□ Step 3: List all files that will change + current LOC
□ Step 4: Calculate predicted LOC after feature
□ Step 5: Check if any file exceeds LOC/Dependency/Responsibility redline
□ Step 6: If any redline exceeded → generate extraction plan, DO NOT implement
□ Step 7: If all checks pass → proceed to Implementation Plan
```

---

## Document metadata

| Field | Value |
|-------|-------|
| Part of | Level 6 Self-Evolving Engineering OS |
| Position | 13 — Final system piece |
| Trigger | EVERY feature request before implementation |
| Bypass condition | Architecture Placement Review passed + all redline checks green |
| Last updated | 2026-06-13 |
