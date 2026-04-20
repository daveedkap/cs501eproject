package com.pulsify.android.ui.settings

import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
    val spotifyName by viewModel.spotifyDisplayName.collectAsStateWithLifecycle()
    val textMode by viewModel.textModePreferred.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Settings & privacy", style = MaterialTheme.typography.titleLarge)
        Text(
            text = "Movement data stays on-device. Spotify and Gemini AI are used for personalized music suggestions when connected.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        SpotifyConnectionCard(
            isConnected = spotifyLinked,
            displayName = spotifyName,
            onConnect = {
                val url = viewModel.buildSpotifyAuthUrl()
                CustomTabsIntent.Builder().build()
                    .launchUrl(context, Uri.parse(url))
            },
            onDisconnect = { viewModel.disconnectSpotify() },
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
private fun SpotifyConnectionCard(
    isConnected: Boolean,
    displayName: String?,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
) {
    Card(
        colors = if (isConnected) {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        } else {
            CardDefaults.cardColors()
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (isConnected) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
                Text(
                    text = if (isConnected) "Spotify connected" else "Spotify",
                    style = MaterialTheme.typography.titleMedium,
                )
            }

            if (isConnected && displayName != null) {
                Text(
                    text = "Signed in as $displayName",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }

            Text(
                text = if (isConnected) {
                    "Your listening history is used to personalize playlists."
                } else {
                    "Connect your Spotify account to get playlists from your real library."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (isConnected) {
                OutlinedButton(
                    onClick = onDisconnect,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
                ) {
                    Icon(Icons.Default.LinkOff, contentDescription = null)
                    Text("  Disconnect", style = MaterialTheme.typography.labelLarge)
                }
            } else {
                Button(onClick = onConnect) {
                    Text("Connect Spotify")
                }
            }
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
