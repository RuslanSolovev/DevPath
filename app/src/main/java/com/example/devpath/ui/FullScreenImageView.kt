package com.example.devpath.ui

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.devpath.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL

@Composable
fun FullScreenImageView(
    imageUrl: String,
    navController: NavHostController
) {
    val context = LocalContext.current
    val view = LocalView.current
    var isUiVisible by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(false) }

    // Зум
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    DisposableEffect(Unit) {
        val activity = context as? MainActivity
        activity?.setFullScreen(true)
        view.bringToFront()
        onDispose { activity?.setFullScreen(false) }
    }

    BackHandler(enabled = true) { navController.popBackStack() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(onTap = { isUiVisible = !isUiVisible })
            }
    ) {
        // Изображение с зумом
        AsyncImage(
            model = ImageRequest.Builder(context).data(imageUrl).crossfade(true).build(),
            contentDescription = "Полноэкранное изображение",
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                )
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(1f, 5f)
                        offset = if (scale > 1f) offset + pan else Offset.Zero
                    }
                },
            contentScale = ContentScale.Fit
        )

        // Верхняя панель
        AnimatedVisibility(
            visible = isUiVisible,
            enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { -it }),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth().statusBarsPadding(),
                color = Color.Black.copy(alpha = 0.7f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Назад", tint = Color.White, modifier = Modifier.size(24.dp))
                    }
                    Spacer(Modifier.weight(1f))
                    // Индикатор зума
                    if (scale > 1f) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Text(
                                "${(scale * 100).toInt()}%",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // Нижняя панель
        AnimatedVisibility(
            visible = isUiVisible,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth().navigationBarsPadding(),
                color = Color.Black.copy(alpha = 0.7f),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Скачать
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(onClick = {
                            isLoading = true
                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    downloadImage(imageUrl, context)
                                    withContext(Dispatchers.Main) {
                                        android.widget.Toast.makeText(context, "Сохранено в Галерею", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    withContext(Dispatchers.Main) {
                                        android.widget.Toast.makeText(context, "Ошибка сохранения", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                } finally {
                                    withContext(Dispatchers.Main) { isLoading = false }
                                }
                            }
                        }, enabled = !isLoading) {
                            Icon(Icons.Outlined.Download, "Скачать", tint = Color.White, modifier = Modifier.size(28.dp))
                        }
                        Text("Скачать", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                    }

                    // Поделиться
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(onClick = {
                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    val file = downloadImageToTemp(imageUrl, context)
                                    withContext(Dispatchers.Main) {
                                        val shareIntent = android.content.Intent().apply {
                                            action = android.content.Intent.ACTION_SEND
                                            putExtra(android.content.Intent.EXTRA_STREAM, androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file))
                                            type = "image/jpeg"
                                            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(android.content.Intent.createChooser(shareIntent, "Поделиться"))
                                    }
                                } catch (e: Exception) {
                                    withContext(Dispatchers.Main) {
                                        android.widget.Toast.makeText(context, "Ошибка", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }) {
                            Icon(Icons.Outlined.Share, "Поделиться", tint = Color.White, modifier = Modifier.size(28.dp))
                        }
                        Text("Поделиться", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                    }
                }
            }
        }

        // Индикатор загрузки
        if (isLoading) {
            Surface(
                modifier = Modifier.align(Alignment.Center).size(56.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color.Black.copy(alpha = 0.7f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(32.dp), strokeWidth = 3.dp)
                }
            }
        }
    }
}

private suspend fun downloadImage(imageUrl: String, context: android.content.Context) {
    withContext(Dispatchers.IO) {
        val url = URL(imageUrl)
        val connection = url.openConnection()
        connection.connect()
        val inputStream = connection.getInputStream()
        val fileName = "image_${System.currentTimeMillis()}.jpg"
        val picturesDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)
        picturesDir?.mkdirs()
        val file = File(picturesDir, fileName)
        FileOutputStream(file).use { inputStream.copyTo(it) }
        android.media.MediaScannerConnection.scanFile(context, arrayOf(file.absolutePath), null, null)
    }
}

private suspend fun downloadImageToTemp(imageUrl: String, context: android.content.Context): File {
    return withContext(Dispatchers.IO) {
        val url = URL(imageUrl)
        val connection = url.openConnection()
        connection.connect()
        val inputStream = connection.getInputStream()
        val fileName = "temp_image_${System.currentTimeMillis()}.jpg"
        val file = File(context.cacheDir, fileName)
        FileOutputStream(file).use { inputStream.copyTo(it) }
        file
    }
}