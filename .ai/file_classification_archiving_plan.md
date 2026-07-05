<!--
  生成时间: 2026-06-14 09:50 UTC+8 → 更新: 2026-06-14 12:56 UTC+8
  依据: 散落状态诊断 + Gradle 配置分析 + 文件功能分析
  前置条件: 已确认 69 个文件在两模块间重复存在
-->

# 文件归类归档方案

---

## 当前状态概述

| 模块 | settings.gradle.kts | build.gradle.kts | 实际文件 |
|------|---------------------|-----------------|----------|
| **:app** | ✅ 已注册 | ✅ | ~216 文件（Screen/VM/UseCase/DI/UI/DataStore/Network/Util） |
| **:domain** | ✅ 已注册 | ✅ (JVM pure) | 29 个文件（model + repository 接口 + util + constants） |
| **:data** | ✅ 已注册 | ✅ (Android Library) | 58 个文件（Entity/DAO/Database/RepositoryImpl/Parser/Mapper/DI） |

**核心问题已解决**: `:domain` 和 `:data` 模块已注册到 `settings.gradle.kts`，`:app` 已依赖它们。所有文件已按层转移到对应模块，编译通过。

---

## 归档原则

```
Rule 1: 每个文件只在一处存在（消除重复）
Rule 2: 遵守现有 Gradle 模块分层（app → data → domain）
Rule 3: :domain 模块是纯 JVM（无 Android 依赖），不可移入任何 import android.* 的文件
Rule 4: :data 模块是 Android Library，可含有 Room/POI 等 Android 依赖
Rule 5: presentation/ (Screen/VM/Coordinator) 全部留在 :app
Rule 6: 每一步独立可编译通过
```

---

## Phase 1 ✅ 已执行 — 模块接线（Gradle 层，文件 0 移动）

settings.gradle.kts 已 include `:domain` 和 `:data`；app/build.gradle.kts 已添加 `implementation(project(":domain"))` 和 `implementation(project(":data"))`。编译验证通过。

---

## Phase 2 ✅ 已执行 — data/ 层归位

所有文件（Entity/TypeConverter/DAO/AppDatabase/RepositoryImpl/Mapper/Parser/DI）已迁移到 `data/` 模块。

### 确认归位后的 :data 模块结构
```
data/src/main/java/com/example/testapp/data/
├── di/DataModule.kt
├── init/QuestionDataInitializer.kt
├── local/
│   ├── AppDatabase.kt
│   ├── dao/          (12 DAO)
│   └── entity/
│       ├── (12 Entity)
│       └── converter/ (6 Converter)
├── mapper/Mappers.kt
├── mappers/ExamHistoryMappers.kt
└── repository/
    ├── (10 RepositoryImpl)
    ├── ExportConstants.kt
    ├── ImportExceptions.kt
    ├── MarkdownNormalizer.kt
    ├── MetadataManager.kt
    └── parser/ (8 Parser)
```

---

## Phase 3 ✅ 已执行 — domain/ 层归位

所有文件（model/repository 接口/util/constants）已迁移到 `domain/` 模块。

### 确认归位后的 :domain 模块结构
```
domain/src/main/kotlin/com/example/testapp/domain/
├── IOConstants.kt
├── LocalizedException.kt
├── ParsingConstants.kt
├── QuestionTypes.kt
├── model/          (12 Domain Models)
├── repository/     (11 Repository Interfaces)
└── util/           (DomainAnswerUtils.kt + MarkdownDomainNormalizer.kt)
```

---

## Phase 4 ✅ 已执行 — 清理重复文件 + 删除遗留文件

- ✅ `app/src/.../domain/model/` 和 `app/src/.../domain/repository/` 中已迁移文件的副本已删除
- ✅ `app/src/.../data/local/` 和 `app/src/.../data/repository/` 下已迁移的文件已删除
- ✅ `FontSettingsDataStore_fixed.kt` 已删除
- ✅ `app/.../data/mapper/` 副本已删除
- ✅ `QuestionUiModel.kt` 已移到 `app/.../presentation/model/`

**保留在 :app 的 domain/usecase/**: 共 37 个 UseCase + 1 个异常的 QuestionAnalysisRepository.kt

**保留在 :app 的 data/**:
```
app/src/.../data/datastore/FontSettingsDataStore.kt
app/src/.../data/network/baidu/BaiduApiService.kt
app/src/.../data/network/deepseek/DeepSeekApiService.kt
app/src/.../data/network/spark/SparkApiService.kt
```

---

## Phase 5 ✅ 已执行 — presentation/screen/ 内部归类

所有散落在根目录的 60+ 个文件已按功能分类到子目录。

### 当前 :app 模块最终目录结构

```
app/src/main/java/com/example/testapp/
├── App.kt / MainActivity.kt
│
├── data/
│   ├── datastore/FontSettingsDataStore.kt
│   └── network/
│       ├── baidu/BaiduApiService.kt
│       ├── deepseek/DeepSeekApiService.kt
│       └── spark/SparkApiService.kt
│
├── di/
│   ├── AppModule.kt
│   ├── FileUseCaseModule.kt
│   ├── NetworkModule.kt
│   ├── NetworkUseCaseModule.kt
│   ├── PersistenceUseCaseModule.kt
│   └── UseCaseModule.kt
│
├── domain/
│   └── usecase/ (37 UseCase + 1 异常文件)
│
├── presentation/
│   ├── model/QuestionUiModel.kt
│   ├── navigation/AppNavHost.kt, AppNavRoutes.kt
│   └── screen/
│       ├── home/HomeScreen.kt, HomeViewModel.kt, HomeProgressRedoHandler.kt
│       ├── exam/ (ExamScreen.kt, ExamViewModel.kt + 15 个辅助文件)
│       ├── practice/ (PracticeScreen.kt, PracticeViewModel.kt + 18 个辅助文件)
│       ├── wrongbook/WrongBookScreen.kt, WrongBookViewModel.kt
│       ├── favorite/FavoriteScreen.kt, FavoriteViewModel.kt
│       ├── history/HistoryScreen.kt, HistoryViewModel.kt
│       ├── note/NoteScreen.kt
│       ├── question/QuestionScreen.kt
│       ├── questionbank/ (5 个文件)
│       ├── ai/ (DeepSeek, Spark, Baidu 各系列 Screen + ViewModel, 共 11 个)
│       ├── file/ (FileFolderViewModel.kt, DragDropViewModel.kt, QuizFileBrowser.kt, ...)
│       ├── settings/ (SettingsScreen.kt, SettingsViewModel.kt, Coordinators, ui/ 面板, 共 15+ 个)
│       └── components/ (50+ 共享 UI 组件)
│
├── ui/theme/ (Color.kt / Theme.kt / Type.kt)
│
└── util/
    ├── AnswerUtils.kt
    ├── FileNameUtils.kt
    ├── FillQuestionTransformUtils.kt
    ├── MarkdownFormatNormalizer.kt
    ├── QuestionFormatter.kt
    ├── SafeUrlDecoder.kt
    └── SoundEffects.kt
```

---

## Phase 6（可选）— 未来可继续优化的方向

| 项目 | 说明 | 优先级 |
|------|------|--------|
| UseCase 下沉 | 将纯逻辑 UseCase 迁移到 `:domain`，需要 :domain 添加 Hilt 依赖 | 🟢 LOW |
| Room/POI 依赖从 app 移除 | Room ✅ P79；POI ✅ P78 | ✅ |
| `domain/usecase/QuestionAnalysisRepository.kt` | 已重命名为 `QuestionAnalysisUseCases.kt` | ✅ P79 |
| 删除 `app/.../util/` 中已下沉到 :domain 的重复函数 | 如 AnswerUtils.kt 中部分函数与 DomainAnswerUtils.kt 重复 | 🟢 LOW |

---

## 最终验证

```
./gradlew :app:compileDebugKotlin --no-build-cache → BUILD SUCCESSFUL ✅
```

---

## 回滚策略

```
Phase 1 回滚:  git checkout settings.gradle.kts app/build.gradle.kts
Phase 2 回滚:  git checkout -- app/src/main/java/com/example/testapp/data/
Phase 3 回滚:  git checkout -- app/src/main/java/com/example/testapp/domain/
全局回滚:      git stash && git clean -fd data/src/ domain/src/
```
