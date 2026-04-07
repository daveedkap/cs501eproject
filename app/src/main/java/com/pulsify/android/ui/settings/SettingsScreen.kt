package com.pulsify.android.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier,
) {
    val spotifyLinked by viewModel.spotifyLinked.collectAsStateWithLifecycle()
    val textMode by viewModel.textModePreferred.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Settings & privacy", style = MaterialTheme.typography.titleLarge)
        Text(
            text = "Biometric and movement data stay on-device in this build. Cloud models and Spotify are not contacted yet.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        PreferenceSwitch(
            title = "Simulate Spotify link",
            subtitle = "Toggles UI state only—no App Remote or Web API calls.",
            checked = spotifyLinked,
            onCheckedChange = viewModel::setSpotifyLinked,
            contentDescription = "Simulate Spotify link",
        )

        PreferenceSwitch(
            title = "Prefer text mode",
            subtitle = "Shows chat entry on Home for libraries or quiet spaces.",
            checked = textMode,
            onCheckedChange = viewModel::setTextMode,
            contentDescription = "Prefer text mode",
        )

        TextButton(
            onClick = { viewModel.resetChat() },
            modifier = Modifier.semantics { contentDescription = "Clear assistant thread" },
        ) {
            Text("Clear assistant thread")
        }
    }
}

@Composable
private fun PreferenceSwitch(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    contentDescription: String,
) {
    Card {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                modifier = Modifier.semantics { this.contentDescription = contentDescription },
            )
        }
    }
}
