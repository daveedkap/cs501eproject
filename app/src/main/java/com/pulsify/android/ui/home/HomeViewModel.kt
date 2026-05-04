package com.pulsify.android.ui.home

import android.app.Application
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
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

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _partialText = MutableStateFlow("")
    val partialText: StateFlow<String> = _partialText.asStateFlow()

    val messages = repository.messages
    val textModePreferred = repository.textModePreferred

    private var speechRecognizer: SpeechRecognizer? = null

    init {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        viewModelScope.launch {
            repository.prefetchSessionCatalogIfLinked()
        }
    }

    override fun onCleared() {
        sensorManager.unregisterListener(this)
        speechRecognizer?.destroy()
        speechRecognizer = null
        super.onCleared()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type != Sensor.TYPE_ACCELEROMETER) return
        _detectedActivity.value = classifier.update(event.values[0], event.values[1], event.values[2])
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
        respondToTextInput(text)
    }

    private fun respondToTextInput(userText: String) {
        val activity = _detectedActivity.value
        val lower = userText.lowercase()
        val response = when {
            lower.contains("playlist") || lower.contains("music") || lower.contains("play") ->
                "Tap 'Generate contextual mix' and I'll build one based on your ${activity.displayLabel().lowercase()} context."
            lower.contains("run") || lower.contains("jog") || lower.contains("workout") ->
                "Sounds like you want something high-energy! Hit generate and I'll match tracks to your pace."
            lower.contains("relax") || lower.contains("chill") || lower.contains("calm") || lower.contains("study") ->
                "Got it — I'll lean toward lo-fi and ambient when you generate a mix."
            lower.contains("walk") || lower.contains("commute") ->
                "A nice mid-tempo indie mix could work. Generate a mix and I'll match it to your stride."
            lower.contains("hello") || lower.contains("hi") || lower.contains("hey") ->
                "Hey! I'm reading your movement as ${activity.displayLabel().lowercase()}. Want me to suggest some music?"
            else -> when (activity) {
                DetectedActivity.Running ->
                    "You're on the move! Tap generate for a high-BPM mix that matches your pace."
                DetectedActivity.Walking ->
                    "Nice and easy — I can put together a mid-tempo playlist. Tap generate when you're ready."
                DetectedActivity.Sitting ->
                    "Looks like downtime. I'd suggest some focus or lo-fi tracks — tap generate to try it."
                DetectedActivity.Unknown ->
                    "I'm still figuring out your context. Tap generate and I'll pick something balanced."
            }
        }
        repository.appendMessage(response, isUser = false)
    }

    fun toggleListening() {
        if (_isListening.value) {
            stopListening()
        } else {
            startListening()
        }
    }

    private fun startListening() {
        val app = getApplication<Application>()
        if (!SpeechRecognizer.isRecognitionAvailable(app)) {
            repository.appendMessage(
                "Speech recognition is not available on this device.",
                isUser = false,
            )
            return
        }

        speechRecognizer?.destroy()
        _partialText.value = ""

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(app).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    _isListening.value = true
                }

                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {
                    _isListening.value = false
                }

                override fun onError(error: Int) {
                    _isListening.value = false
                    _partialText.value = ""
                    val msg = when (error) {
                        SpeechRecognizer.ERROR_NO_MATCH -> "Didn't catch that—try again."
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected—tap the mic to try again."
                        SpeechRecognizer.ERROR_AUDIO -> "Audio error—check microphone permission."
                        else -> null
                    }
                    msg?.let { repository.appendMessage(it, isUser = false) }
                }

                override fun onResults(results: Bundle?) {
                    _isListening.value = false
                    _partialText.value = ""
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val text = matches?.firstOrNull()?.trim()
                    if (!text.isNullOrEmpty()) {
                        repository.appendMessage(text, isUser = true)
                        respondToVoiceInput(text)
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val partial = partialResults
                        ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        ?.firstOrNull()
                    if (!partial.isNullOrBlank()) {
                        _partialText.value = partial
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        speechRecognizer?.startListening(intent)
        _isListening.value = true
    }

    private fun stopListening() {
        speechRecognizer?.stopListening()
        _isListening.value = false
        _partialText.value = ""
    }

    private fun respondToVoiceInput(userText: String) {
        val activity = _detectedActivity.value
        val response = when (activity) {
            DetectedActivity.Running ->
                "Got it — you're running. I'll queue up a high-energy mix."
            DetectedActivity.Walking ->
                "Nice walk! I'll find something mid-tempo for you."
            DetectedActivity.Sitting ->
                "Looks like you're relaxing — how about some focus-friendly tracks?"
            DetectedActivity.Unknown ->
                "Still reading your context — I'll pick a balanced playlist."
        }
        repository.appendMessage(response, isUser = false)
        viewModelScope.launch {
            repository.refreshPlaylistAfterVoice(activity, userText)
        }
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
