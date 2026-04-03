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
import kotlinx.coroutines.launch
import androidx.navigation.NavType
import androidx.navigation.navArgument

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
                        route = "chat_detail/{chatId}",
                        arguments = listOf(navArgument("chatId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
                        ChatDetailScreen(
                            chatId = chatId,
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

    val OWNER_ID = "lL0cV7ZrlQWKL2kRM1O0bgJgKQ42"

    // Загружаем объявления
    LaunchedEffect(Unit) {
        if (currentUser != null) {
            announcementRepository.getActiveAnnouncements(currentUser.uid).collect { anns ->
                announcements = anns
                isLoadingAnnouncements = false
                if (currentAnnouncementIndex >= anns.size && anns.isNotEmpty()) {
                    currentAnnouncementIndex = 0
                }
            }
        }
    }

    // Загружаем имя пользователя из Firestore
    LaunchedEffect(Unit) {
        if (currentUser != null) {
            isLoading = true
            try {
                // ✅ Загружаем профиль из коллекции users
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

    // Функция для закрытия объявления
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
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        if (isLoading) {
                            Text(
                                "Загрузка...",
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                            )
                        } else {
                            Text(
                                text = displayName.ifEmpty { "Пользователь" },
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
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
                Icon(Icons.Default.Announcement, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Создать объявление")
            }
        }

        // Диалог создания объявления
        if (showCreateDialog) {
            CreateAnnouncementDialog(
                onDismiss = {
                    showCreateDialog = false
                },
                onCreate = { title, message ->
                    viewModel.viewModelScope.launch {
                        announcementRepository.createAnnouncement(title, message, currentUser!!.uid)
                        showCreateDialog = false
                    }
                }
            )
        }

        // Баннер с объявлениями (карусель)
        if (!isLoadingAnnouncements && announcements.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Announcement,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            if (currentAnnouncementIndex < announcements.size) {
                                Text(
                                    text = announcements[currentAnnouncementIndex].title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        IconButton(
                            onClick = {
                                if (currentAnnouncementIndex < announcements.size) {
                                    dismissAnnouncement(announcements[currentAnnouncementIndex])
                                }
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Закрыть",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (currentAnnouncementIndex < announcements.size) {
                        Text(
                            text = announcements[currentAnnouncementIndex].message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    if (announcements.size > 1) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
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
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
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
                                                    MaterialTheme.colorScheme.onPrimaryContainer.copy(
                                                        alpha = 0.5f
                                                    )
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
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CreateAnnouncementDialog(
    onDismiss: () -> Unit,
    onCreate: (title: String, message: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Создать объявление") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Заголовок") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Текст объявления") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank() && message.isNotBlank()) {
                        onCreate(title, message)
                        onDismiss()
                    }
                }
            ) {
                Text("Опубликовать")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
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
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
        }

        Text(
            text = "Тема приложения",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
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