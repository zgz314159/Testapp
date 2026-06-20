# 项目重构方案与进度

## Phase 1: 统一状态模型 — 已完成
- UnifiedQuestionState + UnifiedSessionState 创建
- Old types 标记 @Deprecated (保留 JSON 反序列化兼容)

## Phase 2: SessionEngine 接线 — 已完成
- SessionProgressManagerImpl (save/load/clear/restore)
- SessionAnalysisLoaderImpl (analysis/spark/baidu/note)
- SessionMemoryModeImpl (memory round plan)
- SessionModule (Hilt binding)
- PracticeVM + ExamVM 已注入 SessionEngine
- 分析加载已委托、saveProgress/clearProgress 已委托
- loadProgress 公共恢复逻辑已抽离到 SessionProgressManager
- 答题交互/updateShowResult/updateAnalysis 已委托

## Phase 3: 红线 Coordinator 拆分 — 已完成
- PracticeSessionCoordinator → ProgressPersistence + AnalysisManager + QuestionLoader
- PracticeNavigationCoordinator → NavigationState + NavigationHistory + NavigationController
- @Deprecated bridge 类型已清理直接委托 NavigationState.kt

## Phase 4: UseCase Facade — 已完成
- PracticeUseCaseFacade (15→1 注入)
- ExamUseCaseFacade (22→1 注入)

## Phase 5: 模块迁移 — ~90% 完成

| 目标 | 状态 | 说明 |
|-----|------|------|
| feature-practice | 26 文件迁移 | PracticeViewModel + UI 组件 + Coordinators |
| feature-exam | 31 文件迁移 | ExamViewModel + ExamScreenContent + 14 UI 组件 |
| :core session/ | 已创建 | SessionEngine + ProgressManager + AnalysisLoader 等 |
| :domain 模型/UseCase | 37 UseCase | 从 :app 迁移到 :domain |
| :ui-common | 12 文件 | AnswerCardGrid, FontStyleProvider 等 |
| util.* → :core | 6 文件 | core.util |
| LocalizedResult → :core | 已完成 | core.common.LocalizedResult |
| FontSettingsRepository | 已提取到 :core | 接口定义 |
| **PracticeScreen** | **留在 :app** | **因 6 个 :app ViewModel + R + Context + SoundEffects 依赖无法迁移** |
| **ExamScreen wrapper** | **留在 :app** | **薄包装层，收集 StateFlow → ExamScreenContent** |
| **ExamAISyncEffects** | **留在 :app** | **依赖 AI ViewModel** |
| **:app/components** | **37 文件** | **共享 UI 组件（Home/Result/Question editor 等）** |

### 模块文件分布

| 模块 | 文件数 |
|------|-------|
| :app | ~184 |
| :domain | ~64 |
| :data | ~59 |
| feature-practice | 26 |
| feature-exam | 31 |
| :core | 11 |
| :ui-common | 12 |

### 代码分布趋势
- 重构前: app(84%) → data(10%) → domain(6%)
- 当前: app(~55%) → domain(~18%) → data(~17%) → feature-*(~15%) → ui-common(~3%) → core(~3%)
- 目标: app(40%) → core(20%) → feature-*(25%) → ui-common(10%) → domain(5%)

## Phase 6: ExamVM 状态统一 — 已完成
- 16 StateFlow → 1 PracticeSessionState
- 向后兼容派生的 StateFlow

## 额外清理
- `.history/` 备份目录已删除
- `domain/bin/` 已删除
- `AppNavHost_backup.kt` 已删除
- `PracticeNavigationCoordinator` 的 `@Deprecated` bridge 类型已移除

## 遗留阻塞: PracticeScreen 迁移
PracticeScreen 无法迁移到 feature-practice 的原因:
1. `SoundEffects` (`util/`)
2. `FontSettingsDataStore` (`data/datastore/`)
3. 6 个 `:app` ViewModel (Settings/DeepSeek/Spark/BaiduQianfan/WrongBook/Favorite)
4. `localizedQuestionTypeLabel` (`presentation/util/`)
5. `R.string.*` 引用 30+ 处
6. 共享组件 (ExamTopBar/ExamOptionsList/ExamAnalysisSection) 在 `:app/components/`

## 历史 Commits 说明
由于大量文件跨模块移动（:app → feature-*），git diff 显示大量 `D` (删除) + 新增。这是文件级重命名的正常结果，非预期数据丢失。所有关键文件已验证存在于目标模块中。

## 2026-06-14 剩余优化执行方案

### Phase 7: SettingsViewModel Facade — 已完成
- 目标: 将 `SettingsViewModel` 的 Repository 注入从 7 个聚合为 1 个 `SettingsRepositoryFacade`。
- 变更:
  - 新增 `SettingsRepositoryFacade`，聚合 Question/WrongBook/History/Favorite/Analysis/Ask/Note repositories。
  - `SettingsViewModel` 改为通过 facade 访问各 repository。
- 结果: 构造函数注入项从 12 降至 6，回到 <=8 红线内。

### Phase 8: ExamViewModel Coordinator 拆分 — 已完成
- 目标: 将 `ExamViewModel` 中仍内嵌的编辑、评分、统计职责拆出。
- 变更:
  - 新增 `ExamQuestionEditCoordinator`，承接 `prepareEditableQuestion`、`clearEditableQuestion`、`normalizeEditedSelectedOptions`、`saveEditedQuestion`。
  - 新增 `ExamGradeCoordinator`，承接 `scheduleGradeExamAfterDispose`、`gradeExam`、`calculateElapsedTime`。
  - 新增 `ExamStatisticsCoordinator`，承接 `calculateCumulativeStats`、`incrementExamCount`。
- 结果: `ExamViewModel` 中编辑、评分、统计职责已改为委托；当前约 590 行。

### Phase 9: FontSettingsDataStore 泛型代理 — 已完成
- 目标: 降低 DataStore get/set 样板代码。
- 变更:
  - 新增 `PreferenceDelegate<T>` 与 `BooleanPreferenceDelegate`。
  - 在 `FontSettingsDataStore` 内使用代理实现原有 getter/setter。
  - 保留现有公开 API，避免调用方大规模迁移。
- 结果: `FontSettingsDataStore` 当前约 247 行；设置读写逻辑集中，外部公开 API 不变。

### Phase 7-9 验证
- IDE lints: 无错误。
- Compile: `.\gradlew.bat :app:compileDebugKotlin :feature-exam:compileDebugKotlin` ✅ BUILD SUCCESSFUL。
- 已知剩余警告: `HomeScreen` 的 Material `FractionalThreshold` deprecated warning，非本轮改动引入。
