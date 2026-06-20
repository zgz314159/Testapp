<!--
  Part of: Level 6 Self-Evolving Engineering OS — Memory Layer
  Role: Defines when and how .ai/ derived artifacts are synced from Source of Truth
  Last updated: 2026-06-11
-->

# Memory Sync Protocol

> Defines when and how `.ai/` derived artifacts are re-generated from Source of Truth files.

---

## Source of Truth (SoT) — Root Files

| SoT File | Role | Update frequency |
|----------|------|-----------------|
| `CURRENT_STATE.md` | Agent entry point, active tracks, workflow position | Per session / per phase |
| `ARCHITECTURE.md` | Static architecture, modules, layers, pipeline | When architecture changes |
| `KNOWN_ISSUES.md` | Debt inventory (K/D/P IDs) | When issues open/close |
| `TASK_LOG.md` | Chronological change history | Per meaningful change |

---

## Derived Artifacts — `.ai/` Files

| Derived File | Fed by | Rebuild trigger |
|-------------|--------|-----------------|
| `.ai/current_state.md` | `CURRENT_STATE.md` + L6 health scan | State change OR health re-scan |
| `.ai/architecture_map.md` | `ARCHITECTURE.md` | Architecture change |
| `.ai/tech_debt.md` | `KNOWN_ISSUES.md` + L6 debt mining | Issue open/close OR L6 re-scan |
| `.ai/refactor_candidates.md` | L6 refactor analysis | Health scan OR new analysis |
| `.ai/file_registry.md` | Code scan + L6 analysis | Major file shift (>10% change) |
| `.ai/module_map.md` | `PROJECT_SCAN` + code scan | New module or major relocation |
| `.ai/dependency_graph.md` | `PROJECT_SCAN` + coupling analysis | Dependency change |
| `.ai/change_log.md` | `TASK_LOG.md` | New log entry |

---

## Sync Rules

### Rule 1: Incremental, not full rewrite

When a SoT file changes, only sync the derived files that depend on it. Never regenerate all `.ai/` files at once.

| SoT Change | Sync these `.ai/` files only |
|-----------|------------------------------|
| `CURRENT_STATE.md` | `current_state.md` |
| `ARCHITECTURE.md` | `architecture_map.md` |
| `KNOWN_ISSUES.md` | `tech_debt.md` |
| `TASK_LOG.md` | `change_log.md` |
| L6 Health Scan runs | `current_state.md`, `tech_debt.md`, `refactor_candidates.md`, `file_registry.md` |

### Rule 2: Timestamp every sync

Each derived file carries a header:
```
Last synced: YYYY-MM-DD HH:MM UTC+8
```
If a derived file's `Last synced` is older than its SoT's modification time, it is **stale** and must be synced before use.

### Rule 3: Sync on task completion

After any task that changes project state:
1. Update the relevant SoT file(s)
2. Sync the derived `.ai/` files that depend on them
3. Append to `change_log.md`

### Rule 4: Sync before major phases

Before starting Architecture Design, Decomposition, or Migration:
1. Check if any SoT file has changed since last sync
2. Re-sync stale derived files
3. Re-read `.ai/current_state.md`

### Rule 5: Frozen snapshots are append-only

- `.ai/refactor_candidates.md` — scores are a frozen snapshot. Do not overwrite; append new scan results with a new timestamp section.
- `.ai/change_log.md` — append only, never delete history.

---

## Staleness Detection

```
For each derived file D:
  if D.LastSynced < SoT[S].LastModified:
    D is STALE → re-sync before use
  else:
    D is FRESH → safe to use
```

---

## Sync Checklist (Agent Pre-Flight)

Before any analysis task:
- [ ] Read `.ai/current_state.md` — check `Last synced` timestamp
- [ ] If stale → re-read `CURRENT_STATE.md` → re-generate `.ai/current_state.md`

Before any refactor task:
- [ ] Read `.ai/refactor_candidates.md` — check freeze date
- [ ] Read `.ai/dependency_graph.md` — check maturity level (M1/M2/M3)
- [ ] If target file >100 lines changed since registry → re-scan that file

---

## What NOT to sync

- `LEVEL6_OVERVIEW.md` and engine files — these are rules, not data
- `.ai/12_CONTEXT_LOADING_RULES.md` — context loading rules
- `.ai/13_ARCHITECTURE_GUARD.md` — architecture guard rules
- Historical `*_SUMMARY.md` files — never re-parsed
- `PRACTICE_NAVIGATION_REMEDIATION_TODO.md` — track-specific, not general memory
- `REFACTOR_TODO.md` — human-readable checklist, not auto-generated
