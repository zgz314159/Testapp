# ADR-007: Cloudflare Worker + Play Integrity 代理全部 AI 流量

## Status

Superseded (2026-07-18) by [ADR-008](008-byok-ai-provider.md)

> 大陆侧载默认改为 BYOK。本 ADR 仅作历史记录；未来托管服务可参考其代理形态，但不得再把 Play Integrity 当作大陆唯一认证。

## Context

- 旧实现把 `DEEPSEEK_API_KEY` 编入 APK `BuildConfig`，密钥可被反编译提取。
- 「AI 联网纠题」需要 Tavily 实时检索 + DeepSeek 结构化输出，若再把 Tavily Key 放进客户端会扩大泄露面。
- 需要保证只有正版、未被篡改的客户端能调用付费上游 API。

## Decision

1. **独立 Cloudflare Worker**（仓库 `cloudflare-worker/`）持有 `DEEPSEEK_API_KEY`、`TAVILY_API_KEY` 与 Google 服务账号 JSON；APK 仅配置非秘密的 `AI_PROXY_BASE_URL` 与 `PLAY_CLOUD_PROJECT_NUMBER`。
2. **全部 DeepSeek 流量**经 `POST /v1/deepseek/chat`；纠题经 `POST /v1/questions/correct`（Tavily → DeepSeek v4 flash → JSON schema 校验）。
3. **Google Play Integrity（Standard）** 为唯一认证：Android 对规范化请求体算 `requestHash`，每次请求携带 Integrity token；Worker 校验包名、`PLAY_RECOGNIZED`、设备/许可完整性、新鲜度与 `requestHash`。**不提供不安全客户端降级。**
4. 纠题结果只回填编辑弹窗草稿；落库仍走既有「保存修改」→ `SessionCommand` / Repository 路径。

## Consequences

- Debug / 侧载安装在未配置 Play Integrity 时会失败，需 Play 安装包或测试轨道验收。
- 部署依赖：Play Console Integrity、Cloud 项目号、Worker Secrets、证书摘要。
- `DeepSeekApiService` 上层 `chat/analyze/ask` 契约保持不变，减少 feature-ai ViewModel 改动。

## Alternatives Considered

- APK 直连 DeepSeek/Tavily — 拒绝：密钥泄露。
- 自建后端 + 用户登录 — 过重；当前产品无账号体系。
- App Check / 自定义 HMAC — 弱于 Play Integrity 的设备与安装完整性保证。
