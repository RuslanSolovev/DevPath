package com.example.devpath.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.devpath.data.repository.YdbRepository
import com.example.devpath.data.storage.YandexStorageClient
import com.example.devpath.utils.Config
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    ydbRepository: YdbRepository,
    currentUserId: String,
    navController: NavController,
    onNavigateToTabs: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Состояния
    var displayName by remember { mutableStateOf("") }
    var userEmail by remember { mutableStateOf("") }
    var avatarUrl by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var isUploading by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf<String?>(null) }

    // Для выбора изображения
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var showPreviewDialog by remember { mutableStateOf(false) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            showPreviewDialog = true
        }
    }

    // Загрузка данных пользователя при входе
    LaunchedEffect(currentUserId) {
        isLoading = true
        try {
            val user = ydbRepository.getUser(currentUserId)
            displayName = user?.optJSONObject("name")?.optString("S") ?: "Пользователь"
            userEmail = user?.optJSONObject("email")?.optString("S") ?: ""
            avatarUrl = user?.optJSONObject("avatar_url")?.optString("S")?.takeIf { it.isNotEmpty() }
        } catch (e: Exception) {
            showError = "Не удалось загрузить профиль"
        } finally {
            isLoading = false
        }
    }

    // Функция загрузки аватара
    fun uploadAvatar(uri: Uri) {
        coroutineScope.launch {
            isUploading = true
            showError = null
            try {
                val storage = YandexStorageClient(
                    context = context,
                    accessKey = Config.YC_ACCESS_KEY,
                    secretKey = Config.YC_SECRET_KEY,
                    bucketName = Config.YC_BUCKET_NAME
                )

                // Загружаем в Object Storage
                val uploadedUrl = storage.uploadImage(uri, context.contentResolver)

                // Обновляем в YDB
                val updated = ydbRepository.updateUserAvatar(currentUserId, uploadedUrl)

                if (updated) {
                    // Обновляем локально
                    avatarUrl = uploadedUrl

                    // Обновляем SharedPreferences
                    context.getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
                        .edit()
                        .putString("user_avatar", uploadedUrl)
                        .apply()

                    showSuccess = true
                } else {
                    showError = "Не удалось сохранить ссылку в базе"
                }
            } catch (e: Exception) {
                showError = "Ошибка загрузки: ${e.message}"
            } finally {
                isUploading = false
                showPreviewDialog = false
                selectedImageUri = null

                // Скрываем сообщение через 3 секунды
                kotlinx.coroutines.delay(3000)
                showSuccess = false
                showError = null
            }
        }
    }

    // Диалог предпросмотра
    if (showPreviewDialog && selectedImageUri != null) {
        AlertDialog(
            onDismissRequest = { showPreviewDialog = false; selectedImageUri = null },
            title = { Text("Установить аватар") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = "Превью",
                        modifier = Modifier
                            .size(200.dp)
                            .clip(CircleShape)
                            .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Нажмите «Установить» для загрузки", style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                Button(
                    onClick = { uploadAvatar(selectedImageUri!!) },
                    enabled = !isUploading
                ) {
                    if (isUploading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Загрузка...")
                    } else {
                        Text("Установить")
                    }
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showPreviewDialog = false; selectedImageUri = null }) {
                    Text("Отмена")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Профиль", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Назад")
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Аватар
                item {
                    Box(contentAlignment = Alignment.BottomEnd) {
                        if (avatarUrl != null && avatarUrl!!.isNotEmpty()) {
                            AsyncImage(
                                model = avatarUrl,
                                contentDescription = "Аватар",
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape)
                                    .shadow(8.dp, CircleShape)
                                    .clickable { imagePicker.launch("image/*") }
                                    .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .shadow(8.dp, CircleShape)
                                    .clickable { imagePicker.launch("image/*") }
                                    .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = displayName.take(2).uppercase(),
                                    fontSize = 36.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        // Иконка камеры
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                                .clickable { imagePicker.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Rounded.CameraAlt,
                                contentDescription = "Сменить аватар",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Text(
                        "Нажмите для смены",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Сообщения об успехе/ошибке
                item {
                    showSuccess.takeIf { it }?.let {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.CheckCircle, "Успех", tint = MaterialTheme.colorScheme.secondary)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Аватар обновлён!", color = MaterialTheme.colorScheme.onSecondaryContainer)
                            }
                        }
                    }
                    showError.takeIf { !it.isNullOrEmpty() }?.let { msg ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.Error, "Ошибка", tint = MaterialTheme.colorScheme.error)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(msg, color = MaterialTheme.colorScheme.onErrorContainer)
                            }
                        }
                    }
                }

                // Имя
                item {
                    OutlinedTextField(
                        value = displayName,
                        onValueChange = { displayName = it },
                        label = { Text("Имя") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Rounded.Person, null) }
                    )
                }

                // Email (только чтение)
                item {
                    OutlinedTextField(
                        value = userEmail,
                        onValueChange = {},
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        readOnly = true,
                        leadingIcon = { Icon(Icons.Rounded.Email, null) }
                    )
                }

                // Кнопка сохранения имени
                item {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val trimmed = displayName.trim()
                                if (trimmed.isNotEmpty()) {
                                    val ok = ydbRepository.updateUserName(currentUserId, trimmed)
                                    if (ok) {
                                        // Обновляем локально и в SharedPreferences
                                        displayName = trimmed
                                        context.getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
                                            .edit()
                                            .putString("user_name", trimmed)
                                            .apply()
                                        showSuccess = true
                                        kotlinx.coroutines.delay(2000)
                                        showSuccess = false
                                    } else {
                                        showError = "Не удалось сохранить имя"
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = displayName.isNotBlank()
                    ) {
                        Text("Сохранить изменения")
                    }
                }

                // Кнопка выхода
                item {
                    TextButton(
                        onClick = {
                            // Выход
                            context.getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
                                .edit()
                                .clear()
                                .apply()
                            navController.navigate("auth") {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Выйти из аккаунта", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}