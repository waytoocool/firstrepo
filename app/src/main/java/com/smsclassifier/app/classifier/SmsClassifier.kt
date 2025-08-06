package com.smsclassifier.app.classifier

import com.smsclassifier.app.model.SmsCategory
import java.util.regex.Pattern

/**
 * Rule-based SMS classifier that categorizes messages using regex patterns and keywords
 */
class SmsClassifier {

    companion object {
        // OTP patterns
        private val OTP_PATTERNS = listOf(
            Pattern.compile("\\b\\d{4,8}\\b"),                    // 4-8 digit codes
            Pattern.compile("(?i)\\botp\\b"),                     // "OTP" keyword
            Pattern.compile("(?i)\\bcode\\b.*\\d{4,8}"),          // "code" with digits
            Pattern.compile("(?i)\\bpin\\b.*\\d{4,8}"),           // "pin" with digits
            Pattern.compile("(?i)\\bverification\\b.*\\d{4,8}"),  // "verification" with digits
            Pattern.compile("(?i)\\bauth\\w*\\b.*\\d{4,8}"),      // "authentication" with digits
            Pattern.compile("(?i)\\bconfirm\\w*\\b.*\\d{4,8}"),   // "confirmation" with digits
            Pattern.compile("(?i)\\blogin\\b.*\\d{4,8}"),         // "login" with digits
            Pattern.compile("(?i)\\bsecurity\\b.*\\d{4,8}"),      // "security" with digits
        )

        // Spam indicators
        private val SPAM_PATTERNS = listOf(
            Pattern.compile("(?i)\\bunsubscribe\\b"),
            Pattern.compile("(?i)\\bfree\\b"),
            Pattern.compile("(?i)\\bwinner?\\b"),
            Pattern.compile("(?i)\\bprize\\b"),
            Pattern.compile("(?i)\\bcongratulations?\\b"),
            Pattern.compile("(?i)\\bclaim\\b"),
            Pattern.compile("(?i)\\bclick\\s+here\\b"),
            Pattern.compile("(?i)\\blimited\\s+time\\b"),
            Pattern.compile("(?i)\\bact\\s+now\\b"),
            Pattern.compile("(?i)\\b100%\\s+free\\b"),
            Pattern.compile("(?i)\\bmoney\\s+back\\b"),
            Pattern.compile("(?i)\\bguaranteed\\b"),
            Pattern.compile("(?i)\\bno\\s+obligation\\b"),
            Pattern.compile("(?i)\\brisk\\s+free\\b"),
        )

        // Promotional keywords
        private val PROMOTIONAL_PATTERNS = listOf(
            Pattern.compile("(?i)\\boffer\\b"),
            Pattern.compile("(?i)\\bdiscount\\b"),
            Pattern.compile("(?i)\\bsale\\b"),
            Pattern.compile("(?i)\\bdeal\\b"),
            Pattern.compile("(?i)\\b%\\s*off\\b"),
            Pattern.compile("(?i)\\bpromo\\w*\\b"),
            Pattern.compile("(?i)\\bcoupon\\b"),
            Pattern.compile("(?i)\\bvoucher\\b"),
            Pattern.compile("(?i)\\bspecial\\s+price\\b"),
            Pattern.compile("(?i)\\bbuy\\s+now\\b"),
            Pattern.compile("(?i)\\bshop\\s+now\\b"),
            Pattern.compile("(?i)\\blimited\\s+stock\\b"),
            Pattern.compile("(?i)\\bsave\\s+\\$"),
            Pattern.compile("(?i)\\bflash\\s+sale\\b"),
        )

        // Transactional patterns
        private val TRANSACTIONAL_PATTERNS = listOf(
            Pattern.compile("(?i)\\baccount\\b"),
            Pattern.compile("(?i)\\bbalance\\b"),
            Pattern.compile("(?i)\\btransaction\\b"),
            Pattern.compile("(?i)\\bdebit\\w*\\b"),
            Pattern.compile("(?i)\\bcredit\\w*\\b"),
            Pattern.compile("(?i)\\bpayment\\b"),
            Pattern.compile("(?i)\\bbill\\b"),
            Pattern.compile("(?i)\\binvoice\\b"),
            Pattern.compile("(?i)\\breceipt\\b"),
            Pattern.compile("(?i)\\border\\b"),
            Pattern.compile("(?i)\\bdelivery\\b"),
            Pattern.compile("(?i)\\bshipped\\b"),
            Pattern.compile("(?i)\\bconfirmed\\b"),
            Pattern.compile("(?i)\\brefund\\b"),
            Pattern.compile("(?i)\\bstatement\\b"),
            Pattern.compile("(?i)\\balert\\b"),
            Pattern.compile("(?i)\\bnotification\\b"),
            Pattern.compile("(?i)\\breminder\\b"),
            Pattern.compile("(?i)\\bdue\\s+date\\b"),
            Pattern.compile("(?i)\\bexpiry\\b"),
            Pattern.compile("(?i)\\brenew\\w*\\b"),
        )

        // Known spam/promotional sender patterns
        private val SPAM_SENDERS = listOf(
            Pattern.compile("^\\d{5,6}$"),    // Short codes (5-6 digits)
            Pattern.compile("(?i)noreply"),
            Pattern.compile("(?i)no-reply"),
            Pattern.compile("(?i)donotreply"),
            Pattern.compile("(?i)marketing"),
            Pattern.compile("(?i)promo"),
            Pattern.compile("(?i)deals"),
        )

        // Known transactional senders
        private val TRANSACTIONAL_SENDERS = listOf(
            Pattern.compile("(?i)\\bbank\\b"),
            Pattern.compile("(?i)\\bpay\\w*\\b"),
            Pattern.compile("(?i)\\bcard\\b"),
            Pattern.compile("(?i)\\batm\\b"),
            Pattern.compile("(?i)\\bupi\\b"),
            Pattern.compile("(?i)\\bwallet\\b"),
            Pattern.compile("(?i)\\bbill\\w*\\b"),
            Pattern.compile("(?i)\\butility\\b"),
            Pattern.compile("(?i)\\binsurance\\b"),
            Pattern.compile("(?i)\\bmutual\\b"),
            Pattern.compile("(?i)\\bfund\\b"),
            Pattern.compile("(?i)\\binvest\\w*\\b"),
        )
    }

    /**
     * Classifies an SMS message into appropriate category
     */
    fun classify(sender: String, messageBody: String): SmsCategory {
        val body = messageBody.trim()
        val senderLower = sender.lowercase().trim()

        // Priority 1: Check for OTP patterns (highest priority)
        if (isOtp(body)) {
            return SmsCategory.OTP
        }

        // Priority 2: Check sender-based classification
        val senderCategory = classifyBySender(senderLower)
        if (senderCategory != null) {
            return senderCategory
        }

        // Priority 3: Check content-based patterns
        val contentCategory = classifyByContent(body)
        if (contentCategory != null) {
            return contentCategory
        }

        // Default: Personal
        return SmsCategory.PERSONAL
    }

    private fun isOtp(messageBody: String): Boolean {
        return OTP_PATTERNS.any { pattern ->
            pattern.matcher(messageBody).find()
        }
    }

    private fun classifyBySender(sender: String): SmsCategory? {
        // Check for spam senders
        if (SPAM_SENDERS.any { it.matcher(sender).find() }) {
            return SmsCategory.SPAM
        }

        // Check for transactional senders
        if (TRANSACTIONAL_SENDERS.any { it.matcher(sender).find() }) {
            return SmsCategory.TRANSACTIONAL
        }

        return null
    }

    private fun classifyByContent(messageBody: String): SmsCategory? {
        var spamScore = 0
        var promotionalScore = 0
        var transactionalScore = 0

        // Check spam patterns
        SPAM_PATTERNS.forEach { pattern ->
            if (pattern.matcher(messageBody).find()) {
                spamScore++
            }
        }

        // Check promotional patterns
        PROMOTIONAL_PATTERNS.forEach { pattern ->
            if (pattern.matcher(messageBody).find()) {
                promotionalScore++
            }
        }

        // Check transactional patterns
        TRANSACTIONAL_PATTERNS.forEach { pattern ->
            if (pattern.matcher(messageBody).find()) {
                transactionalScore++
            }
        }

        // Determine category based on highest score
        val maxScore = maxOf(spamScore, promotionalScore, transactionalScore)
        
        if (maxScore == 0) {
            return null // No clear pattern found
        }

        return when (maxScore) {
            spamScore -> SmsCategory.SPAM
            promotionalScore -> SmsCategory.PROMOTIONAL
            transactionalScore -> SmsCategory.TRANSACTIONAL
            else -> null
        }
    }
}