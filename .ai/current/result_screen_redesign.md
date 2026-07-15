# 结果详情页改版（2026-07-15）

## 范围

结果页仅重构 `:feature-practice` 的 Compose 展示层，继续使用原有 `ResultViewModel`、历史记录查询、导航参数、答题详情路由和历史记录 BottomSheet。未修改数据库、题目判定、练习/考试提交及历史写入逻辑。

## 页面结构

1. `ResultTopBar`：返回与“练习结果”标题。
2. `ResultQuestionBankHeader`：当前题库文件名。
3. `ResultCurrentScoreCard`：本次练习/考试环形正确率与答对、答错、未答、正确率四项指标。
4. `ResultOverallScoreCard`：累计半环仪表与累计答对、累计答错、累计次数、累计正确率。
5. `ResultAccuracyChartSection`：仅展示当前题库最近 9 次记录；少于 2 次时显示空状态；保留历史记录入口。
6. `ResultBottomActionBar`：固定“返回首页”和“答题详情”操作。

## 数据口径

- `本次已答 = 总题数 - 未答`。
- `本次答错 = 本次已答 - 本次答对`。
- 页面保证 `答对 + 答错 + 未答 = 总题数`，异常输入统一钳制到有效范围。
- 本次正确率的分母是本次已答题数；累计正确率的分母是累计作答数，不与题库题目总量混用。
- 百分比统一限制在 `0..100`，最多保留两位小数并去掉无意义的尾零。
- 趋势按时间排序，只取当前题库最近 9 次，单次正确率分母为该次实际作答数。

## 视觉与适配

- 页面背景 `#F6F8FC`，主色 `#2F66F3`，卡片白色，统一 24dp 圆角与轻边框/阴影。
- 正确、错误、未答和主指标分别使用绿、红、中性灰和蓝色语义色。
- 环形、半环和折线图使用 600–700ms 入场动画。
- 指标区通过 `BoxWithConstraints` 在窄屏切换为纵向布局；可见文本承载核心信息，装饰图标不重复朗读。

## 验证

- `ResultDisplayStatsDashboardTest` 覆盖题数守恒、异常钳制、0/50/100%、无历史记录、最新 9 次和趋势范围。
- `./gradlew ktlintCheck` 通过。
- `./gradlew :feature-practice:testDebugUnitTest` 通过。
- `./gradlew :app:assemblePerformance` 通过。

## 关键文件

- `presentation/screen/result/ResultScreen.kt`
- `presentation/screen/result/ResultDisplayStats.kt`
- `presentation/screen/result/components/ResultDashboardTokens.kt`
- `presentation/screen/result/components/ResultDashboardHeader.kt`
- `presentation/screen/result/components/ResultScoreCards.kt`
- `presentation/screen/result/components/ResultAccuracyChart.kt`
- `presentation/screen/result/components/ResultBottomActionBar.kt`
