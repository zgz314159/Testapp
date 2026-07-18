# ADR-008: BYOK 为默认 AI 提供方，托管额度仅插口预留

## Status

Accepted (2026-07-18) — **Supersedes ADR-007**（大陆侧载场景）

## Context

- 大陆用户以 APK 直装 / 国内商店为主，缺少 Google Play 与可靠 GMS，ADR-007 的强制 Play Integrity 不可用。
- 用户希望各自使用自己的 DeepSeek V4 Flash 与检索 API Key，并预留「支付后使用官方额度」能力。
- Tavily（api.tavily.com）在大陆部分网络下访问不稳定，需要国内可直连的替代：博查 Web Search（api.bochaai.com，Bing 兼容响应）。
- 平台共用上游 Key 绝不能编入 APK。

## Decision

1. **默认 `USER_KEYS`（BYOK）**：用户在设置 → AI 服务填写 Key；Android Keystore AES/GCM 加密存本机；对话直连 `api.deepseek.com`（`deepseek-v4-flash`）；联网纠题直连检索服务。
2. **能力门槛**：`CHAT` 仅需 DeepSeek；`CHAT_ONLINE` / `CORRECT_ONLINE` 需 DeepSeek + 检索 Key（博查或 Tavily 任一）。缺 Key 时抛可识别错误并引导去设置，不静默降级。
3. **检索双通道**（`SearchProviderKind`）：博查（`api.bochaai.com/v1/web-search`，大陆推荐）与 Tavily（`api.tavily.com/search`，海外）；用户填哪个用哪个，**都填时优先博查**。AI 问答可由用户显式开启联网模式（`CHAT_ONLINE`），开启后复用同一检索双通道，并把来源作为当前轮上下文交给 DeepSeek；正文按编号引用，客户端在回答末尾确定性追加实际检索来源；默认关闭。
4. **`MANAGED` 插口**：`AiEntitlementRepository` + `ManagedAiBackend` 预留；本轮 `hasManagedAccess=false`，UI「购买额度，敬请期待」。未来支付成功后仅下发短期 access token，由服务端持有上游 Key。
5. **统一 `AiBackend` 路由**：`DeepSeekApiService` / 纠题仓库只依赖 `AiBackend`；删除 Android 侧强制 Play Integrity / `AI_PROXY_*` BuildConfig。
6. `cloudflare-worker/` 保留为历史与未来托管参考，不参与当前 BYOK 运行时。

## Consequences

- 用户需自行申请 DeepSeek Key + 检索 Key（博查或 Tavily）；无 Key 时 AI 功能不可用（直至托管上线）。
- Key 在用户设备上，反编译 APK 拿不到他人 Key；本机 Root/备份仍有泄露风险，属 BYOK 固有 trade-off。
- ADR-007 的「无降级 Play Integrity」仅适用于未来可能的海外 Play 渠道托管部署，不再作为大陆默认路径。

## Alternatives Considered

- 维持 ADR-007 严格 Integrity — 拒绝：大陆侧载不可用。
- APK 内嵌平台 Key — 拒绝：泄露与费用风险。
- 本轮直接做支付 — 推迟：先留 entitlement 插口与禁用购买按钮。
