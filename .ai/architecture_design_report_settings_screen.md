<!--
  File: architecture_design_report_settings_screen.md
  Target: SettingsScreen.kt (1358 lines → target ~450 lines)
  Generated: 2026-06-13
  Status: DESIGN COMPLETE — ready for Phase D execution
-->

# Architecture Design Report — SettingsScreen

## 1. Current State

| Metric | Value |
|--------|-------|
| Lines | 1358 |
| Composables | 1 @Composable fun + 1 private @Composable |
| ViewModel deps | 1 (SettingsViewModel) |
| ActivityResult launchers | 6 |
| Collapsible sections | 5 (Memory/Exam/Practice/Fill + basic) |
| Local state vars | ~30 `remember` blocks |

**Key observation**: 5 collapsible sections + import/export buttons + loading overlay = 7 clear extraction targets. Each section follows a `Row(clickable) → if(expanded) { content } → Spacer` pattern.

## 2. Bounded Contexts Identification

### BC-1: Basic Settings Panel (~62 lines, lines 293-354)
Font size slider (14-32) + font style radio (Normal/Serif/Monospace)

### BC-2: Sound & Dark Mode Panel (~33 lines, lines 355-387)
Sound toggle switch + dark mode toggle switch

### BC-3: Memory Panel (~137 lines, lines 389-525)
Expandable: toggle switch + batch size slider + wrong mode radio + pool mode radio + help text

### BC-4: Exam Panel (~81 lines, lines 527-607)
Expandable: random switch + question count slider + answer delay slider

### BC-5: Practice Panel (~95 lines, lines 609-703)
Expandable: random switch + question count slider + correct/wrong delay sliders

### BC-6: Fill Panel (~316 lines, lines 705-1020)
Expandable: generation mode chips (5 modes) + blank count slider + help text + full-answer sub-settings (completion mode + order) + score range slider + tag filter chips + filter summary

### BC-7: Import/Export Panel (~103 lines, lines 1022-1124)
6 buttons: import quiz (system), import quiz (local), export quiz, import wrong, export wrong, import favorites, export favorites

### BC-8: Loading Overlay (~61 lines, lines 1126-1186)
Full-screen semi-transparent Card with CircularProgressIndicator, progress percentage, cancel button

### BC-9: ExportSourceSelectionDialog (~58 lines, lines 1299-1356)
AlertDialog with radio-list of file names + confirm/cancel

## 3. Extraction Plan

```
settings/ui/SettingsBasicPanel.kt            (~80 lines)
settings/ui/SettingsMemoryPanel.kt           (~155 lines)
settings/ui/SettingsExamPanel.kt             (~100 lines)
settings/ui/SettingsPracticePanel.kt         (~115 lines)
settings/ui/SettingsFillPanel.kt             (~340 lines)
settings/ui/SettingsImportExportPanel.kt     (~120 lines)
settings/ui/SettingsLoadingOverlay.kt        (~80 lines)
settings/ui/ExportSourceSelectionDialog.kt   (~70 lines)
SettingsScreen.kt                            (~450 lines)
```

**Total extracted**: ~1060 lines from 1358 (78% reduction in main file).

## 4. What Stays in SettingsScreen

After extraction, SettingsScreen retains:
- State collection from ViewModel (44-76)
- Collapsible state vars (79-94)
- LaunchedEffect for init (96-100)
- ActivityResult launcher definitions (102-240)
- `localResult` + snackbar wiring (102-255)
- LaunchedEffects for selected file name sync (257-271)
- Extracted composable calls in Column
- Export dialog instances (3x ExportSourceSelectionDialog)
- Storage permission dialog
- File browser dialog

## 5. Design Decisions

| Decision | Rationale |
|----------|-----------|
| Extract as composables in `settings/ui/` | Follows PracticeScreen pattern; `settings/` already used for VM coordinators |
| Each collapsible panel includes header+body | Encapsulation: parent only needs `expanded` + `onToggle` |
| Keep launchers in SettingsScreen | They hold `rememberLauncherForActivityResult` state which must stay in Composition |
| Pass fontSize + fontFamily as params | Needed by all sections for consistent styling |
| ExportSourceSelectionDialog goes to `settings/ui/` | Already a private composable; just move file |

## 6. Immutable Constraints

1. **NO changes to SettingsViewModel** — Screen-only extraction
2. **NO changes to launcher contracts** — `rememberLauncherForActivityResult` stays in SettingsScreen
3. **`SettingsScreen()` function signature unchanged** — `viewModel` + `onNavigateHome` params
4. **All ViewModel calls pass through SettingsScreen** — extracted composables receive callbacks, not VM
