# Code Review Workflow

> 编码完成后的架构自检（Agent 自审或用户请求 Review 时使用）。

## Review 清单

### 架构

- [ ] 符合 V5 Session：UI → Command，Extension ← Event  
- [ ] 无新模式布尔在 Screen/Route  
- [ ] 新逻辑在正确模块（对照 `architecture/module_rules.md`）  
- [ ] 无未经 ADR 的新抽象  

### 代码质量

- [ ] 单文件 ≤ LOC 红线；职责 ≤ 3  
- [ ] 复杂分支已进 Pipeline  
- [ ] 无复制粘贴已有 Pipeline/Coordinator  
- [ ] Kotlin 子类 `override val` 不在父类 init 中访问（见 Session 初始化陷阱）  

### 测试

- [ ] 编译通过  
- [ ] 相关单测通过  
- [ ] `ArchitectureTest` 未破坏（模块边界）  

### 文档

- [ ] `change_log` / `loc_audit` / `current_state` 已同步（如需要）  
- [ ] 新决策有 ADR  

## 输出格式

```markdown
## Architecture Review

| 项 | 结果 | 说明 |
|----|------|------|
| Session 边界 | PASS/FAIL | … |
| 模块归属 | PASS/FAIL | … |
| LOC/职责 | PASS/FAIL | … |
| 复用 | PASS/FAIL | … |
| 测试 | PASS/FAIL | … |

### 建议（如有）
- …
```

## 可选工具

- `./gradlew detekt`  
- Bugbot / Security Review（用户显式要求时）  
