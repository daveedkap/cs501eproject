package com.pulsify.android.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.pulsify.android.data.local.ActivitySessionEntity
import com.pulsify.android.data.location.LocationReader
import com.pulsify.android.data.repository.PulsifyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MapUiState(
    val latLng: LatLng? = null,
    val lastError: String? = null,
    val isRefreshing: Boolean = false,
)

data class SessionMarker(
    val latLng: LatLng,
    val activityType: String,
    val summary: String,
    val timestampMillis: Long,
)

class MapViewModel(
    private val locationReader: LocationReader,
    repository: PulsifyRepository,
) : ViewModel() {

    private val _ui = MutableStateFlow(MapUiState())
    val ui: StateFlow<MapUiState> = _ui.asStateFlow()

    val sessionMarkers: StateFlow<List<SessionMarker>> = repository.sessions
        .map { sessions -> sessions.toMarkers() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun refreshLocation() {
        viewModelScope.launch {
            _ui.value = _ui.value.copy(isRefreshing = true, lastError = null)
            runCatching { locationReader.getCurrentLocation() }
                .onSuccess { loc ->
                    if (loc == null) {
                        _ui.value = MapUiState(
                            latLng = null,
                            lastError = "No cached location yet. Try outdoors with GPS.",
                            isRefreshing = false,
                        )
                    } else {
                        _ui.value = MapUiState(
                            latLng = LatLng(loc.latitude, loc.longitude),
                            lastError = null,
                            isRefreshing = false,
                        )
                    }
                }
                .onFailure { e ->
                    _ui.value = _ui.value.copy(
                        lastError = e.message ?: "Location unavailable",
                        isRefreshing = false,
                    )
                }
        }
    }

    private fun List<ActivitySessionEntity>.toMarkers(): List<SessionMarker> =
        filter { it.latitude != null && it.longitude != null }
            .map { session ->
                SessionMarker(
                    latLng = LatLng(session.latitude!!, session.longitude!!),
                    activityType = session.activityType,
                    summary = session.playlistSummary ?: "${session.trackCount} tracks",
                    timestampMillis = session.timestampMillis,
                )
            }
}
