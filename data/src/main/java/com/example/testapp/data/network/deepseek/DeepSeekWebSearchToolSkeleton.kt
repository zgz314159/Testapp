package com.example.testapp.data.network.deepseek

/**
 * DeepSeek Tool Calling：联网搜索骨架。
 *
 * 官方 Chat Completions **不会**自动联网；需在请求中声明 tools，
 * 模型返回 tool_calls 后由应用执行检索，再把结果以 role=tool 回传。
 *
 * 当前为可插拔骨架：未配置搜索后端时返回明确占位，避免静默幻觉。
 */
object DeepSeekWebSearchToolSkeleton {

    const val TOOL_NAME = "search_web"

    val TOOL_DEFINITION_JSON = """
        {
          "type": "function",
          "function": {
            "name": "$TOOL_NAME",
            "description": "检索铁路规章、教材或公开资料。当用户提供标答与模型结论冲突，或需要核对条文时调用。",
            "parameters": {
              "type": "object",
              "properties": {
                "query": {
                  "type": "string",
                  "description": "检索关键词，建议含规章名称与条款要点"
                }
              },
              "required": ["query"]
            }
          }
        }
    """.trimIndent()

    /** 无真实搜索后端时的 tool 回传文案。 */
    fun stubToolResult(query: String): String =
        "【检索未配置】query=$query。未能联网核验。请对照题干、【题库标答】与已知规章重新论证；" +
            "若标答与先验冲突，优先说明冲突点，再给出最终答案。"
}
