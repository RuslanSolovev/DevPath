package com.example.devpath.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@Composable
fun UserAvatar(
    avatarUrl: String?,
    name: String,
    size: Int = 56,
    modifier: Modifier = Modifier,
    showOnlineIndicator: Boolean = false,
    isOnline: Boolean = false
) {
    Box(
        modifier = modifier.size(size.dp),
        contentAlignment = Alignment.Center
    ) {
        // Аватар
        Box(
            modifier = Modifier
                .size(size.dp)
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
            if (!avatarUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = "Аватар пользователя",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = name.take(2).uppercase(),
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = (size * 0.4).sp
                )
            }
        }

        // Индикатор онлайн статуса
        if (showOnlineIndicator) {
            Box(
                modifier = Modifier
                    .size((size * 0.3).dp)
                    .clip(CircleShape)
                    .background(if (isOnline) Color(0xFF10B981) else MaterialTheme.colorScheme.surfaceVariant)
                    .align(Alignment.BottomEnd)
            )
        }
    }
}