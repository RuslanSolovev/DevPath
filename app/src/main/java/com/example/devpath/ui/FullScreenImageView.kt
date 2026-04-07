package com.example.devpath.ui

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
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

    // Включаем полноэкранный режим при входе
    DisposableEffect(Unit) {
        val activity = context as? MainActivity
        activity?.setFullScreen(true)

        // Поднимаем view на передний план для корректного отображения
        view.bringToFront()

        onDispose {
            activity?.setFullScreen(false)
        }
    }

    BackHandler(enabled = true) {
        navController.popBackStack()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        isUiVisible = !isUiVisible
                    }
                )
            }
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = "Полноэкранное изображение",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )

        if (isUiVisible) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .align(Alignment.TopCenter),
                color = Color.Black.copy(alpha = 0.6f)
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.padding(8.dp)
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Назад",
                        tint = Color.White
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                color = Color.Black.copy(alpha = 0.6f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceEvenly
                ) {
                    IconButton(
                        onClick = {
                            isLoading = true
                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    downloadImage(imageUrl, context)
                                    withContext(Dispatchers.Main) {
                                        android.widget.Toast.makeText(
                                            context,
                                            "Изображение сохранено",
                                            android.widget.Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                } catch (e: Exception) {
                                    withContext(Dispatchers.Main) {
                                        android.widget.Toast.makeText(
                                            context,
                                            "Ошибка сохранения: ${e.message}",
                                            android.widget.Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                } finally {
                                    withContext(Dispatchers.Main) {
                                        isLoading = false
                                    }
                                }
                            }
                        },
                        enabled = !isLoading
                    ) {
                        Icon(
                            Icons.Default.Download,
                            contentDescription = "Скачать",
                            tint = Color.White
                        )
                    }

                    IconButton(
                        onClick = {
                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    val file = downloadImageToTemp(imageUrl, context)
                                    withContext(Dispatchers.Main) {
                                        val shareIntent = android.content.Intent().apply {
                                            action = android.content.Intent.ACTION_SEND
                                            putExtra(android.content.Intent.EXTRA_STREAM, androidx.core.content.FileProvider.getUriForFile(
                                                context,
                                                "${context.packageName}.fileprovider",
                                                file
                                            ))
                                            type = "image/jpeg"
                                            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(android.content.Intent.createChooser(shareIntent, "Поделиться изображением"))
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    withContext(Dispatchers.Main) {
                                        android.widget.Toast.makeText(
                                            context,
                                            "Ошибка: ${e.message}",
                                            android.widget.Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        }
                    ) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = "Поделиться",
                            tint = Color.White
                        )
                    }
                }
            }

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    color = Color.White
                )
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
        FileOutputStream(file).use { outputStream ->
            inputStream.copyTo(outputStream)
        }
        android.media.MediaScannerConnection.scanFile(
            context,
            arrayOf(file.absolutePath),
            null,
            null
        )
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
        FileOutputStream(file).use { outputStream ->
            inputStream.copyTo(outputStream)
        }
        file
    }
}