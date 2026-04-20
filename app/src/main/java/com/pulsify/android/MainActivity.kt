package com.pulsify.android

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.pulsify.android.di.PulsifyViewModelFactory
import com.pulsify.android.ui.PulsifyApp
import com.pulsify.android.ui.theme.PulsifyTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleSpotifyRedirect(intent)
        val factory = PulsifyViewModelFactory.from(application as PulsifyApplication)
        setContent {
            PulsifyTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PulsifyApp(factory = factory)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleSpotifyRedirect(intent)
    }

    private fun handleSpotifyRedirect(intent: Intent?) {
        val uri = intent?.data ?: return
        if (uri.scheme != "com.pulsify.android" || uri.host != "callback") return

        val app = application as PulsifyApplication
        val authManager = app.container.spotifyAuthManager
        val repository = app.container.repository

        CoroutineScope(Dispatchers.IO).launch {
            val success = authManager.handleCallback(uri)
            if (success) {
                repository.fetchUserProfileName()
            }
        }
    }
}
