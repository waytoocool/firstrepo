package com.smsclassifier.app.model

import java.util.Date

/**
 * Represents an SMS message with classification category
 */
data class SmsMessage(
    val id: String = "",
    val sender: String,
    val body: String,
    val timestamp: Date,
    val category: SmsCategory = SmsCategory.PERSONAL,
    val isRead: Boolean = false
) {
    /**
     * Returns a short snippet of the message body for display
     */
    fun getSnippet(maxLength: Int = 100): String {
        return if (body.length <= maxLength) {
            body
        } else {
            "${body.take(maxLength)}..."
        }
    }

    /**
     * Extracts OTP code from message body if it's an OTP message
     */
    fun extractOtp(): String? {
        if (category != SmsCategory.OTP) return null
        
        // Common OTP patterns
        val otpPatterns = listOf(
            Regex("\\b\\d{4,8}\\b"),          // 4-8 digit codes
            Regex("(?i)otp[:\\s]*([\\d]{4,8})"),  // "OTP: 123456"
            Regex("(?i)code[:\\s]*([\\d]{4,8})"), // "Code: 123456"
            Regex("(?i)pin[:\\s]*([\\d]{4,8})")   // "PIN: 123456"
        )
        
        for (pattern in otpPatterns) {
            val match = pattern.find(body)
            if (match != null) {
                return match.groupValues.lastOrNull()?.takeIf { it.isNotEmpty() } ?: match.value
            }
        }
        
        return null
    }
}

/**
 * Categories for SMS classification
 */
enum class SmsCategory(val displayName: String, val color: Long) {
    OTP("OTP", 0xFF4CAF50),           // Green
    SPAM("Spam", 0xFFF44336),         // Red  
    PROMOTIONAL("Promotional", 0xFFFF9800), // Orange
    TRANSACTIONAL("Transactional", 0xFF2196F3), // Blue
    PERSONAL("Personal", 0xFF9C27B0); // Purple
    
    companion object {
        fun fromString(category: String): SmsCategory {
            return values().find { 
                it.name.equals(category, ignoreCase = true) 
            } ?: PERSONAL
        }
    }
}