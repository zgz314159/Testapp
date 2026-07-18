package com.example.testapp.uicommon.design

/** 定位正文中的 `[n]` 引用标记与行首分节标签，供 UI 上样式。 */
object AiChatCitationStylePipeline {

    private val citationRegex = Regex("""\[\d{1,2}]""")

    /** 行首 ≤10 字的「标签：」段落标题，如「最终答案：」「依据：」「解析：」。 */
    private val sectionTitleRegex = Regex("""(?m)^[ \t]*([\u4e00-\u9fa5A-Za-z0-9（）()]{1,10}[:：])""")

    fun ranges(text: String): List<IntRange> =
        citationRegex.findAll(text).map { it.range }.toList()

    fun sectionTitleRanges(text: String): List<IntRange> =
        sectionTitleRegex.findAll(text).mapNotNull { it.groups[1]?.range }.toList()
}
