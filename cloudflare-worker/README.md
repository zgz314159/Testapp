# Testapp AI Proxy（历史 / 未来托管后端参考）

> **当前 App 运行时默认 BYOK**：用户在设置中填写自己的 DeepSeek / Tavily Key，Android 直连官方 API。  
> 本目录保留为 **未来「购买托管额度」** 的服务端参考实现，**不参与当前 BYOK 运行时**。

## 当前产品模式（ADR-008）

| 模式 | 状态 | 说明 |
|------|------|------|
| USER_KEYS（BYOK） | 默认启用 | 密钥加密存本机；对话需 DeepSeek；联网纠题需 DeepSeek + Tavily |
| MANAGED（托管额度） | 插口预留 | 支付/账户/短期 access token；**禁止**在 APK 内放平台共用 Key |

## 若将来启用托管服务

必须做到：

1. 用户登录 / 订单校验 / 额度扣减在服务端完成  
2. App 只持有短期 `managedAccessToken`，不持有 DeepSeek/Tavily 上游 Key  
3. 大陆侧载场景下 **不要** 依赖 Google Play Integrity 作为唯一认证  
4. 推荐国内可达托管（腾讯云函数 / 阿里云 FC 等）或自有域名，而非仅 `*.workers.dev`

本仓库 Worker 代码（Play Integrity + Tavily + DeepSeek）可作协议与业务逻辑参考，但上线前需按上述鉴权模型重做。

## 历史端点（参考）

| Path | 说明 |
|------|------|
| `POST /v1/deepseek/chat` | DeepSeek 代理 |
| `POST /v1/questions/correct` | Tavily + DeepSeek 纠题 |

## 本地开发（仅维护本目录时）

```bash
cd cloudflare-worker
npm install
cp .dev.vars.example .dev.vars
npm run test
npm run typecheck
npx wrangler dev
```
