package com.pulsify.android.ui.playlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pulsify.android.domain.Track

@Composable
fun PlaylistScreen(
    viewModel: PlaylistViewModel,
    modifier: Modifier = Modifier,
) {
    val playback by viewModel.playback.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Your contextual mix", style = MaterialTheme.typography.titleLarge)
        Text(
            text = "Tracks selected based on your activity and Spotify listening history.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        playback.currentTrack?.let { track ->
            NowPlayingCard(track = track, isPlaying = playback.isPlaying)
        } ?: Text("Generate a mix from Home to populate tracks.", style = MaterialTheme.typography.bodyMedium)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FilledIconButton(
                onClick = { viewModel.skipPrevious() },
                modifier = Modifier.semantics { contentDescription = "Previous track" },
            ) {
                Icon(Icons.Default.SkipPrevious, contentDescription = null)
            }
            FilledIconButton(
                onClick = { viewModel.togglePlayPause() },
                modifier = Modifier.semantics {
                    contentDescription = if (playback.isPlaying) "Pause" else "Play"
                },
            ) {
                Icon(
                    imageVector = if (playback.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.padding(4.dp),
                )
            }
            FilledIconButton(
                onClick = { viewModel.skipNext() },
                modifier = Modifier.semantics { contentDescription = "Next track" },
            ) {
                Icon(Icons.Default.SkipNext, contentDescription = null)
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            itemsIndexed(playback.tracks, key = { _, t -> t.id }) { index, track ->
                TrackRow(
                    track = track,
                    active = index == playback.currentIndex,
                )
            }
        }
    }
}

@Composable
private fun NowPlayingCard(track: Track, isPlaying: Boolean) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(track.title, style = MaterialTheme.typography.titleLarge, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(track.artist, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Text("${track.bpm} BPM · ${track.energyLabel}", style = MaterialTheme.typography.bodyMedium)
            Text(if (isPlaying) "Playing" else "Paused", style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun TrackRow(track: Track, active: Boolean) {
    val colors = if (active) {
        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    } else {
        CardDefaults.cardColors()
    }
    Card(colors = colors) {
        Column(Modifier.padding(12.dp)) {
            Text(track.title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("${track.artist} · ${track.bpm} BPM", style = MaterialTheme.typography.bodySmall)
        }
    }
}
