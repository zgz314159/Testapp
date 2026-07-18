# Change Log

> 记录各 Phase 的主要变更。
> 格式：`YYYY-MM-DD | Phase-N | 描述`

## 2026-07-18 | 填空编辑：答案/标签/分值三框拆分

- 「修改当前题目」填空答案行改为三个输入框：答案正文、属性标签、分值；编辑时不再出现「【】」，保存时由 `buildEditableFillAnswerPart` 拼回 `答案【标签】【N分】`。
- 新增 `parseEditableFillAnswerFields` / `buildEditableFillAnswerPart`（复用 core `parseFillAnswerPartDescriptor`），单测覆盖拆分、往返、分值收敛。
- LOC：工具栏与输入行拆到 `QuestionEditFieldRows.kt`，`QuestionEditDialog.kt` 回落 500 以下。

## 2026-07-18 | 修复既有红测试：全答重答门控串扰 + 结果历史行断言过期

- `PracticeAnswerHandler.isQuestionPendingForCurrentMode`：门控由 `fullAnswerModeActive || fullAnswerRequireCorrect` 改为仅 `fullAnswerModeActive`；「答错需重答」是全答模式子设置，模式未激活时残留开关不再把已判错题标为待答（与 `FillQuestionGenerationMode` 的模式互锁设计一致，`PracticeAnswerHandlerPendingTest` 转绿）。
- `ResultHistoryLinePipelineTest`：断言更新为现行口径（正确率 = 正确/已答、百分比去尾零，2c761db 起），并补充去尾零用例；`:app` 306 项单测全绿。

## 2026-07-18 | 修复：原子题库题目编辑丢失属性标签与分数值

- 根因：Session V5 后练习端 `PracticeQuestionEditCoordinator.prepareEditableQuestion` 直接取会话内展示题；动态填空/自适应渐隐的派生变体（负 id）答案已被剥掉「【标签】【N分】」，导致编辑弹窗看不到也存不回标签/分值。
- 修复①：`prepareEditableQuestion` 一律回查题库源题（`extractSourceQuestionId` + 按 fileName 查询），编辑弹窗恢复显示可修改的 `答案【标签】【N分】` 原文（补 scope 注入，两处构造点同步）。注意：单变体时 `buildConfiguredFillQuestion` 沿用源题正 id 但答案同样已剥离，故不能只对负 id 回查。
- 修复②：`PracticeSessionQuestionContentDelegate.persistQuestionFile(mergeById)` 原按派生负 id 匹配文件永远落空（静默不落库）；改为映射回源题 id 合并写回。
- 修复③：`saveEditedQuestion` 由「整文件覆盖成单题」改为读取现有题目按源 id 合并写回，防止题库被截断。
- 附带：`PracticeSessionEngineTestSupport` 补 `FakeAdaptiveAtomRepository`（`PracticeSessionDeps` 新增 `adaptiveAtoms` 后测试支撑未同步，属既有编译断裂）。

## 2026-07-18 | 编辑题目：添加入口图标化 + 光标插入空位

- 「修改当前题目」：AI 纠题图标从标题行移到题目内容下方工具栏右侧；「添加选项 / 增加填空」改为 `+` 图标，与 AI 同一水平线。
- 选择题点 `+`：在题干光标处插入 `( )`，并追加选项框（复用填空题已有的光标插入思路，新增 `insertEditableChoiceBlankAtCursor`）。
- 填空题点 `+`：沿用 `insertEditableBlankAtCursor` 插入下划线并同步答案空位；粘贴/清空答案仍保留在工具栏。

## 2026-07-18 | 来源编号徽标 + AI 问答字体设置弹层统一

- 来源列表编号徽标：改为品牌蓝文字 + 浅蓝圆底、固定 20dp 圆形；行结构改为「favicon+域名+时间」占 weight(1f)、徽标固定右端，保证 1~N 全部右对齐。
- AI 问答页（DeepSeek/星火/百度 Ask）右上角三点菜单：由普通 DropdownMenu「增大/减小字体」改为与答题页同款的排版设置底部弹层（`AppElevatedActionSheetTokens` + `QuestionTypographyStepperRow` 胶囊步进器，仅字号一行，12–42 步进 2）；`AiAskFontMenu` 签名精简，三处调用点同步。

## 2026-07-18 | 联网回答排版与来源列表升级

- 来源元数据：`QuestionCorrectionSource` 新增 `publishedDate`；博查取 `datePublished`/`dateLastCrawled`、Tavily 取 `published_date`，统一归一为 `yyyy-MM-dd`。
- 持久化格式：`appendCitations` 每条来源在 URL 后追加可选 `时间:` 与单行 `摘要:`（≤150 字）；`AiChatSourcesPipeline` 解析/重挂同步升级（单测更新），旧格式兼容。
- 来源列表对齐 DeepSeek 官方：`AiChatSourcesSheet` 每项改为 favicon（Coil `SubcomposeAsyncImage` 加载站点 `/favicon.ico`，失败回退地球图标）+ 域名 + 发布时间 + 编号徽标 + 加粗标题 + 两行摘要。
- 回答排版：编辑态正文行高提升为 1.6 倍、只读 RichText 行距 1.5；`AiChatCitationStylePipeline` 新增行首「标签：」分节标题定位（单测），`AiChatCitationVisualTransformation` 对分节标题加粗、`[n]` 引用保持小号品牌蓝。

## 2026-07-18 | AI 问答联网开关（ADR-008 增补）

- DeepSeek 问答输入栏新增联网图标按钮；默认关闭，开启后走 `CHAT_ONLINE`。
- `AiBackend.chat(..., useWebSearch)` → `AiWebSearchOrchestrator`（博查优先 / Tavily 兜底）→ `AiWebSearchPromptPipeline` 将来源注入当前轮 user 消息，再交 DeepSeek。
- 联网回答正文使用 `[1]` 等编号标注事实；客户端在回答末尾确定性追加来源标题与网址，避免模型漏列或编造来源。
- 来源展示对齐 DeepSeek 官方样式：`AiChatSourcesPipeline`（ui-common，单测）拆分正文与来源块，气泡内渲染「N 个网页」胶囊，点按弹出搜索结果底部列表（标题+网址，可跳转浏览器）；编辑回写自动重挂来源块，持久化格式不变。
- 输入栏图标统一：`AiChatPromptIcon` 错位暗影复绘出立体感；联网/发送均为 28dp 无圆圈图标、48dp 触控区、等间距；正文 `[n]` 引用经 `AiChatCitationVisualTransformation` 渲染为小号品牌蓝（`AiChatCitationStylePipeline` 定位，单测）；提示词要求相关来源至少标注一次。
- 纠题与问答共用检索编排；缺检索 Key 时明确报错，不静默降级。

## 2026-07-18 | 检索双通道：博查 / Tavily（ADR-008 增补）

- 联网纠题检索支持博查（`BochaDirectClient`，大陆推荐）与 Tavily 双通道；填哪个用哪个，都填优先博查（`SearchProviderKind`）。
- `AiCredentialStatus` 增加 `bochaConfigured/bochaHint`；`CORRECT_ONLINE` 门槛改为 DeepSeek + 任一检索 Key；`MissingTavilyKey` → `MissingSearchKey`。
- 设置 → AI 服务：抽取复用 `ApiKeySection`，新增博查 Key 录入；`QuestionCorrectionOrchestrator.correct` 改收 `SearchCredential`。

## 2026-07-18 | BYOK AI（ADR-008 supersede ADR-007）

- 默认改为用户自备 DeepSeek V4 Flash + Tavily Key；Keystore AES/GCM 加密存本机；Android 直连官方 API。
- 新增 `AiBackend` 路由、`AiCredentialsRepository` / `AiEntitlementRepository`；托管购买入口「敬请期待」。
- 设置 → AI 服务：录入/清除 Key；无 Key 时明确报错引导配置。
- 移除 Android Play Integrity / `AI_PROXY_*` / `AiProxyClient`；`cloudflare-worker/` 降为未来托管参考。

## 2026-07-18 | AI 联网纠题 + Cloudflare Worker / Play Integrity 安全代理

- 新增 `cloudflare-worker/`：`POST /v1/deepseek/chat`、`POST /v1/questions/correct`；Play Integrity + requestHash + 限流；Tavily 检索 → DeepSeek 结构化纠题；Secrets 不入库。
- Android：`AiProxyClient` / `PlayIntegrityTokenProvider` / `AiProxyRequestHashPipeline`；APK 移除 `DEEPSEEK_API_KEY`，仅保留 `AI_PROXY_BASE_URL`、`PLAY_CLOUD_PROJECT_NUMBER`。
- 既有 DeepSeek 解析/问答改走 Worker；领域模型 `QuestionCorrection*` + `CorrectQuestionWithAiUseCase` + 解析校验 Pipeline。
- UI：`QuestionEditDialog` 增加 AI纠题入口与草稿回填；`AiQuestionEditDialog` / 预览弹窗接入练习、考试、题库抽屉；应用建议只改 draft，保存仍走原 `onConfirm`。
- ADR：`.ai/ADR/007-ai-proxy-play-integrity.md`。

## 2026-07-18 | 答题结果页标题栏与全局统一高度

- 根因：`ResultTopBar` 是自绘 72dp Row + 28sp 大标题，且不走 M3 TopAppBar 的状态栏 inset，比主页/错题库/收藏库/设置/记录页（均为 M3 `TopAppBar`，64dp 标准高度）明显更高。
- 改为 M3 `TopAppBar`：标题 20sp Bold（与 `SettingsTopBar` 一致），保留结果页原有的白色圆角返回按钮与页面底色，statusBar inset 由组件自行处理。

## 2026-07-18 | 题库展示名完整保留后缀，同名不同后缀题库可区分

- 根因（从真机 DB 验证）：本 App 导出的单题 xlsx 带 `_yyyyMMdd_HHmm` 时间戳后缀（如 `（旧版）技师计算题_20260620_1455.xlsx`），重新导入后是合法的新题库；但主页展示名 `cleanupDisplayName` 会剥掉 `_final*`/`_vN`/`_20xx…`/`（N题）` 后缀，导致两张卡显示成完全相同的名字、用户以为重复导入了。
- 修复（按用户要求：不同名字都允许导入，靠完整名字区分）：`cleanupDisplayName` 只去扩展名，其余完整保留；`ImportDuplicateFilePipeline` 维持原语义（仅同名/仅扩展名不同判重复），时间戳/副本后缀视为不同题库正常导入。
- 单测更新：展示名保留生成后缀；时间戳副本、` (1)` 下载副本可正常导入。

## 2026-07-18 | 错题库/收藏库点击题库弹出主页同款开始 sheet

- 根因：`ScopedQuestionLibraryScreen` 的 `onOpenFile` 直接导航到题目详情页，不经过 `HomeStartQuizSheet`。
- 复用主页 `HomeStartQuizSheet`（新增 `showAdaptiveOption`，库内关闭自适应项）；`WrongBookScreen`/`FavoriteScreen` 点击卡或 CTA 弹出「开始/接着练习、开始/接着考试、重答、取消」。
- 练习/考试分别导航 `practice_wrongbook|favorite` 与 `exam_wrongbook|favorite`；VM 订阅 scoped 练习进度驱动「接着/重答」，重答清除对应 progress pattern。

## 2026-07-18 | 主页分组内改为两列网格

- `HomeScreenLibrarySection` 的 `useGridLayout` 由仅宽屏（`columnCount > 1`）扩展为 `columnCount > 1 || currentFolder != null`：进入分组后与错题库/收藏库分组内一致，两列网格 + 窄卡竖排紧凑布局；根目录窄屏仍为单列长卡。

## 2026-07-18 | 分组内返回手势 + 网格窄卡自适应

- 返回手势：`HomeScreen` 与 `ScopedQuestionLibraryScreen` 的 `currentFolder` 均为屏内状态，系统返回（边缘手势/按键）不感知，直接退栈/退 App。两处新增 `BackHandler(enabled = currentFolder != null) { currentFolder = null }`，先退回根列表。
- 网格窄卡：`HomeQuestionBankCard` 原按屏宽分档，分组内两列网格的半宽卡仍用整行横排布局，图标/CTA 挤压标题只剩一个字。改为 `BoxWithConstraints` 按卡片实际宽度分档：<260dp 走新的竖排紧凑布局（小图标 + 两行标题 + 全宽 CTA + dense 统计行），≥260dp 保持原横排；拖拽悬浮层因按被拖卡实际宽度约束，自动同步同一布局。

## 2026-07-18 | 主页混合列表：题库在前、文件夹在后

- 根因：`HomeScreenLibrarySection` 曾设 `showFilesFirst=false`，文件夹排在题库前。改为 `true`：题库卡在前，文件夹在后。
- 题库顺序继续走 `buildRootDisplayFileNames` / `reorderByRecentUsage`（使用时间倒序，未使用项保持导入序）；已入分组的题库不回流根列表。
- 文件夹顺序：`FolderDao.getAll()` 改为 `ORDER BY rowid ASC`（SQLite 插入序 ≈ 建立时间升序），`filterVisibleHomeFolders` 保持该序并过滤空文件夹。
- 默认值 `HomeFileList` / `HomeFileListContainer` 的 `showFilesFirst` 同步改为 `true`。

## 2026-07-18 | 消灭"空白文件夹"图标

- 用户反馈部分文件夹图标内部没有标记（题型名分组和哈希兜底用了纯 `Folder`）。`HomeFolderVisual` 新增 `badge` 字段：题型名文件夹保留 Folder 外形，把该题型 glyph（EditNote/FactCheck 等）以纸张色镂空徽标叠加在文件夹腹部。
- 哈希兜底变体中的纯 `Folder` 换成 `FolderCopy`，五个变体全部自带内部标记。
- 新增回归测试：所有文件夹图标要么自带内部标记，要么必须携带徽标；编译、ktlint、单测通过。

## 2026-07-18 | 文件夹卡改为文件夹剪影形状（双层材质版）

- `HomeFolderCard` 从与题库卡同构的白卡改为文件夹剪影：自绘 `GenericShape`（左上标签页 + cubic 平滑斜边 + 圆角主体），阴影/裁剪沿剪影轮廓。
- 参考 M3 Expressive「颜色容器 + 形状表达」：底层为类型色渐变夹背（含标签页），上覆近白纸张前层，只露出顶缘和 tab，形成真实纸质文件夹的双层材质；拖拽悬停时夹背更饱和、纸张加深并加主色边框。
- 左侧改为类型色大号裸图标（不再套渐变方块，与题库卡图标容器错开）；右侧数量胶囊徽章 + chevron（容器语义，区别于题库卡的进度/CTA）。

## 2026-07-18 | 文件夹图标改为文件夹家族形状

- 用户反馈文件夹用文档类图标（Article/Assignment 等）与题库卡无法区分。`HomeFolderVisualPipeline` 全部图标改为文件夹家族：SnippetFolder（归档）/RuleFolder（法规）/Topic（学习）/FolderSpecial（职业）/Folder+题型配色（题型名文件夹）/Folder 变体（哈希兜底）；类型区分主要靠配色，形状统一为"文件夹"。
- icons-extended 1.6.7 无这些图标的 AutoMirrored 变体，使用 `filled` 包。新增回归：所有文件夹视觉图标名必须含 Folder/Topic。
- 顺手清理 3 处既有 ktlint 违规（import 排序 ×2、未用 import ×1），`:feature-practice` ktlint 门禁恢复绿色。

## 2026-07-18 | 文件夹视觉、主页排序与跨题库悬浮卡统一

- 文件夹卡新增 `HomeFolderVisualPipeline`：优先按归档/法规/题型/学习/职业等语义显示 Material 图标和配色，无法识别时按文件夹名哈希分配稳定渐变，避免全部同图标同颜色。
- 最近使用记录移除仅保留 3 项的截断：根目录与分组内均按完整使用记录倒序；从未使用的题库继续保持 `QuestionDao.getOrderedFileNames()` 的导入顺序；已入分组的最近项仍不会回流根列表。
- 错题库/收藏库删除旧 `DraggingFileOverlay`/`DraggingFileCard` 路径，统一复用主页 `HomeDraggingFileOverlay` + `HomeQuestionBankCard`，长按悬浮图标、颜色和卡片结构保持一致。
- 新增文件夹视觉与完整最近排序回归测试；`:feature-practice` 全量单测和相关模块编译通过。

## 2026-07-18 | 修复主页长按拖起闪一下即消失

- 根因 A（父树拆除）：`HomeScreenScaffoldContent` 曾用 `draggingFile == null` 条件挂载 `homeRootDragModifier`；拖起瞬间父 Box modifier 变化，取消卡片上的 `detectDragGesturesAfterLongPress`。改为交互就绪时始终挂载；空白区长按用 `rememberUpdatedState` 门禁，不拆除 pointerInput。
- 根因 B（空白长按抢事件）：根节点「长按新建分组」与题库卡长按拖拽同时触发；现仅在未命中 `fileCardBounds`/`folderBounds` 时打开新建对话框。
- 根因 C（Swipe 拆除）：`SwipeRevealActionBox` 在 `enabled=false` 时拆除 pointerInput/offset，拖起后同样取消子手势；改为 pointerInput 常驻，内部读 `enabledState`，禁用时 snap 关闭。

## 2026-07-18 | 修复主页长按拖拽合并分组 + 分组文件夹新版 UI

- 根因 1（手势中断）：`HomeFileListColumn` 曾把 `isScrolling` 编进 `enableDragDrop` 与 SwipeReveal `enabled`——拖拽触发边缘自动滚动（`listState.scrollBy`）时 `isScrollInProgress=true`，pointerInput 被结构性拆除、拖拽手势中途取消。回退为仅在 `allowDragStart` 判滚动（与原注释一致）。
- 根因 2（状态悬挂）：`onDragCancel` 原为 no-op，手势被打断后 `draggingFile` 悬挂、首页滚动锁死；改为 `dragViewModel.endDragging()` 复位。
- 根因 3（结果不可见）：根列表「最近使用」置顶曾包含已入分组的文件，合并后卡片原地不动、且文件夹排在所有散卡之后（屏外）——`buildRootDisplayFileNames` 改为只置顶根目录可见文件；`showFilesFirst=false` 让分组文件夹排在散卡之前（Drive/Files 信息层级）。
- UI：`HomeFolderCard` 重绘为主页白卡浮起样式（渐变文件夹图标 + 「N 个题库」+ chevron，悬停高亮为放置目标）；拖拽悬浮层 `HomeDraggingFileOverlay` 改用 `HomeQuestionBankCard` 视觉，与列表卡片一致。

## 2026-07-18 | 填空题类内：规则与标签折叠为摘要子区

- 「出题规则」「答案标签侧重练习」由平铺改为填空题类内的两个折叠子区（默认收起），标题下显示当前值摘要（每题填空数 / 已选 N 个标签 / 当前模式不使用）；`SettingsExpandableCardSection` 增加可选 `supportingText`。
- 点开填空题不再直接铺满一屏标签，符合渐进披露的常见设置页做法。

## 2026-07-18 | 出题模式页：三大类信息架构

- 设置主入口「填空题设置」更名为「出题模式」；子页分区标题改为「原子题库出题模式」。
- 子页重组为三大可折叠类：填空题（原五种模式 + 出题规则 + 标签侧重）、自适应渐隐（独立说明 + 预留筛选扩展）、记忆模式（从答题设置卡迁入）。
- 新增 `SettingsAtomBankModePanel`；`SettingsAnswerSettingsCard` 去掉记忆段，仅保留练习/考试。

## 2026-07-18 | Mode badge polish + adaptive fading surfacing

- 徽标重构为共享组件 `SessionModeBadge`（胶囊形、浅蓝底 + 细蓝描边），`QuestionCardHeaderRow` 改三段布局：题型左对齐、徽标在中间弹性区真正居中、进度右对齐。
- 「自适应渐隐」纳入模式展示：ui-common 新增 `adaptiveFadingModeLabel`；填空设置页出题模式卡底部新增说明行（徽标 + 「独立练习方式，不受出题模式/标签/分值影响」）；`AdaptiveFadingPracticeRoute → PracticeScreen(adaptiveFadingMode) → ExternalPracticeState.adaptiveFading` 下传，自适应会话答题头部显示「自适应渐隐」徽标（优先于填空模式徽标）。

## 2026-07-18 | Fill mode ↔ tag filter interlock + header mode badge

- 互锁策略收敛到 `FillQuestionGenerationMode.usesTagFilter / usesScoreRange`（唯一裁决点）：标签筛选仅「标签随机模式」参与出题，其余模式忽略残留标签，消除模式切换后的串扰；出题过滤与设置页 UI 均改用该属性。
- 设置页「答案标签侧重练习」在非标签随机模式下整卡置灰禁点，头部显示「切换到标签随机模式后生效」提示；help 文案同步去掉全答/分值范围模式的标签描述。
- 答题头部（练习 + 考试）在「题型：」与「N/总数」之间新增出题模式蓝底徽标（仅动态填空题显示）：`QuestionCardHeaderRow/QuestionSessionHeader/ExamHeader` 加 `modeLabel`，ui-common 新增 `fillModeShortLabel` + 5 条短名字符串；模式经 `ExternalPracticeState/ExternalExamState.fillGenerationMode` 下传。
- `AnswerTagFilterCodecTest` 补 2 条互锁回归（全答模式忽略残留标签、模式属性裁决表）。

## 2026-07-18 | AnswerTagFilterCodec: single tag-filter contract

- 新建 `core/util/AnswerTagFilterCodec`（encode「、」拼接 / decode 兼容逗号顿号分号空白），设置页 UI 与 `FillQuestionTransformUtils` 出题过滤统一改用，删除两端重复正则。
- 新增 `AnswerTagFilterCodecTest`（6 用例）：往返、旧空格格式、混合分隔符、去重，以及多标签出题过滤命中/未命中两条回归。`:core` 补 junit 测试依赖（原缺失致既有测试无法编译）。

## 2026-07-18 | Fix core tag-filter regex mojibake ("暂无题目")

- `FillQuestionTransformUtils` 的标签分隔正则源码中全角「，、；」曾被损坏为字面 `???`，导致「、」拼接的多选标签被当成单个不存在的标签，出题全被过滤 → 练习页「暂无题目」。
- 修正为 `[,，、；;\s]+`，与设置页 UI 解析一致。

## 2026-07-18 | Fix tag multi-select serialization

- 标签多选失效根因：UI 用空格拼接保存，但 UI/出题过滤解析端分隔符均不含空格，选第二个标签后整串变单 token、全部失去选中态。
- 改为「、」拼接；UI 解析端加 `\s` 兼容旧空格数据，点选一次即自愈重存。

## 2026-07-18 | Mode rows / tag tiles: clickable affordance

- 出题模式行与标签瓦片未选态改白底 + 1dp 细描边 + 2dp 轻阴影（原纯浅底无边框像说明文字）；选中态蓝底 + 1.5dp 蓝描边。标签改 20dp 胶囊形，更「标签」。

## 2026-07-18 | Tag tiles: uniform 2-column grid

- 标签改为等宽两列瓦片栅格（40dp 高、10dp 均匀间距），文本居中、选中蓝底 + 对勾；替代宽度参差的流式 Chip，删除 `SettingsChoiceChip`。

## 2026-07-18 | Tag chips: M3 check + tonal borderless

- 标签 Chip 选中态按 M3 规范加前置对勾；去边框，未选浅灰蓝调性底、选中蓝底，直接铺白卡（移除灰色内嵌底板），行距收紧。

## 2026-07-18 | Fill mode radio list + tag panel polish

- 出题模式：5 个 Chip 流式排列 → 竖排单选列表（选中蓝底 + CheckCircle，未选浅底 + 空圈），互斥语义一眼可读。
- 答案标签：头部「已选 N / 提示」+ 右侧清除文字钮；标签 Chip 收进浅色内嵌底板；统计摘要移到分隔线下带 Info 图标脚注。删除 `SettingsFillTagClearChip`。

## 2026-07-18 | Fill settings page restructure

- 填空题设置拆为三个分区卡：出题模式 / 出题规则 / 答案标签，替代单张长卡 + 冒号标签堆叠。
- 默认米色 FilterChip → `SettingsChoiceChip`（白卡/蓝底选中，2dp 浮起）；全答完成条件与轮次顺序二选一改用 `SettingsSegmentedControl`（从外观卡抽出共享）。
- 每题填空数/分值范围收入浅色内嵌面板；帮助文案仍由「查看详细说明」控制，右上角切换。

## 2026-07-18 | Settings practice/exam inset panels

- 练习/考试展开内容收入浅色内嵌面板（`SettingsInsetPanel`），与外观「文字」区一致，消除展开行贴边错位。
- 答对/答错/答题延迟三条 0–10s 长滑条 → 加减步进（Xs），去掉 0s/10s 刻度行。

## 2026-07-18 | Settings data accordion + typography polish

- 数据管理：三张重复导入/导出卡 → 单卡折叠（题库/错题本/收藏），展开后显示导入导出；一次只开一项。
- 外观文字：摘要行 + Aa 实时预览；字号步进与样式分段控件收入浅色内嵌区，去掉提示文案与 FilterChip。

## 2026-07-17 | Settings typography: stepper + merged style

- 字号改为离散加减步进（14–32），去掉长滑条。
- 字号与字体样式合并进同一「文字」区块；FilterChip 统一白卡色，消除米色底板。

## 2026-07-17 | Wrong/favorite delete + drawer questions + settings depth

- 错题库/收藏库删除确认改用 `AppElevatedConfirmDialog` 白卡主页风格。
- 抽屉题库行 elevation 再加深；展开题目行白卡浮起 + 序号圆钮，便于分辨。
- 设置页：分组卡 10dp、分区标题阴影、顶栏返回浮起、列表 leading/chevron 立体图标。

## 2026-07-17 | Drawer depth + home delete dialog unify

- 左侧题库抽屉：标题文字阴影、关闭/搜索/题库图标浮起；搜索框 elevation 加深。
- 主页划动删除确认改用 `AppElevatedConfirmDialog`（白卡主页风格）；划动删除钮改为浮起红底圆钮。

## 2026-07-17 | AI fullscreen + typography + result depth

- AI 全屏：页底 `#F8FAFD`、用户/助手白卡浮起气泡、输入条与发送钮加深阴影，对齐主页。
- 排版设置：Sheet 改主页底色；步进器 +/- 为浮起圆钮；每行独立立体卡。
- 练习结果页：卡片阴影加深、指标图标浮起、题库头/底栏立体化。

## 2026-07-17 | Answer top-bar AI/overflow → home-style sheet

- AI / 三点菜单从扁平 DropdownMenu 改为 `AppElevatedActionSheet`（主页白卡 + 浮起图标）。
- 位置改为底部 ModalBottomSheet，便于大屏拇指操作，并与题库启动弹窗风格统一。

## 2026-07-17 | Home nav icons + answer card + start-quiz sheet depth

- 底栏图标：未选/选中均为浮起圆钮；选中蓝底白标 10dp 阴影。
- 答题卡格子：圆角 14、阴影 6→当前题 8；Sheet tonal 提高。
- `HomeStartQuizSheet` 对齐主页白卡/品牌色：立体操作卡（练习/考试/重答/自适应）+ 浮起取消。

## 2026-07-17 | Home title / hero / bottom-bar depth

- 问候标题与分区标题增加文字阴影；Hero 改 Surface（16dp shadow，去掉外层 clip 遮挡阴影）。
- 底栏 Surface 18dp 阴影 + 选中态圆形浮起图标；顶栏搜索/通知图标 8dp 立体圆钮。
- 护眼页底色不变。

## 2026-07-17 | Home + export depth polish

- 主页题库卡 `HomeQuestionBankCard` / Hero / 底栏 / 顶栏按钮 elevation 加深；`HomeDesignTokens` 立体层级上调。
- 导出/导入列表与设置分组卡 elevation 提高；护眼页底色不变。

## 2026-07-17 | Overlay UI unify + answer-card jitter fix

- 共用 `AppOverlaySurface` / `AppConfirmDialog`：确认弹窗、BottomSheet、改题 Dialog 对齐答题页 elevated 圆角与 `#F0F0F2` 容器色。
- 导出/导入题库列表改为 ElevatedCard；答题卡格子 Surface 阴影圆角。
- `AppLazyBottomSheet` 增加 nestedScroll 锁，消除答题卡滑到顶部与 Sheet 抢滚动的抖动。

## 2026-07-17 | Fix overall score full ring

- 题库总计误用 `SemiCircularGauge`（半弧）导致圆环残缺、文字溢出；改为与「本次练习」相同的全圆 `CircularScoreGauge`，可选副标题「累计正确率」。

## 2026-07-17 | Result Screen Redesign

- 新增 `domain` 模块 `ResultHistoryRecordStats` 纯函数统计入口
- 重构 `SemiCircularGauge`：Box+Canvas+Column 分层布局，避免文字与弧线重叠
- 重构 `MetricTile`：纵向结构（18dp图标+标签 / 完整数值），最小高度 68dp
- 重构 `ResultHistorySheet`：Column(标题+LazyColumn) 替代 Box 嵌套，独立卡片布局
- 重构 `AccuracyLineChart`：左侧边距 42dp、右侧 16dp，首尾标签偏移对齐
- 重构 `ResultQuestionBankHeader`：复用 `HomeFileTypeVisualPipeline` 图标
- 统一统计口径：`calculateResultHistoryRecordStats` 用于折线图和历史列表
- 百分比统一格式：`formatResultPercent` 返回 `0%`、`50%`、`58.33%`、`100%`
- 历史弹层高度动态：0.58～0.82 范围，最多显示 6 条后滚动
- 奖杯移至卡片标题右侧，不再独占垂直空间
- 响应式断点 330dp：窄屏时仪表居中+2x2指标网格纵向布局
- `AppLazyBottomSheet` 增加可选 `heightFraction` 参数
- 新增字符串资源：`result_history_title`、`result_history_nth`、`result_correct_label`、`result_wrong_label`、`result_rate_label`
- 补充 9 项纯单元测试：边界钳制、百分比格式、历史一致性、12条取9等
- 底部操作栏按钮高度调整为 48dp