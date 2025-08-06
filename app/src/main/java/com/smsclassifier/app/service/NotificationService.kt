package com.smsclassifier.app.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.smsclassifier.app.MainActivity
import com.smsclassifier.app.R
import com.smsclassifier.app.model.SmsCategory
import com.smsclassifier.app.model.SmsMessage

/**
 * Service for handling SMS notifications
 */
object NotificationService {

    private const val CHANNEL_ID_OTP = "sms_otp_channel"
    private const val CHANNEL_ID_TRANSACTIONAL = "sms_transactional_channel"
    private const val CHANNEL_ID_PROMOTIONAL = "sms_promotional_channel"
    private const val CHANNEL_ID_SPAM = "sms_spam_channel"
    private const val CHANNEL_ID_PERSONAL = "sms_personal_channel"

    private const val NOTIFICATION_ID_BASE = 1000

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // OTP Channel - High Priority
            val otpChannel = NotificationChannel(
                CHANNEL_ID_OTP,
                "OTP Messages",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "One-time passwords and verification codes"
                setShowBadge(true)
                enableVibration(true)
            }

            // Transactional Channel - High Priority
            val transactionalChannel = NotificationChannel(
                CHANNEL_ID_TRANSACTIONAL,
                "Transactional Messages",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Bank alerts, payment confirmations, and account notifications"
                setShowBadge(true)
                enableVibration(true)
            }

            // Personal Channel - Default Priority
            val personalChannel = NotificationChannel(
                CHANNEL_ID_PERSONAL,
                "Personal Messages",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Messages from friends and family"
                setShowBadge(true)
                enableVibration(true)
            }

            // Promotional Channel - Low Priority
            val promotionalChannel = NotificationChannel(
                CHANNEL_ID_PROMOTIONAL,
                "Promotional Messages",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Offers, deals, and promotional content"
                setShowBadge(false)
                enableVibration(false)
            }

            // Spam Channel - Min Priority
            val spamChannel = NotificationChannel(
                CHANNEL_ID_SPAM,
                "Spam Messages",
                NotificationManager.IMPORTANCE_MIN
            ).apply {
                description = "Suspected spam and unwanted messages"
                setShowBadge(false)
                enableVibration(false)
            }

            notificationManager.createNotificationChannels(listOf(
                otpChannel, transactionalChannel, personalChannel, promotionalChannel, spamChannel
            ))
        }
    }

    fun showNotification(context: Context, message: SmsMessage) {
        createNotificationChannels(context)
        
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        val channelId = when (message.category) {
            SmsCategory.OTP -> CHANNEL_ID_OTP
            SmsCategory.TRANSACTIONAL -> CHANNEL_ID_TRANSACTIONAL
            SmsCategory.PROMOTIONAL -> CHANNEL_ID_PROMOTIONAL
            SmsCategory.SPAM -> CHANNEL_ID_SPAM
            SmsCategory.PERSONAL -> CHANNEL_ID_PERSONAL
        }

        // Create intent to open main activity
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("category", message.category.name)
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle(getNotificationTitle(message))
            .setContentText(message.getSnippet(100))
            .setSmallIcon(getNotificationIcon(message.category))
            .setColor(ContextCompat.getColor(context, getNotificationColor(message.category)))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(getNotificationPriority(message.category))
            .setStyle(NotificationCompat.BigTextStyle().bigText(message.body))

        // Add action buttons for OTP messages
        if (message.category == SmsCategory.OTP) {
            val otpCode = message.extractOtp()
            if (otpCode != null) {
                // Copy OTP action
                val copyIntent = Intent(context, NotificationActionReceiver::class.java).apply {
                    action = "COPY_OTP"
                    putExtra("otp_code", otpCode)
                }
                val copyPendingIntent = PendingIntent.getBroadcast(
                    context, message.id.hashCode(), copyIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                notification.addAction(
                    R.drawable.ic_copy,
                    "Copy $otpCode",
                    copyPendingIntent
                )

                // Share OTP action
                val shareIntent = Intent(context, NotificationActionReceiver::class.java).apply {
                    action = "SHARE_OTP"
                    putExtra("otp_code", otpCode)
                    putExtra("sender", message.sender)
                }
                val sharePendingIntent = PendingIntent.getBroadcast(
                    context, message.id.hashCode() + 1, shareIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                notification.addAction(
                    R.drawable.ic_share,
                    "Share",
                    sharePendingIntent
                )
            }
        }

        val notificationId = NOTIFICATION_ID_BASE + message.id.hashCode()
        notificationManager.notify(notificationId, notification.build())
    }

    private fun getNotificationTitle(message: SmsMessage): String {
        return when (message.category) {
            SmsCategory.OTP -> "New OTP from ${message.sender}"
            SmsCategory.TRANSACTIONAL -> "Transaction from ${message.sender}"
            SmsCategory.PROMOTIONAL -> "Offer from ${message.sender}"
            SmsCategory.SPAM -> "Spam from ${message.sender}"
            SmsCategory.PERSONAL -> "Message from ${message.sender}"
        }
    }

    private fun getNotificationIcon(category: SmsCategory): Int {
        return when (category) {
            SmsCategory.OTP -> R.drawable.ic_security
            SmsCategory.TRANSACTIONAL -> R.drawable.ic_account_balance
            SmsCategory.PROMOTIONAL -> R.drawable.ic_local_offer
            SmsCategory.SPAM -> R.drawable.ic_block
            SmsCategory.PERSONAL -> R.drawable.ic_person
        }
    }

    private fun getNotificationColor(category: SmsCategory): Int {
        return when (category) {
            SmsCategory.OTP -> R.color.otp_color
            SmsCategory.TRANSACTIONAL -> R.color.transactional_color
            SmsCategory.PROMOTIONAL -> R.color.promotional_color
            SmsCategory.SPAM -> R.color.spam_color
            SmsCategory.PERSONAL -> R.color.personal_color
        }
    }

    private fun getNotificationPriority(category: SmsCategory): Int {
        return when (category) {
            SmsCategory.OTP, SmsCategory.TRANSACTIONAL -> NotificationCompat.PRIORITY_HIGH
            SmsCategory.PERSONAL -> NotificationCompat.PRIORITY_DEFAULT
            SmsCategory.PROMOTIONAL -> NotificationCompat.PRIORITY_LOW
            SmsCategory.SPAM -> NotificationCompat.PRIORITY_MIN
        }
    }
}