package com.example.monitorwatchface

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View

class CurvedTextImageView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val text = "Remember to Charge Me!"
    private val path = Path()

    init {
        // Define the path (for example, a circle)
        val centerX = width / 2f
        val centerY = height / 2f
        val radius = Math.min(centerX, centerY)
        path.addCircle(centerX, centerY, radius, Path.Direction.CW)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Set up paint properties (text size, color, etc.)
        val paint = Paint().apply {
            textSize = 30f
            color = Color.RED
            isAntiAlias = true
        }

        // Draw the text along the path
        canvas.drawTextOnPath(text, path, 0f, 0f, paint)
    }
}