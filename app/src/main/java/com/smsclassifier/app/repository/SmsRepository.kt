package com.smsclassifier.app.repository

import com.smsclassifier.app.model.SmsCategory
import com.smsclassifier.app.model.SmsMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentHashMap

/**
 * Repository for managing SMS messages in memory
 * In production, this would be backed by a database like Room
 */
class SmsRepository private constructor() {

    private val _messages = MutableStateFlow<List<SmsMessage>>(emptyList())
    val messages: StateFlow<List<SmsMessage>> = _messages.asStateFlow()

    private val messageMap = ConcurrentHashMap<String, SmsMessage>()

    companion object {
        @Volatile
        private var INSTANCE: SmsRepository? = null

        fun getInstance(): SmsRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SmsRepository().also { INSTANCE = it }
            }
        }
    }

    /**
     * Add a new SMS message
     */
    fun addMessage(message: SmsMessage) {
        messageMap[message.id] = message
        updateMessagesList()
    }

    /**
     * Get messages by category
     */
    fun getMessagesByCategory(category: SmsCategory): List<SmsMessage> {
        return _messages.value.filter { it.category == category }
            .sortedByDescending { it.timestamp }
    }

    /**
     * Get all messages sorted by timestamp (newest first)
     */
    fun getAllMessages(): List<SmsMessage> {
        return _messages.value.sortedByDescending { it.timestamp }
    }

    /**
     * Get message by ID
     */
    fun getMessageById(id: String): SmsMessage? {
        return messageMap[id]
    }

    /**
     * Mark message as read
     */
    fun markAsRead(messageId: String) {
        messageMap[messageId]?.let { message ->
            val updatedMessage = message.copy(isRead = true)
            messageMap[messageId] = updatedMessage
            updateMessagesList()
        }
    }

    /**
     * Delete message
     */
    fun deleteMessage(messageId: String) {
        messageMap.remove(messageId)
        updateMessagesList()
    }

    /**
     * Get unread count by category
     */
    fun getUnreadCount(category: SmsCategory): Int {
        return _messages.value.count { it.category == category && !it.isRead }
    }

    /**
     * Get latest message by category
     */
    fun getLatestMessage(category: SmsCategory): SmsMessage? {
        return getMessagesByCategory(category).firstOrNull()
    }

    /**
     * Search messages by query
     */
    fun searchMessages(query: String): List<SmsMessage> {
        if (query.isBlank()) return getAllMessages()
        
        val lowercaseQuery = query.lowercase()
        return _messages.value.filter { message ->
            message.sender.lowercase().contains(lowercaseQuery) ||
            message.body.lowercase().contains(lowercaseQuery)
        }.sortedByDescending { it.timestamp }
    }

    /**
     * Clear all messages
     */
    fun clearAllMessages() {
        messageMap.clear()
        updateMessagesList()
    }

    /**
     * Get message count by category
     */
    fun getMessageCount(category: SmsCategory): Int {
        return getMessagesByCategory(category).size
    }

    private fun updateMessagesList() {
        _messages.value = messageMap.values.toList().sortedByDescending { it.timestamp }
    }

    /**
     * Add sample messages for testing
     */
    fun addSampleMessages() {
        val sampleMessages = listOf(
            SmsMessage(
                id = "sample_1",
                sender = "ICICI",
                body = "Your OTP for transaction is 123456. Do not share with anyone.",
                timestamp = java.util.Date(),
                category = SmsCategory.OTP
            ),
            SmsMessage(
                id = "sample_2",
                sender = "12345",
                body = "Congratulations! You have won $1000. Click here to claim your prize now!",
                timestamp = java.util.Date(System.currentTimeMillis() - 3600000),
                category = SmsCategory.SPAM
            ),
            SmsMessage(
                id = "sample_3",
                sender = "Amazon",
                body = "Your order has been confirmed. Delivery expected by tomorrow.",
                timestamp = java.util.Date(System.currentTimeMillis() - 7200000),
                category = SmsCategory.TRANSACTIONAL
            ),
            SmsMessage(
                id = "sample_4",
                sender = "Flipkart",
                body = "Special offer! 50% off on electronics. Limited time deal. Shop now!",
                timestamp = java.util.Date(System.currentTimeMillis() - 10800000),
                category = SmsCategory.PROMOTIONAL
            ),
            SmsMessage(
                id = "sample_5",
                sender = "John",
                body = "Hey, are we still meeting for dinner tonight?",
                timestamp = java.util.Date(System.currentTimeMillis() - 14400000),
                category = SmsCategory.PERSONAL
            )
        )

        sampleMessages.forEach { addMessage(it) }
    }
}