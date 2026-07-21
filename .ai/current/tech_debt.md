<!--
  Last synced: 2026-07-19
-->

# Technical Debt Inventory

## 🔴 HIGH

（无）

---

## 🟡 MEDIUM

| ID | Issue | Status |
|----|-------|--------|
| **AF-001** | 自适应渐隐 MVP 缺少当前环境 Gradle/JDK 21 门禁结果 | **CLOSED（记录过期）** — 后续 Round18–21 已完成全应用构建与 Gate；本轮未追加测试 |
| **LOC-001** | 现有 3 个 Kotlin 文件超过 500 行 | **CLOSED** — 当前为 500 / 497 / 451，见 `loc_audit.md` |
| **K-007** | Exam route device smoke | **CLOSED** — 2026-07-05 真机 PASS |
| **D-003** | detekt tightened (unused/dead-code + EmptyCatchBlock + IgnoredReturnValue) | **CLOSED** — P78 |

---

## 🟢 RECENTLY CLOSED

| ID | Resolution |
|----|------------|
| **SEC-001** | Release 签名凭据移出 Gradle；改由忽略的 `signing.properties` 或 CI 环境变量提供，私钥扩展名加入忽略规则 |
| **IMP-001** | 大文件导入移除完整 Question/Entity 中间副本；Room 500 条分批、单事务写入；TXT 改为逐行读取 |
| **LIFE-001** | DeepSeek 请求单活跃任务；新请求/reset 取消旧任务，取消不再映射为普通失败 |
| **D-001** | `:app` thin shell — P62–P77 主链完成；仅 nav routes + Session bindings + Hilt 为合理终态 |
| **TD-014** | Swipeable/FractionalThreshold — 全仓库无代码引用，P52 确认 CLOSED |
| **K-001** | 真机冒烟 PASS — 2026-07-05 |
| **TD-010** | ktlint — P44 接入，P45 强制 |
| **TD-015** | AppNavHost global VM | CLOSED — P47 SessionHost 路由 |
| Extension 写回在 Effects | P41–P43 |
| bindings→Command 长尾 | P51–P52 Practice/Exam UI 主路径 |
| Settings Screen/ui 留 `:app` | P58 迁入 `:feature-settings` |
| Home/QuestionBank 留 `:app` | P62 迁入 `:feature-practice` |
| `DrawerQuestionEditHost` 未接线 | P64 长按→`question_edit` 路由 ✅ |
| `question_detail` legacy `QuestionScreen` | P65 → Browse Session `targetQuestionId=0` ✅ |
| Result/History/WrongBook 留 `:app` | P65 下沉 `:feature-practice` + 薄路由 ✅ |
| `FavoriteScreen` 留 `:app` | P66 下沉 `:feature-practice` + `FavoriteRoute` ✅ |
| `PracticeScreen`/`ExamScreen` 留 `:app` | P67 下沉 feature + 删除 `SessionCommandDispatch` ✅ |
| AI 叠层 `NavController` 在 `feature-ai` | P68 改回调 + 移除 `navigation-compose` ✅ |
| Settings `ImportResult` 重复 | P68 统一 `feature-settings` + `SettingsImportFilePipeline` ✅ |

---

## Suggested order

（`:app` 薄壳：Session 路由/Hilt + Settings IO coordinators 实现；模块迁移主链完成）

---

## Compatibility constraints

- Do not change `__scope=` progress id format without migration  
- `legacyRandomScopedPracticeProgressId` — keep until planned removal
