package com.pulsify.android.ui.map

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import java.text.DateFormat
import java.util.Date

@Composable
fun MapScreen(
    viewModel: MapViewModel,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val ui by viewModel.ui.collectAsStateWithLifecycle()
    val sessionMarkers by viewModel.sessionMarkers.collectAsStateWithLifecycle()

    var granted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED,
        )
    }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { ok -> granted = ok }

    LaunchedEffect(granted) {
        if (granted) viewModel.refreshLocation()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "header") {
            Text("Your music map", style = MaterialTheme.typography.titleLarge)
            Text(
                text = "See where you've listened. Each session tagged with location shows up as a pin.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (!granted) {
            item(key = "permission") {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Location permission needed", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Grant fine location to see your sessions on the map and tag future ones with place context.",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Button(onClick = { launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION) }) {
                            Text("Grant location permission")
                        }
                    }
                }
            }
        } else {
            item(key = "map") {
                val center = ui.latLng
                    ?: sessionMarkers.firstOrNull()?.latLng
                    ?: LatLng(42.3505, -71.1054)

                SessionMap(
                    center = center,
                    currentLocation = ui.latLng,
                    markers = sessionMarkers,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp),
                )
            }

            item(key = "controls") {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Button(
                        onClick = { viewModel.refreshLocation() },
                        enabled = !ui.isRefreshing,
                    ) {
                        Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(8.dp))
                        Text(if (ui.isRefreshing) "Refreshing…" else "Refresh location")
                    }
                    ui.latLng?.let { latLng ->
                        Text(
                            "${"%.4f".format(latLng.latitude)}, ${"%.4f".format(latLng.longitude)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        ui.lastError?.let { error ->
            item(key = "error") {
                Text(text = error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }
        }

        if (sessionMarkers.isNotEmpty()) {
            item(key = "sessions_header") {
                Text(
                    "Sessions on the map (${sessionMarkers.size})",
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            items(sessionMarkers, key = { "marker_${it.timestampMillis}" }) { marker ->
                SessionMarkerCard(marker)
            }
        } else if (granted) {
            item(key = "empty") {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("No location-tagged sessions yet", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Go to Home and generate a mix with location permission granted. " +
                                "The session will appear here as a pin on the map.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SessionMarkerCard(marker: SessionMarker) {
    val df = remember { DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT) }
    val activityLabel = when (marker.activityType) {
        "Running" -> "Running"
        "Walking" -> "Walking"
        "Sitting" -> "Sitting / studying"
        else -> marker.activityType
    }
    Card {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                Icons.Default.LocationOn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(activityLabel, style = MaterialTheme.typography.titleMedium)
                Text(marker.summary, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                Text(
                    df.format(Date(marker.timestampMillis)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun SessionMap(
    center: LatLng,
    currentLocation: LatLng?,
    markers: List<SessionMarker>,
    modifier: Modifier = Modifier,
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(center, 13f)
    }
    LaunchedEffect(center) {
        cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(center, 13f))
    }
    Card(modifier = modifier, elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
        ) {
            currentLocation?.let {
                Marker(
                    state = MarkerState(position = it),
                    title = "You are here",
                )
            }
            markers.forEach { session ->
                val label = when (session.activityType) {
                    "Running" -> "Running"
                    "Walking" -> "Walking"
                    "Sitting" -> "Studying / sitting"
                    else -> session.activityType
                }
                Marker(
                    state = MarkerState(position = session.latLng),
                    title = label,
                    snippet = session.summary,
                )
            }
        }
    }
}
