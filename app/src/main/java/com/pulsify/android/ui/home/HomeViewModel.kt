package com.pulsify.android.ui.home

import android.app.Application
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pulsify.android.data.location.LocationReader
import com.pulsify.android.data.repository.PulsifyRepository
import com.pulsify.android.data.sensor.ActivityClassifier
import com.pulsify.android.domain.DetectedActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    application: Application,
    private val repository: PulsifyRepository,
    private val classifier: ActivityClassifier,
    private val locationReader: LocationReader,
) : AndroidViewModel(application), SensorEventListener {

    private val sensorManager =
        application.getSystemService(Application.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val _detectedActivity = MutableStateFlow(DetectedActivity.Unknown)
    val detectedActivity: StateFlow<DetectedActivity> = _detectedActivity.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _userDraft = MutableStateFlow("")
    val userDraft: StateFlow<String> = _userDraft.asStateFlow()

    val messages = repository.messages
    val textModePreferred = repository.textModePreferred

    init {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onCleared() {
        sensorManager.unregisterListener(this)
        super.onCleared()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type != Sensor.TYPE_ACCELEROMETER) return
        val ax = event.values[0]
        val ay = event.values[1]
        val az = event.values[2]
        _detectedActivity.value = classifier.update(ax, ay, az)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    fun onUserDraftChange(text: String) {
        _userDraft.value = text
    }

    fun sendUserMessage() {
        val text = _userDraft.value.trim()
        if (text.isEmpty()) return
        repository.appendMessage(text, isUser = true)
        _userDraft.value = ""
    }

    fun simulateVoicePrompt() {
        val activity = _detectedActivity.value
        val suggestion = when (activity) {
            DetectedActivity.Running -> "It looks like you’re moving fast—want your usual high-energy mix or something new?"
            DetectedActivity.Walking -> "Nice walk—want a mid-tempo indie playlist or a calm commute mix?"
            DetectedActivity.Sitting -> "You seem settled—replay your focus playlist or try fresh instrumentals?"
            DetectedActivity.Unknown -> "Still sensing your context—want a balanced playlist while I keep listening?"
        }
        repository.appendMessage(suggestion, isUser = false)
    }

    fun requestPlaylist(onDone: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val loc = runCatching { locationReader.getCurrentLocation() }.getOrNull()
            val activity = _detectedActivity.value
            val note = _userDraft.value.trim().ifEmpty { null }
            repository.requestPlaylistForActivity(
                activity = activity,
                userPrompt = note,
                latitude = loc?.latitude,
                longitude = loc?.longitude,
            )
            _isLoading.value = false
            onDone()
        }
    }
}
