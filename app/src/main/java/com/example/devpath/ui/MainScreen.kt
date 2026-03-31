package com.example.devpath.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.devpath.data.repository.LocalThemeRepository
import com.example.devpath.domain.models.UserProgress
import com.example.devpath.ui.theme.AppTheme
import com.example.devpath.ui.viewmodel.ProgressViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

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

        // ✅ Добавляем состояние для управления видимостью навигации
        var showMainNavigation by remember { mutableStateOf(true) }

        // Скрываем нижнюю навигацию на экранах профиля и настроек
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
                    composable(MainTab2.CHAT.name) { ChatTabScreen() }
                    composable(MainTab2.TEXTBOOK.name) {
                        // ✅ Передаём колбэк для управления видимостью навигации
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
    var displayName by remember { mutableStateOf(currentUser?.displayName ?: "") }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        if (currentUser != null) {
            isLoading = true
            try {
                val progress = viewModel.progressRepository.loadLocalProgress(currentUser.uid)
                displayName = progress?.displayName ?: currentUser.displayName ?: ""
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        // Карточка пользователя: аватар + имя (клик → профиль), иконка шестерёнки (клик → настройки)
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
                // Аватар + имя – кликабельны для перехода в профиль
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
                            Text("Загрузка...", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
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
                // Иконка настроек (шестерёнка) – переход в настройки
                IconButton(onClick = onNavigateToSettings) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "Настройки",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun ChatTabScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Default.Chat,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
            Text(
                text = "Чаты с друзьями",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Скоро здесь появится общение с другими разработчиками",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
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
        // ✅ Заголовок с кнопкой «Назад»
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