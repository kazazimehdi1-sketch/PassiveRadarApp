package com.asatir.passiveradar.location

import android.content.Context
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await

class LocationManager(context: Context) {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation: StateFlow<Location?> = _currentLocation

    suspend fun getLastLocation() {
        try {
            val location = fusedLocationClient.lastLocation.await()
            _currentLocation.value = location
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun getCurrentLocation() {
        try {
            val location = fusedLocationClient.lastLocation.await()
            _currentLocation.value = location
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getLatitude(): Double? = currentLocation.value?.latitude
    fun getLongitude(): Double? = currentLocation.value?.longitude
    fun getAltitude(): Double? = currentLocation.value?.altitude
    fun getAccuracy(): Float? = currentLocation.value?.accuracy
}
