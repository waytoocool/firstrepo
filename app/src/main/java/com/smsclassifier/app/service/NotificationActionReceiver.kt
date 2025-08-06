package com.smsclassifier.app.service

import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast

/**
 * Handles notification actions like copy and share OTP
 */
class NotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        when (intent.action) {
            "COPY_OTP" -> {
                val otpCode = intent.getStringExtra("otp_code")
                if (otpCode != null) {
                    copyToClipboard(context, otpCode)
                    Toast.makeText(context, "OTP $otpCode copied to clipboard", Toast.LENGTH_SHORT).show()
                }
            }
            "SHARE_OTP" -> {
                val otpCode = intent.getStringExtra("otp_code")
                val sender = intent.getStringExtra("sender")
                if (otpCode != null) {
                    shareOtp(context, otpCode, sender ?: "Unknown")
                }
            }
        }
    }

    private fun copyToClipboard(context: Context, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("OTP", text)
        clipboard.setPrimaryClip(clip)
    }

    private fun shareOtp(context: Context, otpCode: String, sender: String) {
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
    }
}