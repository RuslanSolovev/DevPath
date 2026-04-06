package com.example.devpath.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.devpath.data.repository.LocalThemeRepository
import com.example.devpath.ui.theme.AppTheme
import com.example.devpath.ui.viewmodel.ProgressViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.navigation.NavType
import androidx.navigation.navArgument
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

enum class MainTab2(val title: String) {
    HOME("Главная"),
    CHAT("Чат"),
    TEXTBOOK("Учебник")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val auth = Firebase.auth
    var isAuthenticated by remember { mutableStateOf(auth.currentUser != null) }
    val viewModel: ProgressViewModel = hiltViewModel()
    val chatViewModel: com.example.devpath.ui.viewmodel.ChatsViewModel = hiltViewModel()

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            isAuthenticated = auth.currentUser != null
        }
        auth.addAuthStateListener(listener)
        onDispose { auth.removeAuthStateListener(listener) }
    }

    if (!isAuthenticated) {
        AuthScreen(onSuccess = { isAuthenticated = true })
    } else {
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        var showMainNavigation by remember { mutableStateOf(true) }

        val showBottomBar = showMainNavigation &&
                currentRoute != "profile" &&
                currentRoute != "settings"

        val currentUserId = Firebase.auth.currentUser?.uid ?: ""

        DisposableEffect(Unit) {
            if (currentUserId.isNotEmpty()) {
                chatViewModel.updateUserLastActive(currentUserId)
            }
            val job = CoroutineScope(Dispatchers.IO).launch {
                while (true) {
                    delay(30000)
                    val userId = Firebase.auth.currentUser?.uid ?: ""
                    if (userId.isNotEmpty()) {
                        chatViewModel.updateUserLastActive(userId)
                    }
                }
            }
            onDispose {
                job.cancel()
            }
        }

        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(bottom = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            modifier = Modifier.wrapContentSize(),
                            horizontalArrangement = Arrangement.spacedBy(24.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            MainTab2.entries.forEach { tab ->
                                val isSelected = currentRoute == tab.name
                                Surface(
                                    modifier = Modifier.size(if (isSelected) 56.dp else 48.dp),
                                    shape = CircleShape,
                                    color = if (isSelected)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.8f),
                                    tonalElevation = if (isSelected) 0.dp else 2.dp,
                                    onClick = {
                                        navController.navigate(tab.name) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            when (tab) {
                                                MainTab2.HOME -> Icons.Default.Home
                                                MainTab2.CHAT -> Icons.Default.Chat
                                                MainTab2.TEXTBOOK -> Icons.Default.MenuBook
                                            },
                                            contentDescription = tab.title,
                                            modifier = Modifier.size(if (isSelected) 28.dp else 24.dp),
                                            tint = if (isSelected)
                                                MaterialTheme.colorScheme.onPrimary
                                            else
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        if (isSelected) {
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.BottomCenter)
                                                    .padding(bottom = 6.dp)
                                                    .size(4.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.onPrimary)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                NavHost(
                    navController = navController,
                    startDestination = MainTab2.HOME.name
                ) {
                    composable(MainTab2.HOME.name) {
                        HomeTabScreen(
                            onNavigateToProfile = { navController.navigate("profile") },
                            onNavigateToSettings = { navController.navigate("settings") }
                        )
                    }

                    composable(MainTab2.CHAT.name) {
                        val userId = Firebase.auth.currentUser?.uid ?: ""
                        ChatsScreen(
                            userId = userId,
                            navController = navController
                        )
                    }

                    composable(MainTab2.TEXTBOOK.name) {
                        DevPathNavGraph(
                            navController = rememberNavController(),
                            onNavigationVisibilityChanged = { isVisible ->
                                showMainNavigation = isVisible
                            }
                        )
                    }

                    composable("profile") {
                        ProfileScreen(
                            navController = navController,
                            onNavigateToTabs = { navController.popBackStack() }
                        )
                    }

                    composable("settings") {
                        SettingsScreen(onBack = { navController.popBackStack() })
                    }

                    composable("friends") {
                        FriendsScreen(navController = navController)
                    }

                    composable("search_friends") {
                        SearchFriendsScreen(navController = navController)
                    }

                    composable(
                        route = "chat_detail/{chatId}/{friendId}",
                        arguments = listOf(
                            navArgument("chatId") { type = NavType.StringType },
                            navArgument("friendId") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
                        val friendId = backStackEntry.arguments?.getString("friendId") ?: ""
                        ChatDetailScreen(
                            chatId = chatId,
                            friendId = friendId,
                            navController = navController
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HomeTabScreen(
    onNavigateToProfile: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val currentUser = Firebase.auth.currentUser
    val viewModel: ProgressViewModel = hiltViewModel()
    val announcementRepository =
        remember { com.example.devpath.data.repository.AnnouncementRepository() }
    val chatRepository = remember { com.example.devpath.data.repository.ChatRepository() }

    var displayName by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var announcements by remember {
        mutableStateOf<List<com.example.devpath.domain.models.Announcement>>(
            emptyList()
        )
    }
    var isLoadingAnnouncements by remember { mutableStateOf(true) }
    var currentAnnouncementIndex by remember { mutableStateOf(0) }
    var showCreateDialog by remember { mutableStateOf(false) }

    val OWNER_ID = "nPX20T5lVTVQzjLTINkLp0f9xxI2"

    LaunchedEffect(Unit) {
        if (currentUser != null) {
            announcementRepository.getActiveAnnouncements(currentUser.uid).collect { anns ->
                announcements = anns.sortedByDescending { it.priority }
                isLoadingAnnouncements = false
                if (currentAnnouncementIndex >= anns.size && anns.isNotEmpty()) {
                    currentAnnouncementIndex = 0
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        if (currentUser != null) {
            isLoading = true
            try {
                val userProfile = chatRepository.getUser(currentUser.uid)
                displayName = userProfile?.name ?: currentUser.displayName ?: ""
                println("DEBUG: Загружено имя из Firestore: $displayName")
            } catch (e: Exception) {
                e.printStackTrace()
                displayName = currentUser.displayName ?: ""
            } finally {
                isLoading = false
            }
        } else {
            isLoading = false
        }
    }

    fun dismissAnnouncement(announcement: com.example.devpath.domain.models.Announcement) {
        if (currentUser != null) {
            viewModel.viewModelScope.launch {
                announcementRepository.dismissAnnouncement(
                    announcement.announcementId,
                    currentUser.uid
                )
                val newList =
                    announcements.filter { it.announcementId != announcement.announcementId }
                announcements = newList
                if (newList.isEmpty()) {
                    currentAnnouncementIndex = 0
                } else if (currentAnnouncementIndex >= newList.size) {
                    currentAnnouncementIndex = newList.size - 1
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        // Карточка пользователя
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToProfile() },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (!isLoading) displayName.take(2).uppercase() else "?",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        if (isLoading) {
                            Text(
                                "Загрузка...",
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Text(
                                text = displayName.ifEmpty { "Пользователь" },
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = currentUser?.email ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(onClick = onNavigateToSettings) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "Настройки",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Кнопка создания объявления (только для владельца)
        if (currentUser?.uid == OWNER_ID) {
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { showCreateDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Создать объявление")
            }
        }

        // Диалог создания объявления
        if (showCreateDialog) {
            CreateAnnouncementDialogModern(
                onDismiss = { showCreateDialog = false },
                onCreate = { title, message, type, priority, actionText, actionUrl ->
                    viewModel.viewModelScope.launch {
                        announcementRepository.createAnnouncement(
                            title = title,
                            message = message,
                            ownerId = currentUser!!.uid,
                            type = type,
                            priority = priority,
                            actionText = actionText,
                            actionUrl = actionUrl
                        )
                        showCreateDialog = false
                    }
                }
            )
        }

        // Баннер с объявлениями (карусель)
        if (!isLoadingAnnouncements && announcements.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp, max = 200.dp)
            ) {
                when (announcements[currentAnnouncementIndex].type) {
                    "warning" -> AnnouncementCardWarning(
                        announcement = announcements[currentAnnouncementIndex],
                        onDismiss = { dismissAnnouncement(announcements[currentAnnouncementIndex]) }
                    )
                    "success" -> AnnouncementCardSuccess(
                        announcement = announcements[currentAnnouncementIndex],
                        onDismiss = { dismissAnnouncement(announcements[currentAnnouncementIndex]) }
                    )
                    "event" -> AnnouncementCardEvent(
                        announcement = announcements[currentAnnouncementIndex],
                        onDismiss = { dismissAnnouncement(announcements[currentAnnouncementIndex]) }
                    )
                    "update" -> AnnouncementCardUpdate(
                        announcement = announcements[currentAnnouncementIndex],
                        onDismiss = { dismissAnnouncement(announcements[currentAnnouncementIndex]) }
                    )
                    else -> AnnouncementCardInfo(
                        announcement = announcements[currentAnnouncementIndex],
                        onDismiss = { dismissAnnouncement(announcements[currentAnnouncementIndex]) }
                    )
                }

                if (announcements.size > 1) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                currentAnnouncementIndex = if (currentAnnouncementIndex > 0)
                                    currentAnnouncementIndex - 1
                                else
                                    announcements.size - 1
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.Default.ChevronLeft,
                                contentDescription = "Предыдущее",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            announcements.indices.forEach { index ->
                                Box(
                                    modifier = Modifier
                                        .size(if (currentAnnouncementIndex == index) 8.dp else 6.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (currentAnnouncementIndex == index)
                                                MaterialTheme.colorScheme.primary
                                            else
                                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                        )
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                currentAnnouncementIndex =
                                    if (currentAnnouncementIndex < announcements.size - 1)
                                        currentAnnouncementIndex + 1
                                    else
                                        0
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = "Следующее",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CreateAnnouncementDialogModern(
    onDismiss: () -> Unit,
    onCreate: (title: String, message: String, type: String, priority: Int, actionText: String?, actionUrl: String?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("info") }
    var selectedPriority by remember { mutableStateOf(1) }
    var showAdvanced by remember { mutableStateOf(false) }
    var actionText by remember { mutableStateOf("") }
    var actionUrl by remember { mutableStateOf("") }

    val types = listOf(
        "info" to "ℹ️ Информация",
        "warning" to "⚠️ Предупреждение",
        "success" to "✅ Успех",
        "event" to "📅 Событие",
        "update" to "🔄 Обновление"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Announcement, contentDescription = null, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Создать объявление", style = MaterialTheme.typography.titleLarge)
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Заголовок") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Title, contentDescription = null) }
                )

                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Текст объявления") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) }
                )

                Text("Тип объявления", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    types.forEach { (type, label) ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(label) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Text("Приоритет (1-5)", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    (1..5).forEach { priority ->
                        FilterChip(
                            selected = selectedPriority == priority,
                            onClick = { selectedPriority = priority },
                            label = { Text(priority.toString()) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = when (priority) {
                                    5 -> MaterialTheme.colorScheme.error
                                    4 -> MaterialTheme.colorScheme.errorContainer
                                    else -> MaterialTheme.colorScheme.primaryContainer
                                }
                            )
                        )
                    }
                }

                TextButton(
                    onClick = { showAdvanced = !showAdvanced },
                    modifier = Modifier.align(Alignment.Start)
                ) {
                    Icon(
                        if (showAdvanced) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null
                    )
                    Text(if (showAdvanced) "Скрыть доп. настройки" else "Дополнительные настройки")
                }

                if (showAdvanced) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = actionText,
                                onValueChange = { actionText = it },
                                label = { Text("Текст кнопки действия") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                placeholder = { Text("Например: Узнать больше") }
                            )

                            OutlinedTextField(
                                value = actionUrl,
                                onValueChange = { actionUrl = it },
                                label = { Text("Ссылка (URL)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                placeholder = { Text("https://...") }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && message.isNotBlank()) {
                        onCreate(
                            title,
                            message,
                            selectedType,
                            selectedPriority,
                            actionText.takeIf { it.isNotBlank() },
                            actionUrl.takeIf { it.isNotBlank() }
                        )
                    }
                },
                enabled = title.isNotBlank() && message.isNotBlank()
            ) {
                Icon(Icons.Default.Send, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Опубликовать")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun AnnouncementCardInfo(
    announcement: com.example.devpath.domain.models.Announcement,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Text(
                        text = announcement.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Закрыть",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = announcement.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
fun AnnouncementCardWarning(
    announcement: com.example.devpath.domain.models.Announcement,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Text(
                        text = announcement.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Закрыть",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = announcement.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

@Composable
fun AnnouncementCardSuccess(
    announcement: com.example.devpath.domain.models.Announcement,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        color = Color(0xFF4CAF50).copy(alpha = 0.2f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Text(
                        text = announcement.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Закрыть",
                        modifier = Modifier.size(18.dp),
                        tint = Color(0xFF4CAF50)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = announcement.message,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF2E7D32)
            )
        }
    }
}

@Composable
fun AnnouncementCardEvent(
    announcement: com.example.devpath.domain.models.Announcement,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Event,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Text(
                        text = announcement.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Закрыть",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = announcement.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
fun AnnouncementCardUpdate(
    announcement: com.example.devpath.domain.models.Announcement,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.SystemUpdate,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Text(
                        text = announcement.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Закрыть",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = announcement.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
    }
}

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val themeRepository = LocalThemeRepository.current
    val currentTheme by themeRepository.currentTheme.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Назад",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = "Настройки",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            text = "Тема приложения",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        ThemeOption(
            title = "Системная",
            selected = currentTheme == AppTheme.SYSTEM,
            icon = if (isSystemInDarkTheme()) Icons.Default.DarkMode else Icons.Default.LightMode,
            onClick = { themeRepository.setTheme(AppTheme.SYSTEM) }
        )
        ThemeOption(
            title = "Светлая",
            selected = currentTheme == AppTheme.LIGHT,
            icon = Icons.Default.LightMode,
            onClick = { themeRepository.setTheme(AppTheme.LIGHT) }
        )
        ThemeOption(
            title = "Тёмная",
            selected = currentTheme == AppTheme.DARK,
            icon = Icons.Default.DarkMode,
            onClick = { themeRepository.setTheme(AppTheme.DARK) }
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                coroutineScope.launch {
                    Firebase.auth.signOut()
                    onBack()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Icon(Icons.Default.Logout, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Выйти из аккаунта")
        }
    }
}

@Composable
fun ThemeOption(
    title: String,
    selected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape = MaterialTheme.shapes.medium,
        color = if (selected) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surface,
        onClick = onClick,
        tonalElevation = if (selected) 1.dp else 0.dp
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurface
                )
            }
            if (selected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Выбрано",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun isSystemInDarkTheme(): Boolean {
    return android.content.res.Configuration.UI_MODE_NIGHT_MASK ==
            androidx.compose.ui.platform.LocalConfiguration.current.uiMode and
            android.content.res.Configuration.UI_MODE_NIGHT_YES
}