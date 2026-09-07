package com.asatir.passiveradar

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.asatir.passiveradar.camera.CameraManager
import com.asatir.passiveradar.data.database.RadarDatabase
import com.asatir.passiveradar.data.model.DetectionRecord
import com.asatir.passiveradar.location.LocationManager
import com.asatir.passiveradar.radar.PassiveRadarEngine
import com.asatir.passiveradar.sensor.SensorManager
import com.asatir.passiveradar.ui.screen.RadarScreen
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : ComponentActivity() {
    private lateinit var radarEngine: PassiveRadarEngine
    private lateinit var sensorManager: SensorManager
    private lateinit var locationManager: LocationManager
    private lateinit var cameraManager: CameraManager
    private lateinit var database: RadarDatabase

    private val permissionsToRequest = mutableListOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.CAMERA
    )

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.all { it.value }
        if (allGranted) {
            initializeComponents()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // تنظیم‌های پایگاه داده
        database = RadarDatabase.getDatabase(this)

        // بررسی مجوزها
        checkAndRequestPermissions()

        setContent {
            PassiveRadarAppContent()
        }
    }

    private fun checkAndRequestPermissions() {
        val missingPermissions = permissionsToRequest.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            permissionLauncher.launch(missingPermissions.toTypedArray())
        } else {
            initializeComponents()
        }
    }

    private fun initializeComponents() {
        radarEngine = PassiveRadarEngine()
        sensorManager = SensorManager(this)
        locationManager = LocationManager(this)
        cameraManager = CameraManager(this)

        sensorManager.startListening()

        lifecycleScope.launch {
            locationManager.getLastLocation()
        }
    }

    @Composable
    fun PassiveRadarAppContent() {
        val radarResult by radarEngine.radarResult.collectAsState()
        val sensorData by sensorManager.sensorData.collectAsState()
        val currentLocation by locationManager.currentLocation.collectAsState()

        RadarScreen(
            radarResult = radarResult,
            sensorData = sensorData,
            latitude = currentLocation?.latitude,
            longitude = currentLocation?.longitude,
            onStartClick = {
                radarEngine.startEngine()
                lifecycleScope.launch {
                    locationManager.getCurrentLocation()
                }
            },
            onStopClick = {
                radarEngine.stopEngine()
                // ذخیره نتایج در پایگاه داده
                radarResult?.let { result ->
                    lifecycleScope.launch {
                        val detection = DetectionRecord(
                            latitude = currentLocation?.latitude ?: 0.0,
                            longitude = currentLocation?.longitude ?: 0.0,
                            altitude = currentLocation?.altitude ?: 0.0,
                            entropy = result.entropy,
                            dielectric = result.dielectric,
                            fractal = result.fractal,
                            materialType = result.materialType,
                            confidence = result.confidence
                        )
                        database.detectionDao().insertDetection(detection)
                    }
                }
            },
            onCaptureClick = {
                val outputDir = File(cacheDir, "radar_captures")
                outputDir.mkdirs()
                cameraManager.captureImage(
                    outputDir,
                    mainExecutor,
                    onSuccess = { file ->
                        // ذخیره مسیر فایل
                    },
                    onError = { exception ->
                        exception.printStackTrace()
                    }
                )
            }
        )
    }

    override fun onResume() {
        super.onResume()
        sensorManager.startListening()
        cameraManager.startCamera(this)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.stopListening()
        radarEngine.stopEngine()
        cameraManager.stopCamera()
    }
}
