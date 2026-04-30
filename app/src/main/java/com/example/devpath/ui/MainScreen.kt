package com.example.devpath.ui

import android.net.Uri
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.devpath.data.repository.LocalThemeRepository
import com.example.devpath.data.repository.YdbRepository
import com.example.devpath.ui.components.UserAvatar
import com.example.devpath.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import androidx.navigation.NavType
import androidx.navigation.navArgument
import org.json.JSONArray
import org.json.JSONObject
import com.example.devpath.utils.Config
import java.util.UUID

enum class MainTab2(val title: String) {
    HOME("Главная"),
    CHAT("Чат"),
    TEXTBOOK("Учебник")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    // ✅ Получаем репозиторий через Hilt (если используешь Hilt)
    // Если нет — оставляем remember, но тогда убери @Inject из YdbRepository
    val ydbRepository = remember { YdbRepository() }

    var isAuthenticated by remember { mutableStateOf(false) }
    var currentUserId by remember { mutableStateOf("") }
    var currentUserName by remember { mutableStateOf("") }
    var currentUserEmail by remember { mutableStateOf("") }
    var currentUserAvatar by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Проверка авторизации при запуске
    LaunchedEffect(Unit) {
        val prefs = context.getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
        val userId = prefs.getString("user_id", null)
        if (userId != null) {
            currentUserId = userId
            currentUserName = prefs.getString("user_name", "") ?: ""
            currentUserEmail = prefs.getString("user_email", "") ?: ""
            currentUserAvatar = prefs.getString("user_avatar", "") ?: ""
            isAuthenticated = true
        }
    }

    if (!isAuthenticated) {
        AuthScreen(
            ydbRepository = ydbRepository,
            onSuccess = { userId ->
                currentUserId = userId
                isAuthenticated = true
            }
        )
    } else {
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        var showMainNavigation by remember { mutableStateOf(true) }

        val isHiddenRoute = currentRoute == "profile" ||
                currentRoute == "settings" ||
                currentRoute == "step_counter" ||
                currentRoute == "map" ||
                currentRoute == "games_hub" ||
                currentRoute == "journey_map" ||
                currentRoute?.startsWith("chat_detail") == true ||
                currentRoute?.startsWith("fullscreen_image") == true

        LaunchedEffect(currentRoute) {
            println("DEBUG: currentRoute = $currentRoute")
        }

        val showBottomBar = showMainNavigation && !isHiddenRoute

        Scaffold(
            bottomBar = {
                val shouldShowBottomBar = showBottomBar &&
                        currentRoute != null &&
                        currentRoute != "profile" &&
                        currentRoute != "settings" &&
                        currentRoute != "step_counter" &&
                        currentRoute != "map" &&
                        currentRoute != "games_hub" &&
                        !currentRoute.startsWith("journey_map") &&
                        !currentRoute.startsWith("chat_detail") &&
                        !currentRoute.startsWith("fullscreen_image")

                if (shouldShowBottomBar) {
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
                            ydbRepository = ydbRepository,
                            currentUserId = currentUserId,
                            currentUserName = currentUserName,
                            currentUserEmail = currentUserEmail,
                            currentUserAvatar = currentUserAvatar,
                            onNavigateToProfile = { navController.navigate("profile") },
                            onNavigateToSettings = { navController.navigate("settings") },
                            onNavigateToStepCounter = { navController.navigate("step_counter") },
                            onNavigateToMap = { navController.navigate("map") },
                            onNavigateToGamesHub = { navController.navigate("games_hub") }
                        )
                    }

                    composable(MainTab2.CHAT.name) {
                        ChatsScreen(
                            ydbRepository = ydbRepository,
                            currentUserId = currentUserId,
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
                            ydbRepository = ydbRepository,
                            currentUserId = currentUserId,
                            navController = navController,
                            onNavigateToTabs = { navController.popBackStack() }
                        )
                    }

                    composable("settings") {
                        SettingsScreen(
                            onBack = { navController.popBackStack() },
                            onLogout = {
                                coroutineScope.launch {
                                    val prefs = context.getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
                                    prefs.edit().clear().apply()
                                    isAuthenticated = false
                                    currentUserId = ""
                                    currentUserName = ""
                                    currentUserEmail = ""
                                    currentUserAvatar = ""
                                }
                            }
                        )
                    }

                    composable("friends") { FriendsScreen(navController = navController) }
                    composable("search_friends") { SearchFriendsScreen(navController = navController) }
                    composable("step_counter") { StepCounterScreen(navController = navController) }
                    composable("map") { MapScreen(navController = navController) }
                    composable("games_hub") { GamesHubScreen(navController = navController) }

                    composable(
                        route = "journey_map/{steps}",
                        arguments = listOf(navArgument("steps") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val steps = backStackEntry.arguments?.getInt("steps") ?: 0
                        JourneyMapScreen(navController = navController, totalSteps = steps)
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
                        ChatDetailScreen(chatId = chatId, friendId = friendId, navController = navController)
                    }

                    composable(
                        route = "search_messages/{chatId}",
                        arguments = listOf(navArgument("chatId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
                        SearchMessagesScreen(chatId = chatId, navController = navController)
                    }

                    composable(
                        route = "fullscreen_image/{imageUrl}",
                        arguments = listOf(navArgument("imageUrl") { type = NavType.StringType; defaultValue = "" })
                    ) { backStackEntry ->
                        val encodedUrl = backStackEntry.arguments?.getString("imageUrl") ?: ""
                        val imageUrl = Uri.decode(encodedUrl)
                        if (imageUrl.isNotEmpty()) {
                            FullScreenImageView(imageUrl = imageUrl, navController = navController)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HomeTabScreen(
    ydbRepository: YdbRepository,
    currentUserId: String,
    currentUserName: String,
    currentUserEmail: String,
    currentUserAvatar: String,
    onNavigateToProfile: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToStepCounter: () -> Unit,
    onNavigateToMap: () -> Unit,
    onNavigateToGamesHub: () -> Unit
) {
    var announcements by remember { mutableStateOf<JSONArray?>(null) }
    var isLoadingAnnouncements by remember { mutableStateOf(true) }
    var displayName by remember { mutableStateOf(currentUserName) }
    var userEmail by remember { mutableStateOf(currentUserEmail) }
    var userAvatar by remember { mutableStateOf(currentUserAvatar) }  // ✅ Эта переменная есть!

    var showCreateDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val OWNER_ID = Config.OWNER_USER_ID

    LaunchedEffect(currentUserId) {
        try {
            println("DEBUG: Загружаю профиль для userId=$currentUserId")
            val user = ydbRepository.getUser(currentUserId)
            println("DEBUG: Данные пользователя: $user")

            // ✅ Обновляем состояние!
            displayName = user?.optJSONObject("name")?.optString("S", "Пользователь") ?: "Пользователь"
            userEmail = user?.optJSONObject("email")?.optString("S", "") ?: ""
            val avatar = user?.optJSONObject("avatar_url")?.optString("S", "")

            println("DEBUG: Загружено - name=$displayName, email=$userEmail, avatar=$avatar")

            // ✅ ИСПРАВЛЕНО: avatarUrl -> userAvatar
            if (!avatar.isNullOrEmpty()) {
                userAvatar = avatar
            }
        } catch (e: Exception) {
            println("DEBUG: Ошибка загрузки профиля: ${e.message}")
        }
    }

    fun dismissAnnouncement(announcementId: String) {
        announcements = null
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
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
                    modifier = Modifier.weight(1f).clickable { onNavigateToProfile() },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    UserAvatar(avatarUrl = userAvatar, name = displayName, size = 48)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = displayName.ifEmpty { "Пользователь" }, fontWeight = FontWeight.Bold)
                        Text(
                            text = userEmail,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(onClick = onNavigateToSettings) {
                    Icon(Icons.Default.Settings, "Настройки", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // Карточки быстрого доступа
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                QuickAccessCard(
                    title = "Шагомер",
                    subtitle = "Считай шаги",
                    icon = Icons.Outlined.DirectionsWalk,
                    gradient = listOf(Color(0xFF4CAF50), Color(0xFF2E7D32)),
                    onClick = onNavigateToStepCounter
                )
            }
            item {
                QuickAccessCard(
                    title = "Карта",
                    subtitle = "Друзья рядом",
                    icon = Icons.Outlined.Map,
                    gradient = listOf(Color(0xFF2196F3), Color(0xFF0D47A1)),
                    onClick = onNavigateToMap
                )
            }
            item {
                QuickAccessCard(
                    title = "Игры",
                    subtitle = "Мини-игры",
                    icon = Icons.Outlined.SportsEsports,
                    gradient = listOf(Color(0xFFFF9800), Color(0xFFE65100)),
                    onClick = onNavigateToGamesHub
                )
            }
        }

        // Кнопка создания объявления (только для владельца)
        if (currentUserId == OWNER_ID) {
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

        // ✅ Диалог создания объявления (если нужно - реализуй здесь)
        if (showCreateDialog) {
            AlertDialog(
                onDismissRequest = { showCreateDialog = false },
                title = { Text("Новое объявление") },
                text = { Text("Функция в разработке...") },
                confirmButton = {
                    TextButton(onClick = { showCreateDialog = false }) {
                        Text("OK")
                    }
                }
            )
        }

        // Объявления
        if (!isLoadingAnnouncements && announcements != null && announcements!!.length() > 0) {
            val ann = announcements!!.getJSONObject(0)
            val type = ann.optString("type", "info")
            val title = ann.optString("title", "")
            val message = ann.optString("message", "")
            val actionText = ann.optString("action_text", "")
            val actionUrl = ann.optString("action_url", "")

            when (type) {
                "warning" -> ModernAnnouncementCard(
                    title = title,
                    message = message,
                    actionText = actionText,
                    actionUrl = actionUrl,
                    icon = Icons.Default.Warning,
                    iconColor = MaterialTheme.colorScheme.error,
                    backgroundColor = MaterialTheme.colorScheme.errorContainer,
                    onDismiss = { dismissAnnouncement(ann.optString("announcement_id")) }
                )
                "success" -> ModernAnnouncementCard(
                    title = title,
                    message = message,
                    actionText = actionText,
                    actionUrl = actionUrl,
                    icon = Icons.Default.CheckCircle,
                    iconColor = Color(0xFF4CAF50),
                    backgroundColor = Color(0xFF4CAF50).copy(alpha = 0.1f),
                    onDismiss = { dismissAnnouncement(ann.optString("announcement_id")) }
                )
                else -> ModernAnnouncementCard(
                    title = title,
                    message = message,
                    actionText = actionText,
                    actionUrl = actionUrl,
                    icon = Icons.Default.Info,
                    iconColor = MaterialTheme.colorScheme.primary,
                    backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                    onDismiss = { dismissAnnouncement(ann.optString("announcement_id")) }
                )
            }
        }
    }
}

@Composable
fun QuickAccessCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    gradient: List<Color>,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(120.dp)
            .height(130.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(colors = gradient)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(36.dp), tint = Color.White)
                Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
                Text(text = subtitle, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f))
            }
        }
    }
}

@Composable
private fun ModernAnnouncementCard(
    title: String,
    message: String,
    actionText: String,
    actionUrl: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    backgroundColor: Color,
    onDismiss: () -> Unit
) {
    var showFullMessage by remember { mutableStateOf(false) }
    val isLongMessage = message.length > 120
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor.copy(alpha = 0.95f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp, pressedElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(44.dp),
                        shape = CircleShape,
                        color = iconColor.copy(alpha = 0.15f),
                        shadowElevation = 2.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
                        }
                    }
                    Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold,
                        color = when (iconColor) {
                            MaterialTheme.colorScheme.error -> MaterialTheme.colorScheme.onErrorContainer
                            Color(0xFF4CAF50) -> Color(0xFF2E7D32)
                            else -> MaterialTheme.colorScheme.onPrimaryContainer
                        })
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Close, "Закрыть", modifier = Modifier.size(18.dp),
                        tint = when (iconColor) {
                            MaterialTheme.colorScheme.error -> MaterialTheme.colorScheme.onErrorContainer
                            Color(0xFF4CAF50) -> Color(0xFF2E7D32)
                            else -> MaterialTheme.colorScheme.onPrimaryContainer
                        }.copy(alpha = 0.7f))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (showFullMessage || !isLongMessage) message else message.take(120) + "...",
                style = MaterialTheme.typography.bodyMedium,
                color = when (iconColor) {
                    MaterialTheme.colorScheme.error -> MaterialTheme.colorScheme.onErrorContainer
                    Color(0xFF4CAF50) -> Color(0xFF1B5E20)
                    else -> MaterialTheme.colorScheme.onPrimaryContainer
                }.copy(alpha = 0.9f),
                lineHeight = 20.sp
            )
            if (isLongMessage) {
                TextButton(onClick = { showFullMessage = !showFullMessage },
                    modifier = Modifier.padding(top = 4.dp),
                    contentPadding = PaddingValues(horizontal = 0.dp, vertical = 4.dp)) {
                    Text(if (showFullMessage) "Свернуть" else "Читать далее",
                        style = MaterialTheme.typography.labelMedium, color = iconColor)
                }
            }
            if (actionText.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth().clickable {
                        actionUrl.let { url ->
                            try { context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url))) }
                            catch (e: Exception) { e.printStackTrace() }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = iconColor.copy(alpha = 0.12f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        Text(actionText, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium, color = iconColor)
                        Icon(Icons.Default.ArrowForward, null, modifier = Modifier.size(18.dp), tint = iconColor)
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(onBack: () -> Unit, onLogout: () -> Unit) {
    val themeRepository = LocalThemeRepository.current
    val currentTheme by themeRepository.currentTheme.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Назад", tint = MaterialTheme.colorScheme.onSurface)
            }
            Text(text = "Настройки", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }

        Text(text = "Тема приложения", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        ThemeOption(title = "Системная", selected = currentTheme == AppTheme.SYSTEM,
            icon = if (android.content.res.Configuration.UI_MODE_NIGHT_MASK ==
                LocalContext.current.resources.configuration.uiMode and
                android.content.res.Configuration.UI_MODE_NIGHT_YES) Icons.Default.DarkMode else Icons.Default.LightMode,
            onClick = { themeRepository.setTheme(AppTheme.SYSTEM) })
        ThemeOption(title = "Светлая", selected = currentTheme == AppTheme.LIGHT,
            icon = Icons.Default.LightMode,
            onClick = { themeRepository.setTheme(AppTheme.LIGHT) })
        ThemeOption(title = "Тёмная", selected = currentTheme == AppTheme.DARK,
            icon = Icons.Default.DarkMode,
            onClick = { themeRepository.setTheme(AppTheme.DARK) })

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
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
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        onClick = onClick,
        tonalElevation = if (selected) 1.dp else 0.dp
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(icon, null, tint = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant)
                Text(title, style = MaterialTheme.typography.bodyMedium, color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface)
            }
            if (selected) {
                Icon(Icons.Default.Check, "Выбрано", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}