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
import java.lang.Float.min
import java.text.DateFormat
import java.text.DateFormat.getTimeInstance
import java.util.*

class MyWatchFaceService : CanvasWatchFaceService() {

    // Declare variables to be initialised later
    private lateinit var mainTextPaint: Paint
    private lateinit var supportingTextPaint: Paint
    private lateinit var fatigueBackgroundPaint: Paint
    private lateinit var fatigueForegroundPaint: Paint
    private lateinit var batteryBackgroundPaint: Paint
    private lateinit var batteryForegroundPaint: Paint
    private val backgroundColour = Color.parseColor("#3E3939")
    private var currentTime = Calendar.getInstance()
    private var steps = 0
    private var mood = 0
    private var intensity = 0
    private var cep = 0
    private var fatigue = 0
    private var hr = 0

    private lateinit var stepsIcon: Drawable
    private lateinit var moodIcon: Drawable
    private lateinit var intensityIcon: Drawable
    private lateinit var fatigueIcon: Drawable
    private lateinit var heartIcon: Drawable

    // Declare variables for user preferences
    private var moodMedium = 0
    private var fatigueMedium = 0
    private var fatigueImage = 0

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

        fatigueBackgroundPaint = Paint().apply {
            color = Color.WHITE
            strokeWidth = resources.getDimension(R.dimen.progress_width)
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            alpha = 128
            isAntiAlias = true
        }

        fatigueForegroundPaint = Paint().apply {
            color = Color.WHITE
            strokeWidth = resources.getDimension(R.dimen.progress_width)
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
        private lateinit var fatigueView: MyCustomView

        override fun onCreate(surfaceHolder: SurfaceHolder) {
            super.onCreate(surfaceHolder)
            moodView = MyCustomView(this@MyWatchFaceService)
            intensityView = MyCustomView(this@MyWatchFaceService)
            fatigueView = MyCustomView(this@MyWatchFaceService)
            setTouchEventsEnabled(true)
        }

        // Remove circular background click-ability on destroy
        override fun onDestroy() {
            super.onDestroy()
            moodView.toggleClickable(false)
            intensityView.toggleClickable(false)
            fatigueView.toggleClickable(false)
        }

        // Upon circular background touch, launch input screens
        override fun onTouchEvent(event: MotionEvent) {
            super.onTouchEvent(event)
            moodView.onTouchEvent(event)
            intensityView.onTouchEvent(event)
            fatigueView.onTouchEvent(event)
        }

        override fun onDraw(canvas: Canvas, bounds: Rect) {
            super.onDraw(canvas, bounds)
            // Collect screen size information for dynamic element sizing
            verticalLength = bounds.bottom - bounds.top
            horizontalLength = bounds.right - bounds.left

            // Use a ContentResolver to gather updated values and preferences set by the companion app
            preferencesContentResolver()

            // Use a ContentResolver to gather updated step count provided by Mobvoi
//            stepsContentResolver()

            // Set mood, intensity and fatigue icons according to current values
            dynamicChange(applicationContext)

            // Draw the background colour
            canvas.drawColor(backgroundColour)

            // Draw the current time in 12-hour clock format
            drawTime(canvas, bounds)

            // Draw elements related to steps count
            drawStepsCountElements(canvas, bounds)

            // Draw the battery progress ring
            drawBatteryProgressRing(canvas, bounds)

            // Draw elements related to mood
            drawMoodElements(canvas, bounds, moodView)

            // Draw elements related to intensity
            drawIntensityElements(canvas, bounds, intensityView)

            // Draw elements related to fatigue
            drawFatigueElements(canvas, bounds, fatigueView)

            // Draw the fatigue progress ring
            drawFatigueProgressRing(canvas, bounds)

            // Draw elements related to heart rate count
            // SUMMER 2023 UPDATE: REMOVED FOR STUDY
//            drawHRCountElements(canvas, bounds)
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
                fatigueView.toggleClickable(false)
            } else {
                mainTextPaint.color = Color.WHITE
                moodView.toggleClickable(true)
                fatigueView.toggleClickable(true)
            }
            invalidate()
        }

        // Toggle click-ability if the watch face is no longer the focus (such as when swiping up
        // to read notifications)
        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            moodView.toggleClickable(visible)
            fatigueView.toggleClickable(visible)
        }

        // Helper function to set fatigue and mood icons according to current values
        private fun dynamicChange(context: Context) {
            data class State(val icon: Int, val colour: String)

            val moodStates = listOf(
                State(R.drawable.angry, "#FE4B22"),
                State(R.drawable.disgusted, "#55D051"),
                State(R.drawable.surprised, "#FC9908"),
                State(R.drawable.sad2, "#02B1EA"),
                State(R.drawable.happy2, "#FDE21F"),
                State(R.drawable.scared, "#CD66FF")
            )

            val intensityStates = listOf(
                State(R.drawable.strenuous2, "#FE4B22"),
                State(R.drawable.moderate2, "#55D051"),
                State(R.drawable.relaxed2, "#FC9908")
            )

            val beanFatigueStates = listOf(
                State(R.drawable.very_tired, "#EE2C37"),
                State(R.drawable.tired, "#F36831"),
                State(R.drawable.moderately_tired, "#FCD90E"),
                State(R.drawable.energetic, "#9CCC3C"),
                State(R.drawable.very_energetic, "#46B648")
            )

            val batteryFatigueStates = listOf(
                State(R.drawable.very_tired2, "#EE2C37"),
                State(R.drawable.tired2, "#F36831"),
                State(R.drawable.moderately_tired2, "#FCD90E"),
                State(R.drawable.energetic2, "#9CCC3C"),
                State(R.drawable.very_energetic2, "#46B648")
            )

            val moodState = moodStates.getOrElse(mood) { moodStates.first() }
            val intensityState = intensityStates.getOrElse(intensity) { intensityStates.first() }
            val fatigueState: State = if (fatigueImage == 0) {
                beanFatigueStates.getOrElse(fatigue) { beanFatigueStates.first() }
            } else {
                batteryFatigueStates.getOrElse(fatigue) { batteryFatigueStates.first() }
            }

            moodIcon = context.getDrawable(moodState.icon)!!
            intensityIcon = context.getDrawable(intensityState.icon)!!
            fatigueIcon = context.getDrawable(fatigueState.icon)!!
            fatigueForegroundPaint.color = Color.parseColor(fatigueState.colour)
            fatigueBackgroundPaint.color = Color.parseColor(fatigueState.colour)
            fatigueBackgroundPaint.alpha = 128
        }
    }

    // Helper function to gather updated values and preferences set by the companion app
    @SuppressLint("Range")
    private fun preferencesContentResolver() {
        val uri = Uri.parse("content://com.example.fatiguemonitor.provider/preferences")
        val cursor = contentResolver.query(uri, null, null, null, null)
        if (cursor != null && cursor.moveToFirst()) {
            mood = cursor.getInt(cursor.getColumnIndex("value"))
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

    // Helper function to gather updated step count provided by Mobvoi
    private fun stepsContentResolver() {
        val stepsUri = Uri.parse("content://com.mobvoi.ticwear.steps")
        val stepsCursor = contentResolver.query(stepsUri, null, null, null, null)
        if (stepsCursor != null && stepsCursor.moveToFirst()) {
            steps = stepsCursor.getInt(0)
        }
        stepsCursor?.close()
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

    // Helper function to draw elements related to steps count
    private fun drawStepsCountElements(canvas: Canvas, bounds: Rect) {
        stepsIcon = applicationContext.getDrawable(R.drawable.steps_icon)!!

        // Draw the steps count icon
        val stepsIconWidth = (horizontalLength / 6)
        val stepsIconHeight = (verticalLength / 6)
        val stepsIconLeft = (horizontalLength / 4.5)
        val stepsIconTop = bounds.centerY() - (verticalLength / 5) - (stepsIconHeight / 2)
        stepsIcon.setBounds(
            stepsIconLeft.toInt(), stepsIconTop,
            ((stepsIconLeft + stepsIconWidth).toInt()), (stepsIconTop + stepsIconHeight)
        )
        stepsIcon.draw(canvas)

        // Draw the steps count text
        val stepsCountText = "Steps"
        val stepsCountTextX = stepsIconLeft + (stepsIconWidth / 2)
        val stepsCountTextY = stepsIconTop - (supportingTextPaint.textSize / 4)
        canvas.drawText(
            stepsCountText,
            stepsCountTextX.toFloat(), stepsCountTextY, supportingTextPaint
        )

        // Draw the steps count
        val stepsCount = "$steps"
        val stepsCountY = stepsIconTop + stepsIconHeight + supportingTextPaint.textSize
        canvas.drawText(
            stepsCount, stepsCountTextX.toFloat(),
            stepsCountY, supportingTextPaint
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
    private fun drawMoodElements(canvas: Canvas, bounds: Rect, moodView: MyCustomView) {
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
    private fun drawIntensityElements(canvas: Canvas, bounds: Rect, intensityView: MyCustomView) {
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

    // Helper function to draw elements related to fatigue
    private fun drawFatigueElements(canvas: Canvas, bounds: Rect, fatigueView: MyCustomView) {
        // Draw the fatigue icon and it's clickable circular background
        val fatigueIconWidth = (horizontalLength / 7.25)
        val fatigueIconHeight = (verticalLength / 5.15)
        val fatigueIconLeft = (horizontalLength / 5) - (fatigueIconWidth / 2)
        val fatigueIconTop = bounds.centerY() + (verticalLength / 8) - (fatigueIconHeight / 2)
        fatigueIcon.setBounds(
            fatigueIconLeft.toInt(),
            fatigueIconTop.toInt(),
            (fatigueIconLeft + fatigueIconWidth).toInt(),
            (fatigueIconTop + fatigueIconHeight).toInt()
        )

        fatigueView.setIconPosition(
            fatigueIcon.bounds.left.toFloat() * 0.85f,
            fatigueIcon.bounds.top.toFloat() * 0.9f, fatigueIcon.bounds.right.toFloat() * 1.1f,
            fatigueIcon.bounds.bottom.toFloat() * 1.1f
        )

        // Set the circular background icon to launch the preferred input screen for fatigue
        if (fatigueMedium == 0) {
            fatigueView.setActivity("com.example.fatiguemonitor.presentation.EnergySeekBarActivity")
        } else if (fatigueImage == 0) {
            fatigueView.setActivity("com.example.fatiguemonitor.presentation.EnergySliderActivity")
        } else {
            fatigueView.setActivity("com.example.fatiguemonitor.presentation.EnergySliderActivity2")
        }

        fatigueView.draw(canvas)
        fatigueIcon.draw(canvas)

        // Draw the fatigue text
        val fatigueText = "Fatigue"
        val fatigueTextX = fatigueIconLeft + (fatigueIconWidth / 2)
        val fatigueTextY = fatigueIconTop - (supportingTextPaint.textSize / 4)
        canvas.drawText(
            fatigueText,
            fatigueTextX.toFloat(), fatigueTextY.toFloat(), supportingTextPaint
        )
    }

    // Helper function to draw the fatigue progress ring
    private fun drawFatigueProgressRing(canvas: Canvas, bounds: Rect) {
        val centerX = bounds.centerX()
        val centerY = bounds.centerY()
        val radius = min(
            centerX.toFloat(),
            centerY.toFloat()
        ) - resources.getDimension(R.dimen.progress_margin)
        val rectF =
            RectF(centerX - radius, centerY - radius, centerX + radius, centerY + radius)
        canvas.drawArc(rectF, 135f, 50f, false, fatigueBackgroundPaint)
        canvas.drawArc(
            rectF,
            135f,
            (10 * (fatigue + 1)).toFloat(),
            false,
            fatigueForegroundPaint
        )
    }

    // Helper function to draw elements related to heart rate count
    private fun drawHRCountElements(canvas: Canvas, bounds: Rect) {
        heartIcon = applicationContext.getDrawable(R.drawable.heart_icon)!!

        // Draw the heart rate icon
        val heartIconWidth = (horizontalLength / 6)
        val heartIconHeight = (verticalLength / 6)
        val heartIconLeft = (4 * (horizontalLength / 5)) - (heartIconWidth / 2)
        val heartIconTop = bounds.centerY() + (verticalLength / 8) - (heartIconHeight / 2)
        heartIcon.setBounds(
            heartIconLeft, heartIconTop,
            (heartIconLeft + heartIconWidth), (heartIconTop + heartIconHeight)
        )
        heartIcon.draw(canvas)

        // Draw the heart rate text
        val heartRateText = "HR"
        val heartRateTextX = heartIconLeft + (heartIconWidth / 2)
        val heartRateTextY = heartIconTop - (supportingTextPaint.textSize / 4)
        canvas.drawText(
            heartRateText,
            heartRateTextX.toFloat(), heartRateTextY, supportingTextPaint
        )

        // Draw the heart rate count
        if (hr < 0) hr = 0
        val heartRateCount = "$hr BPM"
        val heartRateCountY = heartIconTop + heartIconHeight + supportingTextPaint.textSize
        canvas.drawText(
            heartRateCount,
            heartRateTextX.toFloat(),
            heartRateCountY,
            supportingTextPaint
        )
    }
}