package com.smsclassifier.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsMessage
import android.util.Log
import com.smsclassifier.app.classifier.SmsClassifier
import com.smsclassifier.app.model.SmsMessage as AppSmsMessage
import com.smsclassifier.app.repository.SmsRepository
import com.smsclassifier.app.service.NotificationService
import java.util.Date

/**
 * BroadcastReceiver that handles incoming SMS messages
 */
class SmsReceiver : BroadcastReceiver() {

    private val classifier = SmsClassifier()
    
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return
        
        if (intent.action == "android.provider.Telephony.SMS_RECEIVED") {
            try {
                val bundle = intent.extras
                if (bundle != null) {
                    val pdus = bundle.get("pdus") as Array<*>?
                    val format = bundle.getString("format")
                    
                    if (pdus != null) {
                        for (pdu in pdus) {
                            val smsMessage = if (format != null) {
                                SmsMessage.createFromPdu(pdu as ByteArray, format)
                            } else {
                                SmsMessage.createFromPdu(pdu as ByteArray)
                            }
                            
                            if (smsMessage != null) {
                                processSmsMessage(context, smsMessage)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("SmsReceiver", "Error processing SMS: ${e.message}", e)
            }
        }
    }

    private fun processSmsMessage(context: Context, smsMessage: SmsMessage) {
        try {
            val sender = smsMessage.originatingAddress ?: "Unknown"
            val body = smsMessage.messageBody ?: ""
            val timestamp = Date(smsMessage.timestampMillis)
            
            Log.d("SmsReceiver", "Received SMS from: $sender, Body: ${body.take(50)}...")
            
            // Classify the message
            val category = classifier.classify(sender, body)
            
            // Create our app's SMS message object
            val appSmsMessage = AppSmsMessage(
                id = generateMessageId(sender, timestamp),
                sender = sender,
                body = body,
                timestamp = timestamp,
                category = category,
                isRead = false
            )
            
            // Store the message
            SmsRepository.getInstance().addMessage(appSmsMessage)
            
            // Show notification
            NotificationService.showNotification(context, appSmsMessage)
            
            Log.d("SmsReceiver", "SMS classified as: ${category.displayName}")
            
        } catch (e: Exception) {
            Log.e("SmsReceiver", "Error processing SMS message: ${e.message}", e)
        }
    }
    
    private fun generateMessageId(sender: String, timestamp: Date): String {
        return "${sender}_${timestamp.time}".hashCode().toString()
    }
}