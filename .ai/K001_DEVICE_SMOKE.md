# K-001 设备冒烟清单

> **ID:** K-001 · **优先级:** HIGH  
> **目的:** 验证首页进度聚合与抽屉单题（atomic-bank / Browse）重入在真机上稳定。  
> **门禁:** 通过前避免大规模手势/导航行为变更（见 `current_state.md`）。  
> **自动化辅助:** `baseline-profile/.../HomeJourney.kt`（`home_file_card:` 语义）

---

## 前置条件

| # | 项 | 说明 |
|---|-----|------|
| P1 | 构建 | `.\gradlew assembleDebug` 成功 |
| P2 | 测试题库 | 至少 1 个已导入 `.xlsx` / `.json` 题库（≥5 题） |
| P3 | 设置 | 关闭随机/记忆模式（或记录当前设置并在用例中保持一致） |
| P4 | 清场（可选） | 设置 → 清除该题库练习进度，便于观察进度从 0 增长 |

**记录：** 设备型号 / Android 版本 / 构建号 / 测试文件名 `____________`

---

## A. 首页卡片持久化（Home card persistence）

验证 `preferredHomePracticeProgress` / scoped `practice_*` 进度在回首页后正确显示。

| Step | 操作 | 预期 | Pass |
|------|------|------|------|
| A1 | 冷启动进入首页 | 文件列表加载；目标卡片「进度」数字可见 | ☐ |
| A2 | 点击文件卡片 → 选「练习」→ 进入 `PracticeScreen` | 正常加载，无卡在 loading | ☐ |
| A3 | 作答 **≥2 题**（提交/揭示结果） | 当前题显示已答状态 | ☐ |
| A4 | 按系统返回或顶栏退出 → 回首页 | 回到同一文件列表 | ☐ |
| A5 | 查看该文件卡片「进度」计数 | **≥2**（与 A3 作答数一致，非 0） | ☐ |
| A6 | 完全杀掉 App → 冷启动 → 再看同卡片 | 进度 **仍 ≥2**（持久化） | ☐ |
| A7 |（可选）改设置题量/随机后再练同文件 | 首页进度仍聚合正确（scoped 行不互相覆盖） | ☐ |

**失败线索：** 进度恒为 0 → 查 `PracticeProgressRepository` 保存、`buildPracticeProgressId` scope、Home `practiceProgress` Flow。

---

## B. 原子题库重入（Drawer atomic-bank / Browse re-entry）

验证抽屉点单题 → `BrowseSession` 路由 → 返回后抽屉状态恢复。

| Step | 操作 | 预期 | Pass |
|------|------|------|------|
| B1 | 首页 → 打开题库抽屉（TopBar 菜单） | 抽屉展开，题库列表可见 | ☐ |
| B2 | 搜索或滚动定位某题 → **点击单题** | 进入单题浏览（`BrowseSessionScreen`），**不是**整卷练习 | ☐ |
| B3 | 左右切换 1～2 题（若可） | 索引变化，无崩溃 | ☐ |
| B4 | 返回首页 | 抽屉 **自动重新打开**；搜索词 **保留**（`HomeDrawerRestoreHolder`） | ☐ |
| B5 | 再次点 **同一或另一题** | 仍能进入 Browse，定位正确 | ☐ |
| B6 | 返回 → 关闭抽屉 → 点文件卡片正常练习 | 整卷练习不受 Browse 污染 | ☐ |
| B7 | Browse 中按返回 | **不**弹出练习交卷/退出弹窗（Browse ExitPolicy） | ☐ |

**路由核对：** URL/Nav 应为 `question/{quizId}?targetQuestionId={id}` → `BrowsePracticeRoute`。

---

## C. 回归快检（与 K-001 同次可顺带；Exam 专项见 [K007_EXAM_ROUTE_SMOKE.md](K007_EXAM_ROUTE_SMOKE.md)）

| Step | 操作 | 预期 | Pass |
|------|------|------|------|
| C1 | 同文件「考试」进 `ExamScreen` → 答 1 题 → 退出 | 无崩溃；再进进度合理 | ☐ |
| C2 | 错题本 / 收藏入口各进一次 | 路由正常 | ☐ |

---

## 执行记录

| 日期 | 执行人 | A 段 | B 段 | 结论 |
|------|--------|------|------|------|
| 2026-07-05 | 真机 | ☑ | ☑ | **PASS** |

**FAIL 时必填：** 步骤编号、截图/录屏、logcat 片段、`adb shell dumpsys activity`（如相关）。

---

## 通过后动作

1. 在 `tech_debt.md` 将 K-001 标为 ✅ Closed  
2. 更新 `current_state.md` → What is not done 移除 K-001  
3. `.ai/change_log.md` 追加 VERIFY 条目  
4. 可选：`git tag k001-smoke-pass-YYYYMMDD`

---

## 相关单测（CI 已覆盖逻辑，不能替代本清单）

```bash
./gradlew :app:testDebugUnitTest --tests "com.example.testapp.presentation.screen.HomeViewModelTest"
./gradlew :app:testDebugUnitTest --tests "com.example.testapp.presentation.screen.ProgressScopeTest"
./gradlew :feature-practice:testDebugUnitTest --tests "com.example.testapp.presentation.session.browse.*"
```
