# 编码与终端乱码指南

> **用途**：区分「终端显示乱码」与「源码中文损坏」，避免重复踩坑。  
> **适用**：Cursor Agent、本地 PowerShell/CMD、Android Studio 编辑含中文的 Kotlin/XML。  
> **最后更新**：2026-06-19

---

## 两类问题，不要混为一谈

| 现象 | 本质 | 是否污染源码 |
|------|------|--------------|
| 终端出现 `����λ��`、`��ǡ�&&` | 终端 **输出编码** 与 **显示端解码** 不一致 | **否** |
| 源码出现 `// ƳԶ棬`、`ɹ` | 文件 **读写/保存编码** 错误 | **是** |

跑 `gradlew`、编译、Git 命令 **不会** 直接把 `.kt` 里的中文注释改乱码。  
源码乱码来自 **编辑/脚本/复制** 时的编码错误。

---

## 一、终端乱码（PowerShell / CMD）

### 典型报错（乱码前）

```text
所在位置 … 第 51 个字符
标记“&&”不是此版本中的有效语句分隔符
```

乱码后可能显示为：`����λ��` … `~~` … `��ǡ�&&�����Ǵ˰汾�е���Ч���ָ�����`

### 原因

1. **Windows 中文系统** 默认终端代码页多为 **GBK (CP936)**。
2. **Cursor / 现代工具** 捕获输出时 often 按 **UTF-8** 解析。
3. GBK 字节被当 UTF-8 读 → 中文错误信息变成 `����`。

### 命令失败的真实原因（与乱码无关）

**Windows PowerShell 5.x 不支持 `&&` 链式执行**（PowerShell 7+ 才支持）。

```powershell
# ❌ PowerShell 5.x 会报错
cd "c:\...\Testapp" && .\gradlew :app:compileDebugKotlin

# ✅ 正确写法
cd "c:\Users\zgz31\AndroidStudioProjects\Testapp"; .\gradlew :app:compileDebugKotlin --quiet
```

### 终端修复建议

**命令写法**

- PowerShell 5：用 **`;`** 串联命令，不要用 `&&`。
- 或安装 **PowerShell 7**，在 Cursor 设为默认终端。

**改善中文显示（可选，当前会话）**

```powershell
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
chcp 65001
```

**Cursor 设置**

- `Terminal › Integrated: Default Profile` → PowerShell 7（推荐）或 Command Prompt。
- 若必须用 PowerShell 5，Agent/脚本统一用 `;` 而非 `&&`。

---

## 二、源码中文乱码（注释 / 字符串）

### 典型表现

- 注释：`// 初始化时加载` → `// ʼʱ`
- 字符串：`"导出成功"` → `"ɹ"`

### 常见原因

1. Python/脚本读写 `.kt` 未指定 `encoding='utf-8'`。
2. 编辑器 **Global/Project Encoding** 不是 UTF-8。
3. 从网页/PDF/旧文档复制中文，源内容已损坏。
4. 批量替换或错误转码破坏了 UTF-8 多字节序列。

### 项目已有配置

`gradle.properties`：

```properties
org.gradle.jvmargs=-Xmx4096m -Dfile.encoding=UTF-8
```

Gradle/JVM 编译侧已是 UTF-8；**仍需保证源文件本身以 UTF-8 保存**。

---

## 三、预防清单（Agent / 开发者）

### 1. 编辑器（Android Studio / Cursor）

`Settings → Editor → File Encodings`：

| 项 | 值 |
|----|-----|
| Global Encoding | UTF-8 |
| Project Encoding | UTF-8 |
| Properties Files | UTF-8，勾选 Transparent native-to-ascii conversion |

### 2. 脚本处理源码

```python
# 读
with open(path, "r", encoding="utf-8") as f:
    content = f.read()

# 写
with open(path, "w", encoding="utf-8", newline="\n") as f:
    f.write(content)
```

**禁止**对含中文的 `.kt` 使用默认编码或 `latin-1` 读写。

### 3. 改完含中文的文件后自检

- IDE 中打开文件，目视注释/字符串是否正常。
- 若出现 `�`、`Ƴ`、`ɹ` 等替换字符或拉丁乱码 → **不要提交**，立即用 UTF-8 重新编辑或从 Git 恢复。

### 4. Git（可选，推荐）

`.gitattributes` 示例：

```gitattributes
*.kt text eol=lf encoding=UTF-8
*.kts text eol=lf encoding=UTF-8
*.xml text eol=lf encoding=UTF-8
*.properties text eol=lf encoding=UTF-8
```

### 5. 项目级 `.editorconfig`（推荐）

根目录 `.editorconfig`：

```ini
root = true

[*]
charset = utf-8
end_of_line = lf
insert_final_newline = true

[*.{kt,kts,java,xml,properties,gradle}]
charset = utf-8
```

---

## 四、Agent 操作规范

1. **终端**：在 Windows PowerShell 5 环境下用 `;` 连接命令，不用 `&&`。
2. **写文件**：工具写入含中文内容时，确保文件为 UTF-8（Cursor Write/StrReplace 一般为 UTF-8，避免经中间脚本转码）。
3. **批量修复中文**：优先 **逐文件 StrReplace**，避免未指定编码的 Python one-liner。
4. **误判**：终端 `����` ≠ 源码已坏；以 IDE 中文件内容为准。

---

## 五、快速对照

| 你看到 | 判断 | 动作 |
|--------|------|------|
| 仅终端报错信息乱码 | 显示问题 | 改命令语法（`;`）或终端 UTF-8 / PS7 |
| IDE 里注释已是乱码 | 文件编码问题 | 恢复 Git 版本或手动改回中文并 UTF-8 保存 |
| gradle 编译通过但 UI 中文错 | 可能是 `strings.xml` 或运行时数据 | 查资源文件编码与数据源，非终端问题 |

---

## 相关文档

- [KNOWN_ISSUES.md](../KNOWN_ISSUES.md) — 开放问题索引（含本指南链接）
- [12_CONTEXT_LOADING_RULES.md](./12_CONTEXT_LOADING_RULES.md) — Agent 默认读取规则
