package com.example.monitorwatchface

import android.app.Service
import android.content.Context
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.work.Worker
import androidx.work.WorkerParameters

class StepsWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    var sharedPreferences: SharedPreferences = applicationContext.getSharedPreferences("preferences", Context.MODE_PRIVATE)
    private val sensorManager = context.getSystemService(Service.SENSOR_SERVICE) as SensorManager
    private var pedometer: Sensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    private val sensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            if (event.sensor.type == Sensor.TYPE_STEP_COUNTER) {
                val steps = event.values[0].toInt()
                sharedPreferences.edit()?.putInt("steps", steps)?.apply()
                sensorManager.unregisterListener(this)
            }
        }
        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        }
    }

    override fun doWork(): Result {
        sensorManager.registerListener(sensorListener, pedometer, SensorManager.SENSOR_DELAY_NORMAL)
        return Result.success()
    }
}