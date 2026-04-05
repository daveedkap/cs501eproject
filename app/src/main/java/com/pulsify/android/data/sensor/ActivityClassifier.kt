package com.pulsify.android.data.sensor

import com.pulsify.android.domain.DetectedActivity
import kotlin.math.sqrt

/**
 * Lightweight activity guess from accelerometer magnitude (proposal v1: simple thresholds, no ML).
 */
class ActivityClassifier {

    private var smoothedMag = 0f

    fun reset() {
        smoothedMag = 0f
    }

    /**
     * @param ax ay az accelerometer values including gravity (m/s²).
     */
    fun update(ax: Float, ay: Float, az: Float): DetectedActivity {
        val mag = sqrt(ax * ax + ay * ay + az * az)
        val dynamic = kotlin.math.abs(mag - GRAVITY_ESTIMATE)
        smoothedMag = smoothedMag * SMOOTH + dynamic * (1f - SMOOTH)
        return when {
            smoothedMag < SITTING_MAX -> DetectedActivity.Sitting
            smoothedMag < WALKING_MAX -> DetectedActivity.Walking
            else -> DetectedActivity.Running
        }
    }

    companion object {
        private const val GRAVITY_ESTIMATE = 9.8f
        private const val SMOOTH = 0.88f
        private const val SITTING_MAX = 1.2f
        private const val WALKING_MAX = 3.8f
    }
}
