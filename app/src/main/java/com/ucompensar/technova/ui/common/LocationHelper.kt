package com.ucompensar.technova.ui.common

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale

/** Funciones para sacar la ubicación del celular con el GPS */
object LocationHelper {

    /** Permisos de ubicación que se le piden al usuario */
    val PERMISSIONS = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)

    /** Guarda las coordenadas y la dirección encontrada */
    data class Place(val latitude: Double, val longitude: Double, val address: String, val city: String)

    /** Revisa si el usuario ya dio permiso de ubicación */
    fun hasPermission(context: Context): Boolean = PERMISSIONS.any {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

    /** Saca la ubicación actual y busca la dirección */
    @SuppressLint("MissingPermission")
    suspend fun currentPlace(context: Context): Place {
        val client = LocationServices.getFusedLocationProviderClient(context)
        val location = client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).await()
            ?: client.lastLocation.await()
            ?: throw IllegalStateException("No se pudo obtener la ubicación. Activa el GPS e inténtalo de nuevo.")
        val (address, city) = withContext(Dispatchers.IO) { reverseGeocode(context, location.latitude, location.longitude) }
        return Place(location.latitude, location.longitude, address, city)
    }

    /** Convierte las coordenadas en una dirección y una ciudad */
    @Suppress("DEPRECATION")
    private fun reverseGeocode(context: Context, lat: Double, lng: Double): Pair<String, String> {
        val fallback = String.format(Locale.US, "%.5f, %.5f", lat, lng) to ""
        if (!Geocoder.isPresent()) return fallback
        return runCatching {
            val result = Geocoder(context, Locale("es", "CO")).getFromLocation(lat, lng, 1)?.firstOrNull()
            if (result == null) fallback
            else (result.thoroughfare?.let { "$it ${result.subThoroughfare.orEmpty()}".trim() }
                ?: result.getAddressLine(0).orEmpty()) to (result.locality ?: result.subAdminArea.orEmpty())
        }.getOrDefault(fallback)
    }

    /** Arma el enlace para ver la ubicación en Google Maps */
    fun mapsUrl(lat: Double, lng: Double): String =
        String.format(Locale.US, "https://www.google.com/maps/search/?api=1&query=%.6f,%.6f", lat, lng)
}
