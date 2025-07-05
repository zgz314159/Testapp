package com.example.testapp.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
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
    fileName: String? = null,
    navController: NavController? = null,
    viewModel: FavoriteViewModel = hiltViewModel()
) {
    val favorites = viewModel.favoriteQuestions.collectAsState()
    val fileNames = viewModel.fileNames.collectAsState()
    val filteredFavorites = if (fileName.isNullOrEmpty()) favorites.value else favorites.value.filter { it.question.fileName == fileName }
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            "收藏夹",
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = LocalFontSize.current,
                fontFamily = LocalFontFamily.current
            )
        )
        Spacer(modifier = Modifier.height(16.dp))
        if (fileName.isNullOrEmpty()) {
            if (fileNames.value.isEmpty()) {
                Text(
                    "暂无收藏题目",
                    fontSize = LocalFontSize.current,
                    fontFamily = LocalFontFamily.current
                )
            } else {
                fileNames.value.forEach { name ->
                    val count = favorites.value.count { it.question.fileName == name }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val encoded = java.net.URLEncoder.encode(name, "UTF-8")
                                navController?.navigate("practice_favorite/$encoded")
                            }
                            .padding(vertical = 4.dp)
                    ) {
                        Text(
                            "$name ($count)",
                            modifier = Modifier.weight(1f),
                            fontSize = LocalFontSize.current,
                            fontFamily = LocalFontFamily.current
                        )
                    }
                }
            }
        } else {
            if (filteredFavorites.isEmpty()) {
                Text(
                    "暂无收藏题目",
                    fontSize = LocalFontSize.current,
                    fontFamily = LocalFontFamily.current
                )
            } else {
                filteredFavorites.forEachIndexed { idx, q ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().padding(8.dp)
                        ) {
                            Text(
                                "${idx + 1}. ${q.question.content}",
                                modifier = Modifier.weight(1f),
                                fontSize = LocalFontSize.current,
                                fontFamily = LocalFontFamily.current
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(onClick = { viewModel.removeFavorite(q.question.id) }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
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
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = {
                    val encoded = java.net.URLEncoder.encode(fileName, "UTF-8")
                    navController?.navigate("practice_favorite/$encoded")
                }) {
                    Text(
                        "练习本文件收藏题",
                        fontSize = LocalFontSize.current,
                        fontFamily = LocalFontFamily.current
                    )
                }
            }
        }

    }
}
