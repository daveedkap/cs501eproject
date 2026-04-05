package com.pulsify.android.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.pulsify.android.data.location.LocationReader
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MapUiState(
    val latLng: LatLng? = null,
    val lastError: String? = null,
    val isRefreshing: Boolean = false,
)

class MapViewModel(
    private val locationReader: LocationReader,
) : ViewModel() {

    private val _ui = MutableStateFlow(MapUiState())
    val ui: StateFlow<MapUiState> = _ui.asStateFlow()

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
}
