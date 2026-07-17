# Result Screen Redesign

> 最后更新：2026-07-17

## 一、统计口径

### 核心纯函数

```kotlin
// domain/src/main/kotlin/.../domain/model/ResultHistoryRecordStats.kt
data class ResultHistoryRecordStats(
    val answered: Int,
    val correct: Int,
    val wrong: Int,
    val rate: Double,
    val rateText: String,
)

fun calculateResultHistoryRecordStats(record: HistoryRecord): ResultHistoryRecordStats
```

计算规则：

```
safeTotal = record.total.coerceAtLeast(0)
unanswered = record.unanswered.coerceIn(0, safeTotal)
answered = safeTotal - unanswered
correct = record.score.coerceIn(0, answered)
wrong = answered - correct
rate = if (answered > 0) correct.toDouble() / answered else 0.0
```

### 百分比格式化

```kotlin
fun formatResultPercent(rate: Double): String
```

- 范围 0～100%
- 最多两位小数，去除尾零
- 示例：`0%`、`50%`、`58.33%`、`100%`

### 使用方

折线图（`AccuracyLineChart`）和历史列表（`ResultHistorySheet`）全部使用 `calculateResultHistoryRecordStats`。

---

## 二、响应式断点

| 条件 | 布局 |
|------|------|
| `maxWidth >= 330dp`（normal） | 半圆仪表左（128dp）+ 2x2 指标右 |
| `maxWidth < 330dp`（窄屏）或 `fontScale > 1.2` | 仪表居中 + 2x2 指标下方纵向排列 |

`BoxWithConstraints` + `maxWidth < 330.dp` 检测。

---

## 三、指标卡（MetricTile）

```
Column（最小高度 68dp，背景圆角 12dp）
├─ Row：18～20dp 图标 + 10～11sp 标签
└─ 17sp Bold 数值（完整显示，不省略）
```

### 指标内容

累计卡：
- 累计答对（绿色）
- 累计答错（红色）
- 累计次数 / 累计考试（蓝色）
- 累计正确率（蓝色）

本次卡：
- 答对（绿色）
- 答错（红色）
- 未答（灰色）
- 正确率（蓝色）

---

## 四、半圆仪表（SemiCircularGauge）

```
Box（128～136dp 宽 × 112～120dp 高, contentDescription 已设置）
├─ Canvas：半圆轨道（13dp Stroke） + 进度弧
└─ Column（align=Center, paddingTop=12dp）
   ├─ "7/12"（26sp Bold）
   ├─ "58.33%"（14sp Blue SemiBold）
   └─ "累计正确率"（10sp Gray）
```

- contentDescription：`"累计正确率58.33%，答对7题，共作答12题"`
- 0%、50%、100% 三态正确绘制
- 文字始终位于半圆内部，不与弧线相交

---

## 五、历史弹层（ResultHistorySheet）

```
AppLazyBottomSheet（heightFraction 动态：0.58～0.82）
└─ Column（fillMaxSize）
   ├─ Text "历史成绩记录"（titleMedium, Bold）
   └─ LazyColumn（weight=1f, spacing=10dp）
      └─ HistoryRecordCard（圆角 14dp, 背景 #F7F9FD）
         ├─ Row: "第 N 次" | "yyyy-MM-dd HH:mm"
         └─ Row: 答对 N（绿）, 答错 N（红）, 正确率 N%（蓝）
```

- 标题与第一条记录不重叠（Column 结构替代 Box + LazyColumn 并列）
- 最多显示 6 条后滚动
- 空状态：保留原有 `AppEmptyStateInline`

---

## 六、折线图（AccuracyLineChart）

| 参数 | 值 |
|------|-----|
| 左侧边距 | 42dp |
| 右侧边距 | 16dp |
| 第一个点标签 | `Paint.Align.LEFT`，`x + 5dp` |
| 最后一个点标签 | `Paint.Align.RIGHT`，`x - 5dp` |
| 数据点 Y 标签 | `coerceAtLeast(top + 8dp)` |
| 百分比格式 | `ResultHistoryRecordStats.rateText`，保留 `58.33%` |
| 横轴序号 | 保留真实历史顺序（如第4次～第12次）|
| 数据限制 | 最近 9 条 |

---

## 七、本次成绩卡

- 环形仪表 132～138dp（Stroke 12dp）
- 奖杯图标移至标题行右侧（22dp），不再独占 54dp 垂直空间
- 四个指标完整显示：答对 + 答错 + 未答 + 正确率
- 窄屏时纵向布局

---

## 八、题库标题 & 底部操作栏

### 题库标题

- 使用 `HomeFileTypeVisualPipeline` 解析文件类型图标（xlsx/docx/填空/判断等）
- 文件名最多两行，`maxLines=2`
- 移除无点击行为的 `ChevronRight` 箭头

### 底部操作栏

- 按钮高度调整为 48dp
- 垂直内边距 10dp
- 保留 `navigationBarsPadding()` + Scaffold insets

---

## 九、文件清单

### 新增

| 文件 | 内容 |
|------|------|
| `domain/.../model/ResultHistoryRecordStats.kt` | 纯函数统计入口 |

### 修改

| 文件 | 变更 |
|------|------|
| `feature-practice/.../result/ResultDisplayStats.kt` | 改用 domain 统计，新增 `accuracyList` 使用 `calculateResultHistoryRecordStats` |
| `feature-practice/.../result/ResultHistorySheet.kt` | Column 结构 + 独立卡片 |
| `feature-practice/.../result/ResultScreen.kt` | 透传 `historyRecords` 给折线图 |
| `feature-practice/.../result/components/ResultScoreCards.kt` | 重构半圆仪表、指标卡、奖杯位置、响应式布局 |
| `feature-practice/.../result/components/ResultAccuracyChart.kt` | 边距调整、标签对齐、百分比精度 |
| `feature-practice/.../result/components/ResultDashboardHeader.kt` | 使用 `HomeFileTypeVisualPipeline`，去掉假箭头 |
| `feature-practice/.../result/components/ResultBottomActionBar.kt` | 按钮高度 48dp |
| `ui-common/.../design/AppLazyBottomSheet.kt` | 增加 `heightFraction` 参数 |
| `ui-common/.../screen/result/ResultHistoryLinePipeline.kt` | 改用 domain 统计 |
| `feature-practice/.../res/values/strings.xml` | 新增 5 条字符串 |
| `feature-practice/.../test/result/ResultDisplayStatsDashboardTest.kt` | 9 项单元测试 |

---

## 十、验证结果

| 检查项 | 结果 |
|--------|------|
| ktlintCheck | ✅ PASS |
| `:feature-practice:testDebugUnitTest` | ✅ 48 tests, 0 failures |
| `:app:assemblePerformance` | ✅ BUILD SUCCESSFUL |
| LOC >500 | 无新增 |