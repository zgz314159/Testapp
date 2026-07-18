package com.example.testapp.uicommon.design

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AiChatSourcesPipelineTest {

    private val sample = "答案是 2017 年 6 月 1 日 [1]。\n\n---\n参考来源\n\n" +
        "[1] 网络安全法施行时间\nhttps://www.example.com/a\n时间: 2026-01-29\n摘要: 新法自2026年1月1日起正式施行\n\n" +
        "[2] 网络安全法全文\nhttps://example.com/b"

    @Test
    fun split_extractsBodyAndSources() {
        val result = AiChatSourcesPipeline.split(sample)

        assertEquals("答案是 2017 年 6 月 1 日 [1]。", result.body)
        assertEquals(2, result.sources.size)
        assertEquals(
            AiChatSourceRef(
                index = 1,
                title = "网络安全法施行时间",
                url = "https://www.example.com/a",
                publishedDate = "2026-01-29",
                snippet = "新法自2026年1月1日起正式施行",
            ),
            result.sources[0],
        )
        assertEquals(AiChatSourceRef(2, "网络安全法全文", "https://example.com/b"), result.sources[1])
    }

    @Test
    fun sourceRef_derivesHostAndFavicon() {
        val ref = AiChatSourceRef(1, "标题", "https://www.example.com/a/b")

        assertEquals("example.com", ref.host)
        assertEquals("https://www.example.com/favicon.ico", ref.faviconUrl)
    }

    @Test
    fun split_returnsContentUnchangedWithoutMarker() {
        val result = AiChatSourcesPipeline.split("普通回答，无来源。")

        assertEquals("普通回答，无来源。", result.body)
        assertTrue(result.sources.isEmpty())
    }

    @Test
    fun reattach_roundTripsWithSplit() {
        val split = AiChatSourcesPipeline.split(sample)
        val reattached = AiChatSourcesPipeline.reattach("编辑后的正文", split.sources)
        val again = AiChatSourcesPipeline.split(reattached)

        assertEquals("编辑后的正文", again.body)
        assertEquals(split.sources, again.sources)
    }

    @Test
    fun reattach_returnsBodyWhenNoSources() {
        assertEquals("正文", AiChatSourcesPipeline.reattach("正文", emptyList()))
    }
}
