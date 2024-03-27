@file:Suppress("DEPRECATION")

package com.example.monitorwatchface

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.BatteryManager
import android.support.wearable.watchface.CanvasWatchFaceService
import android.util.Log
import android.view.*
import android.widget.*
import com.google.android.gms.wearable.*
import java.lang.Math.cos
import java.lang.Math.sin
import java.text.DateFormat
import java.text.DateFormat.getTimeInstance
import java.text.SimpleDateFormat
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
    private var dayIndex = 0
    private var act1val = 0
    private var act2val = 0
    private var act3val = 0
    private var act3bval = 0
    private var act4val = 0
    private var steps = 0

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

            button1View.setActivity("com.example.esmartwatch.presentation.Activity1emojis")
            button2View.setActivity("com.example.esmartwatch.presentation.Activity2")
            button3View.setActivity("com.example.esmartwatch.presentation.Activity3time")
            button4View.setActivity("com.example.esmartwatch.presentation.Activity4")

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

            if (hourbetween(19, 24)) {
                val centerX = horizontalLength / 2f
                val centerY = verticalLength / 2f
                val customstrokeWidth = 10f // Adjust the stroke width as needed
                val radius = Math.min(centerX, centerY) - customstrokeWidth // Adjust strokeWidth as needed

                val paint = Paint().apply {
                    color = Color.parseColor("#ffc269")
                    style = Paint.Style.STROKE
                    strokeWidth = customstrokeWidth
                    isAntiAlias = true
                }

                canvas.drawCircle(centerX, centerY, radius, paint)
            } else {
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
            }
            if (hourbetween(18, 24)) {
                drawChargeReminder(canvas,bounds)
            }else{
                // Draw the current time in 12-hour clock format
                drawTime(canvas, bounds.exactCenterX(), bounds.exactCenterY())
            }

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

        private fun hourbetween(firstHour: Int, secondHour: Int): Boolean {
            val calendar = java.util.Calendar.getInstance()
            val currentHour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
            return currentHour in firstHour until secondHour
        }

        @SuppressLint("Range")
        private fun preferencesContentResolver() {
            val uri = Uri.parse("content://com.example.esmartwatch.provider")
            val cursor = contentResolver.query(uri, null, null, null, null)
            if (cursor != null && cursor.moveToFirst()) {

                dayIndex = cursor.getInt(cursor.getColumnIndex("value"))

                if (cursor.moveToNext()) {
                    act1val = cursor.getInt(cursor.getColumnIndex("value"))
                }
                if (cursor.moveToNext()) {
                    act2val = cursor.getInt(cursor.getColumnIndex("value"))
                }
                if (cursor.moveToNext()) {
                    act3val = cursor.getInt(cursor.getColumnIndex("value"))
                }
                if (cursor.moveToNext()) {
                    act3bval = cursor.getInt(cursor.getColumnIndex("value"))
                }
                if (cursor.moveToNext()) {
                    act4val = cursor.getInt(cursor.getColumnIndex("value"))
                }
                if (cursor.moveToNext()) {
                    steps = cursor.getInt(cursor.getColumnIndex("value"))
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
            var hideA1 = false
            var hideA2 = false
            var hideA3 = false
            var hideA4 = false

            if(act1val!=0){
                hideA1 = true
            }
            if(act2val!=0){
                hideA2 = true
            }
            if (act3val!=0 && act3bval!=0){
                hideA3 = true
            }
            if (act4val==2 ){
                hideA4 = true
            }
//            if (dayIndex <= 1) {
//                hideA2 = true
//                hideA3 = true
//                hideA4 = true
//            } else if (dayIndex <= 2) {
//                hideA2 = !hourbetween(5,10)
//                hideA3 = true
//                hideA4 = false
//
//            } else {


//            hideA3 = false
//            hideA4 = false
//            }


//            println("hide a2: $hideA2, hide a3:$hideA3, hide a4:$hideA4")
            icon1 = context.getDrawable(R.drawable.activity_1_icon)!!
            if (hideA1) {
                icon1 = context.getDrawable(R.drawable.activity_1_icon_passive)!!
//                button1View.toggleClickable(false)
            } else {
                icon1 = context.getDrawable(R.drawable.activity_1_icon)!!
                button1View.toggleClickable(true)
            }
            if (!hourbetween(5,10)) {
                icon2 = context.getDrawable(R.drawable.activity_2_icon_disabled)!!
                button2View.toggleClickable(false)
            } else if (hideA2) {
                icon2 = context.getDrawable(R.drawable.activity_2_icon_passive)!!
                button2View.toggleClickable(true)
            }else{
                icon2 = context.getDrawable(R.drawable.activity_2_icon)!!
                button2View.toggleClickable(true)
            }
            if (hideA3) {
                icon3 = context.getDrawable(R.drawable.activity_3_icon_passive)!!
//                button3View.toggleClickable(false)
            } else {
                icon3 = context.getDrawable(R.drawable.activity_3_icon)!!
                button3View.toggleClickable(true)
            }
            if (hideA4) {
                icon4 = context.getDrawable(R.drawable.activity_4_icon_passive)!!
//                button4View.toggleClickable(false)
            } else {
                icon4 = context.getDrawable(R.drawable.activity_4_icon)!!
                button4View.toggleClickable(true)
            }

            b1Text = context.getString(R.string.button1Text)!!
            b2Text = context.getString(R.string.button2Text)!!
            b3Text = context.getString(R.string.button3Text)!!
            b4Text = context.getString(R.string.button4Text)!!

        }
    }

    // Helper function to draw the current time in 12-hour clock format
    private fun drawTime(canvas: Canvas, xlocation: Float, ylocation: Float) {
        canvas.drawText("Day\n$dayIndex", xlocation, ylocation, mainTextPaint)

    }
    private fun drawChargeReminder(canvas: Canvas, bounds: Rect) {
        val customTextPaint = Paint(mainTextPaint)
        customTextPaint.textSize = 25f
        customTextPaint.color = Color.parseColor("#ffc269")
        val chargeReminderText = "Please Charge Me Before Bed"
        val chargeReminderX = bounds.exactCenterX()
        val chargeReminderY = bounds.exactCenterY()
        canvas.drawText(chargeReminderText, chargeReminderX, chargeReminderY, customTextPaint)

        val timeTextSize = 20f // Adjust the size as needed
        drawTime(canvas, bounds.exactCenterX(), bounds.exactCenterY()+timeTextSize*2)
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
        val batteryY = bounds.exactCenterY() + (iconHeight * 1.5)
        canvas.drawText(
            batteryCount, batteryX.toFloat(),
            batteryY.toFloat(), supportingTextPaint
        )

        val circleRadius = (horizontalLength / (iconSize * 4))
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

        val iconLeft = bounds.centerX() - iconWidth / 2 - iconWidth
        val iconTop = bounds.centerY() - (iconHeight * 1.5)

        icon1.setBounds(
            iconLeft.toInt(), iconTop.toInt(),
            (iconLeft + iconWidth).toInt(), (iconTop + iconHeight).toInt()
        )

        b1Button.setButtonPosition(
            icon1.bounds.left.toFloat(),
            icon1.bounds.top.toFloat(), icon1.bounds.right.toFloat(),
            icon1.bounds.bottom.toFloat()
        )

        // Set the circular background icon to launch the preferred input screen for mood


        b1Button.draw(canvas)
        icon1.draw(canvas)

        // Draw the mood text

        val moodTextX = iconLeft + (iconWidth / 2)
        val moodTextY = iconTop + iconHeight + (supportingTextPaint.textSize)

        val button1TextColorHex = "#FFFFFF" // Example hexadecimal color value (red)
        val button1TextColor = Color.parseColor(button1TextColorHex)
        supportingTextPaint.color = button1TextColor

        canvas.drawText(
            b1Text,
            moodTextX.toFloat(), moodTextY.toFloat(), supportingTextPaint
        )
        supportingTextPaint.color = Color.WHITE
    }

    // Helper function to draw elements related to mood
    private fun drawButton2Elements(canvas: Canvas, bounds: Rect, b2Button: MyCustomView) {
        // Draw the intensity icon mood and it's clickable circular background
        val iconWidth = (horizontalLength / iconSize)
        val iconHeight = (verticalLength / iconSize)
        val iconLeft = bounds.centerX() - iconWidth / 2 + iconWidth
        val iconTop = bounds.centerY() - (iconHeight * 1.5)
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

        b2Button.draw(canvas)
        icon2.draw(canvas)

        // Draw the mood text

        val intensityTextX = iconLeft + (iconWidth / 2)
        val intensityTextY = iconTop + iconHeight + (supportingTextPaint.textSize)
        val button2TextColorHex = "#FFFFFF" // Example hexadecimal color value (red)
        val button2TextColor = Color.parseColor(button2TextColorHex)
        supportingTextPaint.color = button2TextColor

        canvas.drawText(
            b2Text,
            intensityTextX.toFloat(), intensityTextY.toFloat(), supportingTextPaint
        )
        supportingTextPaint.color = Color.WHITE

    }

    // Helper function to draw elements related to sleep
    private fun drawButton3Elements(canvas: Canvas, bounds: Rect, b3Button: MyCustomView) {
        // Draw the sleep icon and it's clickable circular background
        val iconWidth = (horizontalLength / iconSize)
        val iconHeight = (verticalLength / iconSize)

        val iconLeft = bounds.centerX() - (iconWidth / 2) - iconWidth
        val iconTop = bounds.centerY() + (iconHeight * 1.5) - iconHeight
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




        b3Button.draw(canvas)
        icon3.draw(canvas)

        // Draw the sleep text

        val sleepTextX = iconLeft + (iconWidth / 2)
        val sleepTextY = iconTop - (supportingTextPaint.textSize / 4)
        val button3TextColorHex = "#FFFFFF" // Example hexadecimal color value (red)
        val button3TextColor = Color.parseColor(button3TextColorHex)
        supportingTextPaint.color = button3TextColor

        canvas.drawText(
            b3Text,
            sleepTextX.toFloat(), sleepTextY.toFloat(), supportingTextPaint
        )
        supportingTextPaint.color = Color.WHITE
    }

    // Helper function to draw elements related to food count
    private fun drawButton4Elements(canvas: Canvas, bounds: Rect, b4Button: MyCustomView) {


        // Draw the food count icon
        val iconWidth = (horizontalLength / iconSize)
        val iconHeight = (verticalLength / iconSize)
        val iconLeft = bounds.centerX() - (iconWidth / 2) + iconWidth
        val iconTop = bounds.centerY() + (iconHeight * 1.5) - iconHeight

        icon4.setBounds(
            iconLeft.toInt(), iconTop.toInt(),
            (iconLeft + iconWidth).toInt(), (iconTop + iconHeight).toInt()
        )
        b4Button.setButtonPosition(
            icon4.bounds.left.toFloat(),
            icon4.bounds.top.toFloat(), icon4.bounds.right.toFloat(),
            icon4.bounds.bottom.toFloat()
        )

        b4Button.draw(canvas)
        icon4.draw(canvas)

        // Draw the food count text

        val foodCountTextX = iconLeft + (iconWidth / 2)
        val foodCountTextY = iconTop - (supportingTextPaint.textSize / 4)
        val button4TextColorHex = "#FFFFFF" // Example hexadecimal color value (red)
        val button4TextColor = Color.parseColor(button4TextColorHex)
        supportingTextPaint.color = button4TextColor

        canvas.drawText(
            b4Text,
            foodCountTextX.toFloat(), foodCountTextY.toFloat(), supportingTextPaint
        )
        supportingTextPaint.color = Color.WHITE

    }



}