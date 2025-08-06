package com.smsclassifier.app.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.smsclassifier.app.model.SmsCategory
import com.smsclassifier.app.model.SmsMessage
import com.smsclassifier.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MessageList(
    messages: List<SmsMessage>,
    category: SmsCategory,
    onMessageClick: (SmsMessage) -> Unit,
    onCopyOtp: (String) -> Unit,
    onShareOtp: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (messages.isEmpty()) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = getEmptyStateIcon(category),
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = "No ${category.displayName.lowercase()} messages",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    } else {
        LazyColumn(
            modifier = modifier,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { message ->
                MessageItem(
                    message = message,
                    onClick = { onMessageClick(message) },
                    onCopyOtp = onCopyOtp,
                    onShareOtp = onShareOtp
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MessageItem(
    message: SmsMessage,
    onClick: () -> Unit,
    onCopyOtp: (String) -> Unit,
    onShareOtp: (String, String) -> Unit
) {
    val categoryColor = getCategoryColor(message.category)
    val categoryColorLight = getCategoryColorLight(message.category)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (message.isRead) {
                MaterialTheme.colorScheme.surface
            } else {
                categoryColorLight.copy(alpha = 0.1f)
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (message.isRead) 2.dp else 4.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with sender and timestamp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Category indicator
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(
                                color = categoryColor,
                                shape = RoundedCornerShape(50)
                            )
                    )
                    
                    Text(
                        text = message.sender,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (message.isRead) FontWeight.Normal else FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Text(
                    text = formatTimestamp(message.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Message body
            Text(
                text = message.body,
                style = MaterialTheme.typography.bodyMedium,
                color = if (message.isRead) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            // OTP Actions (if OTP message)
            if (message.category == SmsCategory.OTP) {
                val otpCode = message.extractOtp()
                if (otpCode != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AssistChip(
                            onClick = { onCopyOtp(otpCode) },
                            label = { Text("Copy $otpCode") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                        
                        AssistChip(
                            onClick = { onShareOtp(otpCode, message.sender) },
                            label = { Text("Share") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun getCategoryColor(category: SmsCategory): Color {
    return when (category) {
        SmsCategory.OTP -> OtpColor
        SmsCategory.SPAM -> SpamColor
        SmsCategory.PROMOTIONAL -> PromotionalColor
        SmsCategory.TRANSACTIONAL -> TransactionalColor
        SmsCategory.PERSONAL -> PersonalColor
    }
}

@Composable
private fun getCategoryColorLight(category: SmsCategory): Color {
    return when (category) {
        SmsCategory.OTP -> OtpColorLight
        SmsCategory.SPAM -> SpamColorLight
        SmsCategory.PROMOTIONAL -> PromotionalColorLight
        SmsCategory.TRANSACTIONAL -> TransactionalColorLight
        SmsCategory.PERSONAL -> PersonalColorLight
    }
}

private fun getEmptyStateIcon(category: SmsCategory): androidx.compose.ui.graphics.vector.ImageVector {
    return when (category) {
        SmsCategory.OTP -> Icons.Default.Security
        SmsCategory.SPAM -> Icons.Default.Block
        SmsCategory.PROMOTIONAL -> Icons.Default.LocalOffer
        SmsCategory.TRANSACTIONAL -> Icons.Default.AccountBalance
        SmsCategory.PERSONAL -> Icons.Default.Person
    }
}

private fun formatTimestamp(timestamp: Date): String {
    val now = Date()
    val diff = now.time - timestamp.time
    
    return when {
        diff < 60_000 -> "Now" // Less than 1 minute
        diff < 3600_000 -> "${diff / 60_000}m ago" // Less than 1 hour
        diff < 86400_000 -> "${diff / 3600_000}h ago" // Less than 1 day
        else -> SimpleDateFormat("MMM dd", Locale.getDefault()).format(timestamp)
    }
}