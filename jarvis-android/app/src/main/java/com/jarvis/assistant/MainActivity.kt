package com.jarvis.assistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jarvis.assistant.data.PermissionsHelper
import com.jarvis.assistant.data.SecurePrefs
import com.jarvis.assistant.ui.ChatScreen
import com.jarvis.assistant.ui.OnboardingScreen
import com.jarvis.assistant.ui.SettingsScreen
import com.jarvis.assistant.ui.theme.JarvisTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { /* results observed by re-checking PermissionsHelper where needed */ }

        val securePrefs = SecurePrefs(this)
        val startDestination = if (securePrefs.onboardingCompleted) "chat" else "onboarding"

        setContent {
            JarvisTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = startDestination) {
                    composable("onboarding") {
                        OnboardingScreen(
                            onRequestRuntimePermissions = {
                                permissionLauncher.launch(PermissionsHelper.RUNTIME_PERMISSIONS.toTypedArray())
                            },
                            onContinue = {
                                securePrefs.onboardingCompleted = true
                                navController.navigate("chat") {
                                    popUpTo("onboarding") { inclusive = true }
                                }
                            }
                        )
                    }
                    composable("chat") {
                        ChatScreen(onOpenSettings = { navController.navigate("settings") })
                    }
                    composable("settings") {
                        SettingsScreen(onDone = { navController.popBackStack() })
                    }
                }
            }
        }
    }
}
