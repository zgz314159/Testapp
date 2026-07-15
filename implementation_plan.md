# Implementation Plan — 首页视觉恢复与比例校准

## [Overview]

校准首页 Hero、统计条、题库卡的比例与视觉，修复"全部 >"不可见、清理显示名数据链未覆盖 Column/Grid、卡片彩色外描边未彻底清除、文件图标映射未双路径验证等问题。

**根因分析：**

| # | 问题 | 根因 |
|---|------|------|
| 1 | Hero 宽高比偏差（1.50:1 vs 2.14:1） | `HomeContinueStudyCard` 固定 `CARD_HEIGHT = 248.dp`，无响应式宽高比 |
| 2 | 统计条占比过高（38% vs 27%） | `STATS_AREA_HEIGHT = 88.dp` 为固定值，未按比例 |
| 3 | "全部 >"不可见 | `HomeScreen.kt:228` 调用 `HomeSectionHeader(title, modifier)` **未传递 `onShowAll`**，默认 null 不渲染 |
| 4 | 显示名清理未达 Column/Grid | `HomeFileListColumn.kt:100` 和 `HomeFileListGrid.kt:63` 各自独立用 `removeSuffix` 生成 `displayName`，未经过 `cleanupDisplayName()` |
| 5 | 卡片彩色外描边未彻底清除 | `HomeFileListColumn.kt:113` 对非选中状态未传入 `cardBorderOverride`，但 `OptimizedFileCard` line 113 已修复为 `visualContent` 时用 `surfaceVariant`。需额外修复 `DraggingFileCard` 和 default `HomeFileListGrid` 渲染路径 |
| 6 | 卡片高度过密 | `HomeQuestionBankCard` 高度固定 `132.dp` / `120.dp`（compact），需压缩 |
| 7 | 文件类型映射单路径验证 | `HomeFileTypeVisualPipeline.resolve()` 在 `HomeQuestionBankCard` 中调用，`HomeQuestionBankCard` 在 Column+Grid 均使用——已验证双路径。但 `OptimizedFileCard` default 内容还用旧 `palette`，需确认视觉使用视觉内容 |
| 8 | 底部导航过大边距 | `HomeBottomBar` 外部 `padding(horizontal = 12.dp)` + `Card` 外层 |

## [Types]

无新类型。`HomeDashboardUiState` 中的 `QuestionBankItem.displayName` 已由 `cleanupDisplayName()` 生成，无需改动 data class。

## [Files]

精确变更清单：

### 新文件
无。

### 修改文件

**Batch A — 比例、Hero、Header、"全部 >"、显示名数据链**

1. **`HomeContinueStudyCard.kt`** — 关键比例修复
   - 删除固定 `CARD_HEIGHT = 248.dp`
   - 删除固定 `STATS_AREA_HEIGHT = 88.dp`
   - 外层 `Card` modifier 改 `height(CARD_HEIGHT)` → `aspectRatio(2.1f)`（响应式）
   - 正文 Column `padding(bottom = STATS_AREA_HEIGHT)` → `padding(bottom = CARD_HEIGHT * 0.27f)`（用 fraction）
   - 统计条定位改为 `fillMaxWidth(0.92f)` 而不是全宽边到边
   - 图片区域 `fillMaxWidth(0.62f)` 保持不变，但不再 `padding(top/bottom)`
   - 标题 fontSize 25sp 不变

2. **`HomeScreen.kt`** — "全部 >"接线
   - line 228-231：给 `HomeSectionHeader` 增加 `onShowAll` 回调
   - 回调绑定到已有导航入口（查看全部题库跳转）

3. **`HomeFileListColumn.kt`** — 显示名清理补全
   - line 100：`fileName.removeSuffix(".txt").removeSuffix(".json")` → `HomeDashboardPipeline.cleanupDisplayName(fileName)`

4. **`HomeFileListGrid.kt`** — 同上
   - line 63：`fileName.removeSuffix(".txt").removeSuffix(".json")` → `HomeDashboardPipeline.cleanupDisplayName(fileName)`

**Batch B — 题库卡、文件类型视觉、底栏**

5. **`HomeQuestionBankCard.kt`** — 高度压缩
   - 当前 `cardHeight = if (isCompact) 120.dp else 132.dp`
   - 改为 `if (isCompact) 96.dp else 108.dp`（目标 5:1 比例）
   - 图标大小 56dp/64dp → 48dp/56dp
   - 标题 fontSize 15sp → 14sp
   - 进度 fontSize 12sp → 11sp
   - 元数据 fontSize 11sp → 10sp
   - CTA 高度不变但 `contentPadding` 缩减

6. **`OptimizedFileCard.kt`** — 彩色描边彻底清除
   - line 113：已改 `visualContent != null` → `surfaceVariant`，确认逻辑不变
   - `DraggingFileCard` line 188：将 `palette.borderColor` → `MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)`
   - 默认非 visualContent 路径的 `palette.borderColor` 不变（`showTypeSummary=true` 时才用，但首页已全量传递 `visualContent`）

7. **`HomeBottomBar.kt`** — 全宽底栏
   - 外层 Card 的 `padding(horizontal = 12.dp)` → `padding(horizontal = 8.dp)`
   - 内部 `NavigationBar` 添加 `Modifier.fillMaxWidth()`

8. **`HomeFileTypeVisualPipeline.kt`** — 确认映射无误
   - 无需改动。已在 `HomeQuestionBankCard` 中通过 `remember(fileName) { HomeFileTypeVisualPipeline.resolve(fileName) }` 中央化调用
   - Column 和 Grid 均通过 `visualContent = { HomeQuestionBankCard(...) }` 使用同一映射

## [Functions]

| 函数 | 文件 | 变更 |
|------|------|------|
| `HomeContinueStudyCard` (Composable) | `HomeContinueStudyCard.kt:47` | 删除固定 height 常量，改为 `aspectRatio(2.1f)`，统计条比例改为 27% |
| `cleanupDisplayName` | `HomeDashboardPipeline.kt:140` | **已有**，无变更 |
| `HomeFileListColumn` 内匿名 Composable | `HomeFileListColumn.kt:100` | 显示名来源从 `removeSuffix` 改为 `cleanupDisplayName(fileName)` |
| `HomeFileListGrid` 内匿名 Composable | `HomeFileListGrid.kt:63` | 同上 |
| `HomeSectionHeader` 调用 | `HomeScreen.kt:228` | 增加 `onShowAll` 参数 |
| `HomeQuestionBankCard` (Composable) | `HomeQuestionBankCard.kt:37` | 高度、图标、字号缩减 |
| `DraggingFileCard` (Composable) | `OptimizedFileCard.kt:178` | border 颜色从 `palette.borderColor` 改为 `surfaceVariant` |
| `HomeBottomBar` (Composable) | `HomeBottomBar.kt` | 外部 padding 缩减 |

**删除的 modifier：**
- `HomeContinueStudyCard.kt`：删除 `height(CARD_HEIGHT)`、`STATS_AREA_HEIGHT` 常量
- `HomeContinueStudyCard.kt`：删除给图片区域的 `padding(top = 8.dp, bottom = 56.dp)`
- `OptimizedFileCard.kt`：`DraggingFileCard` 中删除 `palette.borderColor`

## [Classes]

无 class 变更。所有修改限于 Composable function 参数和内部布局常数。

## [Dependencies]

无新依赖。Compose BOM 2024.03.00 已有 `aspectRatio` 修饰符（`Modifier.aspectRatio()` 自 Compose Foundation 1.2）。

## [Testing]

| 测试 | 文件 | 变更 |
|------|------|------|
| `HomeDashboardPipelineTest` | `feature-practice/.../test/...` | 新增 `cleanupDisplayName` 单测（去_ext、_final、日期后缀）|
| 现有 15 tests | 同上 | 不变，全部通过 |

新增测试函数：
- `cleanupDisplayName_removes_txt_ext()`
- `cleanupDisplayName_removes_final_suffix()`
- `cleanupDisplayName_removes_date_suffix()`

## [Implementation Order]

批次 A（先进行，影响数据链和比例）：
1. `HomeContinueStudyCard.kt` — 固定 height → `aspectRatio(2.1f)`，统计条 27%
2. `HomeScreen.kt` — "全部 >"回调接线
3. `HomeFileListColumn.kt` — 显示名 `cleanupDisplayName`
4. `HomeFileListGrid.kt` — 显示名 `cleanupDisplayName`
5. 编译 + 单测验证

批次 B（视觉微调，依赖批次 A 固定）：
6. `HomeQuestionBankCard.kt` — 高度压缩 + 字号缩减
7. `OptimizedFileCard.kt` — `DraggingFileCard` 描边修复
8. `HomeBottomBar.kt` — 边距缩减
9. 编译 + 单测 + assembleDebug

验证清单：
- `./gradlew :feature-practice:testDebugUnitTest`
- `./gradlew :feature-practice:compileDebugKotlin` (批次 A 后)
- `./gradlew :app:assembleDebug` (全部完成后)
- `scripts/check-loc-over-500.ps1`