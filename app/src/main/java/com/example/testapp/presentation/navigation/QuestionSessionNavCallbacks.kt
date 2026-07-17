package com.example.testapp.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import com.example.testapp.core.util.safeEncode

data class QuestionSessionNavCallbacks(
    val onViewDeepSeek: (String, Int, Int) -> Unit,
    val onViewSpark: (String, Int, Int) -> Unit,
    val onViewBaidu: (String, Int, Int) -> Unit,
    val onAskDeepSeek: (String, Int, Int) -> Unit,
    val onAskSpark: (String, Int, Int) -> Unit,
    val onAskBaidu: (String, Int, Int) -> Unit,
    val onViewExplanation: (String) -> Unit,
    val onEditCorrectAnswer: (String, Int, Int) -> Unit,
    val onEditNote: (String, Int, Int) -> Unit,
)

@Composable
fun NavHostController.rememberQuestionSessionNavCallbacks(): QuestionSessionNavCallbacks {
    val nav = this
    return remember(nav) {
        QuestionSessionNavCallbacks(
            onViewDeepSeek = { text, id, index ->
                val encodedText = safeEncode(text)
                nav.navigate("deepseek_ask/$id/$index/$encodedText")
            },
            onViewSpark = { text, id, index ->
                val encodedText = safeEncode(text)
                nav.navigate("spark/$id/$index/$encodedText")
            },
            onViewBaidu = { text, id, index ->
                val encodedText = safeEncode(text)
                nav.navigate("baidu/$id/$index/$encodedText")
            },
            onAskDeepSeek = { text, id, index ->
                val encodedText = safeEncode(text)
                nav.navigate("deepseek_ask/$id/$index/$encodedText")
            },
            onAskSpark = { text, id, index ->
                val encodedText = safeEncode(text)
                nav.navigate("spark_ask/$id/$index/$encodedText")
            },
            onAskBaidu = { text, id, index ->
                val encodedText = safeEncode(text)
                nav.navigate("baidu_ask/$id/$index/$encodedText")
            },
            onViewExplanation = { text ->
                val encodedText = safeEncode(text)
                nav.navigate("explanation/$encodedText")
            },
            onEditCorrectAnswer = { text, id, index ->
                val encodedText = safeEncode(text)
                nav.navigate("correct_answer/$id/$index/$encodedText")
            },
            onEditNote = { text, id, index ->
                val encodedText = safeEncode(text)
                nav.navigate("note/$id/$index/$encodedText")
            },
        )
    }
}
