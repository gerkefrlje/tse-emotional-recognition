package com.example.tse_emotionalrecognition.presentation.utils

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log

class WearDetectionHelper(private val context: Context): SensorEventListener {

    private val sensorManager: SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val offBodySensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_LOW_LATENCY_OFFBODY_DETECT)
    private var isWatchWorn: Boolean = false
    private var listener: ((Boolean) -> Unit)? = null

    fun start(listener: (Boolean) -> Unit) {
        this.listener = listener
        offBodySensor?.let {
            sensorManager.registerListener(this, offBodySensor, SensorManager.SENSOR_DELAY_NORMAL)
        } ?: run {
            Log.e("WearDetectionHelper", "Off-body sensor not available")
            listener(false)
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
        listener = null
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let { sensorEvent ->
            if (sensorEvent.sensor.type == Sensor.TYPE_LOW_LATENCY_OFFBODY_DETECT) {
                isWatchWorn = sensorEvent.values[0] == 1.0f
                listener?.invoke(isWatchWorn)
                Log.d("WearDetectionHelper", "Off-body sensor changed: $isWatchWorn")
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.d("WearDetectionHelper", "Sensor accuracy changed: ${sensor?.name}, new accuracy: $accuracy")
    }

    fun isWatchWorn(): Boolean {
        return isWatchWorn
    }
}