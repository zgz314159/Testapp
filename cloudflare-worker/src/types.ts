export interface Env {
  DEEPSEEK_API_KEY: string;
  TAVILY_API_KEY: string;
  GOOGLE_SERVICE_ACCOUNT_JSON: string;
  PLAY_CLOUD_PROJECT_NUMBER: string;
  ALLOWED_PACKAGE_NAME: string;
  RATE_LIMIT_PER_MINUTE: string;
}

export interface ChatMessage {
  role: string;
  content: string;
}

export interface DeepSeekChatRequest {
  messages: ChatMessage[];
  enableThinking?: boolean;
  attachWebSearchTool?: boolean;
  integrityToken: string;
  requestHash: string;
}

export interface QuestionCorrectRequest {
  questionType: string;
  content: string;
  options: string[];
  answer: string;
  explanation?: string;
  integrityToken: string;
  requestHash: string;
}

export interface SearchSource {
  title: string;
  url: string;
  snippet: string;
}

export interface QuestionCorrectionSuggestion {
  content: string;
  options: string[];
  answer: string;
  explanation: string;
  reason: string;
  confidence: number;
  sources: SearchSource[];
  verifiedOnline: boolean;
}

export interface RateBucket {
  count: number;
  windowStartMs: number;
}
