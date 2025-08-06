package com.smsclassifier.app.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smsclassifier.app.model.SmsCategory
import com.smsclassifier.app.ui.component.MessageList
import com.smsclassifier.app.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    initialCategory: SmsCategory = SmsCategory.OTP,
    onRequestDefaultSmsApp: () -> Unit,
    viewModel: MainViewModel = viewModel()
) {
    var selectedCategory by remember { mutableStateOf(initialCategory) }
    val messages by viewModel.getMessagesForCategory(selectedCategory).collectAsState()
    val unreadCounts by viewModel.unreadCounts.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = { 
                Text("SMS Classifier") 
            },
            actions = {
                IconButton(onClick = onRequestDefaultSmsApp) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings"
                    )
                }
            }
        )

        // Category Tabs
        TabRow(
            selectedTabIndex = SmsCategory.values().indexOf(selectedCategory),
            modifier = Modifier.fillMaxWidth()
        ) {
            SmsCategory.values().forEach { category ->
                val isSelected = category == selectedCategory
                val unreadCount = unreadCounts[category] ?: 0
                
                Tab(
                    selected = isSelected,
                    onClick = { selectedCategory = category },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = getCategoryIcon(category),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(text = category.displayName)
                            if (unreadCount > 0) {
                                Badge {
                                    Text(
                                        text = if (unreadCount > 99) "99+" else unreadCount.toString(),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    }
                )
            }
        }

        // Messages List
        MessageList(
            messages = messages,
            category = selectedCategory,
            onMessageClick = { message ->
                viewModel.markAsRead(message.id)
            },
            onCopyOtp = { otp ->
                viewModel.copyToClipboard(otp)
            },
            onShareOtp = { otp, sender ->
                viewModel.shareOtp(otp, sender)
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun getCategoryIcon(category: SmsCategory): ImageVector {
    return when (category) {
        SmsCategory.OTP -> Icons.Default.Security
        SmsCategory.SPAM -> Icons.Default.Block
        SmsCategory.PROMOTIONAL -> Icons.Default.LocalOffer
        SmsCategory.TRANSACTIONAL -> Icons.Default.AccountBalance
        SmsCategory.PERSONAL -> Icons.Default.Person
    }
}