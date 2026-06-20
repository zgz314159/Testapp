<!--
  Derived from: L6 Bloated File Scan 2026-06-13 (Round 2)
  Last synced: 2026-06-13 13:37 UTC+8
  Scan scope: All .kt files in app/src/main/java/com/example/testapp/
  Excludes: .history/, _backup/
  Criteria: 短小 / 无状态 / 单一数据流 / 面向管道 / 职责边界清晰
-->

# Bloated File Scan Report — Round 2 (2026-06-13)

> Phase A-G 已完成。本轮扫描剩余所有 Kotlin 文件，按五大特质逐文件打分。
> 2026-06-14 update: 本报告中的部分行数已过期。`PracticeNavigationCoordinator` 已降至约 149 行，`PracticeSessionCoordinator` 已降至约 271 行，`ExamViewModel` 已完成 residual edit/grade/statistics coordinator extraction（约 590 行），`SettingsViewModel` 已通过 `SettingsRepositoryFacade` 解决依赖红线，`FontSettingsDataStore` 已通过泛型 delegate 降至约 247 行。最新状态以 `CURRENT_STATE.md`、`.ai/current_state.md`、`.ai/refactoring_plan.md` 为准。

---

## 五大特质打分规则

| 特质 | 满分含义 |
|------|---------|
| **短小** | ≤200 行=满分，每超 100 行扣 1 分 |
| **无状态** | 0 个 `mutableStateOf` / `MutableStateFlow` 声明=满分，每多 1 个扣 1 分（`remember` 局部状态可豁免） |
| **单一数据流** | 函数只读不写 / 只写不读 / 单向管道=满分，混合读写扣分 |
| **面向管道** | 方法平均 ≤20 行=满分，越长越扣分 |
| **职责边界清晰** | 单一 Bounded Context=满分，每多 1 个扣 1 分 |

---

## Top 12 问题文件评级

### 🔴 1. HomeScreen.kt (1068 行) — 2/10

| 特质 | 得分 | 详情 |
|------|------|------|
| 短小 | 0/2 | 1068 行 |
| 无状态 | 0/2 | 13 个 `mutableStateOf` + 8 个 `LaunchedEffect` |
| 单一数据流 | 0/2 | 拖拽手势直接操作 UI 状态，文件卡状态散落各处 |
| 面向管道 | 0/2 | 拖拽逻辑 200+ 行内嵌在 `@Composable` 中 |
| 职责边界清晰 | 2/2 | 5 职责但紧密耦合：UI + 拖拽 + 文件夹管理 + 文件卡 + 动画 |

**拆分方案**: 拆 `HomeToolBar`, `HomeFolderNav`, `HomeFileGrid`, `HomeDragHost`, `HomeQuickActions`

**拆分优先级**: ⭐⭐⭐⭐⭐

---

### 🔴 2. PracticeScreen.kt (1079 行) — 3/10

| 特质 | 得分 | 详情 |
|------|------|------|
| 短小 | 0/2 | 1079 行 |
| 无状态 | 0/2 | 25 个 `mutableStateOf`（上轮已提取 4 个 Composable，仍有 residual） |
| 单一数据流 | 1/2 | 答题提交、分析触发散落在 Composable 中 |
| 面向管道 | 1/2 | 主 Composable 600+ 行，仅 5 个非 Composable 函数 |
| 职责边界清晰 | 1/2 | 6 职责：UI + 导航 + 剪贴板 + 音效 + 分析触发 + 字数统计 |

**拆分方案**: 进一步拆 `PracticeTopBar`, `PracticeProgressBar`, `PracticeAnswerArea`, `PracticeAnalysisPanel`

**拆分优先级**: ⭐⭐⭐⭐⭐

---

### 🟠 3. PracticeNavigationCoordinator.kt (618 行) — 3/10

| 特质 | 得分 | 详情 |
|------|------|------|
| 短小 | 0/2 | 618 行 |
| 无状态 | 1/2 | 3 个 `MutableState`，17 个 `private var` Lambda 注入（God Coordinator 反模式） |
| 单一数据流 | 1/2 | 导航状态读写混合 |
| 面向管道 | 0/2 | 25 个函数，大量 Lambda 持有状态 |
| 职责边界清晰 | 1/2 | 2 职责：前进/后退 + 答案历史回溯 |

**拆分方案**: 精简为 `PracticeNavigationState` + `PracticeAnswerHistoryTracker` 两个类

**拆分优先级**: ⭐⭐⭐⭐

---

### 🟠 4. PracticeSessionCoordinator.kt (642 行) — 4/10

| 特质 | 得分 | 详情 |
|------|------|------|
| 短小 | 0/2 | 642 行 |
| 无状态 | 1/2 | 3 个 StateFlow 引用（注入），0 声明 |
| 单一数据流 | 1/2 | 读写混合 |
| 面向管道 | 1/2 | 12 个函数，平均 53 行 |
| 职责边界清晰 | 1/2 | 3 职责：进度持久化 + 题库加载 + 收藏/错题加载 |

**拆分方案**: 拆 `PracticeProgressPersistenceCoordinator` + `PracticeRepositoryLoader`

**拆分优先级**: ⭐⭐⭐⭐

---

### 🟡 5. PracticeViewModel.kt (759 行) — 5/10

| 特质 | 得分 | 详情 |
|------|------|------|
| 短小 | 0/2 | 759 行 |
| 无状态 | 1/2 | 4 个 `MutableStateFlow`，16 个 `private var` 配置标志 |
| 单一数据流 | 1/2 | 111 个薄委托函数，Fill 变换逻辑仍留在 VM |
| 面向管道 | 2/2 | 几乎所有方法 ≤5 行 |
| 职责边界清晰 | 1/2 | Fill 变换逻辑（`applyConfiguredFillQuestions` 等）应独立 |

**拆分方案**: 提取 `PracticeFillQuestionCoordinator`（约 150 行 Fill 变换逻辑）

**拆分优先级**: ⭐⭐⭐

---

### 🟡 6. ExportCoordinator.kt (550 行) — 5/10

| 特质 | 得分 | 详情 |
|------|------|------|
| 短小 | 0/2 | 550 行 |
| 无状态 | 2/2 | 0 `mutableStateOf`，纯管道 |
| 单一数据流 | 2/2 | 数据 → 格式化 → 写出 |
| 面向管道 | 1/2 | 16 个函数，JSON/Excel 混合 |
| 职责边界清晰 | 0/2 | 2 职责强耦合：JSON vs Excel |

**拆分方案**: 拆 `JsonExportCoordinator` + `ExcelExportCoordinator`

**拆分优先级**: ⭐⭐⭐

---

### 🟡 7. RichText.kt (892 行) — 5/10

| 特质 | 得分 | 详情 |
|------|------|------|
| 短小 | 0/2 | 892 行 |
| 无状态 | 1/2 | 3 个局部 `mutableStateOf` |
| 单一数据流 | 2/2 | 管道清晰：文本 → 解析 → 渲染 |
| 面向管道 | 1/2 | 36 个小函数，部分解析器 30+ 行 |
| 职责边界清晰 | 1/2 | 4 职责：文本解析 + LaTeX + SVG + 图片 |

**拆分方案**: 拆 `RichTextParser`, `LatexRenderer`, `SvgRenderer`, `ImageRenderer`

**拆分优先级**: ⭐⭐⭐

---

### 🟡 8. HomeViewModel.kt (381 行) — 6/10

| 特质 | 得分 | 详情 |
|------|------|------|
| 短小 | 0/2 | 381 行 |
| 无状态 | 1/2 | 7 个 `MutableStateFlow` |
| 单一数据流 | 2/2 | 状态管道清晰 |
| 面向管道 | 2/2 | 10 个函数，平均 38 行 |
| 职责边界清晰 | 1/2 | 3 职责：文件管理 + 进度追踪 + 缓存刷新 |

**拆分方案**: `HomeFileCoordinator` + `HomeProgressCoordinator`

**拆分优先级**: ⭐⭐⭐

---

### 🟡 9. InlineBlankQuestionContent.kt (609 行) — 5/10

| 特质 | 得分 | 详情 |
|------|------|------|
| 短小 | 0/2 | 609 行 |
| 无状态 | 1/2 | 4 个局部 `mutableStateOf` |
| 单一数据流 | 2/2 | 输入 → 分词 → 渲染 |
| 面向管道 | 1/2 | 27 个函数，分词逻辑 30+ 行 |
| 职责边界清晰 | 1/2 | 2 职责：分词 + UI |

**拆分方案**: 提取分词逻辑到独立 pipeline 类

**拆分优先级**: ⭐⭐⭐

---

### 🟡 10. QuestionBankDrawer.kt (589 行) — 7/10

| 特质 | 得分 | 详情 |
|------|------|------|
| 短小 | 0/2 | 589 行 |
| 无状态 | 2/2 | 0 `mutableStateOf`（纯 UI） |
| 单一数据流 | 2/2 | 纯渲染，无副作用 |
| 面向管道 | 2/2 | 11 个函数 |
| 职责边界清晰 | 1/2 | 1 职责但嵌套层级深 |

**拆分方案**: 低优先级，拆嵌套 Composable 即可

**拆分优先级**: ⭐⭐

---

### 🟢 11. FontSettingsDataStore.kt (556 行) — 6/10

| 特质 | 得分 | 详情 |
|------|------|------|
| 短小 | 0/2 | 556 行 |
| 无状态 | 1/2 | 1 个 `private var`（静态 context） |
| 单一数据流 | 2/2 | 纯 DataStore 读写 |
| 面向管道 | 1/2 | 80 个函数，每条 ≤10 行但全重复样板 |
| 职责边界清晰 | 2/2 | 1 职责 |

**拆分方案**: 泛型代理替换 80 个 getter/setter → ~100 行

**拆分优先级**: ⭐⭐

---

### 🟢 12. AppNavHost.kt (741 行) — 7/10

| 特质 | 得分 | 详情 |
|------|------|------|
| 短小 | 0/2 | 741 行 |
| 无状态 | 2/2 | 2 个 `mutableStateOf` |
| 单一数据流 | 2/2 | 纯路由 → Composable 声明 |
| 面向管道 | 2/2 | 2 个函数 |
| 职责边界清晰 | 1/2 | 1 职责但过于冗长 |

**拆分方案**: 低回报，可用路由表批量注册

**拆分优先级**: ⭐

---

## 综合执行顺序（推荐）

```
Phase H:  HomeScreen.kt                      (1068→~380, 9 个新组件文件)        ✅
Phase I:  PracticeScreen.kt                  (1079→~480, 3 个新组件文件)        ✅
Phase J:  PracticeNavigationCoordinator.kt   (618→—, 取消)                     ✖ 深度重构风险高
Phase K:  PracticeSessionCoordinator.kt      (642→—, 取消)                     ✖ 深度重构风险高
Phase L:  ExportCoordinator.kt               (550→—, 未找到文件)                ✖
Phase M:  PracticeViewModel.kt               (759→724, 委托 ExamFillTransform)  ✅
Phase N:  RichText.kt                        (892→~430, RichTextParser.kt)      ✅
Phase O:  HomeViewModel.kt                   (381→260, HomeProgressRedoHandler) ✅
Phase 9:  InlineBlankQuestionContent.kt      (609→202, InlineBlankTokenizer)    ✅
Phase 10: QuestionBankDrawer.kt              (589→300, QBDrawerRows)            ✅
Phase 11: FontSettingsDataStore.kt           (556→—, 取消)                     ✖ 50+ 调用点迁移成本高
Phase 12: AppNavHost.kt                      (741→294, AppNavRoutes)            ✅
```

---

## 本轮已完成回顾

| Phase | 文件 | 结果 |
|-------|------|------|
| H | HomeScreen.kt | 1068 → ~380 ✅ (9 新文件: HomeTopBar, HomeBottomBar, HomeDragHandler, HomeFileListContainer, HomeDraggingFileOverlay, HomeDialogs, HomeFolderPanel, HomeDragDock, HomeHelpers) |
| I | PracticeScreen.kt | 1079 → ~480 ✅ (3 新文件: PracticeProgressIndicator, PracticeBottomToolbar, PracticeDialogsHost) |
| M | PracticeViewModel.kt | 759 → 724 ✅ (Fill 逻辑委托 ExamFillTransform) |
| N | RichText.kt | 892 → ~430 ✅ (RichTextParser.kt) |
| O | HomeViewModel.kt | 381 → 260 ✅ (HomeProgressRedoHandler.kt) |
| 9 | InlineBlankQuestionContent.kt | 609 → 202 ✅ (InlineBlankTokenizer.kt) |
| 10 | QuestionBankDrawer.kt | 589 → 300 ✅ (QBQDrawerRows.kt) |
| 12 | AppNavHost.kt | 741 → 294 ✅ (AppNavRoutes.kt) |

## 上轮已完成回顾

| Phase | 文件 | 结果 |
|-------|------|------|
| A | QuestionRepositoryImpl.kt | 1619 → 306 ✅ |
| B | SettingsViewModel.kt | 1178 → 416 ✅ |
| C | PracticeScreen.kt | 1278 → 1078 ✅ |
| D | SettingsScreen.kt | 1359 → 487 ✅ |
| E | ExamViewModel.kt | 739 → 373 ✅ |
| G | PracticeViewModel.kt | 2233 → 682 ✅ |
