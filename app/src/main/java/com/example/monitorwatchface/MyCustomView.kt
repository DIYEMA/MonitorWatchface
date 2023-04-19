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

    private var mIsClickable = true
    private val mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var mIconRect = RectF(77.5f, 10f, 150f, 200f)
    private var mPackageName = "com.example.fatiguemonitor"
    private var mActivityName = "com.example.fatiguemonitor.presentation.EnergySeekBarActivity"

    init {
        mPaint.style = Paint.Style.FILL
        mPaint.color = Color.parseColor("#332F2F")
    }

    fun setIconPosition(left: Float, top: Float, right: Float, bottom: Float) {
        mIconRect = RectF(left, top, right, bottom)
        invalidate()
    }

    fun setActivity(activityName: String) {
        mActivityName = activityName
    }

    fun toggleClickable(isClickable: Boolean) {
        mIsClickable = isClickable
    }

    override fun onWindowVisibilityChanged(visibility: Int) {
        super.onWindowVisibilityChanged(visibility)
        mIsClickable = visibility == VISIBLE
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                // User touched the screen
                // Start a delayed runnable to check if the touch is a long press
                val handler = Handler()
                handler.postDelayed({
                    if (event.actionMasked == MotionEvent.ACTION_DOWN && mIsClickable) {
                        // Touch is a long press
                        // Perform the action you want here
                        if (mIconRect.contains(event.x, event.y)) {
                            val intent = Intent().apply {
                                setClassName(mPackageName, mActivityName)
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            }
                            context.startActivity(intent)
                        }
                    }
                }, 250) // Delay of 250 milliseconds
            }
//            MotionEvent.ACTION_UP -> {
//                // User lifted their finger from the screen
//                // Cancel any pending long press callbacks
//                handler.removeCallbacksAndMessages(null)
//            }
        }
        return super.onTouchEvent(event)
    }


//    override fun onTouchEvent(event: MotionEvent): Boolean {
//        if (event.action == MotionEvent.ACTION_UP && mIsClickable) {
//            if (mIconRect.contains(event.x, event.y)) {
//                val intent = Intent().apply {
//                    setClassName(mPackageName, mActivityName)
//                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                    addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
//                }
//                context.startActivity(intent)
//                return true
//            }
//        }
//        return super.onTouchEvent(event)
//    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawCircle(mIconRect.centerX(), mIconRect.centerY(), mIconRect.width() / 2, mPaint)
    }
}



