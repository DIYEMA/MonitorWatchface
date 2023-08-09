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


    private lateinit var b4Icon: Drawable
    private lateinit var icon1: Drawable
    private lateinit var icon2: Drawable
    private lateinit var icon3: Drawable
    private lateinit var icon4: Drawable

    private lateinit var b1Text: String
    private lateinit var b2Text: String
    private lateinit var b3Text: String
    private lateinit var b4Text: String
    // Declare variables for user preferences
    private var b1medium = 0
    private var b2medium = 0
    private var b3medium = 0
    private var b4medium = 0
    private var b1image = 0
    private var b2image = 0
    private var b3image = 0
    private var b4image = 0

    private var iconSize = 4

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
        private lateinit var button1View: MyCustomView
        private lateinit var button2View: MyCustomView
        private lateinit var button3View: MyCustomView
        private lateinit var button4View: MyCustomView

        override fun onCreate(surfaceHolder: SurfaceHolder) {
            super.onCreate(surfaceHolder)
            button1View = MyCustomView(this@MyWatchFaceService)
            button2View = MyCustomView(this@MyWatchFaceService)
            button3View = MyCustomView(this@MyWatchFaceService)
            button4View = MyCustomView(this@MyWatchFaceService)
            setTouchEventsEnabled(true)
        }

        // Remove circular background click-ability on destroy
        override fun onDestroy() {
            super.onDestroy()
            button1View.toggleClickable(false)
            button2View.toggleClickable(false)
            button3View.toggleClickable(false)
            button4View.toggleClickable(false)
        }

        // Upon circular background touch, launch input screens
        override fun onTouchEvent(event: MotionEvent) {
            super.onTouchEvent(event)
            button1View.onTouchEvent(event)
            button2View.onTouchEvent(event)
            button3View.onTouchEvent(event)
            button4View.onTouchEvent(event)
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
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

            // Draw the current time in 12-hour clock format
            drawTime(canvas, bounds)

            // Draw the battery progress ring
            drawBatteryProgressRing(canvas, bounds)

            // Draw elements related to mood
            drawButton1Elements(canvas, bounds, button1View)

            // Draw elements related to intensity
            drawButton2Elements(canvas, bounds, button2View)

            // Draw elements related to sleep
            drawButton3Elements(canvas, bounds, button3View)

            // Draw elements related to food count
            drawButton4Elements(canvas, bounds, button4View)


        }
        @SuppressLint("Range")
        private fun preferencesContentResolver() {
            val uri = Uri.parse("content://com.example.fatiguemonitor.provider")
            val cursor = contentResolver.query(uri, null, null, null, null)
            if (cursor != null && cursor.moveToFirst()) {
                if (cursor.moveToNext()) {
                    b1medium = cursor.getInt(cursor.getColumnIndex("value"))
                }
                if (cursor.moveToNext()) {
                    b1image = cursor.getInt(cursor.getColumnIndex("value"))
                }
                if (cursor.moveToNext()) {
                    b2medium = cursor.getInt(cursor.getColumnIndex("value"))
                }
                if (cursor.moveToNext()) {
                    b2image = cursor.getInt(cursor.getColumnIndex("value"))
                }
                if (cursor.moveToNext()) {
                    b3medium = cursor.getInt(cursor.getColumnIndex("value"))
                }
                if (cursor.moveToNext()) {
                    b3image = cursor.getInt(cursor.getColumnIndex("value"))
                }
                if (cursor.moveToNext()) {
                    b4medium = cursor.getInt(cursor.getColumnIndex("value"))
                }
                if (cursor.moveToNext()) {
                    b4image = cursor.getInt(cursor.getColumnIndex("value"))
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
                button1View.toggleClickable(false)
                button3View.toggleClickable(false)
            } else {
                mainTextPaint.color = Color.WHITE
                button1View.toggleClickable(true)
                button3View.toggleClickable(true)
            }
            invalidate()
        }

        // Toggle click-ability if the watch face is no longer the focus (such as when swiping up
        // to read notifications)
        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            button1View.toggleClickable(visible)
            button2View.toggleClickable(visible)
            button3View.toggleClickable(visible)
            button4View.toggleClickable(visible)
        }

        // Helper function to set sleep and mood icons according to current values
        private fun setIcons(context: Context) {

            icon1 = context.getDrawable(R.drawable.default_icon)!!
            icon2 = context.getDrawable(R.drawable.default_icon)!!
            icon3 = context.getDrawable(R.drawable.default_icon)!!
            icon4 = context.getDrawable(R.drawable.default_icon)!!
            b1Text = context.getString(R.string.button1Text)!!
            b2Text = context.getString(R.string.button2Text)!!
            b3Text = context.getString(R.string.button3Text)!!
            b4Text = context.getString(R.string.button4Text)!!

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



    // Helper function to draw the battery progress ring
    private fun drawBatteryProgressRing(canvas: Canvas, bounds: Rect) {
        val batteryManager = getSystemService(BATTERY_SERVICE) as BatteryManager
        val batteryLevel =
            batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        val batteryCount = "$batteryLevel%"

        val iconWidth = (horizontalLength / iconSize)
        val iconHeight = (verticalLength / iconSize)
        val batteryX = bounds.exactCenterX()
        val batteryY = bounds.exactCenterY()  + (iconHeight*1.5)
        canvas.drawText(
            batteryCount, batteryX.toFloat(),
            batteryY.toFloat(), supportingTextPaint
        )

        val circleRadius = (horizontalLength / (iconSize*4))
        val circleX = batteryX
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
        val batteryText = "Battery"
        val batteryTextY = circleRectF.top - (supportingTextPaint.textSize / 2.5)
        canvas.drawText(batteryText, circleX.toFloat(), batteryTextY.toFloat(), supportingTextPaint)
    }

    // Helper function to draw elements related to mood
    private fun drawButton1Elements(canvas: Canvas, bounds: Rect, b1Button: MyCustomView) {
        // Draw the mood icon and it's clickable circular background
        val iconWidth = (horizontalLength / iconSize)
        val iconHeight = (verticalLength / iconSize)

        val iconLeft = bounds.centerX() - iconWidth/2 - iconWidth
        val iconTop = bounds.centerY()  - (iconHeight*1.5)

        icon1.setBounds(
            iconLeft.toInt(), iconTop.toInt(),
            (iconLeft + iconWidth).toInt(), (iconTop + iconHeight).toInt()
        )

        b1Button.setButtonPosition(
            icon1.bounds.left.toFloat(),
            icon1.bounds.top.toFloat() , icon1.bounds.right.toFloat(),
            icon1.bounds.bottom.toFloat()
        )

        // Set the circular background icon to launch the preferred input screen for mood
        b1Button.setActivity("com.example.fatiguemonitor.presentation.Activity1")

        b1Button.draw(canvas)
        icon1.draw(canvas)

        // Draw the mood text

        val moodTextX = iconLeft + (iconWidth / 2)
        val moodTextY = iconTop + iconHeight + (supportingTextPaint.textSize )
        canvas.drawText(
            b1Text,
            moodTextX.toFloat(), moodTextY.toFloat(), supportingTextPaint
        )
    }

    // Helper function to draw elements related to mood
    private fun drawButton2Elements(canvas: Canvas, bounds: Rect, b2Button: MyCustomView) {
        // Draw the intensity icon mood and it's clickable circular background
        val iconWidth = (horizontalLength / iconSize)
        val iconHeight = (verticalLength / iconSize)
        val iconLeft = bounds.centerX() - iconWidth/2 + iconWidth
        val iconTop = bounds.centerY()  - (iconHeight*1.5)
        icon2.setBounds(
            iconLeft.toInt(), iconTop.toInt(),
            ((iconLeft + iconWidth).toInt()), ((iconTop + iconHeight).toInt())
        )

        b2Button.setButtonPosition(
            icon2.bounds.left.toFloat(),
            icon2.bounds.top.toFloat(), icon2.bounds.right.toFloat(),
            icon2.bounds.bottom.toFloat()
        )

        // Set the intensity icon to launch the input screen for intensity
        b2Button.setActivity("com.example.fatiguemonitor.presentation.IntensityActivity")
        b2Button.draw(canvas)
        icon2.draw(canvas)

        // Draw the mood text

        val intensityTextX = iconLeft + (iconWidth / 2)
        val intensityTextY = iconTop + iconHeight + (supportingTextPaint.textSize )
        canvas.drawText(
            b2Text,
            intensityTextX.toFloat(), intensityTextY.toFloat(), supportingTextPaint
        )
    }

    // Helper function to draw elements related to sleep
    private fun drawButton3Elements(canvas: Canvas, bounds: Rect, b3Button: MyCustomView) {
        // Draw the sleep icon and it's clickable circular background
        val iconWidth = (horizontalLength / iconSize)
        val iconHeight = (verticalLength / iconSize)

        val iconLeft = bounds.centerX() - (iconWidth/2) - iconWidth
        val iconTop = bounds.centerY()  + (iconHeight*1.5) - iconHeight
        icon3.setBounds(
            iconLeft.toInt(),
            iconTop.toInt(),
            (iconLeft + iconWidth).toInt(),
            (iconTop + iconHeight).toInt()
        )

        b3Button.setButtonPosition(
            icon3.bounds.left.toFloat(),
            icon3.bounds.top.toFloat(), icon3.bounds.right.toFloat(),
            icon3.bounds.bottom.toFloat()
        )

        // Set the circular background icon to launch the preferred input screen for sleep

        b3Button.setActivity("com.example.fatiguemonitor.presentation.EnergySliderActivity")


        b3Button.draw(canvas)
        icon3.draw(canvas)

        // Draw the sleep text

        val sleepTextX = iconLeft + (iconWidth / 2)
        val sleepTextY = iconTop - (supportingTextPaint.textSize / 4)
        canvas.drawText(
            b3Text,
            sleepTextX.toFloat(), sleepTextY.toFloat(), supportingTextPaint
        )
    }
    // Helper function to draw elements related to food count
    private fun drawButton4Elements(canvas: Canvas, bounds: Rect, b4Button: MyCustomView) {


        // Draw the food count icon
        val iconWidth = (horizontalLength / iconSize)
        val iconHeight = (verticalLength / iconSize)
        val iconLeft = bounds.centerX() - (iconWidth/2) + iconWidth
        val iconTop = bounds.centerY()  + (iconHeight*1.5) - iconHeight

        icon4.setBounds(
            iconLeft.toInt(), iconTop.toInt(),
            (iconLeft + iconWidth).toInt(), (iconTop + iconHeight).toInt()
        )
        b4Button.setButtonPosition(
            icon4.bounds.left.toFloat(),
            icon4.bounds.top.toFloat(), icon4.bounds.right.toFloat(),
            icon4.bounds.bottom.toFloat()
        )
        b4Button.setActivity("com.example.fatiguemonitor.presentation.EnergySliderActivity")
        b4Button.draw(canvas)
        icon4.draw(canvas)

        // Draw the food count text

        val foodCountTextX = iconLeft + (iconWidth / 2)
        val foodCountTextY = iconTop - (supportingTextPaint.textSize / 4)
        canvas.drawText(
            b4Text,
            foodCountTextX.toFloat(), foodCountTextY.toFloat(), supportingTextPaint
        )

    }
}