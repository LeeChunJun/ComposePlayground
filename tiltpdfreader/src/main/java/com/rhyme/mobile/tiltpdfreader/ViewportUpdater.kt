package com.rhyme.mobile.tiltpdfreader

import android.graphics.Matrix
import android.view.TextureView
import android.util.Log

class ViewportUpdater(private val textureView: TextureView) {

    private val maxOffsetX = 400f
    private val maxOffsetY = 600f

    fun updateOffset(pitch: Float, roll: Float) {
        // Convert from radians to degrees for easier handling
        val pitchDegrees = Math.toDegrees(pitch.toDouble()).toFloat()
        val rollDegrees = Math.toDegrees(roll.toDouble()).toFloat()
        
        // Adjust the sensitivity and invert the directions as needed
        val offsetX = (-rollDegrees * 20).coerceIn(-maxOffsetX, maxOffsetX)
        val offsetY = (pitchDegrees * 15).coerceIn(-maxOffsetY, maxOffsetY)

        val matrix = Matrix()
        matrix.setTranslate(offsetX, offsetY)
        
        Log.d("ViewportUpdater", "Pitch: $pitch, Roll: $roll, OffsetX: $offsetX, OffsetY: $offsetY")
        
        textureView.setTransform(matrix)
    }
}