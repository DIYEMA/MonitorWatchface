package com.example.monitorwatchface

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.MotionEvent
import android.view.View

class MyCustomView(context: Context) : View(context) {

    private val mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var mIconRect = RectF(140f, 40f, 220f, 130f)
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

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            if (mIconRect.contains(event.x, event.y)) {
                val intent = Intent().apply {
                    setClassName(mPackageName, mActivityName)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                }
                context.startActivity(intent)
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawCircle(mIconRect.centerX(), mIconRect.centerY(), mIconRect.width() / 2, mPaint)
    }
}



