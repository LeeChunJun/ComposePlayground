package com.rhyme.mobile.tiltpdfreader

import android.graphics.Matrix
import android.view.TextureView

class ViewportUpdater(private val textureView: TextureView) {

    private val maxOffsetX = 400f
    private val maxOffsetY = 600f

    fun updateOffset(pitch: Float, roll: Float) {
        val offsetX = (roll * 200).coerceIn(-maxOffsetX, maxOffsetX)
        val offsetY = (pitch * 200).coerceIn(-maxOffsetY, maxOffsetY)

        val matrix = Matrix().apply {
            setTranslate(-offsetX, -offsetY)
        }

        textureView.setTransform(matrix)
    }
}
