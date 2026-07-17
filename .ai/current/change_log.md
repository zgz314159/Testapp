# Change Log

> 记录各 Phase 的主要变更。
> 格式：`YYYY-MM-DD | Phase-N | 描述`

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