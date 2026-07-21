# Change Log

> 记录各 Phase 的主要变更。
> 格式：`YYYY-MM-DD | Phase-N | 描述`

## 2026-07-21 | 题库导入 OOM 根治 + 提速（DOM → SAX 流式）

- 根因：`ExcelQuestionParser` 用 `WorkbookFactory.create`（XSSF usermodel/DOM）全量构建工作簿对象树，即便稀疏文件桌面 66MB 可开，手机 app 基线已占 ~130MB，XmlBeans/XSSFCell 对象图叠加突破 368MB 上限 → 每次导入必 OOM。
- 修复：无图 xlsx 改走 POI 事件模型（SAX）`XSSFReader + XSSFSheetXMLHandler` 流式逐行读取，恒定内存、无对象树，速度显著优于 DOM；`.xls` 或含 `xl/media` 的文件（体量小）仍回退 usermodel 以保留内嵌图片导入。
- 抽象：新增 `ExcelRowData`（`PoiExcelRowData` / `MapExcelRowData`），DOM 与 SAX 两条读路径共用同一套 detect/parse 逻辑，行解析函数去除 `DataFormatter`/`Row` 直依赖，零逻辑改动。
- 新增 `ExcelStreamingRowReader`；`ExcelQuestionParser` 按 zip 条目探测 OOXML/media 决定读路径，`buildQuestions` 统一出口。

## 2026-07-21 | 导出文件回导 OOM 修复（稀疏 cell 写出）

- 现象：新导出的 xlsx 在"数据管理 → 题库导入"失败。桌面 JVM + 同版本 POI、-Xmx384m（对齐手机堆上限）复现：`WorkbookFactory.create` DOM 解析 sheet1.xml 时 `OutOfMemoryError`。
- 根因：`XlsxStreamWriter` 每行全量写出 165 列（含空 cell），6653 题的 sheet XML 解压后 ~77MB（110 万 cell 节点），导入端 POI XSSF DOM 全量驻留必然 OOM。
- 修复：空 cell 不写节点（`r` 属性定位列，稀疏行为 OOXML 标准用法）。同文件复验：sheet XML 降 ~99%，POI 384MB 堆 1.1s 打开，6654 行全部可读、表头/列位不变。
- 旧文件挽救：已用相同规则重打包出「修复可导入版」xlsx 供直接导入。

## 2026-07-21 | 题库导出状态与速度优化

- 导入/导出加载态增加 `isExporting` 区分；导出遮罩由错误的“正在导入题库...”改为“正在导出题库...”。
- 题库查询、补充数据加载、行快照构建和 XLSX 写出整体移至 `Dispatchers.IO`，消除主线程卡顿。
- `XlsxStreamWriter` 增加 64KB 双层缓冲、列引用缓存与 ZIP `BEST_SPEED`；常规单元格文本不再无条件复制。
- 补充数据查询改为每批 30 个并发、批次顺序推进，避免大题库一次创建数千协程并争用 Room。

## 2026-07-21 | 题库 Excel 导出 OOM 修复（导出路径去 POI 化）

- 根因一：`writeWorkbookToUri` 用 `XSSFWorkbook` 全量内存构建工作簿，大题库导出堆涨至 368MB growth limit 抛 OOM，且构建在主线程（跳帧 3066）。
- 根因二：换 `SXSSFWorkbook` 后在 Android 上必崩——`SXSSFSheet` 构造时急切创建 `AutoSizeColumnTracker`，其静态块依赖 `java.awt.font.FontRenderContext`（Android 无 AWT，POI bug 65260，5.2.4+ 未修复该场景）。
- 修复：新增 `XlsxStreamWriter`（feature-settings，纯 JDK ZipOutputStream + 手写 OOXML，inline string + 红字行样式），`writeWorkbookToUri` 整体移入 `Dispatchers.IO` 并委托流式写出，恒定内存。
- `:feature-settings` 移除 POI/stax/aalto 依赖（导入解析仍走 `:data` 的 POI 读路径，不受影响）。
- 四条 Excel 导出链路（题库 / 错题本 / 收藏 / 历史）共用此出口，一次修复全部生效。

## 2026-07-21 | Home 文件夹创建序排序调整

- `FolderDao.insert` 冲突策略 REPLACE → IGNORE：重复 addFolder 不再 DELETE+重插导致 rowid 被顶到最大、创建序被打乱。
- 展示链路改为 `ORDER BY rowid DESC`，文件夹按创建时间倒序排列，最后新建的文件夹位于文件夹区首端。

## 2026-07-21 | Home 拖拽误开抽屉修复

- 长按题库卡片拖拽期间（`draggingFile != null`）禁用 `ModalNavigationDrawer` 滑动手势，防止拖动手势被抽屉水平 anchoredDraggable 抢占误开左侧抽屉。
- `HomeNavigationDrawer` / `HomeScreenDrawerHost` 新增 `gesturesEnabled` 参数；抽屉已打开时仍强制允许手势关闭。

## 2026-07-20 | Deprecation API cleanup

- `LocalClipboardManager` → `LocalClipboard`（`QuestionEditDialog` / `QuestionSessionActionRow` / `PracticeBottomToolbar`）
- `centerAlignedTopAppBarColors` → `topAppBarColors`（`AppCenterAlignedTopBar`）
- `LocalLifecycleOwner` 改用 `androidx.lifecycle.compose`（Exam/Practice AI sync + Practice lifecycle）
- `quadraticBezierTo` → `quadraticTo`（`HomeFolderCard`）
- Room `fallbackToDestructiveMigration(dropAllTables = true)`；`WrongBookRepositoryImpl.getAll` 补 `@OptIn(ExperimentalCoroutinesApi)`

## 2026-07-19 | Phase 28：安全、LOC、导入与生命周期治理

- Release 签名密码移出 `app/build.gradle.kts`；支持忽略的 `signing.properties` 和 CI 环境变量，新增安全示例。
- `ExamViewModel` / `ExamSessionEngine` 复用 `ExamQuestionStatePipeline`；`PracticeEditorCoordinator` 抽取 `PracticeEditorStatePipeline`，三者回到 500 / 497 / 451 行。
- 大文件导入取消完整 Question/Entity 中间副本；Room 500 条分批、单事务写入；TXT 改为逐行读取。
- AI 审查确认当前为整包响应，不存在逐 chunk UI 刷新；DeepSeek 增加单活跃任务、reset/新请求取消旧请求。
- 答题页公式位图原有 remember 策略保留；SVG 与本地图片请求对象改为按输入复用。
- 后续按用户授权完成验证：`:app:compileDebugKotlin` / `:app:assembleDebug` PASS；data/feature-exam/feature-practice/ui-common 单测 PASS，feature-ai 为 `NO-SOURCE`；完整 `ktlintCheck` 与 LOC 门禁 PASS。
- 验证期间修复 `feature-exam` 缺少 JUnit 且纯 JVM 单测误跑 Hilt Kapt；并机械修正既有 androidTest、feature-exam、feature-settings import/换行格式。

## 2026-07-19 | Performance infrastructure scope closure

- 用户决定不实施第二款物理机基线；从待办/阻塞项移除。
- 保留现有按 model+SDK 扩展框架；M2007J17C 为当前唯一物理硬门禁，Pixel_7 AVD 继续仅作观察基线。
- Round21 后当前性能基础设施计划正式收口。

## 2026-07-19 | Round 21 Baseline Profile package semantic budget

- BaselineProfile diff gate 新增 app-entry/navigation/home/startup-settings/data-domain/ui-common 六组包级规则数与精确保留率预算。
- M2007J17C 真机生成 PASS：4 份 profile × 6 组 = 24/24 PASS，全组 retained=1.0，release 新增 75 条规则。
- 负向 Home 语义对照 retained=0.3913，正确 EXPECTED FAIL；无生产代码、视觉、业务或依赖改动。
- 修正测试脚本在不同仓库 owner 的沙箱账号下读取 Git 信息失败；命令级 `safe.directory`，不改全局配置；Gate `20260719-165444` PASS。

## 2026-07-19 | Round 20 Home shader-cache feasibility

- 新增独立 `HomeShaderCacheDiagnosticBenchmark`：测量外预热 Home、仅杀进程并保留 shader cache；不进入硬门禁。
- M2007J17C 真机 PASS：warm-cache TTID med 309.5ms；首帧 med 111.1ms；RenderThread `DrawFrames` med 20.9ms；shader compile med 3.7ms。
- 对照确认 Round19 shader 冷编译归因；生产内预热只会前移同一成本，决定不接入，视觉/Eager/业务保持不变。

## 2026-07-19 | Round 19 Home first-frame attribution

- 复用 Round18 真机 `20260719-155654` 的 5 份 Perfetto trace，交叉分析主线程 Compose/measure、thread state 与 RenderThread。
- 首帧 262.6–294.7ms；RenderThread `DrawFrames` 132.5–149.7ms，`shader_compile` 108.9–125.9ms，5/5 稳定复现。
- 新增 `startup-first-frame-attribution.sql`，后续 Performance 报告自动输出首帧 Compose / RenderThread 归因。
- 无 Kotlin、Gradle、资源、视觉、Eager、业务或数据库改动。

## 2026-07-19 | 测试系统 Round18：Home 首帧 / 滚动热路径优化

- Trace：`HomePerformanceLog.measure` 在 performance 构建始终打 `Home:*`；新增 `home-custom-slices.sql`。
- P1：去掉 contentState 二次订阅；interaction-ready / list-bounds 不再触发整树重组；Eager 首批 8→4；关闭态延后组合 Drawer 重内容。
- P4：稳定 drag 回调与 Lazy key 命名空间；卡片文案预计算；`OptimizedFileCard.reportBounds` 可选。
- 真机 M2007J17C：TTID med 566ms，PHYSICAL_GATE + REGRESSION PASS；Gate PASS；见 `performance/audit/round18-home-frame-optimization.md`。

## 2026-07-19 | 测试系统 Round17：依赖清理 + 回归比对门禁

- 删除 `app/build.gradle.kts` 死依赖：okhttp（全仓零引用）、coil-compose/coil-svg、jlatexmath-android（仅 `:ui-common` 使用且已自带声明）。
- P3 修正：`QuestionDataInitializer` 非死代码（Round2 已接入 `GetQuestionsUseCase.fileNames()`），保留。
- P2 审计结论：`bindApplication` 110ms 中 ~85ms 为系统 dex/资源加载，`TestApp` 空实现无可 lazy 化项；首帧 270ms 主因 Text measure/layout + 未打点首次组合（留作 P1）。
- `performance-thresholds.json` regression 门禁启用：同 model+SDK 历史中位数对比，TTID med +15% / frame P99 +30%，`minHistoryRuns=3`。
- 回归门禁负向验证通过（临时 -90% 限值 → 正确 FAIL exit 20 → 恢复正式阈值）。
- 修复 Gate 真实 bug：改按报告目录名时间戳取「各 Mode 最新结果」，避免旧目录文件时间刷新遮盖新 PASS。
- 真机复测 PASS：TTID med 574.2 / 568.1ms 两轮，PHYSICAL_GATE + REGRESSION 双门禁通过；最终 Gate PASS（20260719-152824）；见 `performance/audit/round17-cleanup-regression-gate.md`。

## 2026-07-19 | 测试系统 Round16：破坏性清空 + Profile 语义 diff + 多机型门禁

- 新增 `DestructiveClearIsolationTest`：仅在内存 Room / 测试 DataStore 中验证全清，Isolation 8/8 PASS。
- BaselineProfile 从数量比较升级为精确规则集合保留率门禁（≥0.95）+ MainActivity/HomeRoute 锚点；4 份 Profile retained=1.0。
- `performance-thresholds.json` v4：`deviceProfiles[]` 按 model+SDK 匹配；未知物理机 FAIL；AVD 仅观察。
- 真机 M2007J17C Performance PASS（TTID median 574.9ms）；Pixel_7 AVD 观察基线 PASS（1250.2ms）。
- 最终 Gate PASS：READY 23/23、run-verified 21/23；见 `performance/audit/round16-destructive-semantic-multidevice.md`。

## 2026-07-19 | 测试系统 Round15：Profile diff + Stress 多种子 + SAF 选择器

- BaselineProfile：对照 `baselines/baseline-profile-ref.json`，rules≥0.9×ref + MainActivity 覆盖；`-UpdateProfileRef` 重钉。真机 REF_UPDATED→PASS。
- Stress：`-StressSeeds` 多种子；Pixel_7 上 `20260719,20260720` 均 PASS。
- `SafOpenPickerTest`：设置→导入题库→选择文件→DocumentsUI；Functional 一并跑通（真机 PASS）。
- Functional `backToHome` 优先底栏「题库」；报告目录同秒去重。
- 见 `audit/round15-profile-stress-saf.md`。

## 2026-07-19 | 测试系统 Round14：DataStore/AI 错误隔离 + 覆盖率硬门禁

- Isolation 扩至 package 级 7 用例：新增 `DataStoreIsolationTest`（clear 隔离）+ `FakeAiErrorPathTest`（ERROR/TIMEOUT/恢复）。
- `FakeAiBackend.failureMode`；`coverage-thresholds.json` v3 `gateEnabled=true`（READY≥1.0 / run-verified≥0.8）；负向验证 exit 70。
- `.gitignore` 补 `.kotlin/` `log.txt` `performance/reports/`；删除根目录 `log.txt`。
- 真机实测 Isolation PASS `20260719-122354`；Gate PASS `20260719-122617`；见 `audit/round14-isolation-hardening.md`。

## 2026-07-19 | 测试系统 Round13：真机 PHYSICAL_GATE 硬基线

- 真机 M2007J17C（Android 15 / SDK 35）三轮 Performance：TTID med 563.5 / 549.3 / 548.5 ms；`physicalGate: PASS`。
- `metrics.grade` 按 serial 区分 `PHYSICAL_DEVICE` / `OBSERVATION_EMULATOR`。
- `performance-thresholds.json` v3：`mode=PHYSICAL_GATE`，阈值 TTID med≤800 / max≤950，frame P95≤400 / P99≤500（仅 pinned model）。
- `Invoke-Performance` 真机超标 → status FAIL / exit 50；模拟器仍观察级。
- 报告：`performance/reports/20260719-120821/` + `audit/round13-physical-baseline.md`。

## 2026-07-19 | 测试系统 Round12：Full 全链路收口 + 签名冲突修复

- `Full` 模式：各阶段独立子目录（`01-audit`…`09-gate`），避免 `result.json` 互相覆盖；Gate 递归扫描 `reports/**/result.json`。
- Isolation 前置 `adb uninstall`，修复 Stress(`benchmarkRelease`)→Isolation(`debug`) 的 `INSTALL_FAILED_UPDATE_INCOMPATIBLE`。
- Full 实测：Audit→Smoke→Functional→Explore→Recovery→Performance→Stress 均 PASS；Isolation 修复后 PASS；Gate **8/8 Mode PASS**。
- 报告：`performance/reports/20260719-105011/`（Full 主体）+ `110125`/`110139`（Isolation/Gate 收口）+ `audit/round12-system-closed-loop.md`。

## 2026-07-19 | 测试系统 Round11：@TestInstallIn 内存 Room + Fake AI 隔离

- 生产 DI 最小拆分：`DatabaseModule` / `AiBackendModule`（行为不变，便于 `@TestInstallIn` 替换）。
- `app` androidTest：`HiltTestRunner` + `InMemoryDatabaseModule` + `FakeAiBackendModule` + `TestDataIsolationTest`（2 用例）。
- `run-tests.ps1 -Mode Isolation`；Gate 必过 Mode 列表已加入 Isolation。
- 模拟器实测 PASS：`performance/reports/20260719-104335/`。

## 2026-07-19 | 测试系统 Round10：PerfettoSQL 分析闭环跑通

- 新增 `performance/scripts/fetch-trace-processor.ps1`（下载 v54.0 Windows `trace_processor_shell.exe`，`bin/` gitignore）。
- SQL 包适配 v54 stdlib（`INCLUDE PERFETTO MODULE android.startup.startups`）；Performance 模式强化 trace 收集 + Start-Process 跑查询并计 ok 数。
- 实测 PASS：`performance/reports/20260719-103142/` — `perfettoSql=DONE (15/15 ok; 5 traces x 3 queries)`；CSV 含真实 cold launch / FrameTimeline / 主线程 slice。

## 2026-07-19 | 测试系统 Round9：Baseline Profile 生成闭环

- 新增 `run-tests.ps1 -Mode BaselineProfile`：`:app:generateReleaseBaselineProfile` + class 过滤仅跑 `BaselineProfileGenerator`；清理 stale UTP lock；快照规则文件并计数。
- 模拟器实测 PASS：`performance/reports/20260719-102038/` — release `baseline-prof.txt` **25860** rules，performance **22277** rules；写入 `app/src/{release,performance}/generated/baselineProfiles/`。
- 首轮失败根因：无 class 过滤时全模块测试（Explore 等）与安装抢包导致 Process crashed；已修复。

## 2026-07-19 | 测试系统 Round8：性能指标提取 + 基线历史 + PerfettoSQL 脚手架

- `Invoke-Performance` 解析 `*benchmarkData.json` → 报告 `metrics.json`（TTID min/med/max + frame P50/P90/P95/P99）；追加 `performance/baselines/baseline-history.jsonl`。
- 等级标记 `OBSERVATION_EMULATOR`（模拟器数字不作硬门禁）；`performance-thresholds.json` v2 记录最近观察值。
- 新增 `performance/perfetto/queries/{startup-breakdown,frame-jank,main-thread-blocking}.sql`；有 `trace_processor_shell` 时自动跑，否则 `SKIPPED_NO_TOOL`。
- 实测 PASS：`performance/reports/20260719-100403/`（TTID med 1130.3ms；frame P95 362.8ms；PerfettoSQL SKIPPED_NO_TOOL）。

## 2026-07-19 | 测试系统 Round7：Gate 覆盖率矩阵 + 质量门禁聚合

- 新增 `run-tests.ps1 -Mode Gate`（无需设备）：从 `actions.json` 生成 `performance/coverage/coverage-matrix.json`；聚合 `reports/*/result.json` 各 Mode 最新真实结果；任一缺失/非 PASS → 退出码 70。
- 门禁范围：Audit / Smoke / Functional / Explore / Recovery / Performance / Stress 全部 PASS。
- 覆盖率观察基线：automatable=17，READY=17/17=1.0，run-verified=15/17=0.882；`coverage-thresholds.json` v2（`gateEnabled=false`，数字仅观察）。
- 实测 PASS：`performance/reports/20260719-095550/`；`Full` 链路末尾已串入 Gate。

## 2026-07-19 | 测试系统 Round6：Recovery 错误处理与恢复

- 新增 `RecoverySuiteTest`：r1 杀进程后「接着练习」恢复；r2 旋转后题干/控件保持；r3 断网冷启动可达首页。
- `run-tests.ps1 -Mode Recovery`；`Full` 链路已串入；网络在脚本收尾强制恢复。
- 发现并确认行为：**仅切题不落盘，作答一题才持久化练习进度**（首次运行 r1 因此失败，修正用例后通过；非产品缺陷）。
- 模拟器实测 PASS：`performance/reports/20260719-095031/`。

## 2026-07-19 | 测试系统 Round5：Stress monkey 随机稳定性（定种子 + 断网）

- `run-tests.ps1 -Mode Stress`：`installBenchmarkRelease` → 关闭 wifi/data → `monkey -p com.example.testapp -s 20260719 --throttle 150 2000` → 拉 monkey.log / crash 缓冲 / ANR。
- 判定：Events injected=2000 + 无 `// CRASH` / `// NOT RESPONDING` + crash 缓冲无 App FATAL；失败退出码 30。
- **强制模拟器**：物理设备拒绝执行（STATEFUL_DESTRUCTIVE 仅限隔离 userdata）。
- 模拟器实测 PASS：`performance/reports/20260719-094157/`；`stability-thresholds.json` 记录观察基线（`gateEnabled=false`）。
- `Full` 链路已串入 Stress。

## 2026-07-19 | 测试系统 Round4：Explore 确定性遍历（SAFE_READ + 崩溃门禁）

- 新增 `ExploreCrawlTest`（UiAutomator，复用现有文案/contentDescription；不加 testTag，不改视觉）。
- 遍历：我的→设置→出题模式/AI 服务→首页；错题/收藏/记录→首页；首页抽屉开合；每步做前台/崩溃缓冲断言。
- `run-tests.ps1 -Mode Explore`：跑遍历、拉截图(`/sdcard/explore`)、拉 `logcat -b crash` 全量；App FATAL 崩溃即判 FAIL（退出码 30）；`Full` 链路已串入 Explore。
- 模拟器实测 PASS：`performance/reports/20260719-093554/`（7 张截图，崩溃缓冲无 FATAL）。

## 2026-07-19 | 测试系统 Round3：Functional 确定性套件（作答/收藏/考试/Tab）

- 新增 `FunctionalSuiteTest`（UiAutomator，复用现有 contentDescription/文案；不加 testTag，不改视觉）。
- 用例：t1 单选作答揭示结果；t2 收藏往返；t3 考试入口并返回；t4 底栏 错题/收藏/记录 往返。
- `run-tests.ps1 -Mode Functional`：编译安装、跑套件、拉截图(`/sdcard/functional`)、junit、logcat；失败退出码 10；`Full` 链路已串入 Functional。
- 模拟器实测 PASS：`performance/reports/20260719-093057/`（SAFE_WRITE 仅限模拟器测试题库；收藏已回滚）。

## 2026-07-19 | 测试系统 Round2：Smoke 闭环（Home→练习→切题×10→返回）

- 新增 `HomeSmokeTest`（UiAutomator，复用 `home_file_card:` / `下一题` / 「开始练习」文案；不加 testTag，不改视觉）。
- `run-tests.ps1 -Mode Smoke`：编译安装、跑旅程、拉截图(`/sdcard/smoke`)、junit、logcat；失败退出码 10。
- 接通 `GetQuestionsUseCase.fileNames()` → `ensureBuiltInQuestionsInitialized()`（原先死代码；prefs 幂等；无 assets 时 no-op）。
- 仅 `benchmarkRelease` / `nonMinifiedRelease` sourceSet 加入 `assets/tiku/smoke_small_bank.json`（12 题小题库），不影响 debug/release。
- 模拟器实测 PASS：`performance/reports/20260719-090919/`。

## 2026-07-19 | 测试系统 Round1：审视 + 清单 + 最小 Macrobenchmark 闭环

- 新增 `performance/`：audit / inventory / model / thresholds / test-data / scripts / reports（不改业务视觉与交互）。
- 复用既有 `:baseline-profile`（`HomeStartupBenchmark` + `HomeJourney`）作为最小闭环；一键入口 `performance/scripts/run-tests.ps1`（Audit/Performance 可运行，其余 Mode 显式 `NOT_IMPLEMENTED`）。
- `.ai/performance_test_rules.md` / `app_test_inventory.md` / `test_coverage_rules.md` / `performance_baseline.md` 落地。
- 为模拟器 Macrobenchmark 在 `app` `ndk.abiFilters` 增加 `x86_64`（仅打包 ABI，不改 UI/交互）。
- 阈值暂为 `OBSERVATION_BASELINE`，不伪造门禁数字。

## 2026-07-18 | 修复：从设置/错题/收藏/记录/结果返回主页卡顿

- 根因：NavHost 离开 `home` 会销毁 composition；返回时 Eager 列表又从 `firstPaint=8` 渐进重建。
- `HomeViewModel.registerHomeCompositionAndIsReturn` 以存活的 Home NavEntry 明确区分冷启动/返回，不再依赖易受错题/收藏复用影响的列表签名。
- 冷启动仍走已验证的 Eager 路径；返回第一帧直接走 Lazy 可见区恢复（日志 `home_list_return_lazy`），避免先 Eager 再切 Lazy。
- `HomeViewModel.contentState` 的 `WhileSubscribed` 由 5s 提到 30s，减少短离返回时上游重跑。
- Navigation Compose 从传递依赖 2.5.1 显式升级至稳定版 2.9.8，采用官方导航图比较性能优化。
- 视觉与导航行为不变；Hero 位图缓存路径保持。

## 2026-07-18 | 修复：冷启动主页题库卡上下滑动掉帧

- `HomeViewModel`：filenames / statistics / progress 合并为单一 `contentState`（`Dispatchers.Default` + `distinctUntilChanged`），消除冷启动多路 `StateFlow` 突发重组。
- Dashboard 仅聚合 header/Hero；移除未使用的 `questionBankItems` 热路径与 `selectedFileName` 依赖。
- 列表改为预聚合 `HomeQuestionBankCardModel`；父级决定 `QuestionBankCardLayout`，去掉每卡 `BoxWithConstraints`；题库卡 Card/CTA elevation 与 `DisposableEffect` 保持原设计和诊断能力不变。
- `OptimizedFileCard` 始终挂载 `pointerInput(Unit)`（门禁用 `rememberUpdatedState`），Lazy 槽复用到不同 fileName 时不再重启手势协程；`SwipeRevealActionBox` 跟手偏移改为同步 `mutableFloatState`，避免每 delta launch 协程。
- Column/Grid 保留 `LazyLayoutCacheWindow` 4000dp（大题库回退路径）；真机日志证明冷启动每卡组合约 50–80ms，CacheWindow 空闲帧无法填满，首次下滑仍 `uniqueCardsDelta=17`、回顶 `cardEntriesDelta=21`。
- 题库+文件夹 ≤48 时改走 `HomeFileListEagerColumn`（`verticalScroll` 一次组合，滚动零创建/销毁）；卡片/阴影/CTA/滑动删除视觉与手势不变；更大列表仍用 Lazy。
- 对齐 Google defer-reads：禁止在 composition 订阅 `isScrollInProgress`（`HomeScrollProgressFlag` + snapshotFlow）。
- 去掉可见 `scrollTo` 预热（会把列表滚走造成主页空白）；改为首帧组合 8 张、其余 `delay` 节奏补齐（静止也推进，滑动时暂停避免撞帧）；`contentState` 对统计/进度 `onStart` 空值，题库名一到即出列表。
- 注：Debug 构建每张富卡片冷组合 ~40ms（无 R8/Baseline Profile/JIT 冷），25 张 ~1s；生产性能须以 Performance APK + Baseline Profile 判定。
- `HomeJourney` 扩为 5 次到底 + 5 次回顶；单测覆盖 eager 阈值与既有 dashboard/card model。

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
# 2026-07-19 | 收尾清理

- 删除全部 JVM / Android / Worker 测试源码、测试 DI 与测试资源。
- 删除 `:baseline-profile`、Macrobenchmark、性能脚本、基线、报告和审计目录。
- 移除测试依赖、测试 CI 步骤、性能构建类型与 Ktor logging 插件。
- 清除生产代码中的 Logcat、println、Trace 和调试日志对象；Home 性能钩子改为无输出兼容壳。
