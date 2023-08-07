@file:Suppress("DEPRECATION")

package com.example.monitorwatchface

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.BatteryManager
import android.support.wearable.watchface.CanvasWatchFaceService
import android.view.*
import android.widget.*
import com.google.android.gms.wearable.*
import java.text.DateFormat
import java.text.DateFormat.getTimeInstance
import java.util.*

class MyWatchFaceService : CanvasWatchFaceService() {

    // Declare variables to be initialised later
    private lateinit var mainTextPaint: Paint
    private lateinit var supportingTextPaint: Paint

    private lateinit var batteryBackgroundPaint: Paint
    private lateinit var batteryForegroundPaint: Paint
    private val backgroundColour = Color.parseColor("#00FFFFFF")
    private var currentTime = Calendar.getInstance()


    private lateinit var foodIcon: Drawable
    private lateinit var moodIcon: Drawable
    private lateinit var intensityIcon: Drawable
    private lateinit var button1: Drawable

    // Declare variables for user preferences
    private var moodMedium = 0
    private var sleepMedium = 0
    private var sleepImage = 0

    // Declare variables to collect screen size
    private var verticalLength = 0
    private var horizontalLength = 0

    // Styling for some visual elements
    override fun onCreateEngine(): Engine {

        mainTextPaint = Paint().apply {
            color = Color.WHITE
            textSize = resources.getDimension(R.dimen.main_text_size)
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
        }

        supportingTextPaint = Paint().apply {
            color = Color.WHITE
            textSize = resources.getDimension(R.dimen.supporting_text_size)
            typeface = Typeface.DEFAULT
            textAlign = Paint.Align.CENTER
        }

        batteryBackgroundPaint = Paint().apply {
            color = Color.parseColor("#2196F3")
            strokeWidth = resources.getDimension(R.dimen.progress_width) / 2
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            alpha = 128
            isAntiAlias = true
        }

        batteryForegroundPaint = Paint().apply {
            color = Color.parseColor("#2196F3")
            strokeWidth = resources.getDimension(R.dimen.progress_width) / 2
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            isAntiAlias = true
        }


        return MyEngine()
    }

    private inner class MyEngine : Engine() {

        // Declare circular background
        private lateinit var moodView: MyCustomView
        private lateinit var intensityView: MyCustomView
        private lateinit var sleepView: MyCustomView

        override fun onCreate(surfaceHolder: SurfaceHolder) {
            super.onCreate(surfaceHolder)
            moodView = MyCustomView(this@MyWatchFaceService)
            intensityView = MyCustomView(this@MyWatchFaceService)
            sleepView = MyCustomView(this@MyWatchFaceService)
            setTouchEventsEnabled(true)
        }

        // Remove circular background click-ability on destroy
        override fun onDestroy() {
            super.onDestroy()
            moodView.toggleClickable(false)
            intensityView.toggleClickable(false)
            sleepView.toggleClickable(false)
        }

        // Upon circular background touch, launch input screens
        override fun onTouchEvent(event: MotionEvent) {
            super.onTouchEvent(event)
            moodView.onTouchEvent(event)
            intensityView.onTouchEvent(event)
            sleepView.onTouchEvent(event)
        }

        override fun onDraw(canvas: Canvas, bounds: Rect) {
            super.onDraw(canvas, bounds)
            // Collect screen size information for dynamic element sizing
            verticalLength = bounds.bottom - bounds.top
            horizontalLength = bounds.right - bounds.left

            preferencesContentResolver()

            // Set mood, intensity and sleep icons according to current values
            setIcons(applicationContext)

            // Draw the background colour
            canvas.drawColor(backgroundColour)

            // Draw the current time in 12-hour clock format
            drawTime(canvas, bounds)

            // Draw the battery progress ring
            drawBatteryProgressRing(canvas, bounds)

            // Draw elements related to food count
            drawButton1Elements(canvas, bounds)

            // Draw elements related to mood
            drawButton2Elements(canvas, bounds, moodView)

            // Draw elements related to intensity
            drawButton3Elements(canvas, bounds, intensityView)

            // Draw elements related to sleep
            drawButton4Elements(canvas, bounds, sleepView)


        }
        @SuppressLint("Range")
        private fun preferencesContentResolver() {
            val uri = Uri.parse("content://com.example.fatiguemonitor.provider")
            val cursor = contentResolver.query(uri, null, null, null, null)
            if (cursor != null && cursor.moveToFirst()) {
                steps = cursor.getInt(cursor.getColumnIndex("value"))
                if (cursor.moveToNext()) {
                    mood = cursor.getInt(cursor.getColumnIndex("value"))
                }
                if (cursor.moveToNext()) {
                    moodMedium = cursor.getInt(cursor.getColumnIndex("value"))
                }
                if (cursor.moveToNext()) {
                    intensity = cursor.getInt(cursor.getColumnIndex("value"))
                }
                if (cursor.moveToNext()) {
                    cep = cursor.getInt(cursor.getColumnIndex("value"))
                }
                if (cursor.moveToNext()) {
                    fatigue = cursor.getInt(cursor.getColumnIndex("value"))
                }
                if (cursor.moveToNext()) {
                    fatigueMedium = cursor.getInt(cursor.getColumnIndex("value"))
                }
                if (cursor.moveToNext()) {
                    fatigueImage = cursor.getInt(cursor.getColumnIndex("value"))
                }
                if (cursor.moveToNext()) {
                    hr = cursor.getInt(cursor.getColumnIndex("value"))
                }
            }
            cursor?.close()
        }
        // Redraw watch face every minute
        override fun onTimeTick() {
            super.onTimeTick()
            currentTime = Calendar.getInstance()
            invalidate()
        }

        // Toggle click-ability if the watch goes in ambient mode
        override fun onAmbientModeChanged(inAmbientMode: Boolean) {
            if (inAmbientMode) {
                mainTextPaint.color = Color.GRAY
                moodView.toggleClickable(false)
                sleepView.toggleClickable(false)
            } else {
                mainTextPaint.color = Color.WHITE
                moodView.toggleClickable(true)
                sleepView.toggleClickable(true)
            }
            invalidate()
        }

        // Toggle click-ability if the watch face is no longer the focus (such as when swiping up
        // to read notifications)
        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            moodView.toggleClickable(visible)
            sleepView.toggleClickable(visible)
        }

        // Helper function to set sleep and mood icons according to current values
        private fun setIcons(context: Context) {

            moodIcon = context.getDrawable(R.drawable.default_icon)!!
            intensityIcon = context.getDrawable(R.drawable.default_icon)!!
            button1 = context.getDrawable(R.drawable.default_icon)!!

        }
    }
    
    // Helper function to draw the current time in 12-hour clock format
    private fun drawTime(canvas: Canvas, bounds: Rect) {
        val timeFormat = getTimeInstance(DateFormat.SHORT)
        val timeString = timeFormat.format(currentTime.time)
        val hourMinute = timeString.substringBefore(" ")
        val amPm = timeString.substringAfter(" ").uppercase()
        val amPmY =
            bounds.exactCenterY() + mainTextPaint.descent() + resources.getDimension(R.dimen.text_padding)
        canvas.drawText(hourMinute, bounds.exactCenterX(), bounds.exactCenterY(), mainTextPaint)
        canvas.drawText(amPm, bounds.exactCenterX(), amPmY, mainTextPaint)
    }

    // Helper function to draw elements related to food count
    private fun drawButton1Elements(canvas: Canvas, bounds: Rect) {
        foodIcon = applicationContext.getDrawable(R.drawable.default_icon)!!

        // Draw the food count icon
        val foodIconWidth = (horizontalLength / 6)
        val foodIconHeight = (verticalLength / 6)
        val foodIconLeft = (horizontalLength / 4.5)
        val foodIconTop = bounds.centerY() - (verticalLength / 5) - (foodIconHeight / 2)
        foodIcon.setBounds(
            foodIconLeft.toInt(), foodIconTop,
            ((foodIconLeft + foodIconWidth).toInt()), (foodIconTop + foodIconHeight)
        )
        foodIcon.draw(canvas)

        // Draw the food count text
        val foodCountText = "Food"
        val foodCountTextX = foodIconLeft + (foodIconWidth / 2)
        val foodCountTextY = foodIconTop - (supportingTextPaint.textSize / 4)
        canvas.drawText(
            foodCountText,
            foodCountTextX.toFloat(), foodCountTextY, supportingTextPaint
        )

    }

    // Helper function to draw the battery progress ring
    private fun drawBatteryProgressRing(canvas: Canvas, bounds: Rect) {
        val batteryManager = getSystemService(BATTERY_SERVICE) as BatteryManager
        val batteryLevel =
            batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        val batteryCount = "$batteryLevel%"
        val batteryX = 2.725 * (horizontalLength / 4.5) + (horizontalLength / 12)
        val batteryY = bounds.centerY() - (verticalLength / 5.5)
        canvas.drawText(
            batteryCount, batteryX.toFloat(),
            batteryY.toFloat(), supportingTextPaint
        )

        val circleRadius = (horizontalLength / 12)
        val circleX = 2.725 * (horizontalLength / 4.5) + (horizontalLength / 12)
        val circleY = batteryY - (supportingTextPaint.textSize / 4)
        val circleRectF = RectF(
            (circleX - circleRadius).toFloat(), (circleY - circleRadius).toFloat(),
            (circleX + circleRadius).toFloat(), (circleY + circleRadius).toFloat()
        )
        canvas.drawCircle(
            circleRectF.centerX(), circleRectF.centerY(),
            circleRadius.toFloat(), batteryBackgroundPaint
        )
        canvas.drawArc(
            circleRectF,
            -90f,
            (batteryLevel.toFloat() / 100f) * 360f,
            false,
            batteryForegroundPaint
        )

        // Draw the battery text
        val batteryText = "Device Power"
        val batteryTextY = circleRectF.top - (supportingTextPaint.textSize / 2.5)
        canvas.drawText(batteryText, circleX.toFloat(), batteryTextY.toFloat(), supportingTextPaint)
    }

    // Helper function to draw elements related to mood
    private fun drawButton2Elements(canvas: Canvas, bounds: Rect, moodView: MyCustomView) {
        // Draw the mood icon and it's clickable circular background
        val moodIconWidth = (horizontalLength / 6)
        val moodIconHeight = (verticalLength / 5.15)
        val moodIconLeft = (4 * (horizontalLength / 5)) - (moodIconWidth / 2)
        val moodIconTop =  bounds.centerY() + (verticalLength / 8) - (moodIconHeight / 2)
        moodIcon.setBounds(
            moodIconLeft, moodIconTop.toInt(),
            (moodIconLeft + moodIconWidth), (moodIconTop + moodIconHeight).toInt()
        )

        moodView.setIconPosition(
            moodIcon.bounds.left.toFloat() * 0.97f,
            moodIcon.bounds.top.toFloat() * 0.9f, moodIcon.bounds.right.toFloat() * 1.03f,
            moodIcon.bounds.bottom.toFloat() * 1.1f
        )

        // Set the circular background icon to launch the preferred input screen for mood
        if (moodMedium == 0) {
            moodView.setActivity("com.example.fatiguemonitor.presentation.MoodSeekBarActivity")
        } else {
            moodView.setActivity("com.example.fatiguemonitor.presentation.MoodSliderActivity")
        }

        moodView.draw(canvas)
        moodIcon.draw(canvas)

        // Draw the mood text
        val moodText = "Mood"
        val moodTextX = moodIconLeft + (moodIconWidth / 2)
        val moodTextY = moodIconTop - (supportingTextPaint.textSize / 4)
        canvas.drawText(
            moodText,
            moodTextX.toFloat(), moodTextY.toFloat(), supportingTextPaint
        )
    }

    // Helper function to draw elements related to mood
    private fun drawButton3Elements(canvas: Canvas, bounds: Rect, intensityView: MyCustomView) {
        // Draw the intensity icon mood and it's clickable circular background
        val intensityIconWidth = (horizontalLength / 3)
        val intensityIconHeight = (verticalLength / 7.5)
        val intensityIconLeft = bounds.centerX() - (intensityIconWidth / 2)
        val intensityIconTop =  bounds.centerY() + (verticalLength / 3.5) - (intensityIconHeight / 2)
        intensityIcon.setBounds(
            intensityIconLeft.toInt(), intensityIconTop.toInt(),
            ((intensityIconLeft + intensityIconWidth).toInt()), ((intensityIconTop + intensityIconHeight).toInt())
        )

        intensityView.setPaintColour("#3E3939")

        intensityView.setIconPosition(
            intensityIcon.bounds.left.toFloat(),
            intensityIcon.bounds.top.toFloat(), intensityIcon.bounds.right.toFloat(),
            intensityIcon.bounds.bottom.toFloat()
        )

        // Set the intensity icon to launch the input screen for intensity
        intensityView.setActivity("com.example.fatiguemonitor.presentation.IntensityActivity")

        intensityView.draw(canvas)
        intensityIcon.draw(canvas)

        // Draw the mood text
        val intensityText = "Intensity"
        val intensityTextX = intensityIconLeft + (intensityIconWidth / 2)
        val intensityTextY = intensityIconTop - (supportingTextPaint.textSize / 2)
        canvas.drawText(
            intensityText,
            intensityTextX.toFloat(), intensityTextY.toFloat(), supportingTextPaint
        )
    }

    // Helper function to draw elements related to sleep
    private fun drawButton4Elements(canvas: Canvas, bounds: Rect, sleepView: MyCustomView) {
        // Draw the sleep icon and it's clickable circular background
        val iconWidth = (horizontalLength / 7.25)
        val iconHeight = (verticalLength / 5.15)
        val iconLeft = (horizontalLength / 5) - (iconWidth / 2)
        val iconTop = bounds.centerY() + (verticalLength / 8) - (iconHeight / 2)
        button1.setBounds(
            iconLeft.toInt(),
            iconTop.toInt(),
            (iconLeft + iconWidth).toInt(),
            (iconTop + iconHeight).toInt()
        )

        sleepView.setIconPosition(
            button1.bounds.left.toFloat() * 0.85f,
            button1.bounds.top.toFloat() * 0.9f, button1.bounds.right.toFloat() * 1.1f,
            button1.bounds.bottom.toFloat() * 1.1f
        )

        // Set the circular background icon to launch the preferred input screen for sleep
        if (sleepMedium == 0) {
            sleepView.setActivity("com.example.fatiguemonitor.presentation.EnergySeekBarActivity")
        } else if (sleepImage == 0) {
            sleepView.setActivity("com.example.fatiguemonitor.presentation.EnergySliderActivity")
        } else {
            sleepView.setActivity("com.example.fatiguemonitor.presentation.EnergySliderActivity2")
        }

        sleepView.draw(canvas)
        button1.draw(canvas)

        // Draw the sleep text
        val sleepText = "Sleep"
        val sleepTextX = iconLeft + (iconWidth / 2)
        val sleepTextY = iconTop - (supportingTextPaint.textSize / 4)
        canvas.drawText(
            sleepText,
            sleepTextX.toFloat(), sleepTextY.toFloat(), supportingTextPaint
        )
    }
}