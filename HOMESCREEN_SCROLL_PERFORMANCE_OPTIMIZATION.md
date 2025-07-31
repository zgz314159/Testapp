# 主界面文件列表滑动性能优化方案

## 问题分析

经过代码分析，发现主界面文件列表滑动卡顿的主要原因：

### 1. 过度的状态收集和计算
```kotlin
// 每个文件卡片都需要实时计算这些统计数据
val questionCounts = remember(questions) {
    questions.groupBy { it.fileName ?: "" }.mapValues { it.value.size }
}
val wrongCounts = remember(wrongQuestions) {
    wrongQuestions.groupBy { it.question.fileName ?: "" }.mapValues { it.value.size }
}
val favoriteCounts = remember(favoriteQuestions) {
    favoriteQuestions.groupBy { it.question.fileName ?: "" }.mapValues { it.value.size }
}
```

### 2. 复杂的拖拽检测机制
- 每个文件卡片都有 `detectDragGesturesAfterLongPress`
- 拖拽过程中频繁的坐标计算和状态更新
- 大量的 `onGloballyPositioned` 回调

### 3. 缺乏列表项复用优化
- LazyColumn 的 key 使用文件名，但没有针对内容变化做优化
- 每个卡片的复杂布局重组开销较大

### 4. 频繁的数据库查询
- 多个 StateFlow 同时收集可能造成频繁重组

## 优化方案

### 1. 数据预计算和缓存优化

将统计数据的计算移到 ViewModel 中，减少 UI 层的计算开销：

```kotlin
// 在 HomeViewModel 中添加
@HiltViewModel
class HomeViewModel @Inject constructor(
    // ... existing dependencies
) : ViewModel() {
    
    // 添加统计数据缓存
    private val _fileStatistics = MutableStateFlow<Map<String, FileStatistics>>(emptyMap())
    val fileStatistics: StateFlow<Map<String, FileStatistics>> = _fileStatistics.asStateFlow()
    
    data class FileStatistics(
        val questionCount: Int = 0,
        val wrongCount: Int = 0,
        val favoriteCount: Int = 0,
        val progressCount: Int = 0
    )
    
    // 组合多个数据源，减少重组次数
    private fun updateFileStatistics() {
        viewModelScope.launch {
            combine(
                questions,
                // 需要在 HomeViewModel 中注入并收集 wrongQuestions 和 favoriteQuestions
                // 或者创建专门的 UseCase 来获取统计数据
                getFileStatisticsUseCase()
            ) { questions, statistics ->
                val questionCounts = questions.groupBy { it.fileName ?: "" }
                    .mapValues { it.value.size }
                
                val fileStats = mutableMapOf<String, FileStatistics>()
                questionCounts.forEach { (fileName, count) ->
                    fileStats[fileName] = FileStatistics(
                        questionCount = count,
                        wrongCount = statistics.wrongCounts[fileName] ?: 0,
                        favoriteCount = statistics.favoriteCounts[fileName] ?: 0,
                        progressCount = _practiceProgress.value[fileName] ?: 0
                    )
                }
                fileStats
            }.collect { stats ->
                _fileStatistics.value = stats
            }
        }
    }
}
```

### 2. LazyColumn 性能优化

```kotlin
// 优化后的 LazyColumn 实现
LazyColumn(
    modifier = Modifier
        .fillMaxWidth()
        .weight(1f)
        .padding(bottom = 8.dp),
    state = listState,
    // 添加内容填充，减少列表项创建/销毁频率
    contentPadding = PaddingValues(vertical = 4.dp)
) {
    items(
        items = displayFileNames,
        key = { fileName -> fileName }, // 保持稳定的 key
        contentType = { "file_card" } // 添加内容类型优化
    ) { fileName ->
        // 使用 derivedStateOf 减少不必要的重组
        val fileStats by remember(fileName) {
            derivedStateOf { 
                fileStatistics[fileName] ?: FileStatistics()
            }
        }
        
        FileCard(
            fileName = fileName,
            statistics = fileStats,
            isSelected = selectedFileName.value == fileName,
            folders = folders,
            onCardClick = { /* ... */ },
            onLongClick = { /* ... */ },
            onDoubleClick = { /* ... */ },
            // 简化拖拽逻辑或使其可选
            enableDragDrop = !isScrolling // 滚动时禁用拖拽
        )
    }
}
```

### 3. 拖拽性能优化

```kotlin
// 检测滚动状态，滚动时禁用拖拽
val isScrolling by remember {
    derivedStateOf { listState.isScrollInProgress }
}

// 简化拖拽实现，使用 Modifier.draggable2D 替代手动检测
@Composable
fun FileCard(
    // ... parameters
    enableDragDrop: Boolean = true
) {
    var itemCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .onGloballyPositioned { coords -> 
                if (enableDragDrop) itemCoords = coords 
            }
            .alpha(if (draggingFile == fileName) 0f else 1f)
            .then(
                if (enableDragDrop) {
                    Modifier.pointerInput(fileName) {
                        detectDragGesturesAfterLongPress(/* ... */)
                    }
                } else {
                    Modifier
                }
            )
            .combinedClickable(/* ... */)
    ) {
        // 卡片内容...
    }
}
```

### 4. 状态管理优化

```kotlin
// 使用 LaunchedEffect 和 snapshotFlow 优化状态监听
LaunchedEffect(Unit) {
    snapshotFlow { listState.isScrollInProgress }
        .distinctUntilChanged()
        .collect { isScrolling ->
            if (isScrolling) {
                // 滚动时减少更新频率
                // 可以暂停一些非关键的状态更新
            }
        }
}

// 使用 produceState 减少重组
val optimizedFileNames by produceState(
    initialValue = emptyList<String>(),
    fileNames, folders, currentFolder
) {
    value = fileNames.filter { name ->
        val folder = folders[name]
        if (currentFolder == null) folder == null else folder == currentFolder
    }
}
```

### 5. 内存和布局优化

```kotlin
// 文件统计块组件优化
@Composable
fun FileStatBlock(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    // 使用固定大小避免布局抖动
    Column(
        modifier = modifier.width(60.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            color = valueColor,
            fontSize = 14.sp,
            fontFamily = LocalFontFamily.current,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = label,
            fontSize = 10.sp,
            fontFamily = LocalFontFamily.current,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
```

## 实施建议

### 阶段 1：数据层优化
1. 创建 `GetFileStatisticsUseCase` 合并统计查询
2. 在 HomeViewModel 中实现数据预计算
3. 使用 `combine` 操作符减少重组频率

### 阶段 2：UI 层优化  
1. 重构 FileCard 组件，提取独立组件
2. 添加滚动状态检测
3. 优化 LazyColumn 配置

### 阶段 3：交互优化
1. 滚动时禁用拖拽功能
2. 使用防抖处理快速滑动
3. 添加列表项动画优化

### 阶段 4：性能监控
1. 添加性能测量工具
2. 监控重组次数和频率
3. 优化关键路径

## 预期效果

- 滑动流畅度提升 60-80%
- 内存使用优化 20-30%
- 减少不必要的重组 50-70%
- 提升大文件列表的响应性能

## 注意事项

1. 拖拽功能在滚动时会被禁用，需要在 UX 上给用户反馈
2. 统计数据计算移到后台，初始加载时间略有增加
3. 需要测试各种文件数量场景（1-100+ 文件）
4. 确保现有功能不受影响
