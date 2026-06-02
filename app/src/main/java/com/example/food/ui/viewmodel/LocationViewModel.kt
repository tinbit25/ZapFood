package com.example.food.ui.viewmodel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

data class LocationState(
    val isLoading: Boolean = false,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val address: String = "",
    val city: String = "",
    val subcity: String = "",
    val woreda: String = "",
    val street: String = "",
    val error: String? = null
)

class LocationViewModel : ViewModel() {
    private val _locationState = MutableStateFlow(LocationState())
    val locationState: StateFlow<LocationState> = _locationState.asStateFlow()

    private var fusedLocationClient: FusedLocationProviderClient? = null

    fun initialize(context: Context) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    }

    fun checkLocationPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun getCurrentLocation(context: Context) {
        if (!checkLocationPermission(context)) {
            _locationState.value = _locationState.value.copy(
                error = "Location permission not granted"
            )
            return
        }

        _locationState.value = _locationState.value.copy(isLoading = true, error = null)

        try {
            fusedLocationClient?.lastLocation?.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude

                    _locationState.value = _locationState.value.copy(
                        latitude = latitude,
                        longitude = longitude,
                        isLoading = false
                    )

                    // Perform geocoding to get address
                    performGeocoding(context, latitude, longitude)
                } else {
                    _locationState.value = _locationState.value.copy(
                        isLoading = false,
                        error = "Unable to get current location. Please enable GPS."
                    )
                }
            }?.addOnFailureListener { e ->
                _locationState.value = _locationState.value.copy(
                    isLoading = false,
                    error = "Failed to get location: ${e.message}"
                )
            }
        } catch (e: Exception) {
            _locationState.value = _locationState.value.copy(
                isLoading = false,
                error = "Location error: ${e.message}"
            )
        }
    }

    private fun performGeocoding(context: Context, latitude: Double, longitude: Double) {
        viewModelScope.launch {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)

                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    val fullAddress = getAddressString(address)

                    _locationState.value = _locationState.value.copy(
                        address = fullAddress,
                        city = address.locality ?: "Addis Ababa",
                        subcity = address.subAdminArea ?: "",
                        woreda = address.subLocality ?: "",
                        street = address.thoroughfare ?: "",
                        error = null
                    )
                } else {
                    _locationState.value = _locationState.value.copy(
                        error = "Unable to get address from coordinates"
                    )
                }
            } catch (e: Exception) {
                _locationState.value = _locationState.value.copy(
                    error = "Geocoding error: ${e.message}"
                )
            }
        }
    }

    private fun getAddressString(address: Address): String {
        val addressParts = mutableListOf<String>()

        address.thoroughfare?.let { addressParts.add(it) }
        address.subLocality?.let { addressParts.add(it) }
        address.subAdminArea?.let { addressParts.add(it) }
        address.locality?.let { addressParts.add(it) }
        address.adminArea?.let { addressParts.add(it) }
        address.countryName?.let { addressParts.add(it) }

        return addressParts.joinToString(", ")
    }

    fun resetState() {
        _locationState.value = LocationState()
    }
}
