package com.example.devpath.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.devpath.domain.models.Reaction

val availableReactions = listOf(
    "👍" to "👍", // Like
    "❤️" to "❤️", // Heart
    "😂" to "😂", // Laugh
    "😮" to "😮", // Surprise
    "😢" to "😢", // Sad
    "😡" to "😡"  // Angry
)

@Composable
fun MessageReactions(
    reactions: List<Reaction>,
    currentUserId: String,
    onAddReaction: (String) -> Unit,
    onRemoveReaction: () -> Unit
) {
    val userReaction = reactions.find { it.userId == currentUserId }
    val groupedReactions = reactions.groupBy { it.reaction }
        .map { (reaction, list) -> reaction to list.size }

    if (groupedReactions.isNotEmpty() || userReaction != null) {
        Row(
            modifier = Modifier
                .wrapContentSize()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Отображаем все реакции
            groupedReactions.forEach { (reaction, count) ->
                ReactionChip(
                    reaction = reaction,
                    count = count,
                    isSelected = userReaction?.reaction == reaction,
                    onClick = {
                        if (userReaction?.reaction == reaction) {
                            onRemoveReaction()
                        } else {
                            onAddReaction(reaction)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ReactionChip(
    reaction: String,
    count: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else Color.Transparent
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = reaction,
            fontSize = 14.sp
        )
        if (count > 1) {
            Text(
                text = count.toString(),
                fontSize = 10.sp,
                color = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ReactionPickerPopup(
    onReactionSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    androidx.compose.material3.DropdownMenu(
        expanded = true,
        onDismissRequest = onDismiss,
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface)
            .clip(RoundedCornerShape(20.dp))
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            availableReactions.forEach { (reaction, _) ->
                Text(
                    text = reaction,
                    fontSize = 28.sp,
                    modifier = Modifier
                        .clickable {
                            onReactionSelected(reaction)
                            onDismiss()
                        }
                        .padding(4.dp)
                )
            }
        }
    }
}