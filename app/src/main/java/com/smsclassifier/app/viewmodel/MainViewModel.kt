package com.smsclassifier.app.viewmodel

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.smsclassifier.app.model.SmsCategory
import com.smsclassifier.app.model.SmsMessage
import com.smsclassifier.app.repository.SmsRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SmsRepository.getInstance()
    private val context = getApplication<Application>().applicationContext

    // Expose messages for each category as StateFlow
    private val _messagesFlow = repository.messages

    fun getMessagesForCategory(category: SmsCategory): StateFlow<List<SmsMessage>> {
        return _messagesFlow.map { messages ->
            messages.filter { it.category == category }
                .sortedByDescending { it.timestamp }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    // Unread counts for each category
    val unreadCounts: StateFlow<Map<SmsCategory, Int>> = _messagesFlow.map { messages ->
        SmsCategory.values().associateWith { category ->
            messages.count { it.category == category && !it.isRead }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyMap()
    )

    // All messages flow
    val allMessages: StateFlow<List<SmsMessage>> = _messagesFlow.map { messages ->
        messages.sortedByDescending { it.timestamp }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    /**
     * Mark a message as read
     */
    fun markAsRead(messageId: String) {
        viewModelScope.launch {
            repository.markAsRead(messageId)
        }
    }

    /**
     * Delete a message
     */
    fun deleteMessage(messageId: String) {
        viewModelScope.launch {
            repository.deleteMessage(messageId)
        }
    }

    /**
     * Copy text to clipboard
     */
    fun copyToClipboard(text: String) {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("SMS Classifier", text)
            clipboard.setPrimaryClip(clip)
            
            // Show toast notification
            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to copy", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Share OTP code
     */
    fun shareOtp(otpCode: String, sender: String) {
        try {
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "OTP from $sender: $otpCode")
                putExtra(Intent.EXTRA_SUBJECT, "OTP Code")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }

            val chooserIntent = Intent.createChooser(shareIntent, "Share OTP via")
            chooserIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(chooserIntent)
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to share", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Search messages
     */
    fun searchMessages(query: String): StateFlow<List<SmsMessage>> {
        return _messagesFlow.map { messages ->
            repository.searchMessages(query)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    /**
     * Get latest message for a category
     */
    fun getLatestMessage(category: SmsCategory): SmsMessage? {
        return repository.getLatestMessage(category)
    }

    /**
     * Get message count for a category
     */
    fun getMessageCount(category: SmsCategory): Int {
        return repository.getMessageCount(category)
    }

    /**
     * Clear all messages (for testing)
     */
    fun clearAllMessages() {
        viewModelScope.launch {
            repository.clearAllMessages()
        }
    }

    /**
     * Add sample messages (for testing)
     */
    fun addSampleMessages() {
        viewModelScope.launch {
            repository.addSampleMessages()
        }
    }
}