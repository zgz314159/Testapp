package com.example.testapp.data.network.deepseek

/** 多轮问答持久化：结构化编解码，兼容纯文本与仅答案拼接。 */
object DeepSeekAskPersistFormatPipeline {

    const val ASSISTANT_SEPARATOR = "\n\n---\n\n"
    private const val USER_MARKER = "【DS·问】"
    private const val ASSISTANT_MARKER = "【DS·答】"

    fun encode(turns: List<DeepSeekChatTurn>): String =
        turns.joinToString("\n\n") { turn ->
            "$USER_MARKER\n${turn.user}\n$ASSISTANT_MARKER\n${turn.assistant}"
        }

    fun decode(firstQuestion: String, persisted: String): List<DeepSeekChatTurn> {
        val trimmed = persisted.trim()
        if (trimmed.isBlank()) return emptyList()
        if (USER_MARKER in trimmed && ASSISTANT_MARKER in trimmed) {
            return parseStructured(trimmed)
        }
        if (ASSISTANT_SEPARATOR in trimmed) {
            return trimmed.split(ASSISTANT_SEPARATOR)
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .mapIndexed { index, assistant ->
                    DeepSeekChatTurn(
                        user = if (index == 0) firstQuestion.trim() else DeepSeekAskFollowUpPipeline.restoredFollowUpUser(),
                        assistant = assistant
                    )
                }
        }
        return listOf(DeepSeekChatTurn(user = firstQuestion.trim(), assistant = trimmed))
    }

    private fun parseStructured(persisted: String): List<DeepSeekChatTurn> {
        val lines = persisted.lines()
        val turns = mutableListOf<DeepSeekChatTurn>()
        var i = 0
        while (i < lines.size) {
            if (lines[i].trim() != USER_MARKER) {
                i++
                continue
            }
            i++
            val userBuilder = StringBuilder()
            while (i < lines.size && lines[i].trim() != ASSISTANT_MARKER) {
                if (userBuilder.isNotEmpty()) userBuilder.append('\n')
                userBuilder.append(lines[i])
                i++
            }
            if (i >= lines.size || lines[i].trim() != ASSISTANT_MARKER) break
            i++
            val assistantBuilder = StringBuilder()
            while (i < lines.size && lines[i].trim() != USER_MARKER) {
                if (assistantBuilder.isNotEmpty()) assistantBuilder.append('\n')
                assistantBuilder.append(lines[i])
                i++
            }
            turns.add(DeepSeekChatTurn(user = userBuilder.toString(), assistant = assistantBuilder.toString()))
        }
        return turns
    }
}
