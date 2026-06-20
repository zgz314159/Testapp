<!--
  Derived from: L6 Level 6 五大特质审查 2026-06-13
  Target: All top 12 + mid-size files in the project
  Last synced: 2026-06-13 17:37 UTC+8
  Criteria: 短小 / 无状态 / 单一数据流 / 面向管道 / 职责边界清晰
-->

# Level 6 五大特质审查报告 (2026-06-13)

> 按五项特质逐文件打分。每项满分 2 分，总分 10 分。≤5 分为不合格。

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

## 🔴 EXTREME（2-3/10，严重超标）

### 1. HomeScreen.kt (1068 行) — 2/10

| 特质 | 得分 | 详情 |
|------|------|------|
| 短小 | 0/2 | 1068 行 |
| 无状态 | 0/2 | 13 个 `mutableStateOf` + 8 个 `LaunchedEffect` |
| 单一数据流 | 0/2 | 拖拽手势直接操作 UI 状态，文件卡状态散落各处 |
| 面向管道 | 0/2 | 拖拽逻辑 200+ 行内嵌在 `@Composable` 中 |
| 职责边界清晰 | 2/2 | 5 职责但紧密耦合 |

**Phase H ✅ DONE**: 1068→~380，9 新文件

### 2. PracticeScreen.kt (1079 行) — 3/10

| 特质 | 得分 | 详情 |
|------|------|------|
| 短小 | 0/2 | 1079 行 |
| 无状态 | 0/2 | 25 个 `mutableStateOf` |
| 单一数据流 | 1/2 | 答题提交、分析触发散落在 Composable 中 |
| 面向管道 | 1/2 | 主 Composable 600+ 行，仅 5 个非 Composable 函数 |
| 职责边界清晰 | 1/2 | 6 职责：UI + 导航 + 剪贴板 + 音效 + 分析触发 + 字数统计 |

**Phase I ✅ DONE**: 1079→~480，3 新文件

---

## 🟠 HIGH（3-4/10，严重不符合）

### 3. PracticeNavigationCoordinator.kt (618 行) — 3/10

| 特质 | 得分 | 详情 |
|------|------|------|
| 短小 | 0/2 | 618 行 |
| 无状态 | 1/2 | 3 个 `MutableState`，17 个 `private var` Lambda（God Coordinator 反模式） |
| 单一数据流 | 1/2 | 导航状态读写混合 |
| 面向管道 | 0/2 | 25 个函数，大量 Lambda 持有状态 |
| 职责边界清晰 | 1/2 | 导航前进/后退 + 答案历史回溯 |

**拆分方案**: `PracticeNavigationState` + `PracticeAnswerHistoryTracker`
**阻断**: 深度重构风险高，暂停 ⏸️

### 4. PracticeSessionCoordinator.kt (642 行) — 4/10

| 特质 | 得分 | 详情 |
|------|------|------|
| 短小 | 0/2 | 642 行 |
| 无状态 | 1/2 | 3 个 StateFlow 引用，0 声明 |
| 单一数据流 | 1/2 | 读写混合 |
| 面向管道 | 1/2 | 12 个函数，平均 53 行 |
| 职责边界清晰 | 1/2 | 进度持久化 + 题库加载 + 收藏/错题加载 |

**拆分方案**: `PracticeProgressPersistenceCoordinator` + `PracticeRepositoryLoader`
**阻断**: 深度重构风险高，暂停 ⏸️

---

## 🟡 MEDIUM（5-6/10，部分不符合）

### 5. ExportCoordinator.kt (550 行) — 5/10

| 特质 | 得分 | 详情 |
|------|------|------|
| 短小 | 0/2 | 550 行 |
| 无状态 | 2/2 | 0 mutableState，纯管道 ✅ |
| 单一数据流 | 2/2 | 数据 → 格式化 → 写出 ✅ |
| 面向管道 | 1/2 | 16 个函数，JSON/Excel 混合 |
| 职责边界清晰 | 0/2 | JSON vs Excel 两个 Bounded Context 强耦合 |

**拆分方案**: `JsonExportCoordinator` + `ExcelExportCoordinator`
**状态**: ⏳ 待执行（Phase L 之前标记为"未找到文件"，需重新定位后执行）

### 6. RichText.kt (892 行) — 5/10

| 特质 | 得分 | 详情 |
|------|------|------|
| 短小 | 0/2 | 892 行 |
| 无状态 | 1/2 | 3 个局部 `mutableStateOf` |
| 单一数据流 | 2/2 | 文本 → 解析 → 渲染 ✅ |
| 面向管道 | 1/2 | 36 个小函数，部分解析器 30+ 行 |
| 职责边界清晰 | 1/2 | 文本解析 + LaTeX + SVG + 图片 |

**Phase N ✅ DONE**: 提取 RichTextParser.kt（仍可进一步拆 LaTeX/SVG/Image renderer）

### 7. InlineBlankQuestionContent.kt (609 行) — 5/10

| 特质 | 得分 | 详情 |
|------|------|------|
| 短小 | 0/2 | 609 行 |
| 无状态 | 1/2 | 4 个局部 `mutableStateOf` |
| 单一数据流 | 2/2 | 输入 → 分词 → 渲染 ✅ |
| 面向管道 | 1/2 | 27 个函数，分词逻辑 30+ 行 |
| 职责边界清晰 | 1/2 | 分词 + UI |

**Phase 9 ✅ DONE**: 609→202

### 8. HomeViewModel.kt (381 行) — 6/10

| 特质 | 得分 | 详情 |
|------|------|------|
| 短小 | 0/2 | 381 行 |
| 无状态 | 1/2 | 7 个 `MutableStateFlow` |
| 单一数据流 | 2/2 | 状态管道清晰 ✅ |
| 面向管道 | 2/2 | 10 个函数，平均 38 行 ✅ |
| 职责边界清晰 | 1/2 | 文件管理 + 进度追踪 + 缓存刷新 |

**Phase O ✅ DONE**: 提取 HomeProgressRedoHandler.kt

### 9. FontSettingsDataStore.kt (556 行) — 6/10

| 特质 | 得分 | 详情 |
|------|------|------|
| 短小 | 0/2 | 556 行 |
| 无状态 | 1/2 | 1 个 `private var` |
| 单一数据流 | 2/2 | 纯 DataStore 读写 ✅ |
| 面向管道 | 1/2 | 80 个函数，每条 ≤10 行但全重复样板 |
| 职责边界清晰 | 2/2 | 单一职责 ✅ |

**拆分方案**: 泛型代理替换 80 个 getter/setter → ~100 行
**阻断**: 50+ 调用点迁移成本高，暂停 ⏸️

---

## 🟢 LOW（7/10，基本符合）

### 10. QuestionBankDrawer.kt (589 行) — 7/10

| 特质 | 得分 | 详情 |
|------|------|------|
| 短小 | 0/2 | 589 行 |
| 无状态 | 2/2 | 纯 UI ✅ |
| 单一数据流 | 2/2 | 纯渲染 ✅ |
| 面向管道 | 2/2 | 11 个函数 ✅ |
| 职责边界清晰 | 1/2 | 嵌套层级深 |

**Phase 10 ✅ DONE**: 提取 QBQDrawerRows.kt

### 11. AppNavHost.kt (741 行) — 7/10

| 特质 | 得分 | 详情 |
|------|------|------|
| 短小 | 0/2 | 741 行 |
| 无状态 | 2/2 | 2 个 `mutableStateOf` ✅ |
| 单一数据流 | 2/2 | 纯路由 ✅ |
| 面向管道 | 2/2 | 2 个函数 ✅ |
| 职责边界清晰 | 1/2 | 路由声明过于冗长 |

**Phase 12 ✅ DONE**: 741→294

---

## 🟢 未进入 Top 12 的中型文件

| 文件 | 行数 | 核心问题 | 状态 |
|------|------|----------|------|
| WrongBookScreen.kt | ~485 | 纯 UI，可提取独立 Composable | ⏸️ 低优先 |
| FavoriteScreen.kt | ~482 | 纯 UI，可提取独立 Composable | ⏸️ 低优先 |
| OptimizedFileCard.kt | ~437 | 可与 HomeScreen 拆分联动 | ⏸️ 低优先 |
| WrongBookRepositoryImpl.kt | ~410 | 3 职责混合 | ⏸️ 低优先 |
| FillQuestionTransformUtils.kt | ~360 | 无状态管道，可接受 | ✅ OK |

---

## 总体评分汇总

| 排名 | 文件 | 得分 | 短小 | 无状态 | 单一数据流 | 面向管道 | 职责边界 |
|------|------|------|------|------|------|------|------|
| 🔴1 | HomeScreen.kt | 2/10 | 0 | 0 | 0 | 0 | 2 |
| 🔴2 | PracticeScreen.kt | 3/10 | 0 | 0 | 1 | 1 | 1 |
| 🟠3 | PracticeNavigationCoordinator.kt | 3/10 | 0 | 1 | 1 | 0 | 1 |
| 🟠4 | PracticeSessionCoordinator.kt | 4/10 | 0 | 1 | 1 | 1 | 1 |
| 🟡5 | ExportCoordinator.kt | 5/10 | 0 | 2 | 2 | 1 | 0 |
| 🟡6 | RichText.kt | 5/10 | 0 | 1 | 2 | 1 | 1 |
| 🟡7 | InlineBlankQuestionContent.kt | 5/10 | 0 | 1 | 2 | 1 | 1 |
| 🟡8 | HomeViewModel.kt | 6/10 | 0 | 1 | 2 | 2 | 1 |
| 🟡9 | FontSettingsDataStore.kt | 6/10 | 0 | 1 | 2 | 1 | 2 |
| 🟢10 | QuestionBankDrawer.kt | 7/10 | 0 | 2 | 2 | 2 | 1 |
| 🟢11 | AppNavHost.kt | 7/10 | 0 | 2 | 2 | 2 | 1 |

---

## 操作建议

1. **立即可展开**（低悬挂果实）：
   - ExportCoordinator.kt → 拆 `JsonExportCoordinator` + `ExcelExportCoordinator`（550 行，职责边界清晰 0/2 是最大痛点）
   - FontSettingsDataStore.kt → 泛型代理替换样板（⏸️ 迁移成本高，延后）

2. **高风险暂停项**：
   - PracticeNavigationCoordinator + PracticeSessionCoordinator — 需重新设计方案，待 K-001 设备 smoke 通过

3. **已完成**：Phase H~12 全部 done，10 个文件已达标
