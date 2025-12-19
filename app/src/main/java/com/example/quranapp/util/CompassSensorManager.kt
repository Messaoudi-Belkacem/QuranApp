package com.example.quranapp.util

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class CompassSensorManager(context: Context) {
    private val tag = "CompassSensorManager"
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val rotationVectorSensor: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private val accelerometerSensor: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetometerSensor: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    /**
     * Get the device azimuth (direction) as a Flow
     * Returns angle in degrees (0-360°) where 0° is north
     */
    fun getAzimuthFlow(): Flow<Float> = callbackFlow {
        var azimuth = 0f

        // Arrays for sensor data
        val rotationMatrix = FloatArray(9)
        val orientation = FloatArray(3)
        val gravity = FloatArray(3)
        val geomagnetic = FloatArray(3)

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                when (event.sensor.type) {
                    Sensor.TYPE_ROTATION_VECTOR -> {
                        // Preferred method - more stable
                        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                        SensorManager.getOrientation(rotationMatrix, orientation)
                        azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
                        azimuth = (azimuth + 360) % 360
                        trySend(azimuth)
                    }

                    Sensor.TYPE_ACCELEROMETER -> {
                        // Fallback method
                        System.arraycopy(event.values, 0, gravity, 0, event.values.size)
                        calculateAzimuthFromGravityAndMagnetic()
                    }

                    Sensor.TYPE_MAGNETIC_FIELD -> {
                        // Fallback method
                        System.arraycopy(event.values, 0, geomagnetic, 0, event.values.size)
                        calculateAzimuthFromGravityAndMagnetic()
                    }
                }
            }

            private fun calculateAzimuthFromGravityAndMagnetic() {
                if (gravity.any { it != 0f } && geomagnetic.any { it != 0f }) {
                    val success = SensorManager.getRotationMatrix(
                        rotationMatrix,
                        null,
                        gravity,
                        geomagnetic
                    )
                    if (success) {
                        SensorManager.getOrientation(rotationMatrix, orientation)
                        azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
                        azimuth = (azimuth + 360) % 360
                        trySend(azimuth)
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                Log.d(tag, "Sensor accuracy changed: ${sensor?.name}, accuracy: $accuracy")
            }
        }

        // Register sensor listener with preferred sensor
        if (rotationVectorSensor != null) {
            Log.d(tag, "Using rotation vector sensor")
            sensorManager.registerListener(
                listener,
                rotationVectorSensor,
                SensorManager.SENSOR_DELAY_UI
            )
        } else {
            // Fallback to accelerometer + magnetometer
            Log.d(tag, "Using accelerometer + magnetometer")
            accelerometerSensor?.let {
                sensorManager.registerListener(
                    listener,
                    it,
                    SensorManager.SENSOR_DELAY_UI
                )
            }
            magnetometerSensor?.let {
                sensorManager.registerListener(
                    listener,
                    it,
                    SensorManager.SENSOR_DELAY_UI
                )
            }
        }

        awaitClose {
            Log.d(tag, "Unregistering sensor listener")
            sensorManager.unregisterListener(listener)
        }
    }

    /**
     * Check if compass sensors are available
     */
    fun hasSensors(): Boolean {
        return rotationVectorSensor != null ||
                (accelerometerSensor != null && magnetometerSensor != null)
    }
}

