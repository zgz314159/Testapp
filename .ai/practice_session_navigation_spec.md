# 练习答题页：底栏箭头与横滑导航规格

> **维护约定：** 修改 `NavigationController`、`PracticeFullAnswer*Pipeline`、`NavigationHistory`、`PracticeFullAnswerHistoryNavigation` 或 `PracticeScreen` 手势前，必须先对照本文；改完后同步更新本文与 `change_log.md`。

---

## 1. 术语

| 术语 | 含义 |
|------|------|
| **轮次池（单击 step1）** | 当前词条 + 当前轮次号（`PracticeFullAnswerSourceRoundPoolPipeline`） |
| **全局轮次号池** | 同轮次跨词条（`PracticeFullAnswerRoundPoolPipeline`），仅出池守卫/历史 |
| **多轮全答** | 会话含第 2 轮及以上衍生题（`FullAnswerMultiRoundSessionPipeline`） |
| **单轮/非多轮全答** | 仅第 1 轮，或普通填空题库，无多轮衍生 |
| **词条** | `extractSourceQuestionId` 相同的源题组 |
| **Pending** | 全答：`isQuestionPendingForCurrentMode`；轮次池内另用 `PracticeFullAnswerRoundSlotPendingPipeline` |

---

## 2. 随机 / 顺序来源

| 场景 | 顺序/随机开关 |
|------|----------------|
| 全答模式底栏箭头 | 设置 → 全答轮次顺序 `fillFullAnswerRandomOrder` |
| 普通练习底栏箭头 | 设置 → 随机练习 `randomPractice` |
| 管道 | `PracticeFullAnswerIconNavOrderPipeline.usesRandomOrder` |

---

## 3. 底栏 ← / → 图标（单击）

### 3.1 多轮全答（有几轮的题库池）

**策略：** `FullAnswerIconTapStrategy.MULTI_ROUND_POOL_FIRST`

**单击决策链（Phase 31，严格顺序）：**

1. **同源同轮 pending** → 池内多题时互相跳转；**仅 1 题且无法移动时 fall through**，不弹边界 Toast。
2. **同源其他轮次** — 当前轮无法移动或已无 pending 时进入（**词条尚未任一轮输入时跳过**，见 step0）。
3. 当前词条各轮均无 pending → **跨词条**（`skipToUnansweredSource`）；同源已输入但未完成时硬阻断。

**step0 未触碰词条：** 同源各轮均无输入 → 单击直接 **跨词条**（`PracticeFullAnswerSourceTouchPipeline`）。

~~3. 相邻轮次号池（已移除：会在未完成当前词条时跨词条跳入全局第 N±1 轮）~~

**轮次池 pending 判定**（`PracticeFullAnswerRoundSlotPendingPipeline`）：
   - **无输入** → pending
   - **全答（不要求全对）：** 有输入 → 已完成
   - **须全对：** 有输入未批改 → 已完成；批改后答错 → pending

### 3.2 单轮 / 非多轮全答，或普通练习

**策略：** `GLOBAL_UNANSWERED_FIRST`

- 单击在**全库未作答题**间跳转（顺序环绕 / 随机），不按轮次池约束。
- 全答单轮：仍用 `PracticeFullAnswerIconUnansweredPipeline` + 全答随机设置。
- 普通练习：`PracticeUnansweredNavigation` + `randomPractice`。

### 3.3 底栏 ← / → 图标（双击，仅全答）

- **任意全答会话：** 双击强制 **跨词条**（`skipToUnansweredSource(forceCrossSource=true)`），不受同源 pending 守卫约束。

---

## 4. 横滑屏幕（已答历史浏览）

与底栏箭头**职责分离**：横滑**不**跳未作答题，只浏览**已批改/已提交**的历史快照。

| 手势 | 方向 | 行为 |
|------|------|------|
| 右滑 | Older | `browseAnsweredHistoryOlder` — 更早的已作答题 |
| 左滑 | Newer | `browseAnsweredHistoryNewer` — 更新的已作答题；到最新恢复现场 |

**触发条件：** `QuestionSessionHistorySwipePipeline` — `|Δx| > 100` 且 `|Δx| > 1.5 × |Δy|`（避免纵向滚动误触）。

**全答多轮：** `PracticeFullAnswerHistoryNavigation` — 同词条轮次池内优先按作答时间排序；池边界再沿全局时间跨词条。

**日志 TAG：** `PracticeHistorySwipe`

### 3.4 底栏图标导航排查日志（Phase 30+）

**Logcat 过滤：**

```bash
adb logcat -s PracticeIconNav
```

**TAG：** `PracticeIconNav`（`PracticeFullAnswerIconNavDebugLog`）

| 日志前缀 | 含义 |
|----------|------|
| `entry \| VM.*` | ViewModel 收到单击，含 idx / textLen / showResult |
| `strategy` | fullAnswer / multiRound / tap 策略名 |
| `roundPool` | 当前轮次池 indices、每槽 pending / input / showResult |
| `branch \| step1_inRoundPool` | 池内 pending 导航 |
| `branch \| step1` pendingInRound EMPTY | 认为轮次池已无 pending，尝试出池 |
| `branch \| step2_sameSourceOtherRound` | 同词条换轮 |
| `branch \| step4` | 跨词条 skipSource |
| `skipSource BLOCKED` | 池内仍有 pending，跨词条被硬阻断 |
| `navigate \| N->M` | 实际 index 变更及原因 |
| `result` | 最终 Navigated / AtLastUnanswered 等 |

**排查要点：** 若 `textLen=0` 但 UI 已有输入 → session 未同步；若 `strategy=GLOBAL_UNANSWERED_FIRST` 但题库有多轮 → `isMultiRoundSession` 判定有误；若 `pendingInRound` 为空但用户认为未答完 → 看 `roundPool` 各槽 `slotPending` / `input`。

---

## 5. 管道与编排文件索引

```
单击决策（多轮）
  PracticeFullAnswerRoundIconNavPipeline（池内 pending 优先，有 pending 不出池）
    → PracticeFullAnswerSameSourceRoundAdvancePipeline（同词条换轮）
    → PracticeFullAnswerNextRoundPoolPipeline（换轮次号）
    → skipToUnansweredSource（跨词条，池内仍有 pending 时硬拒绝）
```

---

## 6. 禁止行为（回归检查清单）

- [ ] 多轮全答：当前题已有输入、池内仍有未输入题时，单击不得跨词条（`skipToUnansweredSource` 须被阻断）
- [ ] 多轮全答：单源第 1 轮完成后应进同词条第 2 轮，而非直接跨词条
- [ ] 多轮全答：仅 `showResult` 不应视为轮次完成（全答模式应以**有输入**为准）
- [ ] 单轮全答：不得误用整库轮次池锁死导航
- [ ] 全答箭头不得误用练习 `randomPractice` 代替 `fillFullAnswerRandomOrder`
- [ ] 横滑不得跳转未作答题（与底栏箭头不重叠）
