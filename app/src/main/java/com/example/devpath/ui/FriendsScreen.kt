package com.example.devpath.ui

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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.devpath.BuildConfig
import com.example.devpath.domain.models.FriendRequest
import com.example.devpath.domain.models.UserProfile
import com.example.devpath.ui.components.UserAvatar
import com.example.devpath.ui.viewmodel.ChatsViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(
    navController: NavHostController
) {
    val currentUserId = Firebase.auth.currentUser?.uid ?: ""
    val viewModel: ChatsViewModel = hiltViewModel()
    val friends by viewModel.friends.collectAsState()
    val incomingRequests by viewModel.incomingRequests.collectAsState()
    val sentRequests by viewModel.sentRequests.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Друзья", "Заявки ко мне", "Мои заявки")

    var selectedFriend by remember { mutableStateOf<UserProfile?>(null) }
    var showFriendDialog by remember { mutableStateOf(false) }

    // Состояния для загрузки данных отправителей/получателей заявок
    var requestSenders by remember { mutableStateOf<Map<String, UserProfile>>(emptyMap()) }
    var requestReceivers by remember { mutableStateOf<Map<String, UserProfile>>(emptyMap()) }
    val context = LocalContext.current

    val chatRepository = remember {
        com.example.devpath.data.repository.ChatRepository(
            yandexStorageClient = com.example.devpath.data.storage.YandexStorageClient(
                context = context,
                accessKey = BuildConfig.YC_ACCESS_KEY,
                secretKey = BuildConfig.YC_SECRET_KEY,
                bucketName = BuildConfig.YC_BUCKET_NAME
            )
        )
    }

    // Загружаем данные пользователей для заявок
    LaunchedEffect(incomingRequests, sentRequests) {
        val senders = mutableMapOf<String, UserProfile>()
        for (request in incomingRequests) {
            val user = chatRepository.getUser(request.fromUserId)
            if (user != null) {
                senders[request.requestId] = user
            }
        }
        requestSenders = senders

        val receivers = mutableMapOf<String, UserProfile>()
        for (request in sentRequests) {
            val user = chatRepository.getUser(request.toUserId)
            if (user != null) {
                receivers[request.requestId] = user
            }
        }
        requestReceivers = receivers
    }

    LaunchedEffect(Unit) {
        viewModel.loadFriends(currentUserId)
        viewModel.loadIncomingRequests(currentUserId)
        viewModel.loadSentRequests(currentUserId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
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
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.SemiBold
                            )
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
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
                            Text(
                                title,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (selectedTab == index) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            when (selectedTab) {
                0 -> FriendsList(
                    friends = friends,
                    currentUserId = currentUserId,
                    viewModel = viewModel,
                    navController = navController,
                    onFriendClick = { friend ->
                        selectedFriend = friend
                        showFriendDialog = true
                    }
                )
                1 -> IncomingRequestsList(
                    requests = incomingRequests,
                    requestUsers = requestSenders,
                    onAccept = { requestId, fromUserId, toUserId ->
                        viewModel.acceptFriendRequest(requestId, fromUserId, toUserId)
                    },
                    onReject = { requestId ->
                        viewModel.rejectFriendRequest(requestId)
                    }
                )
                2 -> SentRequestsList(
                    requests = sentRequests,
                    requestUsers = requestReceivers
                )
            }
        }
    }

    // Диалог для друга (Написать сообщение / Удалить из друзей)
    if (showFriendDialog && selectedFriend != null) {
        AlertDialog(
            onDismissRequest = {
                showFriendDialog = false
                selectedFriend = null
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
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
                TextButton(
                    onClick = {
                        val friend = selectedFriend ?: return@TextButton
                        showFriendDialog = false
                        viewModel.createChatAndNavigate(
                            currentUserId = currentUserId,
                            friendId = friend.userId,
                            navController = navController
                        )
                        selectedFriend = null
                    }
                ) {
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
                        // Удаляем из друзей
                        viewModel.removeFriend(currentUserId, friend.userId)
                        selectedFriend = null
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
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
fun FriendsList(
    friends: List<UserProfile>,
    currentUserId: String,
    viewModel: ChatsViewModel,
    navController: NavHostController,
    onFriendClick: (UserProfile) -> Unit
) {
    if (friends.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
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
                Text(
                    text = "У вас пока нет друзей",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Нажмите на поиск, чтобы найти друзей",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = { navController.navigate("search_friends") },
                    modifier = Modifier.padding(top = 8.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Outlined.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Найти друзей")
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
                    onClick = { onFriendClick(friend) }
                )
            }
        }
    }
}

@Composable
fun FriendItemModern(
    friend: UserProfile,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    var isOnline by remember { mutableStateOf(false) }
    var lastActiveFormatted by remember { mutableStateOf("") }

    val chatRepository = remember {
        com.example.devpath.data.repository.ChatRepository(
            yandexStorageClient = com.example.devpath.data.storage.YandexStorageClient(
                context = context,
                accessKey = BuildConfig.YC_ACCESS_KEY,
                secretKey = BuildConfig.YC_SECRET_KEY,
                bucketName = BuildConfig.YC_BUCKET_NAME
            )
        )
    }

    LaunchedEffect(friend.userId) {
        chatRepository.observeUserOnlineStatus(friend.userId).collect { online ->
            isOnline = online
        }
    }

    LaunchedEffect(friend.userId) {
        lastActiveFormatted = chatRepository.getUserLastActiveFormatted(friend.userId)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
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
                    text = friend.name.ifEmpty { "Пользователь" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (isOnline) "В сети" else "Был(а) $lastActiveFormatted",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isOnline) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
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
                        contentDescription = "Открыть",
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
    requestUsers: Map<String, UserProfile>,
    onAccept: (requestId: String, fromUserId: String, toUserId: String) -> Unit,
    onReject: (requestId: String) -> Unit
) {
    if (requests.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
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
                    text = "Нет входящих заявок",
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
                val user = requestUsers[request.requestId]
                RequestItemModern(
                    request = request,
                    user = user,
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
    requestUsers: Map<String, UserProfile>
) {
    if (requests.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
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
                            Icons.Outlined.Send,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                    }
                }
                Text(
                    text = "Вы не отправляли заявок",
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
                val user = requestUsers[request.requestId]
                RequestItemModern(
                    request = request,
                    user = user,
                    type = "sent"
                )
            }
        }
    }
}

@Composable
fun RequestItemModern(
    request: FriendRequest,
    user: UserProfile?,
    type: String, // "incoming" или "sent"
    onAccept: (() -> Unit)? = null,
    onReject: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Аватар пользователя
            UserAvatar(
                avatarUrl = user?.avatarUrl,
                name = user?.name ?: "Пользователь",
                size = 56
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = if (type == "incoming") {
                        "${user?.name ?: "Пользователь"} хочет добавить вас в друзья"
                    } else {
                        when (request.status) {
                            "pending" -> "Заявка отправлена пользователю ${user?.name ?: "Пользователь"}"
                            "accepted" -> "Пользователь ${user?.name ?: "Пользователь"} принял вашу заявку"
                            "rejected" -> "Пользователь ${user?.name ?: "Пользователь"} отклонил заявку"
                            else -> "Заявка"
                        }
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = formatDate(request.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (type == "incoming" && request.status == "pending") {
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
                                contentDescription = "Принять",
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
                                contentDescription = "Отклонить",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            } else if (request.status == "accepted") {
                Surface(
                    modifier = Modifier.size(32.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Outlined.Check,
                            contentDescription = "Принято",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

private fun formatDate(timestamp: com.google.firebase.Timestamp?): String {
    if (timestamp == null) return ""
    val date = timestamp.toDate()
    val formatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    return formatter.format(date)
}