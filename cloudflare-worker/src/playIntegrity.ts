import type { Env } from "./types";
import { jsonError } from "./rateLimit";

interface ServiceAccount {
  client_email: string;
  private_key: string;
  token_uri?: string;
}

interface IntegrityVerdict {
  requestDetails?: {
    requestPackageName?: string;
    requestHash?: string;
    timestampMillis?: string;
  };
  appIntegrity?: {
    appRecognitionVerdict?: string;
    packageName?: string;
  };
  deviceIntegrity?: {
    deviceRecognitionVerdict?: string[];
  };
  accountDetails?: {
    appLicensingVerdict?: string;
  };
}

function pemToArrayBuffer(pem: string): ArrayBuffer {
  const b64 = pem
    .replace(/-----BEGIN PRIVATE KEY-----/, "")
    .replace(/-----END PRIVATE KEY-----/, "")
    .replace(/\s+/g, "");
  const binary = atob(b64);
  const bytes = new Uint8Array(binary.length);
  for (let i = 0; i < binary.length; i++) bytes[i] = binary.charCodeAt(i);
  return bytes.buffer;
}

async function importPrivateKey(pem: string): Promise<CryptoKey> {
  return crypto.subtle.importKey(
    "pkcs8",
    pemToArrayBuffer(pem),
    { name: "RSASSA-PKCS1-v1_5", hash: "SHA-256" },
    false,
    ["sign"],
  );
}

function base64UrlEncode(data: ArrayBuffer | string): string {
  const bytes =
    typeof data === "string" ? new TextEncoder().encode(data) : new Uint8Array(data);
  let binary = "";
  for (const b of bytes) binary += String.fromCharCode(b);
  return btoa(binary).replace(/\+/g, "-").replace(/\//g, "_").replace(/=+$/g, "");
}

async function getAccessToken(sa: ServiceAccount): Promise<string> {
  const now = Math.floor(Date.now() / 1000);
  const header = base64UrlEncode(JSON.stringify({ alg: "RS256", typ: "JWT" }));
  const claim = base64UrlEncode(
    JSON.stringify({
      iss: sa.client_email,
      scope: "https://www.googleapis.com/auth/playintegrity",
      aud: sa.token_uri || "https://oauth2.googleapis.com/token",
      iat: now,
      exp: now + 3600,
    }),
  );
  const unsigned = `${header}.${claim}`;
  const key = await importPrivateKey(sa.private_key);
  const signature = await crypto.subtle.sign(
    "RSASSA-PKCS1-v1_5",
    key,
    new TextEncoder().encode(unsigned),
  );
  const jwt = `${unsigned}.${base64UrlEncode(signature)}`;
  const tokenUri = sa.token_uri || "https://oauth2.googleapis.com/token";
  const res = await fetch(tokenUri, {
    method: "POST",
    headers: { "content-type": "application/x-www-form-urlencoded" },
    body: new URLSearchParams({
      grant_type: "urn:ietf:params:oauth:grant-type:jwt-bearer",
      assertion: jwt,
    }),
  });
  if (!res.ok) {
    throw new Error(`oauth_failed:${res.status}`);
  }
  const body = (await res.json()) as { access_token?: string };
  if (!body.access_token) throw new Error("oauth_missing_token");
  return body.access_token;
}

export async function verifyPlayIntegrity(
  env: Env,
  integrityToken: string,
  expectedHash: string,
): Promise<Response | null> {
  if (!integrityToken || !expectedHash) {
    return jsonError(401, "INTEGRITY_REQUIRED", "缺少 Play Integrity 凭证");
  }
  if (!env.GOOGLE_SERVICE_ACCOUNT_JSON || !env.PLAY_CLOUD_PROJECT_NUMBER) {
    return jsonError(503, "INTEGRITY_NOT_CONFIGURED", "服务端未配置 Play Integrity");
  }

  let sa: ServiceAccount;
  try {
    sa = JSON.parse(env.GOOGLE_SERVICE_ACCOUNT_JSON) as ServiceAccount;
  } catch {
    return jsonError(503, "INTEGRITY_NOT_CONFIGURED", "服务账号配置无效");
  }

  try {
    const accessToken = await getAccessToken(sa);
    const packageName = env.ALLOWED_PACKAGE_NAME || "com.example.testapp";
    const url =
      `https://playintegrity.googleapis.com/v1/${encodeURIComponent(packageName)}:decodeIntegrityToken`;
    const res = await fetch(url, {
      method: "POST",
      headers: {
        authorization: `Bearer ${accessToken}`,
        "content-type": "application/json",
      },
      body: JSON.stringify({ integrityToken }),
    });
    if (!res.ok) {
      return jsonError(401, "INTEGRITY_DECODE_FAILED", "Integrity token 校验失败");
    }
    const decoded = (await res.json()) as { tokenPayloadExternal?: IntegrityVerdict };
    const verdict = decoded.tokenPayloadExternal;
    if (!verdict) {
      return jsonError(401, "INTEGRITY_INVALID", "Integrity 响应无效");
    }

    const pkg =
      verdict.requestDetails?.requestPackageName ||
      verdict.appIntegrity?.packageName ||
      "";
    if (pkg !== packageName) {
      return jsonError(401, "INTEGRITY_PACKAGE", "应用包名不匹配");
    }

    const appVerdict = verdict.appIntegrity?.appRecognitionVerdict || "";
    if (appVerdict !== "PLAY_RECOGNIZED") {
      return jsonError(401, "INTEGRITY_APP", "应用未被 Play 识别，请使用正式渠道安装");
    }

    const device = verdict.deviceIntegrity?.deviceRecognitionVerdict || [];
    const deviceOk =
      device.includes("MEETS_DEVICE_INTEGRITY") ||
      device.includes("MEETS_STRONG_INTEGRITY") ||
      device.includes("MEETS_BASIC_INTEGRITY");
    if (!deviceOk) {
      return jsonError(401, "INTEGRITY_DEVICE", "设备完整性校验未通过");
    }

    const hash = (verdict.requestDetails?.requestHash || "").toLowerCase();
    if (hash !== expectedHash.toLowerCase()) {
      return jsonError(401, "INTEGRITY_HASH", "请求摘要不匹配");
    }

    const ts = Number.parseInt(verdict.requestDetails?.timestampMillis || "0", 10);
    if (ts > 0 && Math.abs(Date.now() - ts) > 10 * 60_000) {
      return jsonError(401, "INTEGRITY_STALE", "Integrity token 已过期");
    }

    return null;
  } catch {
    return jsonError(401, "INTEGRITY_ERROR", "Integrity 校验异常");
  }
}
