package com.example.testapp.presentation.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import android.util.Log
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.testapp.presentation.component.LocalFontFamily
import com.example.testapp.presentation.component.LocalFontSize
import java.time.format.DateTimeFormatter

// 子组件：统计块
@Composable
private fun ResultStatBlock(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.primary
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(
                fontFamily = LocalFontFamily.current
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(
                fontFamily = LocalFontFamily.current
            ),
            color = valueColor
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    score: Int,                        // 本次session答对数
    total: Int,                        // 本次session已答数  
    unanswered: Int,                   // 剩余未答数
    quizId: String,
    cumulativeCorrect: Int? = null,    // 可选：真实累计答对数
    cumulativeAnswered: Int? = null,   // 可选：真实累计已答数
    cumulativeExamCount: Int? = null,  // 可选：真实累计考试次数
    onBackHome: () -> Unit,
    onViewDetail: () -> Unit = {},
    onBack: () -> Unit = onBackHome
) {
    // 添加调试日志：显示接收到的参数

    val viewModel: ResultViewModel = hiltViewModel()
    LaunchedEffect(quizId) { 

        viewModel.load(quizId) 
        
    }

    // 获取历史记录
    val historyList by viewModel.history.collectAsState(initial = emptyList())
    val totalQuestions by viewModel.totalQuestions.collectAsState()
    
    // 调试：监控历史数据的变化
    LaunchedEffect(historyList.size) {

        historyList.forEachIndexed { index, record ->
            
        }
        
    }
    
    // 最新一次记录
    val latest = historyList.maxByOrNull { it.time }

    // 本次练习统计：始终使用传入参数，因为这些是用户刚刚完成的练习结果

    // 模式判断
    val isExamMode = quizId.startsWith("exam_")
    
    // 本次练习卡片显示用户刚刚完成的练习结果
    val currentScore      = score
    val currentActualAnswered = total  // 本次实际答题数
    val currentUnanswered = unanswered
    
    // 考试模式特殊处理：只显示本次新答的题目
    val (currentAnswered, currentWrong) = if (isExamMode) {
        // 考试模式的问题：ExamScreen传入的total包含了历史已答数
        // 但是对于"本次考试"统计，我们只想显示本次session的数据
        
        // 如果没有历史记录，说明这是第一次考试，total就是本次答题数
        if (latest == null) {
            val thisSessionAnswered = total  // 第一次考试，total就是本次答题数
            val thisSessionWrong = (thisSessionAnswered - score).coerceAtLeast(0)
            
            thisSessionAnswered to thisSessionWrong
        } else {
            // 有历史记录时，计算本次新增答题数
            val previousTotal = latest.total + latest.unanswered // 历史的题库总数
            val previousAnswered = latest.total  // 历史已答数
            val newAnswered = (total - previousAnswered).coerceAtLeast(0)
            val newWrong = (newAnswered - score).coerceAtLeast(0)

            newAnswered to newWrong
        }
    } else {
        // 练习模式：直接使用传入参数
        total to (total - score).coerceAtLeast(0)
    }

    val currentTotal = currentAnswered + currentUnanswered  // ✅ 本次练习总题数
    val currentRate = if (currentAnswered > 0) currentScore.toDouble() / currentAnswered else 0.0
    val currentRateText = String.format("%.2f", currentRate * 100)  // ✅ 添加缺少的变量定义

    // 题库总计统计：显示整个题库的完成情况，而不是累计练习统计
    val currentFileName = latest?.fileName ?: ""

    historyList.forEachIndexed { index, record ->
        
    }
    
    val sameFileHistory = historyList.filter { it.fileName == currentFileName }
    
    sameFileHistory.forEachIndexed { index, record ->
        
    }
    
    val overallTotal = totalQuestions.takeIf { it > 0 } ?: (latest?.total ?: 0)

    // 题库总计统计：统一使用传入的准确累计数据，不依赖历史记录
    val (overallScore, overallAnswered) = if (isExamMode) {
        // 考试模式：优先使用传入的准确累计数据，与练习模式逻辑一致

        val examScore = cumulativeCorrect ?: run {
            
            score  // 回退到本次答对数
        }
        
        val examAnswered = cumulativeAnswered ?: run {
            
            total  // 回退到本次已答数
        }

        examScore to examAnswered
    } else {
        // 练习模式：优先使用传入的准确累计数据，否则回退到历史记录推算
        val practiceScore = cumulativeCorrect ?: run {
            // 回退到历史记录推算
            if (sameFileHistory.isNotEmpty()) {
                val latestRecord = sameFileHistory.first()
                val totalAnswered = overallTotal - latestRecord.unanswered
                val bestHistoricalScore = sameFileHistory.maxOf { it.score }
                val estimatedCumulativeCorrect = minOf(bestHistoricalScore + score, totalAnswered)

                estimatedCumulativeCorrect
            } else {
                
                0
            }
        }
        
        // 累计已答数：优先使用传入的准确数据
        val practiceAnswered = cumulativeAnswered ?: run {
            if (sameFileHistory.isNotEmpty()) {
                val latestRecord = sameFileHistory.first()
                overallTotal - latestRecord.unanswered
            } else {
                0
            }
        }
        
        practiceScore to practiceAnswered
    }

    // 未答数：考试模式和练习模式分别计算
    val overallUnanswered = if (isExamMode) {
        // 考试模式：未答数 = 题库总题数 - 累计已答数
        val examUnanswered = (overallTotal - overallAnswered).coerceAtLeast(0)
        
        examUnanswered
    } else if (sameFileHistory.isNotEmpty()) {
        val latestUnanswered = sameFileHistory.first().unanswered
        
        latestUnanswered
    } else {
        
        overallTotal
    }
    
    // 题库答错数：基于准确的累计数据计算
    val overallWrong = (overallAnswered - overallScore).coerceAtLeast(0)

    // 题库已答题数：直接使用累计已答数
    val totalAnsweredInThisFile = overallAnswered

    // 正确率 = 答对数 / 已答题数
    val overallRate = if (totalAnsweredInThisFile > 0) {
        val rate = overallScore.toFloat() / totalAnsweredInThisFile.toFloat()
        
        rate
    } else {
        
        0f
    }
    val overallRateText = String.format("%.2f", overallRate * 100)

    // 数据合理性验证

    // 验证数据一致性
    val currentSum = currentAnswered + currentUnanswered  // 已答 + 未答 = 总数
    val overallSum = overallScore + overallWrong + overallUnanswered

    if (currentSum != currentTotal) {
        
    }
    if (currentScore + currentWrong != currentAnswered) {
        
    }
    if (overallSum != overallTotal) {
        
    }

    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    val scrollState = rememberScrollState()

    // 模式与文件名
    val (modeText, fileName) = when {
        quizId.startsWith("exam_") -> "考试" to quizId.removePrefix("exam_")
        quizId.startsWith("practice_") -> "练习" to quizId.removePrefix("practice_")
        else -> "练习" to quizId
    }
    val currentLabel = if (isExamMode) "本次考试：" else "本次练习："
    val overallLabel = if (isExamMode) "考试总计：" else "题库总计："
    
    // 考试模式的特殊处理：题库总计显示的内容
    val displayOverallTotal = if (isExamMode) {
        // 考试模式：题库总计分母显示题库总题数，不是已答题数
        overallTotal
    } else {
        // 练习模式：显示已答题数
        overallAnswered
    }

    Scaffold(
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Button(
                        onClick = onBack,
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("返回首页", fontSize = LocalFontSize.current * 0.98f, fontFamily = LocalFontFamily.current)
                    }
                    Button(
                        onClick = onViewDetail,
                        enabled = quizId.isNotBlank(),
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("答题详情", fontSize = LocalFontSize.current * 0.98f, fontFamily = LocalFontFamily.current)
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(innerPadding)
                .padding(horizontal = 18.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (fileName.isNotBlank()) {
                Text(
                    text = fileName,
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = LocalFontSize.current * 1.05f, fontFamily = LocalFontFamily.current),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            Spacer(modifier = Modifier.height(18.dp))

            // 当前考试卡片
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(currentLabel, style = MaterialTheme.typography.titleSmall.copy(fontSize = LocalFontSize.current * 0.9f, fontFamily = LocalFontFamily.current), color = MaterialTheme.colorScheme.primary)
                    Text("$currentScore / $currentActualAnswered", style = MaterialTheme.typography.displaySmall.copy(fontSize = LocalFontSize.current * 1.2f, fontFamily = LocalFontFamily.current), color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator(progress = { currentRate.toFloat() }, modifier = Modifier.fillMaxWidth().height(8.dp))  // ✅ 修复progress参数
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        ResultStatBlock("答对", "$currentScore", Color.Green)
                        ResultStatBlock("答错", "$currentWrong", Color.Red)
                        ResultStatBlock("未答", "$currentUnanswered", Color.Yellow)
                        ResultStatBlock("正确率", "${currentRateText}%")  // ✅ 使用定义的变量
                    }
                }
            }

            // 整张考试卡片
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(overallLabel, style = MaterialTheme.typography.titleSmall.copy(fontSize = LocalFontSize.current * 0.9f, fontFamily = LocalFontFamily.current), color = MaterialTheme.colorScheme.primary)
                    Text("$overallScore / $displayOverallTotal", style = MaterialTheme.typography.displaySmall.copy(fontSize = LocalFontSize.current * 1.2f, fontFamily = LocalFontFamily.current), color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator(progress = if (displayOverallTotal > 0) overallScore.toFloat() / displayOverallTotal.toFloat() else 0f, modifier = Modifier.fillMaxWidth().height(8.dp))
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        if (isExamMode) {
                            // 考试模式：显示考试维度的累计统计
                            // 累计考试次数：应该基于实际的交卷确认行为计算，而不是已答数推算
                            val actualExamCount = cumulativeExamCount ?: run {
                                // 修复：从历史记录正确计算考试次数
                                // 考试次数 = 历史记录数量（每次交卷都会保存一条历史记录）
                                val examHistoryCount = sameFileHistory.size
                                
                                examHistoryCount  // 基于历史记录的实际考试次数
                            }

                            ResultStatBlock("累计答对", "$overallScore", Color.Green)
                            ResultStatBlock("累计答错", "$overallWrong", Color.Red)
                            ResultStatBlock("累计考试次数", "$actualExamCount", MaterialTheme.colorScheme.primary)
                            ResultStatBlock("平均正确率", "${String.format("%.1f", if (overallAnswered > 0) overallScore.toFloat() / overallAnswered.toFloat() * 100 else 0f)}%")
                        } else {
                            // 练习模式：保持原来的显示方式
                            ResultStatBlock("累计答对", "$overallScore", Color.Green)
                            ResultStatBlock("累计答错", "$overallWrong", Color.Red)
                            ResultStatBlock("累计次数", "${sameFileHistory.size}", MaterialTheme.colorScheme.primary)
                            ResultStatBlock("累计正确率", "$overallRateText%")
                        }
                    }
                }
            }

            // 历史成绩走势及列表
            val accuracyList = historyList.map { it.score.toFloat() / it.total.toFloat() }
            if (accuracyList.isNotEmpty()) {
                Text("历史成绩走势", style = MaterialTheme.typography.titleMedium.copy(fontSize = LocalFontSize.current * 0.98f, fontFamily = LocalFontFamily.current), color = MaterialTheme.colorScheme.primary, modifier = Modifier.align(Alignment.Start).padding(bottom = 6.dp))
                val primaryColor = MaterialTheme.colorScheme.primary
                val secondaryColor = MaterialTheme.colorScheme.secondary
                Canvas(modifier = Modifier.fillMaxWidth().height(100.dp)) {
                    val step = if (accuracyList.size > 1) size.width / (accuracyList.size - 1).toFloat() else 0f
                    val points = accuracyList.mapIndexed { idx, v -> Offset(idx * step, size.height - v.coerceIn(0f, 1f) * size.height) }
                    drawLine(Color.DarkGray, Offset(0f, size.height), Offset(size.width, size.height), strokeWidth = 2f)
                    drawLine(Color.DarkGray, Offset(0f, size.height), Offset(0f, 0f), strokeWidth = 2f)
                    for (i in 0 until points.size - 1) drawLine(primaryColor, points[i], points[i + 1], strokeWidth = 4f)
                    points.forEach { drawCircle(secondaryColor, 6f, it) }
                }
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp)) {
                    items(historyList) { h ->
                        val idx = historyList.indexOf(h)
                        val wrong = h.total - h.score - h.unanswered
                        val rate = if (h.total > 0) h.score.toFloat() / h.total.toFloat() else 0f
                        Column(Modifier.padding(vertical = 3.dp)) {
                            Text("${idx + 1}. 正确:${h.score} 错误:${wrong} 正确率:${"%.2f".format(rate)}% 时间:${h.time.format(formatter)}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = LocalFontSize.current * 0.98f, fontFamily = LocalFontFamily.current), color = MaterialTheme.colorScheme.onSurface)
                            Divider(Modifier.padding(vertical = 2.dp))
                        }
                    }
                }
            }
        }
    }
}
