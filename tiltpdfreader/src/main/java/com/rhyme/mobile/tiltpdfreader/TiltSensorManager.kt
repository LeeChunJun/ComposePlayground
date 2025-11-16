package com.rhyme.mobile.tiltpdfreader

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class TiltSensorManager(
    private val context: Context, private val onTiltChanged: (Float, Float) -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val rotationVector = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

    fun start() {
        sensorManager.registerListener(this, rotationVector, SensorManager.SENSOR_DELAY_UI)
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        val rotationMatrix = FloatArray(9)
        val orientation = FloatArray(3)
        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
        SensorManager.getOrientation(rotationMatrix, orientation)

        val pitch = orientation[1]
        val roll = orientation[2]

        onTiltChanged(pitch, roll)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
}