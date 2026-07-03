package com.example.testapp.uicommon.design

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AiChatSaveGatePipelineTest {

    @Test
    fun shouldConfirmSave_blocksParsingAndEmpty() {
        assertFalse(
            AiChatSaveGatePipeline.shouldConfirmSave(
                content = "",
                isParsing = false,
                parsingKeyword = "解析中",
                parseFailedKeyword = "解析失败"
            )
        )
        assertFalse(
            AiChatSaveGatePipeline.shouldConfirmSave(
                content = "有内容",
                isParsing = true,
                parsingKeyword = "解析中",
                parseFailedKeyword = "解析失败"
            )
        )
    }

    @Test
    fun shouldConfirmSave_allowsSavableContent() {
        assertTrue(
            AiChatSaveGatePipeline.shouldConfirmSave(
                content = "有效回答",
                isParsing = false,
                parsingKeyword = "解析中",
                parseFailedKeyword = "解析失败"
            )
        )
    }
}
