package com.smsclassifier.app

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.smsclassifier.app.model.SmsCategory
import com.smsclassifier.app.repository.SmsRepository
import com.smsclassifier.app.service.NotificationService
import com.smsclassifier.app.ui.screen.MainScreen
import com.smsclassifier.app.ui.screen.PermissionScreen
import com.smsclassifier.app.ui.theme.SMSClassifierTheme

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize notification channels
        NotificationService.createNotificationChannels(this)

        // Add sample messages for testing
        SmsRepository.getInstance().addSampleMessages()

        setContent {
            SMSClassifierTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val context = LocalContext.current
                    
                    // Required permissions
                    val permissionsState = rememberMultiplePermissionsState(
                        permissions = listOf(
                            Manifest.permission.RECEIVE_SMS,
                            Manifest.permission.READ_SMS,
                            Manifest.permission.POST_NOTIFICATIONS
                        )
                    )

                    // Check if we need to show permission screen
                    if (permissionsState.allPermissionsGranted) {
                        // Get initial category from intent (for notification taps)
                        val initialCategory = remember {
                            intent.getStringExtra("category")?.let { 
                                SmsCategory.fromString(it) 
                            } ?: SmsCategory.OTP
                        }
                        
                        MainScreen(
                            initialCategory = initialCategory,
                            onRequestDefaultSmsApp = {
                                requestDefaultSmsApp()
                            }
                        )
                    } else {
                        PermissionScreen(
                            permissionsState = permissionsState,
                            onOpenSettings = {
                                openAppSettings()
                            }
                        )
                    }
                }
            }
        }
    }

    private fun requestDefaultSmsApp() {
        try {
            val intent = Intent(Settings.ACTION_DEFAULT_SMS_APP_SETTINGS)
            startActivity(intent)
        } catch (e: Exception) {
            // Fallback to general app settings if SMS settings not available
            openAppSettings()
        }
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
        startActivity(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}