package com.example.devpath.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.devpath.R
import com.example.devpath.domain.models.UserProgress
import com.example.devpath.domain.models.UserProfile
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.devpath.ui.viewmodel.ProgressViewModel
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    onNavigateToTabs: () -> Unit
) {
    val currentUser = Firebase.auth.currentUser
    val context = LocalContext.current
    val viewModel: ProgressViewModel = hiltViewModel()
    val progressRepo = viewModel.progressRepository
    val coroutineScope = rememberCoroutineScope()

    // ChatRepository для работы с аватарами
    val chatRepository = remember {
        com.example.devpath.data.repository.ChatRepository(
            yandexStorageClient = com.example.devpath.data.storage.YandexStorageClient(
                context = context,
                accessKey = com.example.devpath.BuildConfig.YC_ACCESS_KEY,
                secretKey = com.example.devpath.BuildConfig.YC_SECRET_KEY,
                bucketName = com.example.devpath.BuildConfig.YC_BUCKET_NAME
            )
        )
    }

    // Загружаем существующий прогресс пользователя
    var existingProgress by remember { mutableStateOf<UserProgress?>(null) }
    var userProfile by remember { mutableStateOf<UserProfile?>(null) }
    var displayName by remember { mutableStateOf("") }
    var avatarUrl by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var isUploadingAvatar by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showSuccess by remember { mutableStateOf(false) }

    // Состояние для выбора изображения
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var showAvatarDialog by remember { mutableStateOf(false) }

    // Лаунчер для выбора изображения из галереи
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            showAvatarDialog = true
        }
    }

    // Загружаем существующий прогресс и профиль при входе
    LaunchedEffect(Unit) {
        if (currentUser != null) {
            try {
                // Загружаем прогресс
                val progress = progressRepo.loadLocalProgress(currentUser.uid)
                existingProgress = progress
                displayName = progress?.displayName ?: currentUser.displayName ?: ""

                // Загружаем профиль пользователя из Firestore
                val profile = chatRepository.getUser(currentUser.uid)
                userProfile = profile
                avatarUrl = profile?.avatarUrl

                println("DEBUG: Загружен профиль: аватар=${avatarUrl}")
            } catch (e: Exception) {
                println("DEBUG: Ошибка загрузки: ${e.message}")
                displayName = currentUser.displayName ?: ""
            }
        }
    }

    // Диалог для подтверждения загрузки аватара
    if (showAvatarDialog && selectedImageUri != null) {
        AlertDialog(
            onDismissRequest = {
                showAvatarDialog = false
                selectedImageUri = null
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Rounded.AccountCircle,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Установить аватар", style = MaterialTheme.typography.titleMedium)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Превью изображения
                    Card(
                        modifier = Modifier
                            .size(200.dp)
                            .shadow(8.dp, CircleShape),
                        shape = CircleShape,
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = "Превью аватара",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Text(
                        text = "Это изображение будет использоваться как ваш аватар",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            isUploadingAvatar = true
                            try {
                                selectedImageUri?.let { uri ->
                                    // Загружаем изображение в Yandex Cloud
                                    val imageUrl = chatRepository.uploadImageAndGetUrl(uri, context.contentResolver)

                                    // Сохраняем URL аватара в Firestore
                                    val db = Firebase.firestore
                                    val userDocRef = db.collection("users").document(currentUser!!.uid)
                                    userDocRef.update("avatarUrl", imageUrl).await()

                                    // Обновляем локальное состояние
                                    avatarUrl = imageUrl
                                    userProfile = userProfile?.copy(avatarUrl = imageUrl)

                                    // Показываем сообщение об успехе
                                    showSuccess = true
                                    isUploadingAvatar = false
                                    showAvatarDialog = false
                                    selectedImageUri = null

                                    // Скрываем сообщение через 2 секунды
                                    kotlinx.coroutines.delay(2000)
                                    showSuccess = false
                                }
                            } catch (e: Exception) {
                                errorMessage = "Ошибка загрузки аватара: ${e.message}"
                                isUploadingAvatar = false
                                showAvatarDialog = false
                                selectedImageUri = null

                                // Скрываем ошибку через 3 секунды
                                kotlinx.coroutines.delay(3000)
                                errorMessage = null
                            }
                        }
                    },
                    enabled = !isUploadingAvatar,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    if (isUploadingAvatar) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Загрузка...")
                    } else {
                        Icon(Icons.Rounded.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Установить")
                    }
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showAvatarDialog = false
                        selectedImageUri = null
                    }
                ) {
                    Text("Отмена")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "Настройка профиля",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            "Создайте свою личность в DevPath",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Назад",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Декоративный фон
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                Color.Transparent
                            )
                        )
                    )
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    // Аватар пользователя с возможностью изменения
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(140.dp)
                                .shadow(
                                    elevation = 8.dp,
                                    shape = CircleShape,
                                    clip = true
                                )
                                .clip(CircleShape)
                                .clickable { imagePickerLauncher.launch("image/*") }
                        ) {
                            // Отображаем аватар
                            if (avatarUrl != null && avatarUrl!!.isNotEmpty()) {
                                AsyncImage(
                                    model = avatarUrl,
                                    contentDescription = "Аватар пользователя",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                // Дефолтный аватар
                                Card(
                                    modifier = Modifier.fillMaxSize(),
                                    shape = CircleShape,
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = displayName.take(2).uppercase(),
                                            fontSize = 48.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }

                            // Иконка камеры для изменения аватара
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                                    .border(
                                        width = 3.dp,
                                        color = MaterialTheme.colorScheme.surface,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Rounded.CameraAlt,
                                    contentDescription = "Изменить аватар",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // Бейдж онлайн статуса
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981))
                                .border(
                                    width = 2.dp,
                                    color = MaterialTheme.colorScheme.surface,
                                    shape = CircleShape
                                )
                                .align(Alignment.BottomEnd)
                                .offset(x = (-8).dp, y = (-8).dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Подсказка для изменения аватара
                    Text(
                        text = "Нажмите на аватар, чтобы изменить",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Показать сообщение об ошибке если есть
                if (errorMessage != null) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.Warning,
                                    contentDescription = "Внимание",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(24.dp)
                                )
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        "Ошибка",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Text(
                                        errorMessage ?: "",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                    }
                }

                // Показать сообщение об успехе если есть
                if (showSuccess) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f)
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.CheckCircle,
                                    contentDescription = "Успех",
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        "Данные сохранены",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    Text(
                                        "Ваш профиль обновлен!",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    // Карточка с информацией о пользователе
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.Person,
                                    contentDescription = "Имя",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        "Ваше имя",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Medium
                                        ),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        "Будет отображаться во всех разделах",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                            }

                            OutlinedTextField(
                                value = displayName,
                                onValueChange = { displayName = it },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                                    cursorColor = MaterialTheme.colorScheme.primary
                                ),
                                placeholder = {
                                    Text(
                                        "Например, Алексей",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Rounded.Edit,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Medium
                                )
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.Email,
                                    contentDescription = "Email",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        "Email",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Medium
                                        ),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        currentUser?.email ?: "Не указан",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                // Статистика пользователя (показываем, если есть прогресс)
                if (existingProgress != null && (existingProgress?.completedLessons?.isNotEmpty() == true ||
                            existingProgress?.totalXP ?: 0 > 0)) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        Icons.Rounded.Star,
                                        contentDescription = "Статистика",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Text(
                                        "Ваш прогресс",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }

                                Column(
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        "✅ Пройдено уроков: ${existingProgress?.completedLessons?.size ?: 0}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        "💻 Решено задач: ${existingProgress?.completedPracticeTasks?.size ?: 0}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        "📝 Пройдено тестов: ${existingProgress?.generalTestHistory?.size ?: 0}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        "⭐ Всего XP: ${existingProgress?.totalXP ?: 0}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        "🏆 Уровень: ${existingProgress?.level ?: 1}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        "🔥 Дней подряд: ${existingProgress?.dailyStreak ?: 0}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    // Карточка с информацией о приложении
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.2f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.Info,
                                    contentDescription = "Информация",
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    "💡 Советы для начала",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }

                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                TipItem(
                                    icon = Icons.Rounded.School,
                                    text = "Начните с основ Kotlin в разделе 'Обучение'",
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                                TipItem(
                                    icon = Icons.Rounded.Quiz,
                                    text = "Проверьте знания в тестах и собеседованиях",
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                                TipItem(
                                    icon = Icons.Rounded.Code,
                                    text = "Закрепите навыки на практике с реальными задачами",
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }
                    }
                }

                item {
                    Button(
                        onClick = {
                            if (currentUser != null && displayName.isNotBlank()) {
                                isLoading = true
                                errorMessage = null
                                showSuccess = false

                                coroutineScope.launch {
                                    try {
                                        // Обновляем существующий прогресс локально
                                        val currentProgress = existingProgress ?: UserProgress.createEmpty(currentUser.uid)
                                        val updatedProgress = currentProgress.copy(
                                            displayName = displayName.trim()
                                        )

                                        println("DEBUG: Сохраняем прогресс локально: имя='${updatedProgress.displayName}', уроков=${updatedProgress.completedLessons.size}, XP=${updatedProgress.totalXP}")

                                        // Сохраняем локально
                                        progressRepo.saveProgress(updatedProgress)

                                        // Обновляем существующий прогресс в памяти
                                        existingProgress = updatedProgress

                                        // Обновляем профиль пользователя в Firestore
                                        try {
                                            val db = Firebase.firestore
                                            val userProfileData = hashMapOf(
                                                "name" to displayName.trim(),
                                                "nameLowercase" to displayName.trim().lowercase()
                                            )
                                            db.collection("users")
                                                .document(currentUser.uid)
                                                .set(userProfileData, com.google.firebase.firestore.SetOptions.merge())
                                                .await()

                                            println("DEBUG: Профиль обновлен в Firestore")
                                        } catch (e: Exception) {
                                            println("DEBUG: Не удалось обновить профиль в Firestore: ${e.message}")
                                        }

                                        // Попытка обновить FirebaseAuth
                                        try {
                                            val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                                                .setDisplayName(displayName.trim())
                                                .build()
                                            currentUser.updateProfile(profileUpdates).await()
                                            println("DEBUG: Имя обновлено в FirebaseAuth")
                                        } catch (e: Exception) {
                                            println("DEBUG: Не удалось обновить имя в FirebaseAuth: ${e.message}")
                                        }

                                        showSuccess = true
                                        isLoading = false

                                        kotlinx.coroutines.delay(1000)
                                        onNavigateToTabs()

                                    } catch (e: Exception) {
                                        println("DEBUG: Ошибка сохранения: ${e.message}")
                                        errorMessage = "Ошибка сохранения. Попробуйте позже."
                                        isLoading = false
                                    }
                                }
                            }
                        },
                        enabled = displayName.isNotBlank() && !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.RocketLaunch,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    if (existingProgress?.completedLessons?.isNotEmpty() == true)
                                        "Продолжить обучение"
                                    else
                                        "Начать обучение",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "Ваши достижения и прогресс сохраняются автоматически даже без интернета",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TipItem(
    icon: ImageVector,
    text: String,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
        }

        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
            modifier = Modifier.weight(1f)
        )
    }
}