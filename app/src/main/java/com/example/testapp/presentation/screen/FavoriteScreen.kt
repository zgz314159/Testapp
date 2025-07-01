package com.example.testapp.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.testapp.presentation.component.LocalFontFamily
import com.example.testapp.presentation.component.LocalFontSize

@Composable
fun FavoriteScreen(
    navController: NavController? = null,
    viewModel: FavoriteViewModel = hiltViewModel()
) {
    val favorites = viewModel.favoriteQuestions.collectAsState()
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            "收藏夹",
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = LocalFontSize.current,
                fontFamily = LocalFontFamily.current
            )
        )
        Spacer(modifier = Modifier.height(16.dp))
        if (favorites.value.isEmpty()) {
            Text(
                "暂无收藏题目",
                fontSize = LocalFontSize.current,
                fontFamily = LocalFontFamily.current
            )
        } else {
            favorites.value.forEachIndexed { idx, q ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(8.dp)
                    ) {
                        Text(
                            "${idx + 1}. ${q.content}",
                            modifier = Modifier.weight(1f),
                            fontSize = LocalFontSize.current,
                            fontFamily = LocalFontFamily.current
                        )
                        Button(onClick = { navController?.navigate("question_fav") }) {
                            Text(
                                "开始练习",
                                fontSize = LocalFontSize.current,
                                fontFamily = LocalFontFamily.current
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = { viewModel.removeFavorite(q.id) }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                            Text(
                                "移除",
                                color = MaterialTheme.colorScheme.onError,
                                fontSize = LocalFontSize.current,
                                fontFamily = LocalFontFamily.current
                            )
                        }
                    }
                }
            }
        }
        if (favorites.value.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = { navController?.navigate("question_fav") }) {
                Text(
                    "练习全部收藏题",
                    fontSize = LocalFontSize.current,
                    fontFamily = LocalFontFamily.current
                )
            }
        }
    }
}
