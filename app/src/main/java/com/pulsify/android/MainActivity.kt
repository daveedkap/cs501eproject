package com.pulsify.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.pulsify.android.di.PulsifyViewModelFactory
import com.pulsify.android.ui.PulsifyApp
import com.pulsify.android.ui.theme.PulsifyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val factory = PulsifyViewModelFactory.from(application as PulsifyApplication)
        setContent {
            PulsifyTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PulsifyApp(factory = factory)
                }
            }
        }
    }
}
