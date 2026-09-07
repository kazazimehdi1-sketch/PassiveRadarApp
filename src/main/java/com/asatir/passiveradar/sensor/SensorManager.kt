package com.asatir.passiveradar.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.asatir.passiveradar.data.model.SensorData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SensorManager(context: Context) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    
    private val _sensorData = MutableStateFlow(SensorData())
    val sensorData: StateFlow<SensorData> = _sensorData

    private var accelerometer: Sensor? = null
    private var magnetometer: Sensor? = null
    private var thermometer: Sensor? = null
    private var barometer: Sensor? = null
    private var humidityS: Sensor? = null

    init {
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        thermometer = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)
        barometer = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)
        humidityS = sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY)
    }

    fun startListening() {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        magnetometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        thermometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        barometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        humidityS?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    fun stopListening() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        val currentData = _sensorData.value
        
        val updatedData = when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                currentData.copy(
                    accelerometerX = event.values[0],
                    accelerometerY = event.values[1],
                    accelerometerZ = event.values[2]
                )
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                currentData.copy(
                    magneticX = event.values[0],
                    magneticY = event.values[1],
                    magneticZ = event.values[2]
                )
            }
            Sensor.TYPE_AMBIENT_TEMPERATURE -> {
                currentData.copy(temperature = event.values[0])
            }
            Sensor.TYPE_PRESSURE -> {
                currentData.copy(pressure = event.values[0])
            }
            Sensor.TYPE_RELATIVE_HUMIDITY -> {
                currentData.copy(humidity = event.values[0])
            }
            else -> currentData
        }
        
        _sensorData.value = updatedData
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
