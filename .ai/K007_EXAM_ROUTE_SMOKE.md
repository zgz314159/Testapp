# K-007 考试路由设备冒烟清单

> **ID:** K-007 · **优先级:** MEDIUM  
> **目的:** 验证 Exam 经 `SessionHost` / `ExamExamRoute` 路由后的导航、交卷、复盘重入在真机上稳定。  
> **关系:** 可并入 [K001_DEVICE_SMOKE.md](K001_DEVICE_SMOKE.md) C 段；本清单为 Exam 专项扩展。

---

## 前置条件

| # | 项 | 说明 |
|---|-----|------|
| P1 | 构建 | `.\gradlew assembleDebug` 成功 |
| P2 | 测试题库 | 至少 1 个已导入题库（≥5 题） |
| P3 | 设置 | 记录当前考试题量 / 随机 / 记忆模式设置 |
| P4 | K-001 | 建议先完成 K-001 A/B 段（首页 + Browse 基线稳定） |

**记录：** 设备型号 / Android 版本 / 构建号 / 测试文件名 `____________`

---

## E1. 整卷考试入口（ExamExamRoute）

| Step | 操作 | 预期 | Pass |
|------|------|------|------|
| E1.1 | 首页 → 文件卡片 → 选「考试」 | 进入 `ExamScreen`，加载正常 | ☐ |
| E1.2 | 作答 1 题（选题 / 填空提交） | 显示结果或进入下一流程，无崩溃 | ☐ |
| E1.3 | 底栏 ← / →（未答模式） | 仅在未答题间跳转；**不**浏览已答历史 | ☐ |
| E1.4 | 左右滑（非复盘） | 顺序 `prev/nextSequential`；边缘滑出触发退出/交卷提示 | ☐ |
| E1.5 | 退出考试 → 回首页 | 无崩溃；再次进入同文件考试进度合理 | ☐ |

**路由核对：** `exam/{quizId}` → `ExamExamRoute` → `SessionHost(Exam)`。

---

## E2. 错题本 / 收藏考试入口

| Step | 操作 | 预期 | Pass |
|------|------|------|------|
| E2.1 | 错题本 → 选文件 → 考试 | `ExamExamRoute(wrongBookFileName=…)` 正常 | ☐ |
| E2.2 | 收藏 → 选文件 → 考试 | `ExamExamRoute(favoriteFileName=…)` 正常 | ☐ |

---

## E3. 复盘模式（Review Exam）

| Step | 操作 | 预期 | Pass |
|------|------|------|------|
| E3.1 | 完成一次考试并进入复盘 | `ExamExamRoute(reviewProgressId=…, isReviewMode=true)` | ☐ |
| E3.2 | 底栏 ← / → | `PrevQuestion` / `NextQuestion` 浏览历史 | ☐ |
| E3.3 | 左右滑已答历史 | `BrowseAnsweredHistory*` 经 Command；边界 Toast | ☐ |
| E3.4 | 复盘返回 | 回到结果页 / 上一屏，无交卷弹窗 | ☐ |

---

## E4. Command 路径快检（P50 后）

| Step | 操作 | 预期 | Pass |
|------|------|------|------|
| E4.1 | 考试底栏 ← 点击 | 经 `SessionCommand.NavPrevIcon`（logcat 无异常） | ☐ |
| E4.2 | 题号列表点选跳转 | 经 `SessionCommand.GoToQuestion` | ☐ |
| E4.3 | 答后自动前进 | 经 `SessionCommand.NextQuestion` | ☐ |

---

## 执行记录

| 日期 | 执行人 | E1 | E2 | E3 | 结论 |
|------|--------|----|----|-----|------|
| | | ☐ | ☐ | ☐ | |

**FAIL 时必填：** 步骤编号、截图/录屏、logcat 片段。

---

## 通过后动作

1. `tech_debt.md` — K-007 → Closed  
2. `current_state.md` — 移除 K-007 待办  
3. `.ai/change_log.md` — VERIFY 条目  

---

## 相关单测（CI，不能替代本清单）

```bash
./gradlew :app:testDebugUnitTest --tests "com.example.testapp.presentation.session.*"
./gradlew :app:testDebugUnitTest --tests "com.example.testapp.core.session.*"
```
