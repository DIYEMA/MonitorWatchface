package com.example.monitorwatchface

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.net.Uri
import android.support.wearable.watchface.CanvasWatchFaceService
import android.util.Log
import android.view.*
import android.widget.*
import com.google.android.gms.wearable.*
import java.lang.Float.min
import java.text.DateFormat
import java.text.DateFormat.getTimeInstance
import java.util.*

class MyWatchFaceService : CanvasWatchFaceService() {

    private lateinit var mainTextPaint: Paint
    private lateinit var supportingTextPaint: Paint
    private lateinit var moodForegroundPaint: Paint
    private lateinit var moodBackgroundPaint: Paint
    private lateinit var fatigueForegroundPaint: Paint
    private lateinit var fatigueBackgroundPaint: Paint
    private val backgroundColor = Color.parseColor("#3E3939")
    private var currentTime = Calendar.getInstance()
    private var fatigue = 4
    private var mood = 3
    private var hr = 0

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

        moodForegroundPaint = Paint().apply {
            color = Color.WHITE
            strokeWidth = resources.getDimension(R.dimen.progress_width)
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            isAntiAlias = true
        }

        moodBackgroundPaint = Paint().apply {
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

        fatigueBackgroundPaint = Paint().apply {
            color = Color.WHITE
            strokeWidth = resources.getDimension(R.dimen.progress_width)
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            alpha = 128
            isAntiAlias = true
        }

        return MyEngine()
    }

    private inner class MyEngine : Engine() {

        private val stepsIcon: Drawable = applicationContext.getDrawable(R.drawable.steps_icon)!!
        private val heartIcon: Drawable = applicationContext.getDrawable(R.drawable.heart_icon)!!
        private var fatigueIcon: Drawable = applicationContext.getDrawable(R.drawable.default_icon)!!
        private var moodIcon: Drawable = applicationContext.getDrawable(R.drawable.default_icon)!!

        private lateinit var fatigueView: MyCustomView
        private lateinit var moodView: MyCustomView

        override fun onCreate(surfaceHolder: SurfaceHolder) {
            super.onCreate(surfaceHolder)
            fatigueView = MyCustomView(this@MyWatchFaceService)
            moodView = MyCustomView(this@MyWatchFaceService)
            setTouchEventsEnabled(true)
        }

        override fun onTouchEvent(event: MotionEvent) {
            super.onTouchEvent(event)
            fatigueView.onTouchEvent(event)
            moodView.onTouchEvent(event)
        }

        @SuppressLint("Range")
        override fun onDraw(canvas: Canvas, bounds: Rect) {
            super.onDraw(canvas, bounds)

            val uri = Uri.parse("content://com.example.fatiguemonitor.provider/preferences")
            val cursor = contentResolver.query(uri, null, null, null, null)
            if (cursor != null && cursor.moveToFirst()) {
                fatigue = cursor.getInt(cursor.getColumnIndex("value"))
                if (cursor.moveToNext()) {
                    mood = cursor.getInt(cursor.getColumnIndex("value"))
                }
                if (cursor.moveToNext()) {
                    hr = cursor.getInt(cursor.getColumnIndex("value"))
                }
            }
            cursor?.close()

            dynamicChange(applicationContext)

            // Draw the background color
            canvas.drawColor(backgroundColor)

            // Get the formatted time string
            val timeFormat = getTimeInstance(DateFormat.SHORT)
            val timeString = timeFormat.format(currentTime.time)

            // Split the time string into hours and minutes, and AM/PM
            val hourMinute = timeString.substringBefore(" ")
            val amPm = timeString.substringAfter(" ").uppercase()

            // Calculate the y position of the AM/PM text
            val amPmY = bounds.exactCenterY() + mainTextPaint.descent() + resources.getDimension(R.dimen.text_padding)

            // Draw the hours and minutes text
            canvas.drawText(hourMinute, bounds.exactCenterX(), bounds.exactCenterY(), mainTextPaint)

            // Draw the AM/PM text
            canvas.drawText(amPm, bounds.exactCenterX(), amPmY, mainTextPaint)

            // Draw the steps count icon
            val stepsIconWidth = 60f
            val stepsIconHeight = 60f
            val stepsIconLeft = bounds.exactCenterX() - resources.getDimension(R.dimen.icon_margin) - stepsIconWidth
            val stepsIconTop = bounds.exactCenterY() - (stepsIconHeight / 2)
            stepsIcon.setBounds(stepsIconLeft.toInt(), stepsIconTop.toInt(),
                (stepsIconLeft + stepsIconWidth).toInt(), (stepsIconTop + stepsIconHeight).toInt()
            )
            stepsIcon.draw(canvas)

            // Draw the steps count text
            val stepsCountText = "Steps"
            val stepsCountTextX = bounds.exactCenterX() - resources.getDimension(R.dimen.icon_margin) - (stepsIconWidth / 2)
            val stepsCountTextY = stepsIconTop - (supportingTextPaint.textSize / 4)
            canvas.drawText(stepsCountText, stepsCountTextX, stepsCountTextY, supportingTextPaint)

            // Draw the steps count
            val stepsCount = "0"
            val stepsCountX = bounds.exactCenterX() - resources.getDimension(R.dimen.icon_margin) - (stepsIconWidth / 2)
            val stepsCountY = stepsIconTop + stepsIconHeight + supportingTextPaint.textSize
            canvas.drawText(stepsCount, stepsCountX, stepsCountY, supportingTextPaint)

            // Draw the heart rate icon
            val heartIconWidth = 60f
            val heartIconHeight = 60f
            val heartIconLeft = bounds.exactCenterX() + resources.getDimension(R.dimen.icon_margin)
            val heartIconTop = bounds.exactCenterY() - (heartIconHeight / 2)
            heartIcon.setBounds(heartIconLeft.toInt(), heartIconTop.toInt(),
                (heartIconLeft + heartIconWidth).toInt(), (heartIconTop + heartIconHeight).toInt()
            )
            heartIcon.draw(canvas)

            // Draw the heart rate text
            val heartRateText = "HR"
            val heartRateTextX = bounds.exactCenterX() + resources.getDimension(R.dimen.icon_margin) + (heartIconWidth / 2)
            val heartRateTextY = heartIconTop - (supportingTextPaint.textSize / 4)
            canvas.drawText(heartRateText, heartRateTextX, heartRateTextY, supportingTextPaint)

            // Draw the heart rate count
            val heartRateCount = "$hr BPM"
            val heartRateCountX = bounds.exactCenterX() + resources.getDimension(R.dimen.icon_margin) + (heartIconWidth / 2)
            val heartRateCountY = heartIconTop + heartIconHeight + supportingTextPaint.textSize
            canvas.drawText(heartRateCount, heartRateCountX, heartRateCountY, supportingTextPaint)

            // Draw the fatigue icon and it's clickable background
            fatigueView.draw(canvas)

            val fatigueIconWidth = 50f
            val fatigueIconHeight = 70f
            val fatigueIconLeft = bounds.exactCenterX() - (fatigueIconWidth / 2)
            val fatigueIconTop = bounds.exactCenterY() - (resources.getDimension(R.dimen.icon_margin) / 1.1) - fatigueIconHeight
            fatigueIcon.setBounds(fatigueIconLeft.toInt(), fatigueIconTop.toInt(),
                (fatigueIconLeft + fatigueIconWidth).toInt(), (fatigueIconTop + fatigueIconHeight).toInt())
            fatigueIcon.draw(canvas)


            // Draw the fatigue text
            val fatigueText = "Fatigue"
            val fatigueTextX = bounds.exactCenterX()
            val fatigueTextY = fatigueIconTop - (supportingTextPaint.textSize / 4)
            canvas.drawText(fatigueText, fatigueTextX, fatigueTextY.toFloat(), supportingTextPaint)

            // Draw the mood icon and it's clickable background (with new position and activity)
            moodView.setIconPosition(140f, 235f, 220f, 335f)
            moodView.setActivity("com.example.fatiguemonitor.presentation.MoodSeekBarActivity")
            moodView.draw(canvas)

            val moodIconWidth = 68f
            val moodIconHeight = 85f
            val moodIconLeft = bounds.exactCenterX() - (moodIconWidth / 2)
            val moodIconTop = bounds.exactCenterY() + (resources.getDimension(R.dimen.icon_margin) / 1.1)
            moodIcon.setBounds(moodIconLeft.toInt(), moodIconTop.toInt(),
                (moodIconLeft + moodIconWidth).toInt(), (moodIconTop + moodIconHeight).toInt()
            )
            moodIcon.draw(canvas)

            // Draw the mood text
            val moodText = "Mood"
            val moodTextX = bounds.exactCenterX()
            val moodTextY = moodIconTop + moodIconHeight + (supportingTextPaint.textSize / 4)
            canvas.drawText(moodText, moodTextX, moodTextY.toFloat(), supportingTextPaint)

            // Draw the progress ring
            val centerX = bounds.exactCenterX()
            val centerY = bounds.exactCenterY()
            val radius = min(centerX, centerY) - resources.getDimension(R.dimen.progress_margin)
            val rectF = RectF(centerX - radius, centerY - radius, centerX + radius, centerY + radius)
            canvas.drawArc(rectF, 225f, (18 * (fatigue + 1)).toFloat(), false, fatigueForegroundPaint)
            canvas.drawArc(rectF, 225f, 90f, false, fatigueBackgroundPaint)
            canvas.drawArc(rectF, 45f, (18 * (mood + 1)).toFloat(), false, moodForegroundPaint)
            canvas.drawArc(rectF, 45f, 90f, false, moodBackgroundPaint)
        }

        override fun onTimeTick() {
            super.onTimeTick()
            currentTime = Calendar.getInstance()
            invalidate()
        }

        override fun onAmbientModeChanged(inAmbientMode: Boolean) {
            if (inAmbientMode) {
                mainTextPaint.color = Color.GRAY
//                progressPaint.color = Color.WHITE
            } else {
                mainTextPaint.color = Color.WHITE
//                progressPaint.color = Color.BLUE
            }
            invalidate()
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            if (visible) {
                // Register sensors
            } else {
                // Unregister sensors
            }
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)
            // Adjust the watch face layout here if necessary
        }

        override fun onApplyWindowInsets(insets: WindowInsets) {
            super.onApplyWindowInsets(insets)
            // Adjust the watch face layout based on the given insets here
        }

        private fun dynamicChange(context: Context) {
            data class State(val icon: Int, val colour: String)

            val moodStates = listOf(
                State(R.drawable.very_sad, "#EE2C37"),
                State(R.drawable.sad, "#F36831"),
                State(R.drawable.moderate, "#FCD90E"),
                State(R.drawable.happy, "#9CCC3C"),
                State(R.drawable.very_happy, "#46B648")
            )

            val fatigueStates = listOf(
                State(R.drawable.very_tired2, "#EE2C37"),
                State(R.drawable.tired2, "#F36831"),
                State(R.drawable.moderately_tired2, "#FCD90E"),
                State(R.drawable.energetic2, "#9CCC3C"),
                State(R.drawable.very_energetic2, "#46B648")
            )

            val moodState = moodStates.getOrElse(mood) { moodStates.last() }
            val fatigueState = fatigueStates.getOrElse(fatigue) { fatigueStates.last() }

            moodIcon = context.getDrawable(moodState.icon)!!
            moodForegroundPaint.color = Color.parseColor(moodState.colour)
            moodBackgroundPaint.color = Color.parseColor(moodState.colour)
            moodBackgroundPaint.alpha = 128

            fatigueIcon = context.getDrawable(fatigueState.icon)!!
            fatigueForegroundPaint.color = Color.parseColor(fatigueState.colour)
            fatigueBackgroundPaint.color = Color.parseColor(fatigueState.colour)
            fatigueBackgroundPaint.alpha = 128
        }
    }
}

//class MyWatchFaceService : CanvasWatchFaceService() {
//
//    override fun onCreateEngine(): Engine {
//        val watchViewStub = WatchViewStub(this)
//        val layoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
//        val layout = layoutInflater.inflate(R.layout.watchface, watchViewStub, false)
//        watchViewStub.addView(layout)
//        return MyWatchFaceEngine(watchViewStub)
//    }
//
//    inner class MyWatchFaceEngine(watchViewStub: WatchViewStub) : CanvasWatchFaceService.Engine() {
//
//        private val paint = Paint().apply {
//            color = Color.WHITE
//            textAlign = Paint.Align.CENTER
//            textSize = resources.getDimension(R.dimen.text_size)
//        }
//
//        override fun onDraw(canvas: Canvas, bounds: Rect) {
//            val text: TextView = findViewById(R.id.time_text)
//            canvas.drawText(text, bounds.centerX().toFloat(), bounds.centerY().toFloat(), paint)
//        }
//
//        override fun onTimeTick() {
//            invalidate()
//        }
//
//        override fun onAmbientModeChanged(inAmbientMode: Boolean) {
//            if (inAmbientMode) {
//                paint.color = Color.GRAY
//            } else {
//                paint.color = Color.WHITE
//            }
//            invalidate()
//        }
//
//        override fun onVisibilityChanged(visible: Boolean) {
//            super.onVisibilityChanged(visible)
//            if (visible) {
//                // Register any sensors or receivers here
//            } else {
//                // Unregister any sensors or receivers here
//            }
//        }
//
//        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
//            super.onSurfaceChanged(holder, format, width, height)
//            // Adjust the watch face layout here if necessary
//        }
//
//        override fun onApplyWindowInsets(insets: WindowInsets) {
//            super.onApplyWindowInsets(insets)
//            // Adjust the watch face layout based on the given insets here
//        }
//    }
//}

//    inner class MyWatchFaceEngine : CanvasWatchFaceService.Engine() {
//
//        private lateinit var background: ImageView
//        private lateinit var timeText: TextView
//        private lateinit var calendar: Calendar
//
//        override fun onCreate(surfaceHolder: SurfaceHolder) {
//            super.onCreate(surfaceHolder)
//
////            // Find the views in the layout
////            background = watchFaceLayout.findViewById(R.id.background_image)
////            timeText = watchFaceLayout.findViewById(R.id.time_text)
////
////            // Add the layout to the watch face surface
////            setWatchFaceStyle(WatchFaceStyle.Builder(this@MyWatchFaceService)
////                .setAcceptsTapEvents(true)
////                .build())
////            set(watchFaceLayout)
//        }
//
//        override fun onSurfaceChanged(
//            holder: SurfaceHolder,
//            format: Int,
//            width: Int,
//            height: Int
//        ) {
//            super.onSurfaceChanged(holder, format, width, height)
//
//            // Set the size of the watch face layout to the surface size
//            val layoutParams = FrameLayout.LayoutParams(width, height)
//            contentView.layoutParams = layoutParams
//        }
//
//        override fun onDraw(canvas: Canvas, bounds: Rect) {
//            super.onDraw(canvas, bounds)
//
//            // Draw the background image
//            background.draw(canvas)
//
//            // Update the time text
//            calendar = Calendar.getInstance()
//            val hours = calendar.get(Calendar.HOUR_OF_DAY)
//            val minutes = calendar.get(Calendar.MINUTE)
//            val timeString = String.format("%02d:%02d", hours, minutes)
//            timeText.text = timeString
//
//            // Draw the time text
//            timeText.draw(canvas)
//        }
//    }
//}

//    override fun onCreateEngine(): Engine {
//        return Engine()
//    }

//    private class EngineHandler(reference: CustomWatchFace.Engine) : Handler(Looper.myLooper()!!) {
//        private val mWeakReference: WeakReference<CustomWatchFace.Engine> = WeakReference(reference)
//
//        override fun handleMessage(msg: Message) {
//            val engine = mWeakReference.get()
//            if (engine != null) {
//                when (msg.what) {
//                    MSG_UPDATE_TIME -> engine.handleUpdateTimeMessage()
//                }
//            }
//        }
//    }
//
//        private lateinit var mCalendar: Calendar
//
//        private var mRegisteredTimeZoneReceiver = false
//        private var mMuteMode: Boolean = false
//        private var mCenterX: Float = 0F
//        private var mCenterY: Float = 0F
//
//        private var mSecondHandLength: Float = 0F
//        private var sMinuteHandLength: Float = 0F
//        private var sHourHandLength: Float = 0F
//
//        /* Colors for all hands (hour, minute, seconds, ticks) based on photo loaded. */
//        private var mWatchHandColor: Int = 0
//        private var mWatchHandHighlightColor: Int = 0
//        private var mWatchHandShadowColor: Int = 0
//
//        private lateinit var mHourPaint: Paint
//        private lateinit var mMinutePaint: Paint
//        private lateinit var mSecondPaint: Paint
//        private lateinit var mTickAndCirclePaint: Paint
//
//        private lateinit var mBackgroundPaint: Paint
//        private lateinit var mBackgroundBitmap: Bitmap
//        private lateinit var mGrayBackgroundBitmap: Bitmap
//
//        private var mAmbient: Boolean = false
//        private var mLowBitAmbient: Boolean = false
//        private var mBurnInProtection: Boolean = false
//
//        /* Handler to update the time once a second in interactive mode. */
//        private val mUpdateTimeHandler = EngineHandler(this)
//
//        private val mTimeZoneReceiver = object : BroadcastReceiver() {
//            override fun onReceive(context: Context, intent: Intent) {
//                mCalendar.timeZone = TimeZone.getDefault()
//                invalidate()
//            }
//        }
//
//        override fun onCreate(holder: SurfaceHolder) {
//            super.onCreate(holder)
//
//            setWatchFaceStyle(
//                WatchFaceStyle.Builder(this@CustomWatchFace)
//                    .setAcceptsTapEvents(true)
//                    .build()
//            )
//
//            mCalendar = Calendar.getInstance()
//
//            initializeBackground()
//            initializeWatchFace()
//        }
//
//        private fun initializeBackground() {
//            mBackgroundPaint = Paint().apply {
//                color = Color.BLACK
//            }
//            mBackgroundBitmap =
//                BitmapFactory.decodeResource(resources, R.drawable.watchface_service_bg)
//
//            /* Extracts colors from background image to improve watchface style. */
//            Palette.from(mBackgroundBitmap).generate {
//                it?.let {
//                    mWatchHandHighlightColor = it.getVibrantColor(Color.RED)
//                    mWatchHandColor = it.getLightVibrantColor(Color.WHITE)
//                    mWatchHandShadowColor = it.getDarkMutedColor(Color.BLACK)
//                    updateWatchHandStyle()
//                }
//            }
//        }
//
//        private fun initializeWatchFace() {
//            /* Set defaults for colors */
//            mWatchHandColor = Color.WHITE
//            mWatchHandHighlightColor = Color.RED
//            mWatchHandShadowColor = Color.BLACK
//
//            mHourPaint = Paint().apply {
//                color = mWatchHandColor
//                strokeWidth = HOUR_STROKE_WIDTH
//                isAntiAlias = true
//                strokeCap = Paint.Cap.ROUND
//                setShadowLayer(
//                    SHADOW_RADIUS, 0f, 0f, mWatchHandShadowColor
//                )
//            }
//
//            mMinutePaint = Paint().apply {
//                color = mWatchHandColor
//                strokeWidth = MINUTE_STROKE_WIDTH
//                isAntiAlias = true
//                strokeCap = Paint.Cap.ROUND
//                setShadowLayer(
//                    SHADOW_RADIUS, 0f, 0f, mWatchHandShadowColor
//                )
//            }
//
//            mSecondPaint = Paint().apply {
//                color = mWatchHandHighlightColor
//                strokeWidth = SECOND_TICK_STROKE_WIDTH
//                isAntiAlias = true
//                strokeCap = Paint.Cap.ROUND
//                setShadowLayer(
//                    SHADOW_RADIUS, 0f, 0f, mWatchHandShadowColor
//                )
//            }
//
//            mTickAndCirclePaint = Paint().apply {
//                color = mWatchHandColor
//                strokeWidth = SECOND_TICK_STROKE_WIDTH
//                isAntiAlias = true
//                style = Paint.Style.STROKE
//                setShadowLayer(
//                    SHADOW_RADIUS, 0f, 0f, mWatchHandShadowColor
//                )
//            }
//        }
//
//        override fun onDestroy() {
//            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME)
//            super.onDestroy()
//        }
//
//        override fun onPropertiesChanged(properties: Bundle) {
//            super.onPropertiesChanged(properties)
//            mLowBitAmbient = properties.getBoolean(
//                WatchFaceService.PROPERTY_LOW_BIT_AMBIENT, false
//            )
//            mBurnInProtection = properties.getBoolean(
//                WatchFaceService.PROPERTY_BURN_IN_PROTECTION, false
//            )
//        }
//
//        override fun onTimeTick() {
//            super.onTimeTick()
//            invalidate()
//        }
//
//        override fun onAmbientModeChanged(inAmbientMode: Boolean) {
//            super.onAmbientModeChanged(inAmbientMode)
//            mAmbient = inAmbientMode
//
//            updateWatchHandStyle()
//
//            // Check and trigger whether or not timer should be running (only
//            // in active mode).
//            updateTimer()
//        }
//
//        private fun updateWatchHandStyle() {
//            if (mAmbient) {
//                mHourPaint.color = Color.WHITE
//                mMinutePaint.color = Color.WHITE
//                mSecondPaint.color = Color.WHITE
//                mTickAndCirclePaint.color = Color.WHITE
//
//                mHourPaint.isAntiAlias = false
//                mMinutePaint.isAntiAlias = false
//                mSecondPaint.isAntiAlias = false
//                mTickAndCirclePaint.isAntiAlias = false
//
//                mHourPaint.clearShadowLayer()
//                mMinutePaint.clearShadowLayer()
//                mSecondPaint.clearShadowLayer()
//                mTickAndCirclePaint.clearShadowLayer()
//
//            } else {
//                mHourPaint.color = mWatchHandColor
//                mMinutePaint.color = mWatchHandColor
//                mSecondPaint.color = mWatchHandHighlightColor
//                mTickAndCirclePaint.color = mWatchHandColor
//
//                mHourPaint.isAntiAlias = true
//                mMinutePaint.isAntiAlias = true
//                mSecondPaint.isAntiAlias = true
//                mTickAndCirclePaint.isAntiAlias = true
//
//                mHourPaint.setShadowLayer(
//                    SHADOW_RADIUS, 0f, 0f, mWatchHandShadowColor
//                )
//                mMinutePaint.setShadowLayer(
//                    SHADOW_RADIUS, 0f, 0f, mWatchHandShadowColor
//                )
//                mSecondPaint.setShadowLayer(
//                    SHADOW_RADIUS, 0f, 0f, mWatchHandShadowColor
//                )
//                mTickAndCirclePaint.setShadowLayer(
//                    SHADOW_RADIUS, 0f, 0f, mWatchHandShadowColor
//                )
//            }
//        }
//
//        override fun onInterruptionFilterChanged(interruptionFilter: Int) {
//            super.onInterruptionFilterChanged(interruptionFilter)
//            val inMuteMode = interruptionFilter == WatchFaceService.INTERRUPTION_FILTER_NONE
//
//            /* Dim display in mute mode. */
//            if (mMuteMode != inMuteMode) {
//                mMuteMode = inMuteMode
//                mHourPaint.alpha = if (inMuteMode) 100 else 255
//                mMinutePaint.alpha = if (inMuteMode) 100 else 255
//                mSecondPaint.alpha = if (inMuteMode) 80 else 255
//                invalidate()
//            }
//        }
//
//        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
//            super.onSurfaceChanged(holder, format, width, height)
//
//            /*
//             * Find the coordinates of the center point on the screen, and ignore the window
//             * insets, so that, on round watches with a "chin", the watch face is centered on the
//             * entire screen, not just the usable portion.
//             */
//            mCenterX = width / 2f
//            mCenterY = height / 2f
//
//            /*
//             * Calculate lengths of different hands based on watch screen size.
//             */
//            mSecondHandLength = (mCenterX * 0.875).toFloat()
//            sMinuteHandLength = (mCenterX * 0.75).toFloat()
//            sHourHandLength = (mCenterX * 0.5).toFloat()
//
//            /* Scale loaded background image (more efficient) if surface dimensions change. */
//            val scale = width.toFloat() / mBackgroundBitmap.width.toFloat()
//
//            mBackgroundBitmap = Bitmap.createScaledBitmap(
//                mBackgroundBitmap,
//                (mBackgroundBitmap.width * scale).toInt(),
//                (mBackgroundBitmap.height * scale).toInt(), true
//            )
//
//            /*
//             * Create a gray version of the image only if it will look nice on the device in
//             * ambient mode. That means we don"t want devices that support burn-in
//             * protection (slight movements in pixels, not great for images going all the way to
//             * edges) and low ambient mode (degrades image quality).
//             *
//             * Also, if your watch face will know about all images ahead of time (users aren"t
//             * selecting their own photos for the watch face), it will be more
//             * efficient to create a black/white version (png, etc.) and load that when you need it.
//             */
//            if (!mBurnInProtection && !mLowBitAmbient) {
//                initGrayBackgroundBitmap()
//            }
//        }
//
//        private fun initGrayBackgroundBitmap() {
//            mGrayBackgroundBitmap = Bitmap.createBitmap(
//                mBackgroundBitmap.width,
//                mBackgroundBitmap.height,
//                Bitmap.Config.ARGB_8888
//            )
//            val canvas = Canvas(mGrayBackgroundBitmap)
//            val grayPaint = Paint()
//            val colorMatrix = ColorMatrix()
//            colorMatrix.setSaturation(0f)
//            val filter = ColorMatrixColorFilter(colorMatrix)
//            grayPaint.colorFilter = filter
//            canvas.drawBitmap(mBackgroundBitmap, 0f, 0f, grayPaint)
//        }
//
//        /**
//         * Captures tap event (and tap type). The [WatchFaceService.TAP_TYPE_TAP] case can be
//         * used for implementing specific logic to handle the gesture.
//         */
//        override fun onTapCommand(tapType: Int, x: Int, y: Int, eventTime: Long) {
//            when (tapType) {
//                WatchFaceService.TAP_TYPE_TOUCH -> {
//                    // The user has started touching the screen.
//                }
//                WatchFaceService.TAP_TYPE_TOUCH_CANCEL -> {
//                    // The user has started a different gesture or otherwise cancelled the tap.
//                }
//                WatchFaceService.TAP_TYPE_TAP ->
//                    // The user has completed the tap gesture.
//                    // TODO: Add code to handle the tap gesture.
//                    Toast.makeText(applicationContext, R.string.message, Toast.LENGTH_SHORT)
//                        .show()
//            }
//            invalidate()
//        }
//
//        override fun onDraw(canvas: Canvas, bounds: Rect) {
//            val now = System.currentTimeMillis()
//            mCalendar.timeInMillis = now
//
//            drawBackground(canvas)
//            drawWatchFace(canvas)
//        }
//
//        private fun drawBackground(canvas: Canvas) {
//
//            if (mAmbient && (mLowBitAmbient || mBurnInProtection)) {
//                canvas.drawColor(Color.BLACK)
//            } else if (mAmbient) {
//                canvas.drawBitmap(mGrayBackgroundBitmap, 0f, 0f, mBackgroundPaint)
//            } else {
//                canvas.drawBitmap(mBackgroundBitmap, 0f, 0f, mBackgroundPaint)
//            }
//        }
//
//        private fun drawWatchFace(canvas: Canvas) {
//
//            /*
//             * Draw ticks. Usually you will want to bake this directly into the photo, but in
//             * cases where you want to allow users to select their own photos, this dynamically
//             * creates them on top of the photo.
//             */
//            val innerTickRadius = mCenterX - 10
//            val outerTickRadius = mCenterX
//            for (tickIndex in 0..11) {
//                val tickRot = (tickIndex.toDouble() * Math.PI * 2.0 / 12).toFloat()
//                val innerX = Math.sin(tickRot.toDouble()).toFloat() * innerTickRadius
//                val innerY = (-Math.cos(tickRot.toDouble())).toFloat() * innerTickRadius
//                val outerX = Math.sin(tickRot.toDouble()).toFloat() * outerTickRadius
//                val outerY = (-Math.cos(tickRot.toDouble())).toFloat() * outerTickRadius
//                canvas.drawLine(
//                    mCenterX + innerX, mCenterY + innerY,
//                    mCenterX + outerX, mCenterY + outerY, mTickAndCirclePaint
//                )
//            }
//
//            /*
//             * These calculations reflect the rotation in degrees per unit of time, e.g.,
//             * 360 / 60 = 6 and 360 / 12 = 30.
//             */
//            val seconds =
//                mCalendar.get(Calendar.SECOND) + mCalendar.get(Calendar.MILLISECOND) / 1000f
//            val secondsRotation = seconds * 6f
//
//            val minutesRotation = mCalendar.get(Calendar.MINUTE) * 6f
//
//            val hourHandOffset = mCalendar.get(Calendar.MINUTE) / 2f
//            val hoursRotation = mCalendar.get(Calendar.HOUR) * 30 + hourHandOffset
//
//            /*
//             * Save the canvas state before we can begin to rotate it.
//             */
//            canvas.save()
//
//            canvas.rotate(hoursRotation, mCenterX, mCenterY)
//            canvas.drawLine(
//                mCenterX,
//                mCenterY - CENTER_GAP_AND_CIRCLE_RADIUS,
//                mCenterX,
//                mCenterY - sHourHandLength,
//                mHourPaint
//            )
//
//            canvas.rotate(minutesRotation - hoursRotation, mCenterX, mCenterY)
//            canvas.drawLine(
//                mCenterX,
//                mCenterY - CENTER_GAP_AND_CIRCLE_RADIUS,
//                mCenterX,
//                mCenterY - sMinuteHandLength,
//                mMinutePaint
//            )
//
//            /*
//             * Ensure the "seconds" hand is drawn only when we are in interactive mode.
//             * Otherwise, we only update the watch face once a minute.
//             */
//            if (!mAmbient) {
//                canvas.rotate(secondsRotation - minutesRotation, mCenterX, mCenterY)
//                canvas.drawLine(
//                    mCenterX,
//                    mCenterY - CENTER_GAP_AND_CIRCLE_RADIUS,
//                    mCenterX,
//                    mCenterY - mSecondHandLength,
//                    mSecondPaint
//                )
//
//            }
//            canvas.drawCircle(
//                mCenterX,
//                mCenterY,
//                CENTER_GAP_AND_CIRCLE_RADIUS,
//                mTickAndCirclePaint
//            )
//
//            /* Restore the canvas" original orientation. */
//            canvas.restore()
//        }
//
//        override fun onVisibilityChanged(visible: Boolean) {
//            super.onVisibilityChanged(visible)
//
//            if (visible) {
//                registerReceiver()
//                /* Update time zone in case it changed while we weren"t visible. */
//                mCalendar.timeZone = TimeZone.getDefault()
//                invalidate()
//            } else {
//                unregisterReceiver()
//            }
//
//            /* Check and trigger whether or not timer should be running (only in active mode). */
//            updateTimer()
//        }
//
//        private fun registerReceiver() {
//            if (mRegisteredTimeZoneReceiver) {
//                return
//            }
//            mRegisteredTimeZoneReceiver = true
//            val filter = IntentFilter(Intent.ACTION_TIMEZONE_CHANGED)
//            this@CustomWatchFace.registerReceiver(mTimeZoneReceiver, filter)
//        }
//
//        private fun unregisterReceiver() {
//            if (!mRegisteredTimeZoneReceiver) {
//                return
//            }
//            mRegisteredTimeZoneReceiver = false
//            this@CustomWatchFace.unregisterReceiver(mTimeZoneReceiver)
//        }
//
//        /**
//         * Starts/stops the [.mUpdateTimeHandler] timer based on the state of the watch face.
//         */
//        private fun updateTimer() {
//            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME)
//            if (shouldTimerBeRunning()) {
//                mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME)
//            }
//        }
//
//        /**
//         * Returns whether the [.mUpdateTimeHandler] timer should be running. The timer
//         * should only run in active mode.
//         */
//        private fun shouldTimerBeRunning(): Boolean {
//            return isVisible && !mAmbient
//        }
//
//        /**
//         * Handle updating the time periodically in interactive mode.
//         */
//        fun handleUpdateTimeMessage() {
//            invalidate()
//            if (shouldTimerBeRunning()) {
//                val timeMs = System.currentTimeMillis()
//                val delayMs = INTERACTIVE_UPDATE_RATE_MS - timeMs % INTERACTIVE_UPDATE_RATE_MS
//                mUpdateTimeHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs)
//            }
//        }
//    }
//}