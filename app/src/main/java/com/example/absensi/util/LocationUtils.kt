package com.example.absensi.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.*

object LocationUtils {
    private const val EARTH_RADIUS = 6371 // Earth's radius in kilometers

    fun calculateDistance(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS * c * 1000 // Convert to meters
    }

    fun isWithinRadius(
        currentLat: Double,
        currentLon: Double,
        targetLat: Double,
        targetLon: Double,
        radiusInMeters: Int
    ): Boolean {
        val distance = calculateDistance(currentLat, currentLon, targetLat, targetLon)
        return distance <= radiusInMeters
    }

    suspend fun getCurrentLocation(context: Context): Location {
        if (!hasLocationPermission(context)) {
            throw SecurityException("Location permission not granted")
        }

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        return getCurrentLocationInternal(fusedLocationClient)
    }

    private suspend fun getCurrentLocationInternal(
        fusedLocationClient: FusedLocationProviderClient
    ): Location = suspendCancellableCoroutine { continuation ->
        try {
            val currentLocationTask = fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                object : CancellationToken() {
                    private val tokenSource = CancellationTokenSource()
                    override fun onCanceledRequested(listener: OnTokenCanceledListener) =
                        tokenSource.token

                    override fun isCancellationRequested() = false
                }
            )

            currentLocationTask.addOnSuccessListener { location ->
                if (location != null) {
                    continuation.resume(location)
                } else {
                    continuation.resumeWithException(Exception("Unable to get current location"))
                }
            }

            currentLocationTask.addOnFailureListener { exception ->
                continuation.resumeWithException(exception)
            }

            continuation.invokeOnCancellation {
                currentLocationTask.cancel()
            }
        } catch (e: SecurityException) {
            continuation.resumeWithException(e)
        }
    }

    suspend fun getAddressFromLocation(
        context: Context,
        latitude: Double,
        longitude: Double
    ): String {
        return try {
            val geocoder = Geocoder(context)
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                var addressResult = ""
                geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                    if (addresses.isNotEmpty()) {
                        addressResult = formatAddress(addresses[0])
                    }
                }
                addressResult
            } else {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    formatAddress(addresses[0])
                } else {
                    throw IOException("No address found")
                }
            }
        } catch (e: Exception) {
            "Unknown location"
        }
    }

    private fun formatAddress(address: Address): String {
        val addressParts = mutableListOf<String>()

        // Add street address
        if (!address.thoroughfare.isNullOrBlank()) {
            addressParts.add(address.thoroughfare)
        }

        // Add sublocality/district
        if (!address.subLocality.isNullOrBlank()) {
            addressParts.add(address.subLocality)
        }

        // Add city/locality
        if (!address.locality.isNullOrBlank()) {
            addressParts.add(address.locality)
        }

        // Add state/province
        if (!address.adminArea.isNullOrBlank()) {
            addressParts.add(address.adminArea)
        }

        // Add postal code
        if (!address.postalCode.isNullOrBlank()) {
            addressParts.add(address.postalCode)
        }

        return addressParts.joinToString(", ")
    }

    fun hasLocationPermission(context: Context): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    fun createLocationRequest(): LocationRequest {
        return LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
            .setWaitForAccurateLocation(true)
            .setMinUpdateIntervalMillis(5000)
            .setMaxUpdateDelayMillis(15000)
            .build()
    }

    fun formatDistance(distanceInMeters: Double): String {
        return when {
            distanceInMeters < 1000 -> "${distanceInMeters.roundToInt()} m"
            else -> String.format("%.1f km", distanceInMeters / 1000)
        }
    }

    fun getLocationAccuracyMessage(accuracy: Float): String {
        return when {
            accuracy <= 10 -> "High accuracy (±${accuracy.roundToInt()}m)"
            accuracy <= 50 -> "Medium accuracy (±${accuracy.roundToInt()}m)"
            else -> "Low accuracy (±${accuracy.roundToInt()}m)"
        }
    }
}
