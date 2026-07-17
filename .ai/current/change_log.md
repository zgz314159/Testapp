# Change Log

## 2026-07-17 — 答题页立体感加深（保护眼底色）

在 `8b6c53a` Material 规格上仅提高 elevation：顶栏 9dp、主卡 8dp、选项 7→2dp、底栏 10dp；提交钮 56dp / 12dp 阴影并上浮 6dp。卡片色仍 `#F0F0F2`，页面护眼背景不变。

## 2026-07-17 — 还原答题界面为 APK 既定 Material 规格

撤销 soft-card 轻拟物尝试，答题 chrome 回到 `8b6c53a`：`Surface`/`ElevatedCard`、顶栏 56dp、底栏内嵌 52dp 提交钮。导航箭头保留 AutoMirrored。

## 2026-07-17 — 正确答案全屏可编辑写回

双击「正确答案」进入 `CorrectAnswerEditScreen`（交互对齐笔记全屏）；退出有改动时确认保存；经 `AppNavAiWritebackPipeline.updateQuestionAnswer` 写回 Session（练习 `UpdateQuestionAllFields` / 考试 `SaveEditedQuestionFields`）；`DrawingAnswerEditPipeline` 保 DRAWING 标签不丢图。解析区仍走只读 `ExplanationScreen`。

## 2026-07-17 — 正确答案区双击全屏

答题结果区双击进入与解析相同的 `ExplanationScreen`；练习/考试对称接线；`ReadingCollapsibleSection` 增加 `onDoubleTap`。

## 2026-07-17 — 答题页立体视觉恢复为既定成功规格

### 原因
- 前两次提交仅按设计说明近似重做，未严格复用 2026-07-16 已完成并通过 208 个 Gradle 任务的实际实现。
- 本次以用户保留的原 `app-debug.apk` 反编译实参为准恢复，不再凭截图估算尺寸。

### 恢复内容
- 使用 APK 中实际采用的 Material3 `Surface` / `ElevatedCard` elevation，不使用后加的自定义 `questionSessionSoftCard` 视觉体系。
- 顶栏高 56dp、20dp 圆角、1/5dp tonal/shadow elevation；进度条 4dp；题目卡 20dp 圆角、浅色 `#FFF0F0F2`、默认阴影 4dp。
- 选项卡 16dp 圆角、`0.985f` 弹性按压反馈，阴影随按压从 4dp 降至 1dp。
- 底栏 20dp 圆角、5dp 阴影；中央提交按钮 52dp、6dp 阴影；导航箭头保持 48dp 点击区。
- 顶栏和底栏外层使用不裁切的 `Box`，避免软阴影被父 `Surface` 截断。

### 业务边界
- Practice、Exam、Adaptive 继续共用现有 Session / Command；未修改判题、进度、AI、收藏、错题、笔记或数据库。

## 2026-07-17 — 共用答题页立体视觉外壳

### 范围
- 常规练习、考试与自适应渐隐练习继续复用既有 Screen / Session / Command 数据流。
- 仅重构 `:ui-common` 共用答题视觉：悬浮顶栏、分层进度与题型卡、立体题目卡、独立选项卡、悬浮底栏和中央凸起提交按钮。
- 保留原护眼灰背景、答对绿/答错红语义色以及深色模式适配。

### 边界
- 未修改判题、导航策略、进度保存、历史记录、AI、错题、收藏、笔记和数据库逻辑。
- Practice / Exam 选项继续使用各自原有点击回调，只共同复用 `QuestionOptionSurface` 外观。

### 验证
- `git diff --check`：PASS。
- 修改文件均低于 500 LOC；全仓 LOC 门禁仅报告 3 个既有基线文件。
- 当前容器无法访问 Gradle 腾讯镜像，完整 Android 编译待本地 JDK 21 环境执行。

## 2026-07-16 — 自适应渐隐原子练习（MVP）

### 边界
- 新增独立 `AdaptiveFading` Session；常规练习、考试、复习、错题与原子填空转换保持原路径。
- 仅对 `.sqlite` / `.db` 原子题库显示新入口；源题只读，学习状态写入独立 `adaptive_atom_states`。

### 实现
| 项 | 变更 |
|----|------|
| 抽题 | 核心池 + 15% 探测池；同轮同条文最多一个原子；遗忘→已学到期→新原子排序 |
| 渐隐 | `CHOICE → HINTED → RECALL → MATURE`；选项不足自动降级为提示填空 |
| 调度 | 固定间隔 MVP；错答降阶、10 分钟后复习并提升至核心池 |
| 持久化 | Room 26→27 显式迁移；按 `bankId + atomId` 隔离状态 |
| Session | 新 Kind / Creator / Registry / Policy / Extension；复用 Practice UI，跳过常规填空再转换 |
| 结果 | 显示本轮成绩，不写常规练习历史、不生成普通错题/收藏副作用、不提供无效详情入口 |
| 隔离 | 不读取普通进度、不加载普通题目元数据；源题编辑、AI、笔记、收藏入口在新模式隐藏 |

### 文档与测试
- 根目录：`ADAPTIVE_FADING_ATOM_PRACTICE.md`
- ADR：`.ai/ADR/006-adaptive-fading-atom-practice.md`
- 新增抽题与阶段推进单元测试。
- 独立 Kotlin JVM 编译：domain 契约、Session policies/Snapshot、data Repository、Room 迁移源码、进度 Extension 均 PASS。
- 真实执行阶段推进、双池比例、同条文避让、到期优先、数字单位干扰项及自适应结果统计：PASS。
- 构建验证受环境网络限制：Gradle 腾讯镜像不可达；需在可联网 JDK 21 环境补跑门禁。

## 2026-07-15 — 导入重复 / 绘图答案图 / DeepSeek 返回题号

### 问题
1. 桌面「（旧版）*计算题.xlsx」解析本身可得 10–11 题，但重复导入仅按**完全相同**文件名拦截；扩展名有无不一致时首页去扩展后显示为同名题库（如 11 题 + 1 题并存）。
2. `技师绘图题.docx` 纯标题「绘图题」未匹配章节正则 → 题型落成简答题；需删库重导后答案图标签才完整。
3. DeepSeek 全屏保存返回：`SessionHost` 在子路由 dispose 时 `leave()` 销毁会话，再 `enter` 重建 → 回到第 1 题。

### 修复
| 项 | 变更 |
|----|------|
| `ImportDuplicateFilePipeline` | 同 stem / 忽略扩展名判重；批量导入同步 knownNames |
| `DocxQuestionParser` | 章节标题序号可选；文件名推断绘图题；统一 MetadataManager 存图 |
| `SessionHost` + `QuestionSessionHostViewModel` | dispose 不 leave；同 kind 复用；`onCleared` 才 destroy |
| `AnswerResultRow` | 文本/绘图题展示 `TextResponseAnswerContent`（答案图） |

### 用户操作
- 已存在的错误「1 题」或无图题库：首页长按删除后重新导入对应文件。
- 防重复：再导同名（含仅扩展名不同）会提示重复失败。

### 验证
- `:data` ImportDuplicate + ExcelLegacyCalculation + Desktop 夹具（有文件时）
- `:feature-practice` SessionHostEnterReusePipelineTest
- 手测：DeepSeek 保存返回仍停在原题

## 2026-07-15 — 首页题库语义图标

### 目标
- 首页题库卡片不再统一显示通用文档图标。
- 图标同时反映题库来源格式和真实题型；Column/Grid 使用同一套映射结果。

### 解析优先级
1. 文件名明确标注的题型（填空、绘图、判断、单选、多选、计算、简答/论述）。
2. 导入来源扩展名（XLS/XLSX/CSV、DOC/DOCX、SQLite、JSON、TXT/Markdown、图片）。
3. `FileStatistics.primaryQuestionType` 与 `questionTypeStats`；多种题型映射为 Mixed。
4. 无有效信息时使用 Generic 题库图标。

### 变更
| 文件 | 变更 |
|------|------|
| `HomeFileTypeVisualPipeline.kt` | 新增 `HomeQuestionBankVisualKind`；覆盖 Spreadsheet/Document/Database/Data/Text/Drawing/Fill/Judge/SingleChoice/MultipleChoice/Calculation/Written/Mixed/Generic，并为每类提供独立图标与渐变色 |
| `HomeQuestionBankCard.kt` | 接收真实 `FileStatistics`，按 `fileName + primaryQuestionType + questionTypeStats` 缓存视觉结果 |
| `HomeFileListColumn.kt` / `HomeFileListGrid.kt` | 将当前题库统计传入视觉卡片，保证两种布局一致 |
| `HomeFileTypeVisualPipelineTest.kt` | 覆盖 Excel、Word、SQLite、填空、绘图、判断、多选与混合题库 |

### 验证
- `ktlintCheck`：PASS（全仓）。
- `:feature-practice:testDebugUnitTest --tests HomeFileTypeVisualPipelineTest`：4 tests PASS。
- `:app:assemblePerformance`：PASS。

## 2026-07-15 — Performance 构建、首页滑动与兼容性收口

### 性能
- Compose BOM 升级到 `2026.06.00`，Kotlin 升级到 `2.0.21`（Strong Skipping 默认开启）。
- 接入 `:baseline-profile`、ProfileInstaller 与 `performance` 构建类型；真机生成首页冷启动/滚动 Baseline Profile。
- 首页题库卡片改为轻量布局，Hero 位图与绘制缓存，滚动期间门控侧滑/拖拽重交互。

### 兼容修复
- Performance/R8 下 Excel 导入崩溃：保留 `org.apache.logging.log4j.message.**`，避免 Apache POI `DataFormatter` 初始化时反射构造器被裁剪。
- Material3 升级后的答题卡浅紫回归：`AppCard` 浅色容器显式锁定原值 `#F0F0F2`；答对绿、答错红保持原有颜色管道。
- Performance APK 使用 debug signing 覆盖安装，保留现有用户数据；R8、资源压缩和 Baseline Profile 保持启用。

### 验证
- 全仓 `ktlintCheck`、数据模块 Excel 单元测试、答题颜色测试：PASS。
- `:app:assemblePerformance`：PASS。

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
