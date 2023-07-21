package com.example.monitorwatchface

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Handler
import android.view.MotionEvent
import android.view.View

class MyCustomView(context: Context) : View(context) {

    // Declare and initialise variables
    private var mIsClickable = true
    private val mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var mIconRect = RectF(0f, 0f, 0f, 0f)
    private var mPackageName = "com.example.fatiguemonitor"
    private var mActivityName = "com.example.fatiguemonitor.presentation.EnergySeekBarActivity"

    init {
        mPaint.style = Paint.Style.FILL
        mPaint.color = Color.parseColor("#332F2F")
    }

    // Change circular background paint colour
    fun setPaintColour(colourString: String) {
        mPaint.color = Color.parseColor(colourString)
        invalidate()
    }

    // Change circular background icon position
    fun setIconPosition(left: Float, top: Float, right: Float, bottom: Float) {
        mIconRect = RectF(left, top, right, bottom)
        invalidate()
    }

    // Change intended activity to be launched
    fun setActivity(activityName: String) {
        mActivityName = activityName
    }

    // Toggle click-ability when the watch face is no longer in focus
    fun toggleClickable(isClickable: Boolean) {
        mIsClickable = isClickable
    }

    override fun onWindowVisibilityChanged(visibility: Int) {
        super.onWindowVisibilityChanged(visibility)
        mIsClickable = visibility == VISIBLE
    }

    // Custom override to launch preferred input screen once the circular background icon is touched
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                // User touched the screen, start a delayed runnable
                val handler = Handler()
                handler.postDelayed({
                    if (event.actionMasked == MotionEvent.ACTION_DOWN && mIsClickable) {
                        // Launch preferred input screen
                        if (mIconRect.contains(event.x, event.y)) {
                            val intent = Intent().apply {
                                setClassName(mPackageName, mActivityName)
                                // Flags used to ensure a single task is started and is brought to
                                // the top of a blank task stack
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            }
                            context.startActivity(intent)
                        }
                    }
                }, 250) // Delay of 250 milliseconds
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawCircle(mIconRect.centerX(), mIconRect.centerY(), mIconRect.width() / 2.0f, mPaint)
    }
}



