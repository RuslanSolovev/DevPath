package com.example.devpath.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.devpath.data.repository.YdbRepository
import com.example.devpath.domain.models.FriendRequest
import com.example.devpath.domain.models.UserProfile
import com.example.devpath.ui.components.UserAvatar
import com.example.devpath.ui.viewmodel.ChatsViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(
    ydbRepository: YdbRepository,
    currentUserId: String,
    navController: NavHostController
) {
    val viewModel: ChatsViewModel = hiltViewModel()
    val friends by viewModel.friends.collectAsState()
    val incomingRequests by viewModel.incomingRequests.collectAsState()
    val sentRequests by viewModel.sentRequests.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Друзья", "Заявки ко мне", "Мои заявки")

    var selectedFriend by remember { mutableStateOf<UserProfile?>(null) }
    var showFriendDialog by remember { mutableStateOf(false) }

    // Состояния загрузки
    var isLoadingFriends by remember { mutableStateOf(true) }
    var isLoadingIncoming by remember { mutableStateOf(true) }
    var isLoadingSent by remember { mutableStateOf(true) }

    LaunchedEffect(currentUserId) {
        isLoadingFriends = true
        isLoadingIncoming = true
        isLoadingSent = true

        viewModel.loadFriends(currentUserId)
        viewModel.loadIncomingRequests(currentUserId)
        viewModel.loadSentRequests(currentUserId)

        delay(800)

        isLoadingFriends = false
        isLoadingIncoming = false
        isLoadingSent = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Outlined.People,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            "Друзья",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Outlined.ArrowBack,
                            contentDescription = "Назад",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { navController.navigate("search_friends") },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                    ) {
                        Icon(
                            Icons.Outlined.Search,
                            contentDescription = "Поиск",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                )
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // Вкладки со счётчиками
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        height = 3.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    title,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = if (selectedTab == index) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (selectedTab == index) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                // Счётчик
                                val count = when (index) {
                                    0 -> friends.size
                                    1 -> incomingRequests.count { it.status == "pending" }
                                    2 -> sentRequests.count { it.status == "pending" }
                                    else -> 0
                                }
                                if (count > 0) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = when {
                                            index == 1 -> MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                                            else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                        }
                                    ) {
                                        Text(
                                            text = count.toString(),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = when {
                                                index == 1 -> MaterialTheme.colorScheme.error
                                                else -> MaterialTheme.colorScheme.primary
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    )
                }
            }

            // Контент вкладок
            when (selectedTab) {
                0 -> {
                    if (isLoadingFriends) {
                        LoadingContent(message = "Загружаем друзей...")
                    } else {
                        FriendsList(
                            friends = friends,
                            ydbRepository = ydbRepository,
                            onFriendClick = { friend ->
                                selectedFriend = friend
                                showFriendDialog = true
                            }
                        )
                    }
                }
                1 -> {
                    if (isLoadingIncoming) {
                        LoadingContent(message = "Загружаем входящие заявки...")
                    } else {
                        IncomingRequestsList(
                            requests = incomingRequests,
                            ydbRepository = ydbRepository,
                            onAccept = { requestId, fromUserId, toUserId ->
                                viewModel.acceptFriendRequest(requestId, fromUserId, toUserId)
                            },
                            onReject = { viewModel.rejectFriendRequest(it) }
                        )
                    }
                }
                2 -> {
                    if (isLoadingSent) {
                        LoadingContent(message = "Загружаем отправленные заявки...")
                    } else {
                        SentRequestsList(
                            requests = sentRequests,
                            ydbRepository = ydbRepository
                        )
                    }
                }
            }
        }
    }

    // Диалог для друга
    if (showFriendDialog && selectedFriend != null) {
        AlertDialog(
            onDismissRequest = { showFriendDialog = false; selectedFriend = null },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    UserAvatar(
                        avatarUrl = selectedFriend?.avatarUrl,
                        name = selectedFriend?.name ?: "Пользователь",
                        size = 40
                    )
                    Text(
                        selectedFriend?.name?.ifEmpty { "Пользователь" } ?: "Пользователь",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            text = { Text("Выберите действие") },
            confirmButton = {
                TextButton(onClick = {
                    val friend = selectedFriend ?: return@TextButton
                    showFriendDialog = false
                    viewModel.createChatAndNavigate(
                        currentUserId = currentUserId,
                        friendId = friend.userId,
                        navController = navController
                    )
                    selectedFriend = null
                }) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Outlined.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text("Написать сообщение")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        val friend = selectedFriend ?: return@TextButton
                        showFriendDialog = false
                        viewModel.removeFriend(currentUserId, friend.userId)
                        selectedFriend = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Outlined.PersonRemove, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text("Удалить из друзей")
                    }
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun LoadingContent(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Анимированный индикатор
            Box(
                modifier = Modifier.size(80.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(80.dp),
                    strokeWidth = 3.dp,
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Outlined.People,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    message,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "Пожалуйста, подождите...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Прогресс-бар
            Box(
                modifier = Modifier
                    .width(200.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                val infiniteTransition = rememberInfiniteTransition(label = "loadingBar")
                val progress by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1500, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "progress"
                )
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary
                                )
                            )
                        )
                )
            }
        }
    }
}

@Composable
fun FriendsList(
    friends: List<UserProfile>,
    ydbRepository: YdbRepository,
    onFriendClick: (UserProfile) -> Unit
) {
    if (friends.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    modifier = Modifier.size(80.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Outlined.People,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "У вас пока нет друзей",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        "Добавьте друзей через поиск",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(friends, key = { it.userId }) { friend ->
                FriendItemModern(
                    friend = friend,
                    ydbRepository = ydbRepository,
                    onClick = { onFriendClick(friend) }
                )
            }
        }
    }
}

@Composable
fun FriendItemModern(
    friend: UserProfile,
    ydbRepository: YdbRepository,
    onClick: () -> Unit
) {
    var isOnline by remember { mutableStateOf(false) }
    var lastActiveFormatted by remember { mutableStateOf("") }

    LaunchedEffect(friend.userId) {
        val user = ydbRepository.getUser(friend.userId)
        val lastSeen = user?.optJSONObject("last_seen")?.optString("S")?.toLongOrNull() ?: 0
        isOnline = System.currentTimeMillis() - lastSeen < 120_000

        val date = Date(lastSeen)
        val formatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        lastActiveFormatted = if (lastSeen > 0) formatter.format(date) else ""
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UserAvatar(
                avatarUrl = friend.avatarUrl,
                name = friend.name,
                size = 56,
                showOnlineIndicator = true,
                isOnline = isOnline
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    friend.name.ifEmpty { "Пользователь" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    if (isOnline) "В сети"
                    else if (lastActiveFormatted.isNotEmpty()) "Был(а) $lastActiveFormatted"
                    else "",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isOnline) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Surface(
                modifier = Modifier.size(32.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Outlined.ChevronRight,
                        "Открыть",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun IncomingRequestsList(
    requests: List<FriendRequest>,
    ydbRepository: YdbRepository,
    onAccept: (requestId: String, fromUserId: String, toUserId: String) -> Unit,
    onReject: (requestId: String) -> Unit
) {
    if (requests.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    modifier = Modifier.size(80.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Outlined.PersonAdd,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                    }
                }
                Text(
                    "Нет входящих заявок",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(requests, key = { it.requestId }) { request ->
                RequestItemModern(
                    request = request,
                    ydbRepository = ydbRepository,
                    type = "incoming",
                    onAccept = { onAccept(request.requestId, request.fromUserId, request.toUserId) },
                    onReject = { onReject(request.requestId) }
                )
            }
        }
    }
}

@Composable
fun SentRequestsList(
    requests: List<FriendRequest>,
    ydbRepository: YdbRepository
) {
    if (requests.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Вы не отправляли заявок",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(requests, key = { it.requestId }) { request ->
                RequestItemModern(
                    request = request,
                    ydbRepository = ydbRepository,
                    type = "sent"
                )
            }
        }
    }
}

@Composable
fun RequestItemModern(
    request: FriendRequest,
    ydbRepository: YdbRepository,
    type: String,
    onAccept: (() -> Unit)? = null,
    onReject: (() -> Unit)? = null
) {
    var userName by remember { mutableStateOf("") }
    var userAvatar by remember { mutableStateOf<String?>(null) }
    var isLoadingUser by remember { mutableStateOf(true) }

    LaunchedEffect(request.requestId) {
        isLoadingUser = true
        val userId = if (type == "incoming") request.fromUserId else request.toUserId
        val user = ydbRepository.getUser(userId)
        userName = user?.optJSONObject("name")?.optString("S", "Пользователь") ?: "Пользователь"
        userAvatar = user?.optJSONObject("avatar_url")?.optString("S", "")?.ifEmpty { null }
        delay(300) // Небольшая задержка для плавности
        isLoadingUser = false
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Аватар с индикатором загрузки
            if (isLoadingUser) {
                Box(
                    modifier = Modifier.size(56.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            } else {
                UserAvatar(avatarUrl = userAvatar, name = userName, size = 56)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (isLoadingUser) {
                    // Заглушка для имени
                    Box(
                        modifier = Modifier
                            .width(180.dp)
                            .height(20.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    // Заглушка для даты
                    Box(
                        modifier = Modifier
                            .width(120.dp)
                            .height(14.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    )
                } else {
                    Text(
                        text = if (type == "incoming") "$userName хочет добавить вас в друзья"
                        else when (request.status) {
                            "pending" -> "Заявка отправлена $userName"
                            "accepted" -> "$userName принял(а) вашу заявку"
                            "rejected" -> "$userName отклонил(а) заявку"
                            else -> "Заявка"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        formatDate(request.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (type == "incoming" && request.status == "pending" && !isLoadingUser) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        onClick = { onAccept?.invoke() }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Outlined.Check,
                                "Принять",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                        onClick = { onReject?.invoke() }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Outlined.Close,
                                "Отклонить",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatDate(timestamp: Long): String {
    if (timestamp <= 0) return ""
    val date = Date(timestamp)
    val formatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    return formatter.format(date)
}