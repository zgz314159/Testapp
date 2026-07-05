<!--
  Part of: Level 6 Self-Evolving Engineering OS — Memory Layer
  Role: Defines what files agents MUST and MUST NOT read per task type
  Last updated: 2026-06-11
-->

# Context Loading Rules

> **UPDATED 2026-07-05:** 路径已迁入 PowerAI Engineering OS。默认读 `.ai/current/current_state.md`；Guard 读 `.ai/workflows/architecture_guard.md`。入口：`.ai/README.md`。

> **CRITICAL**: These rules prevent token waste by limiting default context loading to what is necessary for the task.

---

## Hard Rule: NO DEFAULT FULL-PROJECT SCAN

```
❌ FORBIDDEN: Re-scanning all 225+ .kt files for every task
❌ FORBIDDEN: Reading all root *_SUMMARY.md files by default
❌ FORBIDDEN: grep/rg across entire project without a focused target
```

---

## Default Context Load (EVERY task)

Every agent session MUST read these files first, in order:

| # | File | Why |
|---|------|-----|
| 1 | `.ai/current/current_state.md` | Know where we are, what's active, what's blocked |
| 2 | `.ai/current/refactoring_plan.md` or `refactor_candidates.md` | Know priorities |

**Total**: 2 files. Minimal token cost.

---

## Task-Specific Context Load

After the default load, ONLY load files relevant to the task type:

### 🆕 Feature Implementation (MUST load guard first)

| File | Why |
|------|-----|
| `.ai/workflows/architecture_guard.md` | **MANDATORY** — LOC/Dependency/Responsibility redlines |
| `.ai/current/file_registry.md` | Current LOC for impacted files |
| `.ai/current/dependency_graph.md` | Dependency counts for impacted files |
| Targeted `.kt` file(s) | The file(s) being modified |
| Unit test file(s) for target | Regression guard |

**Forbidden**: Writing ANY code before Architecture Placement Review passes.

### 🏗 Architecture Design

| File | Why |
|------|-----|
| `.ai/architecture_map.md` | Module structure, layers, pipeline |
| `.ai/dependency_graph.md` | Coupling matrix, hidden deps |
| `.ai/file_registry.md` | Target file metrics |
| Target `.kt` file | The file being analyzed |
| Files it imports (only direct imports) | Dependency chain |

**Forbidden**: Reading unrelated ViewModels, unrelated Screens, unrelated test files.

### 🔨 Refactoring (Single File)

| File | Why |
|------|-----|
| `.ai/tech_debt.md` | Known debts for this file |
| `.ai/dependency_graph.md` | What this file couples to |
| Target `.kt` file | The file being refactored |
| Files it directly imports | Dependency chain |
| Unit test file(s) for this file | Regression guard |

**Forbidden**: Reading files of the same type but different domain (e.g., don't read ExamViewModel when refactoring PracticeViewModel).

### 🩺 Health Scan

| File | Why |
|------|-----|
| All `.ai/` files | Full memory is relevant |
| `KNOWN_ISSUES.md` | Source of truth for debts |
| Top 20 files from `.ai/file_registry.md` | Re-measure hotspots |

**Forbidden**: Reading all 225+ files. Only re-scan the Top 30 hot files.

### 📝 Documentation / Memory Sync

| File | Why |
|------|-----|
| SoT files being synced (`CURRENT_STATE.md`, `TASK_LOG.md`, etc.) | Source |
| `.ai/11_MEMORY_SYNC_PROTOCOL.md` | Sync rules |
| `.ai/12_CONTEXT_LOADING_RULES.md` (this file) | Loading rules |

**Forbidden**: Reading any `.kt` source files.

### 🧪 Testing / Debugging

| File | Why |
|------|-----|
| `.ai/architecture_map.md` (testing section) | Test map |
| Target test file(s) | Tests to run |
| Target source file(s) | Code under test |

**Forbidden**: Reading unrelated test files.

### 🖥 Windows 终端 / 中文编码

| File | Why |
|------|-----|
| `.ai/ENCODING_AND_TERMINAL_GUIDE.md` | 终端乱码 vs 源码乱码；PowerShell 5 用 `;` 不用 `&&`；UTF-8 预防 |

**触发条件**：在 Windows 跑 shell 命令；批量修改含中文注释/字符串的 `.kt` / `.xml`。

---

## Context Budget Enforcement

| Task Type | Max files to read | Approx. token budget |
|-----------|------------------|---------------------|
| Feature Implementation | 2 default + 5 task = **7** | ~15,000 tokens |
| Architecture Design | 2 default + 4 task = **6** | ~10,000 tokens |
| Refactoring (single file) | 2 default + 4 task = **6** | ~15,000 tokens |
| Health Scan | 2 default + 3 memory + 20 hot = **25** | ~50,000 tokens |
| Documentation | 2 default + 2 task = **4** | ~5,000 tokens |
| Testing/Debugging | 1 default + 3 task = **4** | ~10,000 tokens |

**If a task requires exceeding this budget, it MUST be split into sub-tasks.**

---

## File Discovery Rules

When you need to find a file NOT in `.ai/file_registry.md`:

1. First check `.ai/module_map.md` — which module should contain it?
2. Then glob within that module only, NOT the entire project
3. Do NOT glob recursively across all modules for a single file

Example:
```
✅ Good: Glob "app/src/main/java/com/example/testapp/presentation/screen/*.kt"
❌ Bad:  Glob "**/*.kt" across entire project
```

---

## Summary: The Golden Rule

```
READ WHAT YOU NEED. NOTHING MORE.
TRUST THE MEMORY LAYER. DON'T RE-SCAN.
```
