# Change Log

## 2026-07-15 — Excel 题库版式兼容扩展

### 目标
不新增扩展名；兼容桌面「题库」新增 Excel 版式（序号 15 列 / 精简 12 列）与答案别名。

### 根因
1. `答案A`–`答案G` / `答案解析` 被误收为填空多答案槽，挤掉「正确答案」
2. `sheet.drop(headerRowIndex+1)` 按迭代条数跳行，表头前缺行时误删首题

### 修复
| 项 | 变更 |
|----|------|
| `ExcelParserCellPipeline` | `答案A–G`→选项；`答案解析`→解析；忽略序号/难易度/标签等 |
| `ExcelImportAnswerNormalizePipeline` | 多选分隔归一、判断别名、注意事项跳过、字母越界跳过 |
| `ExcelParserRowPipeline` | 优先单列正确答案；异常行跳过 |
| `ExcelQuestionParser` | 按 `rowNum` 过滤表头后行 |
| `:data` 单测 | 14/15/12 列夹具 + 桌面 8 样本下限断言 |
| `poi-ooxml-full` | `:data` 补齐 OOXML schemas（与 settings 一致） |

### 策略
缺答案 / 答案含超出选项列字母 → 跳过该行；其余有效题照常导入。

## 2026-07-15 — 追问保存根因：AnalysisRepository 非 Singleton

### 日志结论（DSAskPersist）
- `VM.save` / `Repo.saveAnalysis` 写出 len=1348（含追问）
- 紧接着 `Repo.getAnalysis source=cache` 仍为 len=396 → **多实例独立 cache**
- `Sync.stored` 用 396 覆盖会话 1348 → 再双击只剩首答

### 修复
| 项 | 变更 |
|----|------|
| `QuestionAnalysisRepositoryImpl` + `@Binds` | `@Singleton` 统一 cache |
| `DeepSeekAskPersistPipeline.resolveLoadText` | 优先结构化/更长（含 ask 表） |
| Practice/Exam session update | `preferStructured`，禁止 Sync 短文降级 |

## 2026-07-14 — AI 追问保存被扁平化覆盖（续修）

### 根因
保存写回会话后，`UpdateAnalysis` 再写库用的是「仅助手拼接」正文，覆盖了 `【DS·问】/【DS·答】` 多轮结构；再次进全屏只剩首答，追问气泡丢失。

### 修复
| 文件 | 变更 |
|------|------|
| `DeepSeekAskSavePipeline` / VM | 写回会话也用结构化 encode |
| `DeepSeekAskLoadSeedPipeline` | 优先结构化 / 更长文本 |
| Practice/Exam `updateAnalysis` | `resolvePreferStructured`，禁止扁平面覆盖 |
| `SessionAnalysisSyncPipeline` | 会话写原文，不预扁平化 |
| Practice/Exam Screen | 展示经 `toDisplayText` |
| `DeepSeekAskScreen` | seed 晚到只 merge，不再 reset 冲掉追问 |

## 2026-07-14 — AI 问答：保存确认手势 / 可选编辑 / 追问保存

### 目标
1. 保存确认窗：点窗外无动作；返回/侧滑只关窗，不等于「取消并退出」
2. 对话正文长按可选复制，助手气泡可编辑
3. 全屏追问保存：合并会话 seed + 结构化落库

### 变更
| 文件 | 变更 |
|------|------|
| `AiAskSaveConfirmDialog` | `dismissOnClickOutside=false`；回退仅 `onCloseDialogOnly` |
| `DeepSeek/Spark/BaiduAskScreen` | BackHandler 分层；DeepSeek 可编辑气泡 |
| `AiChatBubble/List/Layout` | SelectionContainer + BasicTextField |
| `DeepSeekAskViewModel/Save/LoadSeed` | seed 合并；encode 落库 |
| `AppNavAiRoutes` / `seedDeepSeekAnalysisFrom` | 从会话注入 seed |

## 2026-07-14 — DeepSeek 标答校对 / thinking / 搜索骨架

### 目标
修复「用户给出正确答案是对」仍被当成空口质疑 → 回复「未收到新依据，结论不变」；标答轮次可改口；可选 thinking + search_web 骨架。

### 变更
| 文件 | 变更 |
|------|------|
| `DeepSeekExamPromptPipeline` | 优先级含【题库标答】；challenge vs 标答校对分流；锚点展示【题库标答】 |
| `DeepSeekChatHistoryPipeline` | `isAnswerKeyFeedbackMessage`；challenge 排除标答 |
| `DeepSeekAskFollowUpPipeline` | `FollowUpKind` + `enableThinking` |
| `DeepSeekAskViewModel` | `resolve()` → `chat(enableThinking, attachWebSearchTool)` |
| `DeepSeekApiService` / `DeepSeekChatConfig` | thinking 开关；tools 声明 |
| `DeepSeekWebSearchToolSkeleton` | search_web 定义 + stub 回填 |
| `DeepSeekAskFollowUpPipelineTest` | 标答 vs challenge 用例 |

## 2026-07-14 — 主页左侧抽屉视觉同调

### 目标
题库浏览抽屉与主页 / 设置卡面风格融合：`HomeDesignTokens` 背景、白底圆角行、主色强调、圆形关闭钮。

| 文件 | 变更 |
|------|------|
| `QuestionBankDrawerHeader.kt` | Section 标题 + 圆形关闭 |
| `QuestionBankDrawerRows.kt` | 白卡片树行 / 浅底题目行 / 白搜索条 |
| `QuestionBankDrawer.kt` | 间距与分割线令牌化 |
| `QuestionBankSearchText.kt` | 高亮色改用 HomeDesignTokens |

## 2026-07-14 — 答题卡历史标记 + 首页接着/重答

### 设计
- **接着进度**：恢复的作答写入 session → 答题卡显示 SELECTED/CORRECT/WRONG
- **新题库 / 重答清进度后**：无作答数据 → 答题卡全 UNANSWERED
- Sheet：有进度时「接着练习/接着考试」+「重答」；无进度时「开始练习/开始考试」，不显示重答

### 修复
| 文件 | 变更 |
|------|------|
| `AnswerCardStateBuilder.kt` | 计入 textAnswer；填空/简答正确性 |
| `HomeStartQuizSheet.kt` | hasProgress 文案与重答按钮 |
| `HomeViewModel.kt` | `clearProgressForFile`（练习+考试） |
| `HomeActionOverlays` / `HomeScreen` | 接线；有进度 CTA/Hero 开 sheet |

## 2026-07-14 — 首页冷启动 / 滑动掉帧优化

### 根因（摘要）
- 滚动时 `enableDragDrop` 随 `isScrollInProgress` 翻转 → 可见卡片重建 `pointerInput`
- 双 Card 阴影 + 每行 `BoxWithConstraints` Subcompose
- 首帧同步解码 Hero WebP + Header 入场动画抢主线程

### 修复
| 文件 | 变更 |
|------|------|
| `OptimizedFileCard.kt` | `allowDragStart`；`pointerInput(fileName)` 稳定；visualContent 无边框/0 elevation |
| `HomeFileListColumn.kt` | 滚动门控移入 allowDragStart；外层 elevation 0 |
| `HomeQuestionBankCard.kt` | 去 BoxWithConstraints，用 screenWidthDp |
| `HomeGreetingHeader.kt` | 去掉 AnimatedVisibility |
| `HomeContinueStudyCard.kt` | 首帧后再解码列车图 |
| `HeroTrainIllustration.kt` | `drawWithCache` 缓存遮罩 |
| `HomeScreen.kt` | dashboard `remember(keys)`；去掉无用 scrollBehavior |

## 2026-07-14 — 首页 CTA 直达练习 + 底栏恢复错题

### 修复
1. 题库卡「开始练习 / 继续学习」与 Hero「继续学习」→ 直接 `onStartQuiz`（续答进度）；练习/考试二选一仍可在卡片再点一次弹出 sheet
2. 底栏恢复「错题」：题库 / 错题 / 收藏 / 记录 / 我的；接通已有 `onWrongBook`

| 文件 | 变更 |
|------|------|
| `HomeBottomBar.kt` | 插入错题 tab |
| `strings.xml` | `home_bottom_wrongbook` |
| `HomeScreen.kt` | CTA / Hero 直达练习 |
| `HomeScreenState.kt` | nav index coerceIn(0,4) |

## 2026-07-14 — 修复首页练习/考试「暂无题目」与 CTA 无响应

### 根因
- Home Redesign 后底栏语义改为 题库/收藏/记录/我的，但 `HomeStartQuizSheet` 仍用 `bottomNavIndex` 路由到错题本/收藏练习，默认 tab=0 → `practice_wrongbook` → 空列表 →「暂无题目」
- `HomeQuestionBankCard` CTA 的 `onFileCtaClick` 未接线

### 修复
| 文件 | 变更 |
|------|------|
| `HomeStartQuizSheet.kt` | 固定走 `onStartQuiz` / `onStartExam` |
| `HomeActionOverlays.kt` | 去掉 sheet 错题本/收藏分支 |
| `HomeScreen.kt` | 接通 `onFileCtaClick` 打开 sheet |
| `HomeScreenLibrarySection.kt` | 去掉 `bottomNavIndex==2→查看成绩` 误路由 |
| `HomeRoute.kt` / `AppNavHost.kt` | 清理 Home 侧无用回调 |

Owner: `:feature-practice` / `:app` 薄壳。ADR: N/A。

## 2026-07-13 — PowerAI 首页视觉重构（Phase Home Redesign）

### 范围
- 首页全新视觉设计，保留所有既有功能
- Owner: `:feature-practice`（`home/` 包）

### 新增文件
| 文件 | LOC | 职责 |
|------|-----|------|
| `design/HomeDesignTokens.kt` | ~80 | 首页设计令牌（颜色、间距、圆角、字体） |
| `model/HomeDashboardUiState.kt` | ~35 | 首页 UI 数据模型 |
| `HomeDashboardPipeline.kt` | ~115 | 纯函数聚合管道（问候语、统计、排序、响应式列数） |
| `components/HomeGreetingHeader.kt` | ~80 | 问候 Header，入场动画 280ms |
| `components/HomeHeaderAction.kt` | ~65 | 搜索/通知圆形按钮，使用 MaterialTheme 颜色 |
| `components/HomeContinueStudyCard.kt` | ~90 | Hero 继续学习卡片，渐变背景 |
| `components/HomeStatisticsStrip.kt` | ~80 | 四项统计，错题可点击 |
| `components/HomeSectionHeader.kt` | ~45 | Section 标题行 |
| `components/HomeQuestionBankCard.kt` | ~125 | 题库卡片，紧凑布局（<380dp），CTA 单行 |
| `test/HomeDashboardPipelineTest.kt` | ~125 | 11 项纯函数测试 |
| `strings.xml` | +17 | 首页文案条目 |

### 修改文件
| 文件 | 变更 |
|------|------|
| `HomeBottomBar.kt` | 导航项改为：题库/收藏/记录/我的；改用 MaterialTheme 颜色 |
| `HomeScreenShell.kt` | 简化 Scaffold 容器 |
| `HomeScreen.kt` | 集成 DashboardPipeline，生成 headerContent 统一滚动 |
| `HomeScreenState.kt` | 底栏默认索引 0 |
| `HomeFileList.kt` | 新增 onFileCtaClick 参数链 |
| `HomeFileListColumn.kt` | 新增 headerContent + showHeader；真实调用 HomeQuestionBankCard |
| `HomeFileListGrid.kt` | 同上 + 支持 GridItemSpan |
| `HomeFileListContainer.kt` | 新增 onFileCtaClick 参数 |
| `HomeScreenLibrarySection.kt` | BoxWithConstraints 响应式，resolveHomeColumnCount |
| `app/.../Theme.kt` | DarkColorScheme 添加背景/文字色值 |
| `app/.../MainActivity.kt` | 合并系统暗色与设置暗色 |
| `ui-common/OptimizedFileCard.kt` | 新增 visualContent + cardShapeOverride 等可选覆盖参数 |

### 复用
- OptimizedFileCard 手势（拖拽、侧滑删除、bounds 上报）
- SwipeRevealActionBox
- HomeStartQuizSheet、QuestionBankDrawer
- HomeActionOverlays
- DashboardPipeline 纯函数（可测试）

### 验证
| 命令 | 结果 |
|------|------|
| `:feature-practice:testDebugUnitTest` | ✅ 10 tests PASS |
| `ktlintCheck` | ✅ 本模块通过；`:app` navigation、`:feature-exam` 有预存 import 排序问题 |
| `detekt` | ✅ PASS |
| `:app:arch.ArchitectureTest` | ✅ 6 tests PASS |
| `:app:assembleDebug` | ✅ 207 tasks PASS |
| `check-loc-over-500.ps1` | ✅ 无 >500 行文件 |

## 2026-07-14 — 首页视觉恢复与比例校准

### 范围
- 校准首页 Hero 宽高比（2.1:1）、统计条占比（27%）、修复"全部 >"不可见
- 清理显示名数据链未覆盖 Column/Grid、消除卡片彩色外描边
- 压缩题库卡高度、缩减底栏边距
- Owner: `:feature-practice`（`home/` 包）、`:ui-common`（`OptimizedFileCard`）

### 修改文件
| 文件 | 变更 | LOC |
|------|------|-----|
| `HomeContinueStudyCard.kt` | 固定 height → `aspectRatio(2.1f)`；统计条 92% 宽；删除 CARD_HEIGHT/STATS_AREA_HEIGHT 常量 | ~95 |
| `HomeScreen.kt` | line 228-231：给 `HomeSectionHeader` 增加 `onShowAll` 回调到 drawer | ~364 |
| `HomeFileListColumn.kt` | line 100：`removeSuffix` → `HomeDashboardPipeline.cleanupDisplayName(fileName)` | ~131 |
| `HomeFileListGrid.kt` | line 63：同上 | ~98 |
| `HomeQuestionBankCard.kt` | 高度 120/132dp→96/108dp；图标 56/64dp→48/56dp；字号 15sp→14sp/11sp→10sp | ~147 |
| `OptimizedFileCard.kt` | DraggingFileCard border: `palette.borderColor` → `surfaceVariant.copy(alpha=0.5f)` | ~215 |
| `HomeBottomBar.kt` | 外层 Card `padding(horizontal=12dp)` → `8dp` | ~111 |

### 测试
| 文件 | 新增 |
|------|------|
| `HomeDashboardPipelineTest.kt` | +3 单测：`cleanupDisplayName` 去 txt/json 扩展名、去 _final 后缀、去日期后缀 |

### 验证
| 命令 | 结果 |
|------|------|
| `:feature-practice:compileDebugKotlin` | ✅ PASS |
| `:feature-practice:testDebugUnitTest` | ✅ 18 tests PASS |
| `:app:assembleDebug` | ✅ BUILD SUCCESSFUL |
| `check-loc-over-500.ps1` | ✅ 无 >500 行文件 |
